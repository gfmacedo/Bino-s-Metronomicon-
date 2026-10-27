package com.example

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LoopEditorSection
import com.example.ui.components.LoopListSection
import com.example.ui.components.PracticeControlBar
import com.example.ui.components.SoundSelectorSection
import com.example.ui.components.VisualPulseIndicator
import com.example.ui.theme.MetronomeBg
import com.example.ui.theme.MetronomiconTheme
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.MetronomiconViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MetronomiconViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable hardware volume buttons to control media stream directly
        volumeControlStream = AudioManager.STREAM_MUSIC
        enableEdgeToEdge()
        setContent {
            MetronomiconTheme(darkTheme = true) {
                MetronomiconMainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MetronomiconMainScreen(
    viewModel: MetronomiconViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("metronomicon_main_screen"),
        containerColor = MetronomeBg,
        topBar = {
            // Very small and clean app header without icon or description
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BINO's Metronomicon",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Compact Mustard Dots Pulse Indicator
                item(key = "visual_pulse_indicator") {
                    VisualPulseIndicator(state = uiState)
                }

                // 2. Master Practice Controls Deck (Single Start / Stop Button + Initial Count-In)
                item(key = "practice_control_bar") {
                    PracticeControlBar(
                        state = uiState,
                        viewModel = viewModel
                    )
                }

                // 3. Compact Loop Builder Section (BPM, Steppers for Beats, Subdivisions, Count-In)
                item(key = "loop_editor_section") {
                    LoopEditorSection(
                        state = uiState,
                        viewModel = viewModel
                    )
                }

                // 4. Practice Sequence List (Single-line per loop, empty by default)
                item(key = "loop_list_section") {
                    LoopListSection(
                        state = uiState,
                        viewModel = viewModel
                    )
                }

                // 5. Click Sound Selector at the bottom below the practice list
                item(key = "sound_selector_section") {
                    SoundSelectorSection(
                        state = uiState,
                        viewModel = viewModel
                    )
                }

                // Bottom padding spacer
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
