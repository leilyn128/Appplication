import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseauth.model.DTRRecord
import com.example.firebaseauth.model.GeofenceData
import com.example.firebaseauth.repository.DTRRepository
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DTRViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    // Initialize DTR repository
    private val dtrRepository: DTRRepository = DTRRepository(firestore)

    // StateFlow for DTR history
    private val _dtrHistory = MutableStateFlow<List<DTRRecord>>(emptyList())
    val dtrHistory: StateFlow<List<DTRRecord>> get() = _dtrHistory

    // LiveData for geofence data
    private val _geofenceData = MutableLiveData<GeofenceData>()
    val geofenceData: LiveData<GeofenceData> get() = _geofenceData


    init {
        fetchGeofenceData()
    }

    // Save DTR record
    fun saveDTR(dtr: DTRRecord) {
        viewModelScope.launch {
            try {
                dtrRepository.saveDTR(dtr)
            } catch (e: Exception) {
                Log.e("DTRViewModel", "Error saving DTR: ${e.message}")
            }
        }
    }

    // Fetch geofence data
    private fun fetchGeofenceData() {
        firestore.collection("geofences")
            .document("bisu_clarin") // Geofence document ID
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    val radius = document.getDouble("radius")?.toFloat() ?: 0f

                    val geofenceData = GeofenceData(LatLng(latitude, longitude), radius.toDouble())
                    _geofenceData.value = geofenceData
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DTRViewModel", "Failed to fetch geofence data: ${exception.message}")
            }
    }

    // Fetch DTR history
    fun fetchDTRHistory() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users") // User collection
                .document(userId) // User document ID
                .collection("dtr_records") // Sub-collection
                .get()
                .addOnSuccessListener { snapshot ->
                    val records = snapshot.documents.mapNotNull { document ->
                        document.toObject(DTRRecord::class.java)
                    }
                    _dtrHistory.value = records
                }
                .addOnFailureListener { exception ->
                    Log.e("DTRViewModel", "Error fetching DTR history: ${exception.message}")
                }
        }
    }

    // Get current month as string
    fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
            ?: "Unknown Month"
    }

    // Format times
    fun formatDTRRecordTimes(record: DTRRecord): DTRRecord {
        return record.copy(
            amArrival = record.amArrival?.let { formatTime(parseTime(it)) } ?: "____",
            amDeparture = record.amDeparture?.let { formatTime(parseTime(it)) } ?: "____",
            pmArrival = record.pmArrival?.let { formatTime(parseTime(it)) } ?: "____",
            pmDeparture = record.pmDeparture?.let { formatTime(parseTime(it)) } ?: "____"
        )
    }

    // Helper function to format time
    private fun parseTime(timeString: String): Date? {
        return try {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            dateFormat.parse(timeString)
        } catch (e: Exception) {
            null
        }
    }

    // Helper function to format times like "8:00 AM"
    fun formatTime(date: Date?): String {
        return if (date != null) {
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            format.format(date)
        } else {
            "____"
        }
    }

}