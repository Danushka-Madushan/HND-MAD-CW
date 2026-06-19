package nibm.mad.snapshop.screens.main

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import nibm.mad.snapshop.R
import nibm.mad.snapshop.composables.AnimatedShutterButton
import nibm.mad.snapshop.composables.CameraPermissionRequest
import nibm.mad.snapshop.composables.ScanningOverlay
import nibm.mad.snapshop.controllers.extractMainObject
import nibm.mad.snapshop.data.NavRoutes
import java.io.File
import java.util.concurrent.Executor

@Composable
fun MainScreen(
    onNavigate: (NavRoutes) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isInspection = LocalInspectionMode.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            if (isInspection) true else
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isFlashOn by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    isProcessing = true
                    try {
                        val croppedUri = extractMainObject(context, uri)
                        if (croppedUri != null) {
                            onNavigate(NavRoutes.ObjectResults(croppedUri.toString()))
                        } else {
                            Log.e("ML_KIT", "No prominent object found to crop.")
                        }
                    } finally {
                        isProcessing = false
                    }
                }
            }
        }
    )

    val cameraController = remember {
        if (!isInspection) {
            LifecycleCameraController(context).apply {
                setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            }
        } else null
    }

    LaunchedEffect(Unit) {
        (context as? ComponentActivity)?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(isFlashOn) {
        cameraController?.enableTorch(isFlashOn)
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Camera Preview (Full Screen Background)
            if (cameraController != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            controller = cameraController
                            cameraController.bindToLifecycle(lifecycleOwner)
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Camera Preview Placeholder", color = Color.White)
                }
            }

            // UI Overlay
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Controls (History - Flash - Settings)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 20.dp, start = 32.dp, end = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onNavigate(NavRoutes.History) },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_history),
                            contentDescription = "History",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { isFlashOn = !isFlashOn },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.flash_icon),
                            contentDescription = "Toggle Flash",
                            tint = if (isFlashOn) Color.Yellow else Color.White
                        )
                    }

                    IconButton(
                        onClick = { onNavigate(NavRoutes.Settings) },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Scanner Frame Overlay
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.scanner_frame),
                        contentDescription = "Scanner Frame",
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(1f)
                    )
                }

                // Bottom Controls (Gallery & Shutter)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.pick_image_icon),
                            contentDescription = "Pick Image",
                            tint = Color.White
                        )
                    }

                    AnimatedShutterButton(
                        onClick = {
                            // Guard against double-triggers since AnimatedShutterButton
                            // may not expose an enabled parameter
                            if (!isProcessing) {
                                cameraController?.let { controller ->
                                    takePhoto(
                                        controller = controller,
                                        executor = ContextCompat.getMainExecutor(context),
                                        onPhotoTaken = { uri ->
                                            coroutineScope.launch {
                                                isProcessing = true
                                                try {
                                                    val croppedUri = extractMainObject(context, uri)
                                                    if (croppedUri != null) {
                                                        onNavigate(NavRoutes.ObjectResults(croppedUri.toString()))
                                                    } else {
                                                        Log.e("ML_KIT", "No prominent object found to crop.")
                                                    }
                                                } finally {
                                                    isProcessing = false
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.size(56.dp))
                }
            }

            // Scanning overlay — rendered above everything while MLKit runs
            if (isProcessing) {
                ScanningOverlay()
            }
        }
    } else {
        CameraPermissionRequest(
            onAllowClicked = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    }
}



private fun takePhoto(
    controller: LifecycleCameraController,
    executor: Executor,
    onPhotoTaken: (Uri) -> Unit
) {
    val photoFile = File.createTempFile("snapshot_", ".jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    controller.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let { onPhotoTaken(it) } ?: onPhotoTaken(Uri.fromFile(photoFile))
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("Camera", "Failed to take photo: ", exception)
            }
        }
    )
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen {}
}
