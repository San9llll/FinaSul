package me.weishu.kernelsu.aster.ui

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.aster.AsterUiConfig
import me.weishu.kernelsu.ui.screen.home.HomeActions
import me.weishu.kernelsu.ui.screen.home.HomeUiState

/**
 * AsterUI 的主页。
 *
 * ## 组件全是从 Nakour 搬过来的
 *
 * 这一支**不自己造 UI** —— 用的是原样搬过来的 [SceneHero]、[SceneGreeting]、
 * [SceneStatusStrip]、[SceneClock] 和 [HomeSceneRail]。之前我自己用 Material3 手搓过一版，
 * 做出来跟 Nakour 完全不像（中文排版、字号、圆角、动画全是 Miuix 定的），所以推倒重来。
 *
 * ## 数据是 FinaSU 的
 *
 * 显示的内容（root 状态、版本、模块数、超级用户数）全部来自 FinaSU 自己的 [HomeUiState]
 * 和 [HomeActions]，挂在壳上的那一层导航也走 FinaSU 的 pager。**root 管理逻辑一行没动。**
 *
 * 主页这里只负责把 Nakour 的视觉和 FinaSU 的数据接上，不产生新的状态。
 */
@Composable
fun AsterHomeContent(
    state: HomeUiState,
    actions: HomeActions,
    bottomInnerPadding: Dp,
    superuserCount: Int = 0,
    moduleEnabledCount: Int = 0,
    /** 大图。FinaSU 的壁纸系统给，没设就 null（hero 会退化成纯色）。 */
    wallpaper: ImageBitmap? = null,
) {
    val corner = AsterUiConfig.corner.dp
    val scrollState = rememberScrollState()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val layout = homeSceneLayout(
            width = maxWidth,
            height = (maxHeight - topInset - bottomInset).coerceAtLeast(0.dp),
        )


        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = topInset + 12.dp),
        ) {
            Spacer(Modifier.height(6.dp))

            // 问候 —— 时间 + 日语小句 + 中文翻译
            if (AsterUiConfig.showGreeting) {
                val (line, quote, translation) = asterGreeting()
                SceneGreeting(
                    greetingLine = line,
                    quote = quote,
                    translation = translation,
                )
                Spacer(Modifier.height(16.dp))
            }

            // hero 卡：大图 + 内核版本 + 状态徽标
            SceneHero(
                wallpaper = wallpaper,
                title = state.ksuVersion?.let { "KernelSU $it" } ?: "KernelSU",
                subtitle = asterKernelLine(state),
                badge = if (state.isRootAvailable) null else "无 root",
                scrollState = scrollState,
                shape = RoundedCornerShape(corner),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(14.dp))

            // 状态条：左边超级用户数、右边已启用模块数
            SceneStatusStrip(
                leftTitle = "超级用户",
                leftValue = superuserCount.toString(),
                rightTitle = "已启用模块",
                rightValue = moduleEnabledCount.toString(),
            )

            Spacer(Modifier.height(10.dp))

            // 设备行 —— 用 FinaSU 能拿到的信息，不额外申请权限
            SceneStatusStrip(
                leftTitle = "设备",
                leftValue = Build.MANUFACTURER + " " + Build.MODEL,
                rightTitle = "模式",
                rightValue = if (state.lkmMode == true) "LKM" else "GKI",
            )

            Spacer(Modifier.height(bottomInnerPadding + 28.dp))
        }

        // 导轨不在这儿画 —— 它要能在五个页面之间切，所以归 MainActivity 的壳管。
        // （这边只放内容，rail 由外层叠上来。）
    }
}

/** 内核那一行的副标题。 */
private fun asterKernelLine(state: HomeUiState): String {
    val parts = mutableListOf<String>()
    if (state.isSafeMode) parts += "安全模式"
    if (state.requiresNewKernel) parts += "内核需更新"
    if (state.zygiskProvider != null) parts += state.zygiskProvider
    return if (parts.isEmpty()) "本地版" else parts.joinToString(" · ")
}

/** 按时间给一条问候。 */
private fun asterGreeting(): Triple<String, String, String> {
    return when (java.time.LocalTime.now().hour) {
        in 0..4 -> Triple("凌晨好", "まだ起きてるの？", "还没睡吗")
        in 5..10 -> Triple("早上好", "いい一日を", "祝你有个好心情")
        in 11..13 -> Triple("中午好", "お昼の時間", "该吃饭了")
        in 14..17 -> Triple("下午好", "午後のひととき", "下午的时光")
        in 18..22 -> Triple("晚上好", "お疲れさま", "辛苦了")
        else -> Triple("夜深了", "そろそろ休もう", "该休息了")
    }
}
