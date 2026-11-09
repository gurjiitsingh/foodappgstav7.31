package com.it10x.foodappgstav7_31.service

import android.app.*
import android.content.*
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.FirebaseApp
import com.it10x.foodappgstav7_31.R
import com.it10x.foodappgstav7_31.printer.AutoPrintManager
import com.it10x.foodappgstav7_31.data.online.repository.OrdersRepository
import com.it10x.foodappgstav7_31.printer.PrinterManager
//Called from MainActivity
//Call FirestoreOnlineOrderListener
class OnlineOrderMonitoringService : Service() {

    private lateinit var listener: FirestoreOnlineOrderListener
    private var isReceiverRegistered = false

    private val stopSoundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
           listener.stopRingtone()
          }
    }


    override fun onCreate() {
        super.onCreate()

        Log.d("ORDER_MONITOR", "🟢 Service onCreate() called")

        if (FirebaseApp.getApps(this).isEmpty()) {
            Log.e("ORDER_MONITOR", "❌ Firebase is not initialized. Stopping service.")
            stopSelf()
            return
        }

        Log.d("ORDER_MONITOR", "✅ Firebase initialized")

        try {
            val printerManager = PrinterManager.getInstance(this)
            val ordersRepo = OrdersRepository()

            val autoPrint = AutoPrintManager(
                printerManager = printerManager,
                ordersRepository = ordersRepo
            )

            listener = FirestoreOnlineOrderListener(
                context = application,
                autoPrintManager = autoPrint
            )

            Log.d("ORDER_MONITOR", "📡 Calling listener.startListening()")
            listener.startListening()
            Log.d("ORDER_MONITOR", "✅ listener.startListening() returned")

            val filter = IntentFilter("STOP_RINGTONE")

            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(
                    stopSoundReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
                )
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                registerReceiver(stopSoundReceiver, filter)
            }

            isReceiverRegistered = true
            Log.d("ORDER_MONITOR", "✅ STOP_RINGTONE receiver registered")

            startForeground(99, buildNotification())
            Log.d("ORDER_MONITOR", "✅ Foreground service started")

        } catch (e: Exception) {
            Log.e("ORDER_MONITOR", "❌ Error while starting order monitoring", e)
            stopSelf()
        }
    }

    private fun buildNotification(): Notification {
        val channelId = "orders_monitor"
        val channelName = "Order Monitoring"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Order Monitoring Active")
            .setContentText("Listening for new orders…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {

        if (isReceiverRegistered) {
            try {
                unregisterReceiver(stopSoundReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isReceiverRegistered = false
        }

        if (::listener.isInitialized) {
            listener.stopListening()
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}