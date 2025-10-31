package com.example.lab_week_08

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NotificationService : Service() { // [cite: 300]

    // Builder untuk notifikasi [cite: 303]
    private lateinit var notificationBuilder: NotificationCompat.Builder // [cite: 304]
    // Handler untuk menjalankan proses di thread terpisah [cite: 305]
    private lateinit var serviceHandler: Handler // [cite: 306]

    // Tidak digunakan untuk komunikasi satu arah [cite: 307-309]
    override fun onBind(intent: Intent): IBinder? = null // [cite: 310]

    // Dipanggil saat service pertama kali dibuat [cite: 311-313]
    override fun onCreate() { // [cite: 314]
        super.onCreate() // [cite: 315]
        // Membangun notifikasi [cite: 316]
        notificationBuilder = startForegroundService() // [cite: 318]

        // Membuat HandlerThread (thread terpisah) [cite: 319-325]
        val handlerThread = HandlerThread("SecondThread").apply { start() } // [cite: 333-334]
        serviceHandler = Handler(handlerThread.looper) // [cite: 335, 337]
    }

    // Fungsi untuk membuat dan memulai notifikasi foreground [cite: 338]
    private fun startForegroundService(): NotificationCompat.Builder { // [cite: 339]
        // Membuat PendingIntent untuk membuka MainActivity saat notifikasi diklik [cite: 341-345]
        val pendingIntent = getPendingIntent() // [cite: 346]
        // Membuat channel notifikasi [cite: 347]
        val channelId = createNotificationChannel() // [cite: 350]
        
        // Membangun notifikasi [cite: 351-353]
        val notificationBuilder = getNotificationBuilder(pendingIntent, channelId) // [cite: 354-356]

        // Memulai foreground service [cite: 357-359]
        startForeground(NOTIFICATION_ID, notificationBuilder.build()) // [cite: 360]
        return notificationBuilder // [cite: 361]
    }

    // Membuat PendingIntent [cite: 362]
    private fun getPendingIntent(): PendingIntent { // [cite: 364]
        // Menyesuaikan flag berdasarkan versi SDK [cite: 365-369]
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) // [cite: 372]
            PendingIntent.FLAG_IMMUTABLE else 0 // [cite: 373]
        
        // Intent untuk membuka MainActivity [cite: 374-376]
        return PendingIntent.getActivity( // [cite: 377]
            this, 0, Intent(this, MainActivity::class.java), flag // [cite: 378-381]
        )
    }

    // Membuat channel notifikasi (wajib untuk API 26+) [cite: 386-389]
    private fun createNotificationChannel(): String { // [cite: 390]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // [cite: 391-393]
            val channelId = "001" // [cite: 395]
            val channelName = "001 Channel" // [cite: 397]
            val channelPriority = NotificationManager.IMPORTANCE_DEFAULT // [cite: 407]
            
            // Membangun channel [cite: 410]
            val channel = NotificationChannel(
                channelId, channelName, channelPriority
            ) // [cite: 411-413]
            
            // Mendaftarkan channel ke NotificationManager [cite: 416-421]
            val service = requireNotNull(
                ContextCompat.getSystemService(this, NotificationManager::class.java)
            ) // [cite: 417-419]
            service.createNotificationChannel(channel) // [cite: 422]
            return channelId // [cite: 424]
        } else {
            return "" // [cite: 426]
        }
    }

    // Membangun (build) notifikasi [cite: 429]
    private fun getNotificationBuilder(pendingIntent: PendingIntent, channelId: String) = // [cite: 429]
        NotificationCompat.Builder(this, channelId) // [cite: 430]
            .setContentTitle("Second worker process is done") // [cite: 432]
            .setContentText("Check it out!") // [cite: 434]
            .setSmallIcon(R.drawable.ic_launcher_foreground) // [cite: 436]
            .setContentIntent(pendingIntent) // [cite: 439]
            .setTicker("Second worker process is done, check it out!") // [cite: 440]
            .setOngoing(true) // [cite: 445] (Notifikasi tidak bisa di-dismiss user)

    // Dipanggil saat service di-start [cite: 466-467]
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { // [cite: 470]
        val returnValue = super.onStartCommand(intent, flags, startId) // [cite: 472-473]

        // Mengambil ID yang dikirim dari MainActivity [cite: 474]
        val id = intent?.getStringExtra(EXTRA_ID) // [cite: 475]
            ?: throw IllegalStateException("Channel ID must be provided") // [cite: 476]

        // Menjalankan tugas notifikasi di thread terpisah [cite: 477-478]
        serviceHandler.post { // [cite: 479]
            // Memulai hitung mundur di notifikasi [cite: 483]
            countDownFromTenToZero(notificationBuilder) // [cite: 484]
            // Memberi tahu MainActivity bahwa proses selesai [cite: 485]
            notifyCompletion(id) // [cite: 486]

            // Menghentikan foreground service (notifikasi hilang) [cite: 487-489]
            stopForeground(STOP_FOREGROUND_REMOVE) // [cite: 489]
            // Menghancurkan service [cite: 490]
            stopSelf() // [cite: 490]
        } // [cite: 481]

        return returnValue // [cite: 493]
    }

    // Fungsi untuk hitung mundur di notifikasi [cite: 497-498]
    private fun countDownFromTenToZero(notificationBuilder: NotificationCompat.Builder) { // [cite: 499-500]
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager // [cite: 501-503]
        
        for (i in 10 downTo 0) { // [cite: 504-505]
            Thread.sleep(1000L) // [cite: 506]
            // Update teks notifikasi [cite: 507]
            notificationBuilder.setContentText("$i seconds until last warning") // [cite: 508]
                .setSilent(true) // [cite: 509]
            
            // Tampilkan update [cite: 510]
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build()) // [cite: 511-514]
        }
    }

    // Update LiveData untuk memberi tahu MainActivity [cite: 517-519]
    private fun notifyCompletion(id: String) { // [cite: 520]
        Handler(Looper.getMainLooper()).post { // [cite: 521]
            mutableID.value = id // [cite: 524]
        }
    }

    companion object { // [cite: 446]
        const val NOTIFICATION_ID = 0xCA7 // [cite: 447]
        const val EXTRA_ID = "Id" // [cite: 448]

        // LiveData untuk melacak status service [cite: 449-453]
        private val mutableID = MutableLiveData<String>() // [cite: 458]
        val trackingCompletion: LiveData<String> = mutableID // [cite: 459]
    }
}