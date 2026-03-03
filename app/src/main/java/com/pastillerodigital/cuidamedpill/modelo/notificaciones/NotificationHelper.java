package com.pastillerodigital.cuidamedpill.modelo.notificaciones;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationHelper {

    public static final String CHANNEL_ID = "canal_medicacion";

    public static void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Recordatorios medicación";
            String description = "Avisos para tomar medicamentos";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}