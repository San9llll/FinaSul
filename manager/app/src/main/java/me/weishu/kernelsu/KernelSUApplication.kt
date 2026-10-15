package me.weishu.kernelsu

import me.weishu.kernelsu.aster.AsterUiConfig
import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.UserManager
import android.system.Os
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.data.security.SecurityRepository
import me.weishu.kernelsu.data.security.SecurityRepositoryImpl
import me.weishu.kernelsu.data.backup.SecretStore
import me.weishu.kernelsu.data.backup.device.KeystoreSecretStore
import me.weishu.kernelsu.ui.component.security.BiometricAuthenticator
import me.weishu.kernelsu.ui.util.LauncherIconUtils
import me.weishu.kernelsu.ui.viewmodel.SuperUserViewModel
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.io.File
import java.util.Locale

lateinit var ksuApp: KernelSUApplication

class KernelSUApplication : Application(), ViewModelStoreOwner {

    companion object {
        fun setEnableOnBackInvokedCallback(appInfo: ApplicationInfo, enable: Boolean) {
            runCatching {
                val applicationInfoClass = ApplicationInfo::class.java
                val method = applicationInfoClass.getDeclaredMethod("setEnableOnBackInvokedCallback", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(appInfo, enable)
            }
        }
    }

    lateinit var okhttpClient: OkHttpClient
    val securityRepository: SecurityRepository by lazy {
        SecurityRepositoryImpl(
            getSharedPreferences(SecurityRepositoryImpl.PREFS_NAME, MODE_PRIVATE),
            biometricSupported = { BiometricAuthenticator.isSupported(this) },
        )
    }
    /** Reversible secret storage (WebDAV password); distinct from the one-way credential vault. */
    val backupSecrets: SecretStore by lazy { KeystoreSecretStore(this) }
    private val appViewModelStore by lazy { ViewModelStore() }

    private fun isUserUnlocked(): Boolean =
        getSystemService(UserManager::class.java)?.isUserUnlocked == true

    override fun onCreate() {
        super.onCreate()
        ksuApp = this

        // Aster UI 的活状态（布局 / 玻璃 / 边栏）。外壳、卡片、首页都从它读，
        // 所以必须在第一次组合之前装好。
        //
        // 放在 unlocked 检查**之前**：锁屏状态下冷启动时，下面的 return 会跳过所有
        // 依赖已解锁存储的初始化，但界面照样要渲染一次，那时候这些值不能是未初始化的。
        // load() 本身对读不到的情况是安全的 —— 拿不到 prefs 就保留默认值。
        AsterUiConfig.load()

        if (!isUserUnlocked()) {
            return
        }

        LauncherIconUtils.applySaved(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val enable = SettingsRepositoryImpl().enablePredictiveBack
            HiddenApiBypass.addHiddenApiExemptions("Landroid/content/pm/ApplicationInfo;->setEnableOnBackInvokedCallback")
            setEnableOnBackInvokedCallback(applicationInfo, enable)
        }

        val superUserViewModel = ViewModelProvider(this)[SuperUserViewModel::class.java]
        superUserViewModel.loadAppList()

        val webroot = File(dataDir, "webroot")
        if (!webroot.exists()) {
            webroot.mkdir()
        }

        // Provide working env for rust's temp_dir()
        Os.setenv("TMPDIR", cacheDir.absolutePath, true)

        okhttpClient =
            OkHttpClient.Builder().cache(Cache(File(cacheDir, "okhttp"), 10 * 1024 * 1024))
                .addInterceptor { block ->
                    block.proceed(
                        block.request().newBuilder()
                            .header("User-Agent", "KernelSU/${BuildConfig.VERSION_CODE}")
                            .header("Accept-Language", Locale.getDefault().toLanguageTag()).build()
                    )
                }.build()
    }

    override val viewModelStore: ViewModelStore
        get() = appViewModelStore
}
