package com.example.firebaseauth.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseauth.R
import com.example.firebaseauth.model.DTRRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class DTRHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dtrRecordsList: List<DTRRecord> // Declare the list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dtr_history)

        recyclerView = findViewById(R.id.dtr_history)

        // Fetch the DTR records from Firestore
        fetchDTRRecords()

        // Set the LayoutManager and Adapter for the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    // Fetch DTR records from Firestore
    private fun fetchDTRRecords() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            // Handle error when user is not logged in
            return
        }

        db.collection("users") // Assuming the user collection
            .document(userId) // Use the logged-in user ID
            .collection("dtr_records") // Sub-collection for DTR records
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                val recordsList = mutableListOf<DTRRecord>()
                for (document: QueryDocumentSnapshot in result) {
                    // Extract day, arrival, departure times, and photo URL
                    val day = document.getLong("day")?.toInt() ?: 0
                    val amArrival = document.getString("amArrival") ?: ""
                    val amDeparture = document.getString("amDeparture") ?: ""
                    val pmArrival = document.getString("pmArrival") ?: ""
                    val pmDeparture = document.getString("pmDeparture") ?: ""
                    val photoUrl = document.getString("photoUrl") // This could be nullable

                    // Create a DTRRecord object
                    val record = DTRRecord(
                        day = day,
                        amArrival = amArrival,
                        amDeparture = amDeparture,
                        pmArrival = pmArrival,
                        pmDeparture = pmDeparture,
                        photoUrl = photoUrl
                    )
                    recordsList.add(record)
                }

                // Update the RecyclerView adapter with the fetched records
                dtrRecordsList = recordsList
                recyclerView.adapter = DTRHistoryAdapter(dtrRecordsList)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace() // Handle errors
            }
    }

    // Adapter class for RecyclerView
    class DTRHistoryAdapter(private val dtrRecordsList: List<DTRRecord>) :
        RecyclerView.Adapter<DTRHistoryAdapter.DTRViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DTRViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.dtr_row, parent, false)
            return DTRViewHolder(view)
        }

        override fun onBindViewHolder(holder: DTRViewHolder, position: Int) {
            val record = dtrRecordsList[position]

            // Bind the data to the views
            holder.date.text = record.day.toString() // Displaying the day instead of date (based on your model)
            holder.morningArrival.text = record.amArrival.takeIf { it.isNotEmpty() } ?: "-"
            holder.morningDeparture.text = record.amDeparture.takeIf { it.isNotEmpty() } ?: "-"
            holder.afternoonArrival.text = record.pmArrival.takeIf { it.isNotEmpty() } ?: "-"
            holder.afternoonDeparture.text = record.pmDeparture.takeIf { it.isNotEmpty() } ?: "-"
        }

        override fun getItemCount(): Int = dtrRecordsList.size

        // ViewHolder class for DTR records
        class DTRViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val date: TextView = itemView.findViewById(R.id.dtr_date)
            val morningArrival: TextView = itemView.findViewById(R.id.dtr_morning_in)
            val morningDeparture: TextView = itemView.findViewById(R.id.dtr_morning_out)
            val afternoonArrival: TextView = itemView.findViewById(R.id.dtr_afternoon_in)
            val afternoonDeparture: TextView = itemView.findViewById(R.id.dtr_afternoon_out)
        }
    }
}
