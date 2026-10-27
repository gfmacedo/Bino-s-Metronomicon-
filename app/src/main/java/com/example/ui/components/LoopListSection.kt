package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MetronomeLoop
import com.example.model.PracticeStatus
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MetronomeBorder
import com.example.ui.theme.MetronomeSurface
import com.example.ui.theme.MetronomeSurfaceHigh
import com.example.ui.theme.MetronomeSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.MetronomeUiState
import com.example.viewmodel.MetronomiconViewModel

@Composable
fun LoopListSection(
    state: MetronomeUiState,
    viewModel: MetronomiconViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("loop_list_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MetronomeSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MetronomeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRACTICE SEQUENCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldLight,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${state.practiceLoops.size} loops",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.practiceLoops.isEmpty()) {
                Text(
                    text = "No loops defined. Use the settings above to add loops.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.practiceLoops.forEachIndexed { index, loop ->
                        val isCurrent = (state.practiceStatus != PracticeStatus.STOPPED) && (state.currentLoopIndex == index)
                        SingleLineLoopItem(
                            loop = loop,
                            index = index,
                            isCurrent = isCurrent,
                            isFirst = index == 0,
                            isLast = index == state.practiceLoops.size - 1,
                            onJumpTo = { viewModel.jumpToLoop(index) },
                            onEdit = { viewModel.editLoop(loop) },
                            onDelete = { viewModel.deleteLoop(loop.id) },
                            onMoveUp = { viewModel.moveLoopUp(index) },
                            onMoveDown = { viewModel.moveLoopDown(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleLineLoopItem(
    loop: MetronomeLoop,
    index: Int,
    isCurrent: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onJumpTo: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isCurrent) MetronomeSurfaceHigh else MetronomeSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isCurrent) 1.5.dp else 0.8.dp,
            color = if (isCurrent) GoldPrimary else MetronomeBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("loop_item_$index")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Index & Single-line Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Small indicator / index
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent) GoldPrimary else MetronomeSurfaceHigh)
                        .border(0.8.dp, if (isCurrent) Color.White else MetronomeBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) Color.Black else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${loop.name} (${loop.bpm} BPM • ${loop.subdivision.title})",
                    fontSize = 12.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCurrent) GoldLight else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Compact Action Buttons: Jump, Move, Edit, Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Play / Jump
                IconButton(
                    onClick = onJumpTo,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Jump to loop",
                        tint = if (isCurrent) GoldPrimary else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Move Up
                IconButton(
                    onClick = onMoveUp,
                    enabled = !isFirst,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Move Up",
                        tint = if (!isFirst) TextSecondary else TextTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Move Down
                IconButton(
                    onClick = onMoveDown,
                    enabled = !isLast,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Move Down",
                        tint = if (!isLast) TextSecondary else TextTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Edit
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit loop",
                        tint = GoldLight,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete loop",
                        tint = CrimsonAccent,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
