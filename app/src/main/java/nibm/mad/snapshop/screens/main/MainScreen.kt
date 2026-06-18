package nibm.mad.snapshop.screens.main

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import nibm.mad.snapshop.R
import nibm.mad.snapshop.composables.AnimatedShutterButton
import java.io.File
import java.util.concurrent.Executor

@Composable
fun MainScreen(
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isInspection = LocalInspectionMode.current

    var hasCameraPermission by remember {
        mutableStateOf(
            if (isInspection) true else
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isFlashOn by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
                // TODO: Handle the picked image (e.g., upload it or analyze it)
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

    // Request permission on start if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Toggle Flashlight
    LaunchedEffect(isFlashOn) {
        cameraController?.enableTorch(isFlashOn)
    }
    
    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. Camera Preview (Full Screen Background)
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
                // 3. Top Controls (History - Flash - Settings)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 20.dp, start = 32.dp, end = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // History (top-left)
                    IconButton(
                        onClick = { onNavigate("history") },
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

                    // Flash (top-center)
                    IconButton(
                        onClick = { isFlashOn = !isFlashOn },
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

                    // Settings (top-right)
                    IconButton(
                        onClick = { onNavigate("settings") },
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

                // 2. Scanner Frame Overlay (Weighted to Center Visually)
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
                            .fillMaxWidth(0.8f) // Adjust width to fit nicely on screen
                            .aspectRatio(1f) // Assumes a square frame
                    )
                }

                // 4. Bottom Controls (Gallery & Shutter)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Left: Pick Image from Gallery
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
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

                    // Center: Animated Shutter Button
                    AnimatedShutterButton(
                        onClick = {
                            cameraController?.let { controller ->
                                takePhoto(
                                    controller = controller,
                                    executor = ContextCompat.getMainExecutor(context),
                                    onPhotoTaken = { uri ->
                                        // TODO: Handle the taken photo URI (e.g., navigate to results)
                                        Log.d("Camera", "Photo saved: $uri")
                                    }
                                )
                            }
                        }
                    )

                    // Right: Empty spacer to keep the shutter perfectly centered
                    Spacer(modifier = Modifier.size(56.dp))
                }
            }
        }
    } else {
        // Fallback UI if permission is denied
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required to use this feature.")
        }
    }
}

private fun takePhoto(
    controller: LifecycleCameraController,
    executor: Executor,
    onPhotoTaken: (Uri) -> Unit
) {
    // Create a temporary file to store the image
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
