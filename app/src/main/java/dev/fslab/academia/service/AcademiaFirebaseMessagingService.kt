package dev.fslab.academia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.fslab.academia.MainActivity
import dev.fslab.academia.R
import dev.fslab.academia.model.FcmTokenRequest
import dev.fslab.academia.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AcademiaFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "academia_push_channel"
        const val CHANNEL_NAME = "Notificações Academia"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val titulo = message.notification?.title ?: message.data["title"] ?: "Academia"
        val corpo  = message.notification?.body  ?: message.data["body"]  ?: ""
        exibirNotificacao(titulo, corpo)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Enviar novo token à API de forma assíncrona
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.authApi.updateFcmToken(FcmTokenRequest(token))
            } catch (_: Exception) { /* silencia falha de rede */ }
        }
    }

    private fun exibirNotificacao(titulo: String, corpo: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Criar canal (obrigatório para Android 8+)
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(corpo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
