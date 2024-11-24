import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.firebaseauth.databinding.ActivityCameraBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.util.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions



class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var imageUri: Uri

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the image URI
        imageUri = createUri()

        // Register the ActivityResultLauncher
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    // Display the captured image in the ImageView
                    binding.imageView.setImageURI(imageUri)

                    // Upload the captured image to Firebase and register it
                    uploadImageToFirebase(
                        uri = imageUri,
                        onUploadSuccess = { downloadUrl ->
                            // Store the image URL in Firestore after upload
                            saveImageUrlToFirestore(userId = "USER_ID", imageUrl = downloadUrl)
                            Toast.makeText(
                                this,
                                "Image uploaded and registered successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onUploadFail = { error ->
                            Toast.makeText(this, "Image upload failed: $error", Toast.LENGTH_SHORT)
                                .show()
                        }
                    )
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            }

        // Set up the button to take a picture
        binding.btnTakePicture.setOnClickListener {
            if (checkCameraPermission()) {
                if (imageUri != null) {
                    takePictureLauncher.launch(imageUri)
                } else {
                    Log.e("CameraActivity", "imageUri is null!")
                    Toast.makeText(this, "Failed to prepare image URI", Toast.LENGTH_SHORT).show()
                }
            } else {
                requestCameraPermission()
            }
        }
    }

    private fun createUri(): Uri {
        try {
            val imageFile = File(applicationContext.filesDir, "camera_photo.jpg")
            return FileProvider.getUriForFile(
                applicationContext,
                "com.example.firebaseauth.fileProvider",
                imageFile
            )
        } catch (e: Exception) {
            Log.e("CameraActivity", "Error creating URI", e)
            throw e // Rethrow the exception to crash early if URI creation fails
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    private fun uploadImageToFirebase(
        uri: Uri,
        onUploadSuccess: (String) -> Unit,
        onUploadFail: (String) -> Unit
    ) {
        try {
            val storageReference: StorageReference = FirebaseStorage.getInstance().reference
            val fileName = "images/${UUID.randomUUID()}.jpg"

            // Upload the image to Firebase Storage
            val uploadTask = storageReference.child(fileName).putFile(uri)
            uploadTask.addOnSuccessListener {
                // Get the download URL of the uploaded image
                storageReference.child(fileName).downloadUrl.addOnSuccessListener { downloadUri ->
                    onUploadSuccess(downloadUri.toString())  // Pass the download URL
                }
            }.addOnFailureListener { exception ->
                onUploadFail(exception.message ?: "Upload failed")  // Handle upload failure
                Log.e("Firebase", "Upload failed", exception)
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error uploading image", e)
            onUploadFail("Error uploading image: ${e.message}")
        }
    }

    private fun saveImageUrlToFirestore(userId: String, imageUrl: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userDoc = firestore.collection("users").document(userId)

        // Save the image URL under the user's profile
        userDoc.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile image URL saved successfully", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to save profile image URL: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("Firestore", "Failed to save profile image URL", exception)
            }
    }

    private fun detectFaceInImage(imageUri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

            // Convert the image to an InputImage
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            // Set up the Face Detector
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()

            val detector = FaceDetection.getClient(options)

            // Process the image with face detection
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        // Face detected, proceed with upload
                        uploadImageToFirebase(
                            uri = imageUri,
                            onUploadSuccess = { downloadUrl ->
                                // Handle success: store the URL in Firestore, or any other action
                                saveImageUrlToFirestore(userId = "USER_ID", imageUrl = downloadUrl)
                                Toast.makeText(
                                    this,
                                    "Image uploaded and registered successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onUploadFail = { error ->
                                // Handle failure: show an error message
                                Toast.makeText(
                                    this,
                                    "Image upload failed: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    } else {
                        // No face detected, show an error message
                        Toast.makeText(this, "No face detected in the image", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FaceDetection", "Face detection failed: $e")
                    Toast.makeText(this, "Face detection failed", Toast.LENGTH_SHORT).show()
                }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }
}