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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


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

        db.collection("dtr_records")
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                val recordsList = mutableListOf<DTRRecord>()
                for (document: QueryDocumentSnapshot in result) {
                    // Convert Firestore Timestamp to Date, then to String
                    val timestamp = document.getTimestamp("date")
                    val date: Date = timestamp?.toDate() ?: Date()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedDate = dateFormat.format(date) // Convert Date to String

                    // Extract the other fields from Firestore
                    val morningArrival = document.getString("morningArrival")
                    val morningDeparture = document.getString("morningDeparture")
                    val afternoonArrival = document.getString("afternoonArrival")
                    val afternoonDeparture = document.getString("afternoonDeparture")

                    // Create a DTRRecord object
                    val record = DTRRecord(
                        date = formattedDate,
                        morningArrival = morningArrival,
                        morningDeparture = morningDeparture,
                        afternoonArrival = afternoonArrival,
                        afternoonDeparture = afternoonDeparture
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


    class DTRHistoryAdapter(private val dtrRecordsList: List<DTRRecord>) :
        RecyclerView.Adapter<DTRHistoryAdapter.DTRViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DTRViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.dtr_row, parent, false)
            return DTRViewHolder(view)
        }

        override fun onBindViewHolder(holder: DTRViewHolder, position: Int) {
            val record = dtrRecordsList[position]

            // Bind the data to the views
            holder.date.text = record.date // Directly bind the date string
            holder.morningArrival.text = record.morningArrival ?: "-"
            holder.morningDeparture.text = record.morningDeparture ?: "-"
            holder.afternoonArrival.text = record.afternoonArrival ?: "-"
            holder.afternoonDeparture.text = record.afternoonDeparture ?: "-"
        }

        override fun getItemCount(): Int = dtrRecordsList.size

        class DTRViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val date: TextView = itemView.findViewById(R.id.dtr_date)
            val morningArrival: TextView = itemView.findViewById(R.id.dtr_morning_in)
            val morningDeparture: TextView = itemView.findViewById(R.id.dtr_morning_out)
            val afternoonArrival: TextView = itemView.findViewById(R.id.dtr_afternoon_in)
            val afternoonDeparture: TextView = itemView.findViewById(R.id.dtr_afternoon_out)
        }
    }
}