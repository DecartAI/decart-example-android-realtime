package ai.decart.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ai.decart.example.MainViewModel
import ai.decart.example.model.ViewMode
import org.webrtc.SurfaceViewRenderer

@Composable
fun VideoRenderer(
    viewModel: MainViewModel,
    viewMode: ViewMode,
    modifier: Modifier = Modifier
) {
    val eglContext = remember { viewModel.eglBase?.eglBaseContext }

    when (viewMode) {
        ViewMode.TRANSFORMED -> {
            // Remote only, full screen
            Box(modifier = modifier) {
                RemoteView(viewModel, eglContext, Modifier.fillMaxSize())
            }
        }

        ViewMode.PIP -> {
            Box(modifier = modifier) {
                RemoteView(viewModel, eglContext, Modifier.fillMaxSize())
                // Local PIP in bottom-left corner
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 140.dp)
                        .width(120.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    LocalView(viewModel, eglContext, Modifier.fillMaxSize())
                }
            }
        }

        ViewMode.VERTICAL_SPLIT -> {
            Column(modifier = modifier) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black)
                ) {
                    LocalView(viewModel, eglContext, Modifier.fillMaxSize())
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black)
                ) {
                    RemoteView(viewModel, eglContext, Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun RemoteView(
    viewModel: MainViewModel,
    eglContext: org.webrtc.EglBase.Context?,
    modifier: Modifier
) {
    if (eglContext == null) return
    AndroidView(
        factory = { ctx ->
            SurfaceViewRenderer(ctx).also { renderer ->
                renderer.init(eglContext, null)
                renderer.setEnableHardwareScaler(true)
                renderer.setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                viewModel.remoteRenderer = renderer
            }
        },
        modifier = modifier,
        onRelease = { renderer ->
            viewModel.remoteRenderer = null
            renderer.release()
        }
    )
}

@Composable
private fun LocalView(
    viewModel: MainViewModel,
    eglContext: org.webrtc.EglBase.Context?,
    modifier: Modifier
) {
    if (eglContext == null) return
    AndroidView(
        factory = { ctx ->
            SurfaceViewRenderer(ctx).also { renderer ->
                renderer.init(eglContext, null)
                renderer.setMirror(true)
                renderer.setEnableHardwareScaler(true)
                renderer.setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                viewModel.localRenderer = renderer
            }
        },
        modifier = modifier,
        onRelease = { renderer ->
            viewModel.localRenderer = null
            renderer.release()
        }
    )
}
