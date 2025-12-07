package com.r3d1.interval.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // For now, we don't auto-restore timers after boot
            // This receiver is here for future enhancement if needed
            // e.g., restore a timer that was running when phone was shut down
        }
    }
}
