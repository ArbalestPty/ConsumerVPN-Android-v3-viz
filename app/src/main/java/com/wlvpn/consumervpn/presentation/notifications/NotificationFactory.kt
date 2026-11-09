package com.wlvpn.consumervpn.presentation.notifications

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.domain.value.ServerLocation
import com.wlvpn.consumervpn.presentation.MainActivity
import com.wlvpn.consumervpn.presentation.disconnectNotification.receiver.RunDisconnectWorkerReceiver

private const val FOREGROUND_NOTIFICATION_REQUEST_CODE = 1729

class NotificationFactory(val application: Application) {

    private val baseNotificationBuilder: NotificationCompat.Builder
        get() =
            NotificationCompat.Builder(
                application,
                application.getString(R.string.notification_channel_id)
            )
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setSmallIcon(R.drawable.notification_icon)

    val revokedNotification: Notification
        get() =
            NotificationCompat.Builder(
                application,
                application.getString(R.string.notification_channel_id)
            )
                .setContentTitle(application.getString(R.string.notification_revoked_label_title))
                .setOngoing(false)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setSmallIcon(R.drawable.notification_icon)
                .build()

    val vpnNotification: Notification
        get() =
            baseNotificationBuilder
                .setContentTitle(
                    application.getString(R.string.notification_vpn_label_title_starting)
                )
                .setSmallIcon(R.drawable.notification_icon)
                .build()

    val connectingNotification: Notification
        get() =
            baseNotificationBuilder
                .setContentTitle(
                    application.getString(R.string.notification_vpn_label_title_connecting)
                )
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(openAppAction)
                .build()

    fun connectedNotification(server: ServerLocation.Server?): Notification {

        val notificationLayoutExpanded = RemoteViews(
            application.packageName,
            R.layout.view_notification_expanded
        )
        val notificationLayoutCollapsed = RemoteViews(
            application.packageName,
            R.layout.view_notification_collapsed
        )

        notificationLayoutExpanded.setTextViewText(
            R.id.expanded_notification_title,
            application.getString(R.string.notification_vpn_label_title_connected)
        )
        notificationLayoutExpanded.setOnClickPendingIntent(
            R.id.expanded_notification_dissconect_action,
            disconnectVpnAction
        )
        server?.let {
            notificationLayoutExpanded.setTextViewText(
                R.id.expanded_notification_info,
                "${it.city.country.name},${it.city.name}"
            )
            notificationLayoutCollapsed.setTextViewText(
                R.id.collapsed_notification_title,
                application.getString(
                    R.string.notification_vpn_label_title_connected_server,
                    "${it.city.country.name},${it.city.name}"
                )
            )

        } ?: notificationLayoutCollapsed.setTextViewText(
            R.id.collapsed_notification_title,
            application.getString(R.string.notification_vpn_label_title_connected)
        )

        return baseNotificationBuilder
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(true)
            .setShowWhen(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setColor(ContextCompat.getColor(application, R.color.brand_notification_icon))
            .setCustomContentView(notificationLayoutCollapsed)
            .setCustomBigContentView(notificationLayoutExpanded)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(openAppAction)
            .build()
    }

    private val openAppAction
        get() = PendingIntent.getActivity(
            application,
            FOREGROUND_NOTIFICATION_REQUEST_CODE,
            mainActivityIntent(),
            PendingIntent.FLAG_IMMUTABLE
        )

    private val disconnectVpnAction
        get() = PendingIntent.getBroadcast(
            application,
            RunDisconnectWorkerReceiver.REQUEST_CODE,
            Intent(RunDisconnectWorkerReceiver.ACTION_DISCONNECT_VPN),
            PendingIntent.FLAG_IMMUTABLE
        ).apply {
            ContextCompat.registerReceiver(
                application,
                RunDisconnectWorkerReceiver(),
                IntentFilter(RunDisconnectWorkerReceiver.ACTION_DISCONNECT_VPN),
                ContextCompat.RECEIVER_EXPORTED
            )
        }

    private fun mainActivityIntent(): Intent =
        Intent(application, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
}