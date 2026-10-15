package me.weishu.kernelsu.aster

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import me.weishu.kernelsu.data.model.HomeLayoutStyle
import me.weishu.kernelsu.ksuApp

/**
 * Live state for the Aster UI.
 *
 * ## Why an object instead of more ViewModel plumbing
 *
 * The home surface, the glass cards on every other page and the settings screen all need these
 * values, and they sit in three unrelated parts of the tree. Threading a ViewModel through all of
 * them would touch far more of the app than this feature warrants.
 *
 * [WallpaperConfig] already solved exactly this problem for the wallpaper, so this follows the
 * same shape: Compose state for anything read inside composition, persistence in the shared
 * `settings` prefs file, and no dependency on Activity wiring.
 *
 * The prefs keys are the ones [me.weishu.kernelsu.data.repository.SettingsRepositoryImpl] writes,
 * so the settings screen is the single source of truth — this object just mirrors it into
 * observable state.
 */
object AsterUiConfig {

    /** Same file the settings repository uses, so both sides see the same values. */
    private const val PREFS_NAME = "settings"
    private const val KEY_HOME_LAYOUT = "home_layout_style"
    private const val KEY_SHOW_RAIL = "aster_show_rail"
    private const val KEY_SHOW_GREETING = "aster_show_greeting"
    private const val KEY_SHOW_INFO_BLOCK = "aster_show_info_block"
    private const val KEY_GLASS_CARD = "aster_glass_card"
    private const val KEY_GLASS_STRENGTH = "aster_glass_strength"
    private const val KEY_GLASS_TRANSPARENCY = "aster_glass_transparency"
    private const val KEY_CORNER = "aster_corner"

    private val prefs: android.content.SharedPreferences?
        get() = runCatching {
            ksuApp.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }.getOrNull()

    // ---- Observable state ----

    var showRail by mutableStateOf(true)
        private set
    var showGreeting by mutableStateOf(true)
        private set

    /** 导轨上那块信息（电量 / 温度 / 内存…）。Nakour 那边叫"信息块"。 */
    var showInfoBlock by mutableStateOf(true)
        private set
    var glassCard by mutableStateOf(true)
        private set
    var glassStrength by mutableFloatStateOf(0.7f)
        private set
    var glassTransparency by mutableFloatStateOf(0.3f)
        private set
    var corner by mutableIntStateOf(20)
        private set

    /** The persisted home layout token; kept here so the layout switch is observable too. */
    var homeLayout by mutableStateOf(HomeLayoutStyle.DEFAULT)
        private set

    /**
     * Whether the Aster layout currently owns the shell.
     *
     * Read this instead of the raw token when deciding whether to swap the bottom bar for the
     * rail, or whether the appearance entries should be greyed out.
     */
    val active: Boolean get() = HomeLayoutStyle.takesOverAppearance(homeLayout)

    /**
     * True when cards anywhere in the app should render as liquid glass.
     *
     * Gated on [active]: the glass material is part of the Aster look. Leaving it on for the
     * stock layouts would change pages the user never asked to change.
     */
    val glassCardsActive: Boolean get() = active && glassCard

    /**
     * Pull every value out of prefs.
     *
     * Called once from the app entry point and again after a restore, so a restored backup shows
     * up without a restart. Safe to call repeatedly.
     */
    fun load() {
        val p = prefs ?: return
        homeLayout = HomeLayoutStyle.fromValue(p.getString(KEY_HOME_LAYOUT, HomeLayoutStyle.DEFAULT))
        showRail = p.getBoolean(KEY_SHOW_RAIL, true)
        showGreeting = p.getBoolean(KEY_SHOW_GREETING, true)
        showInfoBlock = p.getBoolean(KEY_SHOW_INFO_BLOCK, true)
        glassCard = p.getBoolean(KEY_GLASS_CARD, true)
        glassStrength = p.getFloat(KEY_GLASS_STRENGTH, 0.7f).coerceIn(0f, 1f)
        glassTransparency = p.getFloat(KEY_GLASS_TRANSPARENCY, 0.3f).coerceIn(0f, 1f)
        corner = p.getInt(KEY_CORNER, 20).coerceIn(0, 36)
    }

    // ---- Writers ----
    //
    // Each one updates the in-memory state *and* persists. The settings ViewModel calls these
    // alongside writing through the repository, so both the reactive readers and any later
    // restore see the same thing.

    fun applyHomeLayout(value: String) {
        val normalized = HomeLayoutStyle.fromValue(value)
        homeLayout = normalized
        prefs?.edit { putString(KEY_HOME_LAYOUT, normalized) }
    }

    fun applyShowRail(value: Boolean) {
        showRail = value
        prefs?.edit { putBoolean(KEY_SHOW_RAIL, value) }
    }

    fun applyShowGreeting(value: Boolean) {
        showGreeting = value
        prefs?.edit { putBoolean(KEY_SHOW_GREETING, value) }
    }

    fun applyShowInfoBlock(value: Boolean) {
        showInfoBlock = value
        prefs?.edit { putBoolean(KEY_SHOW_INFO_BLOCK, value) }
    }

    fun applyGlassCard(value: Boolean) {
        glassCard = value
        prefs?.edit { putBoolean(KEY_GLASS_CARD, value) }
    }

    fun applyGlassStrength(value: Float) {
        val v = value.coerceIn(0f, 1f)
        glassStrength = v
        prefs?.edit { putFloat(KEY_GLASS_STRENGTH, v) }
    }

    fun applyGlassTransparency(value: Float) {
        val v = value.coerceIn(0f, 1f)
        glassTransparency = v
        prefs?.edit { putFloat(KEY_GLASS_TRANSPARENCY, v) }
    }

    fun applyCorner(value: Int) {
        val v = value.coerceIn(0, 36)
        corner = v
        prefs?.edit { putInt(KEY_CORNER, v) }
    }
}
