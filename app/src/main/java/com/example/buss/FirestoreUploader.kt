package com.example.buss

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.io.BufferedReader
import java.io.InputStreamReader

object FirestoreUploader {

    fun uploadCsvToFirestore(context: Context) {

        val db = FirebaseFirestore.getInstance()

        try {

            val inputStream =
                context.assets.open("routes.csv")

            val reader =
                BufferedReader(
                    InputStreamReader(inputStream)
                )

            var line: String?

            var isFirstLine = true

            while (true) {

                line = reader.readLine()

                if (line == null)
                    break

                if (isFirstLine) {
                    isFirstLine = false
                    continue
                }

                val tokens = line.split(",")

                if (tokens.size >= 4) {

                    val routeNo =
                        tokens[1].trim()

                    val source =
                        tokens[2].trim()

                    val destination =
                        tokens[3].trim()

                    val routeData = hashMapOf(
                        "routeNo" to routeNo,
                        "source" to source,
                        "destination" to destination
                    )

                    db.collection("routes")
                        .document(routeNo)
                        .set(routeData)
                        .addOnSuccessListener {

                            Log.d(
                                "UPLOAD",
                                "Uploaded: $routeNo"
                            )
                        }
                        .addOnFailureListener {

                            Log.e(
                                "UPLOAD",
                                "Failed: $routeNo"
                            )
                        }
                }
            }

            reader.close()

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }
}