package me.weishu.kernelsu.data.model

/**
 * Tokens persisted under the `home_layout_style` preference key.
 *
 * The strings are fixed and intentionally kept identical to the tokens the appearance settings
 * write, so a stored configuration maps onto the same layout. Unknown or legacy tokens fall back
 * to [CIRCLE] instead of failing.
 */
object HomeLayoutStyle {
    /** The standard single-column home. */
    const val CIRCLE = "circle"

    /** The two-column card grid home. */
    const val GRID = "kernelsu"

    /** The responsive tile grid that becomes a 2x2 board on a landscape screen. */
    const val FOCUS = "focus"

    /** The hero-first home with a single wide status banner. */
    const val DASHBOARD = "dashboard_ui"

    /** The counters-first home built around the superuser and module tallies. */
    const val STATS = "stats"

    /**
     * The Aster UI — a full-bleed wallpaper home with a left scene rail instead of the bottom bar.
     *
     * Unlike the other tokens this one does not only rearrange the existing cards: it replaces the
     * whole home surface (wallpaper, greeting, glass cards, rail) and therefore takes over a few
     * appearance settings that would otherwise fight with it. The settings screen greys those out
     * while this layout is active.
     */
    const val ASTER = "aster_ui"

    /** Layout used when the preference is missing or holds an unsupported token. */
    const val DEFAULT = CIRCLE

    /** Normalise a stored value to a supported token. */
    fun fromValue(value: String?): String = when (value) {
        GRID -> GRID
        FOCUS -> FOCUS
        DASHBOARD -> DASHBOARD
        STATS -> STATS
        ASTER -> ASTER
        CIRCLE -> CIRCLE
        else -> DEFAULT
    }

    /**
     * Whether a layout draws live device metrics, and therefore has to keep the collector polled.
     * The others read a single snapshot of the module state and must leave the sampler idle.
     */
    fun usesMetrics(value: String): Boolean =
        value == FOCUS || value == STATS || value == ASTER

    /** Every supported token, in display order. */
    val supported: List<String> = listOf(CIRCLE, GRID, FOCUS, DASHBOARD, STATS, ASTER)

    /**
     * Whether a layout takes over the appearance settings that would otherwise shape the home
     * surface. When true the settings screen disables the conflicting entries instead of letting
     * the user change something that has no visible effect.
     */
    fun takesOverAppearance(value: String): Boolean = value == ASTER
}
