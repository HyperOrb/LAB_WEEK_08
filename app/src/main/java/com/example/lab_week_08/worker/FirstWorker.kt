package com.example.lab_week_08.worker

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class FirstWorker( // [cite: 67]
    context: Context, workerParams: WorkerParameters // [cite: 68]
): Worker(context, workerParams) { // [cite: 69]
    // Fungsi ini mengeksekusi proses di background [cite: 70]
    override fun doWork(): Result { // [cite: 72]
        // Mengambil data input [cite: 73]
        val id = inputData.getString(INPUT_DATA_ID) // [cite: 74]
        
        // Mensimulasikan proses berat selama 3 detik [cite: 75, 92]
        Thread.sleep(3000L) // [cite: 76]

        // Membuat data output [cite: 77]
        val outputData = Data.Builder() // [cite: 78]
            .putString(OUTPUT_DATA_ID, id) // [cite: 79]
            .build() // [cite: 80]

        // Mengembalikan hasil sukses beserta data output [cite: 81]
        return Result.success(outputData) // [cite: 82]
    }

    companion object { // [cite: 86]
        const val INPUT_DATA_ID = "inId" // [cite: 87]
        const val OUTPUT_DATA_ID = "outId" // [cite: 88]
    } // [cite: 89]
}