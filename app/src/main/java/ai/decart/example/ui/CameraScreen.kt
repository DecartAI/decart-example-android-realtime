package ai.decart.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ai.decart.example.MainViewModel
import ai.decart.sdk.ConnectionState

@Composable
fun CameraScreen(viewModel: MainViewModel) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val currentModel by viewModel.currentModel.collectAsStateWithLifecycle()
    val currentSkinIndex by viewModel.currentSkinIndex.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()

    val isConnected = connectionState == ConnectionState.CONNECTED ||
            connectionState == ConnectionState.GENERATING
    val isConnecting = connectionState == ConnectionState.CONNECTING ||
            connectionState == ConnectionState.RECONNECTING

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (totalDrag < -100f) viewModel.nextSkin()
                        else if (totalDrag > 100f) viewModel.prevSkin()
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                )
            }
    ) {
        // Video layer
        VideoRenderer(
            viewModel = viewModel,
            viewMode = viewMode,
            modifier = Modifier.fillMaxSize()
        )

        // Top controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(80.dp))

            ModelSelector(
                currentModel = currentModel,
                onModelSelected = { viewModel.switchModel(it) }
            )

            ActionButtons(
                onFlipCamera = { viewModel.switchCamera() },
                onCycleViewMode = { viewModel.cycleViewMode() }
            )
        }

        // Bottom: style carousel
        if (isConnected) {
            StyleCarousel(
                skins = viewModel.currentSkins,
                selectedIndex = currentSkinIndex,
                onSkinSelected = { viewModel.switchSkin(it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
            )
        }

        // Connect button when disconnected
        if (connectionState == ConnectionState.DISCONNECTED) {
            ConnectButton(
                onClick = { viewModel.connect() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 48.dp)
            )
        }

        // Overlays
        if (isConnecting) {
            WarmingUpOverlay()
        }

        if (showOnboarding && isConnected) {
            OnboardingOverlay(onDismiss = { viewModel.dismissOnboarding() })
        }
    }
}
