package me.weishu.kernelsu.aster.ui

import top.yukonga.miuix.kmp.theme.ColorSchemeMode

/**
 * 明暗模式 —— 从 Nakour 的 Theme.kt 里抠出来的。
 *
 * 不能直接用 FinaSU 自己的 ColorMode：那个有七档（含 AMOLED、含 MONET_* 的区分），
 * 而 AsterUI 这套设置界面和它的偏好存储都是按 Nakour 这三档写的。
 * 两边的档位在桥接里会做一次映射（见 AsterMiuixTheme）。
 */
enum class DarkMode(val id: String, val label: String, val summary: String) {
    System("system", "跟随系统", "根据系统的深色模式自动切换"),
    Light("light", "浅色", "始终亮色"),
    Dark("dark", "深色", "始终暗色");

    fun toMode(systemDark: Boolean): ColorSchemeMode = when (this) {
        System -> if (systemDark) ColorSchemeMode.MonetDark else ColorSchemeMode.MonetLight
        Light -> ColorSchemeMode.MonetLight
        Dark -> ColorSchemeMode.MonetDark
    }

    companion object { fun of(id: String?) = entries.firstOrNull { it.id == id } ?: System }
}
