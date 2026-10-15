package me.weishu.kernelsu.ui.component.card

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquid

/**
 * Liquid-glass surfaces for cards.
 *
 * ## Why this exists next to `bottombar/GlassEffect.kt`
 *
 * That one is written for the floating navigation bar: it is a **capsule**, and it hard-codes
 * `radius = height / 2f` in its `drawBehind`, so it cannot be reused for a card. This file is the
 * rectangular sibling — same Liquid backend, same visual language, but the corner is a parameter
 * and the highlight/border/specular bands are laid out for a rounded rectangle instead of a pill.
 *
 * ## Where the [LiquidState] comes from
 *
 * Liquid refraction is **not** per-element: one [LiquidState] belongs to one *content layer* that
 * gets marked as refractable, and every glass surface on top of it samples that same layer. The
 * app already builds one for the floating bar in `MainActivity`; [LocalFolkLiquidState] hands the
 * same instance down so cards refract the very same backdrop instead of fighting over layers.
 *
 * A null state is a supported, expected value: below Android 13 there is no real-time blur at all,
 * and the caller may keep the feature disabled. Both cases degrade to a translucent tint.
 */

/** The app-wide liquid state, or null when real-time blur is unavailable / turned off. */
val LocalFolkLiquidState = staticCompositionLocalOf<LiquidState?> { null }

/** Real-time blur needs Android 13; below that every glass surface falls back to a tint. */
val isGlassBlurAvailable: Boolean
    get() = Build.VERSION.SDK_INT >= 33

/**
 * Rounded-rectangle liquid glass.
 *
 * @param cornerRadius corner of both the tint and the refracting shape.
 * @param blurStrength 0..1 — drives frost, curve, refraction, dispersion and saturation together,
 *   so one slider moves the whole look instead of five that have to be balanced by hand.
 * @param transparency 0..1 — higher lets more of the wallpaper through.
 * @param liquidState shared state; null disables refraction and leaves only the tint.
 */
@Composable
fun Modifier.folkGlassCard(
    cornerRadius: Dp = 20.dp,
    blurStrength: Float = 0.7f,
    transparency: Float = 0.3f,
    liquidState: LiquidState? = null,
    shape: Shape? = null,
): Modifier {
    val strength = blurStrength.coerceIn(0f, 1f)
    val fade = transparency.coerceIn(0f, 1f)
    val isDark = !(MaterialTheme.colorScheme.background.luminance() > 0.5f)
    val density = LocalDensity.current
    val cornerPx = with(density) { cornerRadius.toPx() }
    val halfPx = with(density) { 0.5.dp.toPx() }
    val onePx = with(density) { 1.dp.toPx() }
    val twoPx = with(density) { 2.dp.toPx() }
    val effectiveShape = shape ?: RoundedCornerShape(cornerRadius)

    // The tint that keeps text readable no matter how transparent the glass is.
    // Mirrors the bar version's curve so the two surfaces read as the same material.
    val tint = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.14f + (1f - fade) * 0.10f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.10f + (1f - fade) * 0.08f)
    }

    // Everything that does not need the blur backend: border, body gradient, specular line, glow.
    val painted = this.drawBehind {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@drawBehind
        val corner = CornerRadius(cornerPx, cornerPx)

        // Hairline border — slightly brighter in light mode, barely there in dark.
        val borderColor = if (isDark) {
            Color.White.copy(alpha = 0.08f * (0.5f + strength * 0.5f))
        } else {
            Color.White.copy(alpha = 0.25f * (0.5f + strength * 0.5f))
        }
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(halfPx, halfPx),
            size = Size((w - onePx).coerceAtLeast(0f), (h - onePx).coerceAtLeast(0f)),
            cornerRadius = corner,
            style = Stroke(width = onePx),
        )

        // Body: a soft top-to-bottom sheen.
        val highAlpha = if (isDark) 0.05f + 0.10f * strength else 0.08f + 0.18f * strength
        val lowAlpha = if (isDark) 0.01f + 0.04f * strength else 0.03f + 0.08f * strength
        val base = 0.45f + (1f - fade) * 0.35f
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = highAlpha * base),
                    Color.White.copy(alpha = lowAlpha * base),
                )
            ),
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = corner,
        )

        // Specular: the bright sliver near the top edge that sells the "glass" read.
        val specularAlpha = if (isDark) 0.10f + 0.12f * strength else 0.25f + 0.25f * strength
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = specularAlpha), Color.Transparent),
                startX = w * 0.12f,
                endX = w * 0.88f,
            ),
            topLeft = Offset(w * 0.12f, twoPx),
            size = Size(w * 0.76f, onePx * 1.5f),
            cornerRadius = CornerRadius(onePx),
        )
    }

    if (!isGlassBlurAvailable || liquidState == null) {
        // No real-time blur: keep the painted glass and lay the tint under it.
        return painted.then(
            Modifier.drawBehind { drawRoundRect(color = tint, cornerRadius = CornerRadius(cornerPx, cornerPx)) }
        )
    }

    return painted
        .then(
            Modifier.drawBehind {
                drawRoundRect(color = tint, cornerRadius = CornerRadius(cornerPx, cornerPx))
            }
        )
        .liquid(liquidState) {
            this.shape = effectiveShape
            this.frost = (strength * 20f).dp.coerceIn(8.dp, 20.dp)
            this.curve = if (isDark) 0.34f + strength * 0.10f else 0.40f + strength * 0.10f
            this.refraction = if (isDark) 0.07f + strength * 0.06f else 0.10f + strength * 0.08f
            this.dispersion = if (isDark) 0.16f + strength * 0.08f else 0.20f + strength * 0.10f
            this.saturation = if (isDark) 0.38f + strength * 0.12f else 0.48f + strength * 0.14f
            this.contrast = if (isDark) 1.7f + strength * 0.2f else 1.55f + strength * 0.2f
        }
}
