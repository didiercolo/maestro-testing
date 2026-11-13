package com.example.stackdemo.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Width below which the two panes stack vertically instead of sitting side by side. */
private val COMPACT_MAX_WIDTH = 600.dp

/**
 * Lays out [first] and [second] side by side on wide screens and stacked (scrollable)
 * on compact ones. Each pane receives the [Modifier] it should apply to its root.
 */
@Composable
fun ResponsiveTwoPane(
    modifier: Modifier = Modifier,
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        if (maxWidth < COMPACT_MAX_WIDTH) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                first(Modifier.fillMaxWidth())
                second(Modifier.fillMaxWidth())
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                first(Modifier.weight(1f).verticalScroll(rememberScrollState()))
                second(Modifier.weight(1f))
            }
        }
    }
}
