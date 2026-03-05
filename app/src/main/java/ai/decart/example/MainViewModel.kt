package ai.decart.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ai.decart.sdk.*
import ai.decart.sdk.realtime.*
import ai.decart.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.webrtc.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val API_KEY = "your-api-key-here"
        private const val BASE_URL = "wss://api.decart.ai"
        private const val PREFS_NAME = "decart_prefs"
        private const val KEY_ONBOARDING_SHOWN = "onboarding_shown"
    }

    private val context: Context get() = getApplication()

    // WebRTC - initialized once, shared EGL context for camera + SDK + renderers
    var eglBase: EglBase? = null
        private set
    private var peerConnectionFactory: PeerConnectionFactory? = null

    // SDK client - created once, reused across connect/disconnect cycles
    private var client: RealTimeClient? = null
    private var stateCollectorJob: Job? = null

    private var videoCapturer: CameraVideoCapturer? = null
    private var videoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var isFrontFacingCamera = true

    // Renderers set by UI
    var localRenderer: SurfaceViewRenderer? = null
    var remoteRenderer: SurfaceViewRenderer? = null

    // Remote video track ref
    private var remoteVideoTrack: VideoTrack? = null
    private var hasEverConnected = false

    // State
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _currentModel = MutableStateFlow(AppModel.RESTYLE)
    val currentModel: StateFlow<AppModel> = _currentModel.asStateFlow()

    private val _currentSkinIndex = MutableStateFlow(0)
    val currentSkinIndex: StateFlow<Int> = _currentSkinIndex.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.TRANSFORMED)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    val currentSkins: List<Skin>
        get() = when (_currentModel.value) {
            AppModel.RESTYLE -> SkinLists.mirageSkins
            AppModel.EDIT -> SkinLists.lucySkins
        }

    val currentSkin: Skin
        get() = currentSkins.getOrElse(_currentSkinIndex.value) { currentSkins.first() }

    val isConnected: Boolean
        get() {
            val state = _connectionState.value
            return state == ConnectionState.CONNECTED || state == ConnectionState.GENERATING
        }

    init {
        eglBase = EglBase.create()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ONBOARDING_SHOWN, false)) {
            _showOnboarding.value = true
        }
    }

    private fun ensureInitialized() {
        if (peerConnectionFactory == null) {
            val eglContext = eglBase!!.eglBaseContext
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context)
                    .setEnableInternalTracer(false)
                    .createInitializationOptions()
            )
            peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglContext, true, true))
                .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglContext))
                .createPeerConnectionFactory()
        }
        if (client == null) {
            val rtClient = RealTimeClient(
                context = context,
                config = RealTimeClientConfig(
                    apiKey = API_KEY,
                    baseUrl = BASE_URL,
                    logger = AndroidLogger(LogLevel.WARN)
                )
            )
            rtClient.initialize(eglBase)
            client = rtClient

            stateCollectorJob = viewModelScope.launch {
                rtClient.connectionState.collect { state ->
                    _connectionState.value = state
                }
            }
        }
    }

    fun dismissOnboarding() {
        _showOnboarding.value = false
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ONBOARDING_SHOWN, true).apply()
    }

    fun connect() {
        viewModelScope.launch {
            try {
                _connectionState.value = ConnectionState.CONNECTING
                hasEverConnected = true

                ensureInitialized()
                startCamera()

                val skin = currentSkin
                val renderer = remoteRenderer

                client!!.connect(
                    localVideoTrack = localVideoTrack,
                    options = ConnectOptions(
                        model = _currentModel.value.realtimeModel,
                        onRemoteVideoTrack = { track ->
                            remoteVideoTrack = track
                            renderer?.let { track.addSink(it) }
                        },
                        initialPrompt = InitialPrompt(text = skin.prompt, enhance = false)
                    )
                )
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.DISCONNECTED
                try { client?.disconnect() } catch (_: Exception) {}
                stopCamera()
            }
        }
    }

    fun disconnect() {
        remoteVideoTrack?.let { track ->
            remoteRenderer?.let { track.removeSink(it) }
        }
        remoteVideoTrack = null
        client?.disconnect()
        stopCamera()
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun switchModel(model: AppModel) {
        if (model == _currentModel.value) return
        _currentModel.value = model
        _currentSkinIndex.value = 0
        if (isConnected || _connectionState.value == ConnectionState.CONNECTING) {
            disconnect()
            connect()
        }
    }

    fun switchSkin(index: Int) {
        val skins = currentSkins
        if (index < 0 || index >= skins.size) return
        _currentSkinIndex.value = index
        if (isConnected) {
            client?.setPrompt(skins[index].prompt, false)
        }
    }

    fun nextSkin() {
        val next = (_currentSkinIndex.value + 1) % currentSkins.size
        switchSkin(next)
    }

    fun prevSkin() {
        val prev = (_currentSkinIndex.value - 1 + currentSkins.size) % currentSkins.size
        switchSkin(prev)
    }

    fun switchCamera() {
        val capturer = videoCapturer ?: return
        isFrontFacingCamera = !isFrontFacingCamera
        capturer.switchCamera(null)
    }

    fun cycleViewMode() {
        _viewMode.value = when (_viewMode.value) {
            ViewMode.TRANSFORMED -> ViewMode.PIP
            ViewMode.PIP -> ViewMode.VERTICAL_SPLIT
            ViewMode.VERTICAL_SPLIT -> ViewMode.TRANSFORMED
        }
    }

    fun onForeground() {
        if (hasEverConnected && _connectionState.value == ConnectionState.DISCONNECTED) {
            connect()
        }
    }

    fun onBackground() {
        if (isConnected) {
            disconnect()
        }
    }

    private fun startCamera() {
        val egl = eglBase ?: return
        val factory = peerConnectionFactory ?: return

        val enumerator = Camera2Enumerator(context)
        val deviceName = if (isFrontFacingCamera) {
            enumerator.deviceNames.firstOrNull { enumerator.isFrontFacing(it) }
        } else {
            enumerator.deviceNames.firstOrNull { !enumerator.isFrontFacing(it) }
        } ?: enumerator.deviceNames.firstOrNull() ?: return

        val capturer = enumerator.createCapturer(deviceName, null)
        videoCapturer = capturer

        videoSource = factory.createVideoSource(capturer.isScreencast)
        localVideoTrack = factory.createVideoTrack("local_video", videoSource)
        localVideoTrack?.setEnabled(true)

        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", egl.eglBaseContext)
        capturer.initialize(surfaceTextureHelper, context, videoSource!!.capturerObserver)
        capturer.startCapture(1280, 720, 30)

        localRenderer?.let { localVideoTrack?.addSink(it) }
    }

    private fun stopCamera() {
        localRenderer?.let { localVideoTrack?.removeSink(it) }
        localVideoTrack?.dispose()
        localVideoTrack = null
        try { videoCapturer?.stopCapture() } catch (_: Exception) {}
        videoCapturer?.dispose()
        videoCapturer = null
        videoSource?.dispose()
        videoSource = null
        surfaceTextureHelper?.dispose()
        surfaceTextureHelper = null
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
        stateCollectorJob?.cancel()
        client?.release()
        client = null
        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
        // eglBase is released by client.release() since we passed it to initialize()
        eglBase = null
    }
}
