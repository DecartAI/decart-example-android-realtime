package ai.decart.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ai.decart.example.model.ViewMode
import ai.decart.sdk.realtime.RealtimeMediaStream
import io.livekit.android.compose.ui.VideoTrackView

@Composable
fun VideoRenderer(
    localStream: RealtimeMediaStream?,
    remoteStream: RealtimeMediaStream?,
    viewMode: ViewMode,
    modifier: Modifier = Modifier
) {
    when (viewMode) {
        ViewMode.TRANSFORMED -> {
            // Remote only, full screen
            Box(modifier = modifier) {
                StreamView(remoteStream, Modifier.fillMaxSize())
            }
        }

        ViewMode.PIP -> {
            Box(modifier = modifier) {
                StreamView(remoteStream, Modifier.fillMaxSize())
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
                    StreamView(localStream, Modifier.fillMaxSize(), mirror = true)
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
                    StreamView(localStream, Modifier.fillMaxSize(), mirror = true)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black)
                ) {
                    StreamView(remoteStream, Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun StreamView(
    stream: RealtimeMediaStream?,
    modifier: Modifier,
    mirror: Boolean = false
) {
    val videoTrack = stream?.videoTrack
    val room = stream?.room

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (videoTrack != null && room != null) {
            VideoTrackView(
                videoTrack = videoTrack,
                passedRoom = room,
                modifier = Modifier.fillMaxSize(),
                mirror = mirror
            )
        }
    }
}
