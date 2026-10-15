package me.weishu.kernelsu.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.weishu.kernelsu.aster.AsterUiConfig
import me.weishu.kernelsu.ui.component.card.LocalFolkLiquidState
import me.weishu.kernelsu.ui.component.card.folkGlassCard
import me.weishu.kernelsu.ui.theme.ContinuousCornerShape
import java.time.LocalTime

/**
 * The Aster home surface.
 *
 * ## Where this sits
 *
 * It replaces *only* the home page. Everything else in the manager — modules, superuser,
 * settings — keeps its own screen and its own logic; this composable reads the same
 * [HomeUiState] and calls the same [HomeActions] the other layouts do, so nothing about root
 * handling moves.
 *
 * The whole page is a wallpaper with glass on top, plus an optional scene rail down the left
 * edge. The bottom bar is hidden by the shell while this layout is active, which is what makes
 * the full-bleed look possible.
 */
@Composable
fun AsterHomeContent(
    state: HomeUiState,
    actions: HomeActions,
    bottomInnerPadding: Dp,
    superuserCount: Int = 0,
    moduleEnabledCount: Int = 0,
    metrics: HomeMetrics = HomeMetrics(),
) {
    val corner = AsterUiConfig.corner

    Box(Modifier.fillMaxSize()) {
        AsterBackdrop()

        // The scene rail is drawn by the shell (it navigates all five pages), so this page only
        // contributes the content column. That also means the home content automatically gets the
        // narrower width the rail leaves behind, without having to know the rail exists.
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
        ) {
                Spacer(Modifier.height(52.dp))

                if (AsterUiConfig.showGreeting) {
                    AsterGreeting()
                    Spacer(Modifier.height(22.dp))
                }

                AsterStatusCard(
                    state = state,
                    superuserCount = superuserCount,
                    moduleEnabledCount = moduleEnabledCount,
                    corner = corner,
                )

                Spacer(Modifier.height(14.dp))

                AsterDeviceCard(state = state, corner = corner)
                InfoCard(
                    systemInfo = state.systemInfo,
                    susfs = state.susfs,
                    zygiskProvider = state.zygiskProvider,
                    onSusfs = actions.onSusfs,
                )

            Spacer(Modifier.height(bottomInnerPadding + 32.dp))
        }
    }
}

/**
 * The wallpaper layer.
 *
 * Deliberately cheap: a colour-field gradient built from the active Material scheme rather than
 * a bitmap. It costs nothing to draw, needs no storage permission, and it inherits whatever
 * palette style / key colour the user picked, so the Aster page still feels like part of the
 * same app. A bitmap picker can slot in later without touching anything below it.
 */
@Composable
private fun AsterBackdrop() {
    val scheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to scheme.primaryContainer,
                    0.45f to scheme.surface,
                    1f to scheme.surfaceContainerLowest,
                )
            )
    )
}

/** Time-based greeting, matching the tokens the other shells use. */
@Composable
private fun AsterGreeting() {
    val hour = LocalTime.now().hour
    val (title, subtitle) = when (hour) {
        in 0..4 -> "Late night" to "もう少しだけ"
        in 5..10 -> "Good morning" to "良い一日を"
        in 11..13 -> "Good noon" to "お昼の時間"
        in 14..17 -> "Good afternoon" to "午後のひととき"
        in 18..22 -> "Good evening" to "お疲れさま"
        else -> "Good night" to "そろそろ休もう"
    }
    Column {
        Text(
            text = title,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** KernelSU status: the root state first, then the tallies. */
@Composable
private fun AsterStatusCard(
    state: HomeUiState,
    superuserCount: Int,
    moduleEnabledCount: Int,
    corner: Int,
) {
    val shape = ContinuousCornerShape(corner.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .folkGlassCard(
                cornerRadius = corner.dp,
                blurStrength = AsterUiConfig.glassStrength,
                transparency = AsterUiConfig.glassTransparency,
                liquidState = LocalFolkLiquidState.current,
                shape = shape,
            )
            .padding(18.dp),
    ) {
        val rooted = state.isRootAvailable
        Text(
            text = state.ksuVersion?.let { "KernelSU $it" } ?: "KernelSU",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (rooted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (rooted) "root 已就绪" else "root 不可用",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.isSafeMode) {
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "安全模式",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth()) {
            AsterStat("超级用户", superuserCount.toString(), Modifier.weight(1f))
            AsterStat("已启用模块", moduleEnabledCount.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun AsterStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Device line, kept short so it survives narrow rails. */
@Composable
private fun AsterDeviceCard(state: HomeUiState, corner: Int) {
    val shape = ContinuousCornerShape(corner.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .folkGlassCard(
                cornerRadius = corner.dp,
                blurStrength = AsterUiConfig.glassStrength,
                transparency = AsterUiConfig.glassTransparency,
                liquidState = LocalFolkLiquidState.current,
                shape = shape,
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "内核 " + state.kernelVersion.toString(),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (state.lkmMode == true) "LKM" else "GKI",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
