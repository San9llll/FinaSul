package me.weishu.kernelsu.aster.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 「主题」页面里的 AsterUI 入口卡。
 *
 * ## 为什么单独一张卡
 *
 * AsterUI 不是单个开关，是一整套外观（玻璃怎么糊、背景画什么、侧栏显示哪些、
 * 时钟长什么样……）。跟「配色方案」「明暗」那些平铺在同一层里会显得很乱，
 * 所以给一张卡当入口，点进去是完整的设置页 —— 跟 Nakour 那边一样。
 *
 * ## 只在 AsterUI 布局下显示
 *
 * 这一整套设置对别的布局没有任何作用（其它布局的首页不画玻璃、不画侧栏），
 * 所以调用方应该先判断 [me.weishu.kernelsu.aster.AsterUiConfig.active] 再决定要不要放它。
 * 这儿的卡片本身不做判断 —— 它只管画。
 *
 * ## 外观跟 FinaSU 的主题页一致
 *
 * 用的 Miuix 的 `Card`，配 [AsterMiuixTheme] 提供的配色，所以颜色跟着
 * 「设置 → 主题」里选的基准色走，不会跟旁边那几张卡各说各话。
 */
@Composable
fun AsterThemeEntryCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MiuixIcons.Tune,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MiuixTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(14.dp))
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "AsterUI 设置",
                    style = MiuixTheme.textStyles.title4,
                )
                Text(
                    text = "玻璃、背景、时钟、侧栏 —— 只对 AsterUI 布局生效",
                    style = MiuixTheme.textStyles.footnote1,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "›",
                style = MiuixTheme.textStyles.title4,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}
