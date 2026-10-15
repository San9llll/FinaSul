package me.weishu.kernelsu.aster

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.ui.theme.ColorMode
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle
import top.yukonga.miuix.kmp.theme.ThemeController as MiuixThemeController

/**
 * Miuix 主题桥接。
 *
 * ## 为什么需要这个
 *
 * AsterUI 的界面是从 Nakour 直接搬过来的，那些 composable 里到处是
 * `MiuixTheme.colorScheme.xxx` / `MiuixTheme.textStyles.xxx` —— Miuix 的组件要求
 * 上面有一个 `MiuixTheme` 提供者，否则读不到。
 *
 * 但 FinaSU 自己的界面跑在 Material3 + MaterialKolor 上，不能整棵树换成 Miuix
 * （那会把模块页、超级用户页、设置页的配色全改掉）。所以只在 AsterUI 这一支外面包一层。
 *
 * ## 颜色从哪来
 *
 * **不另起一套配色**。这个桥接把 FinaSU 已经算好的那套参数
 * （明暗 / 基准色 / 配色方案 / 配色规范）翻译成 Miuix 的枚举再交给它 ——
 * 两边走的都是 Material You 那套规则，所以同一个 keyColor 展开出来的色板是一致的。
 *
 * 结果：用户在「设置 → 主题」里换基准色或切深色，AsterUI 跟着一起变，
 * 不会出现"主页是这套色、其它页是另一套色"。
 *
 * ## 类型映射
 *
 * FinaSU 用 MaterialKolor 的枚举，Miuix 用自己的，名字一样但不是一个类：
 *
 * ```
 * com.materialkolor.PaletteStyle       →  miuix.ThemePaletteStyle
 * com.materialkolor.dynamiccolor.ColorSpec.SpecVersion  →  miuix.ThemeColorSpec
 * ```
 *
 * 按名字对应，对不上的退回调色方案/规范的默认值。
 */
@Composable
fun AsterMiuixTheme(
    content: @Composable () -> Unit,
) {
    val repo = remember { SettingsRepositoryImpl() }

    val colorMode = ColorMode.fromValue(repo.themeMode)
    val keyColor = repo.keyColor
    val paletteStyle = runCatching { PaletteStyle.valueOf(repo.colorStyle) }
        .getOrDefault(PaletteStyle.TonalSpot)
    val colorSpec = runCatching { ColorSpec.SpecVersion.valueOf(repo.colorSpec) }
        .getOrDefault(ColorSpec.SpecVersion.SPEC_2025)
    val useSystemColor = repo.useSystemDynamicColor

    val systemDark = isSystemInDarkTheme()
    val miuixMode = colorMode.toMiuixMode(systemDark)

    val controller = remember(miuixMode, keyColor, paletteStyle, colorSpec, useSystemColor) {
        MiuixThemeController(
            colorSchemeMode = miuixMode,
            // 系统动态取色时 keyColor 传 0（FinaSU 就是这么约定的），
            // Miuix 那边同样认 0 为"用系统色"。
            keyColor = Color(if (useSystemColor) 0 else keyColor),
            colorSpec = colorSpec.toMiuix(),
            paletteStyle = paletteStyle.toMiuix(),
        )
    }

    MiuixTheme(controller = controller) {
        content()
    }
}

/** FinaSU 的明暗模式 → Miuix 的。只有 Monet 那三档在 Miuix 里有对应。 */
private fun ColorMode.toMiuixMode(systemDark: Boolean): ColorSchemeMode = when (this) {
    ColorMode.LIGHT, ColorMode.MONET_LIGHT -> ColorSchemeMode.MonetLight
    ColorMode.DARK, ColorMode.DARK_AMOLED, ColorMode.MONET_DARK -> ColorSchemeMode.MonetDark
    // SYSTEM / MONET_SYSTEM：跟着系统走
    else -> if (systemDark) ColorSchemeMode.MonetDark else ColorSchemeMode.MonetLight
}

/** MaterialKolor 的配色方案 → Miuix 的。名字对不上就退回 TonalSpot。 */
private fun PaletteStyle.toMiuix(): ThemePaletteStyle = when (this) {
    PaletteStyle.TonalSpot -> ThemePaletteStyle.TonalSpot
    PaletteStyle.Neutral -> ThemePaletteStyle.Neutral
    PaletteStyle.Vibrant -> ThemePaletteStyle.Vibrant
    PaletteStyle.Expressive -> ThemePaletteStyle.Expressive
    else -> ThemePaletteStyle.TonalSpot
}

/** MaterialKolor 的配色规范 → Miuix 的。 */
private fun ColorSpec.SpecVersion.toMiuix(): ThemeColorSpec = when (this) {
    ColorSpec.SpecVersion.SPEC_2025 -> ThemeColorSpec.Spec2025
    else -> ThemeColorSpec.Spec2021
}
