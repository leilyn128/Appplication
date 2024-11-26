import androidx.lifecycle.ViewModel
import com.example.firebaseauth.model.DTRRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*



class DTRViewModel : ViewModel() {
    private val _dtrHistory = MutableStateFlow<List<DTRRecord>>(emptyList())
    val dtrHistory: StateFlow<List<DTRRecord>> get() = _dtrHistory

    private val firestore = FirebaseFirestore.getInstance()

    // Fetch DTR records from Firestore
    fun fetchDTRHistory() {
        firestore.collection("DTRRecords")
            .get()
            .addOnSuccessListener { snapshot ->
                val records = snapshot.documents.mapNotNull { document ->
                    document.toObject(DTRRecord::class.java) // Convert Firestore document to DTRRecord
                }
                _dtrHistory.value = records // Update the state
            }
            .addOnFailureListener { exception ->
                println("Error fetching DTR history: ${exception.message}")
            }
    }

    // Get Current Month
    fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        return month ?: "Unknown Month"
    }


    // Parse Time String to Date Object (HH:mm format)
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

    // Helper function to format DTR record times (A.M. and P.M.)
    fun formatDTRRecordTimes(record: DTRRecord): DTRRecord {
        return record.copy(
            morningArrival = record.morningArrival?.let { formatTime(parseTime(it)) } ?: "____",
            morningDeparture = record.morningDeparture?.let { formatTime(parseTime(it)) } ?: "____",
            afternoonArrival = record.afternoonArrival?.let { formatTime(parseTime(it)) } ?: "____",
            afternoonDeparture = record.afternoonDeparture?.let { formatTime(parseTime(it)) }
                ?: "____"
        )
    }
}