import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.firebaseauth.activity.uploadImageToFirebase
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.util.*

@Composable
fun CameraPage(
    navController: NavController,
    onImageCaptured: (Uri) -> Unit,
    onSaveImage: (Uri) -> Unit
) {
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // File and URI for the image
    val imageFile = File(context.filesDir, "camera_photo.jpg")
    val imageUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileProvider",
        imageFile
    )

    // Register the ActivityResultLauncher for capturing image
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Log.d("CameraPage", "Picture captured successfully")
            capturedImageUri?.let { uri ->
                // Handle the captured image URI and upload to Firebase or other actions
                onImageCaptured(uri)  // You can pass the URI to the callback if needed
                Toast.makeText(context, "Image captured", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("CameraPage", "Failed to capture image")
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    // Request permission for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                capturedImageUri = imageUri
                takePictureLauncher.launch(imageUri)
            } else {
                Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // UI to show the page
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            IconButton(onClick = { navController.popBackStack() }) { // Use popBackStack to go back
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Captured image preview
                    if (capturedImageUri != null) {
                        Image(
                            painter = rememberImagePainter(capturedImageUri),
                            contentDescription = "Captured Image",
                            modifier = Modifier
                                .size(250.dp)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Text("No image captured yet")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Button to take picture
                    Button(
                        onClick = {
                            val permissionStatus = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            )
                            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                                // If permission is granted, launch the camera
                                capturedImageUri = imageUri
                                takePictureLauncher.launch(imageUri)
                            } else {
                                // If permission is not granted, request it
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    ) {
                        Text("Take Picture")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            capturedImageUri?.let { uri ->
                                onSaveImage(uri)
                                // Show a success message using Toast
                                Toast.makeText(context, "Saved Successfully", Toast.LENGTH_SHORT).show()
                            } ?: Log.e("SaveImage", "No image URI to save")
                        }
                    ) {
                        Text("Save Picture")
                    }

                    }
                }

        }
    )
}
