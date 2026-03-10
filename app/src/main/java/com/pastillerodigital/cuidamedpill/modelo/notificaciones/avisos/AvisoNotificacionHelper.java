package com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.medicacion.NotificationHelper;

public class AvisoNotificacionHelper {

    public static final String CHANNEL_AVISOS = "canal_avisos";

    public static void mostrarAviso(Context context, Aviso aviso){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_AVISOS)
                    .setSmallIcon(R.drawable.ic_warning)
                    .setContentTitle(aviso.getTitulo())
                    .setContentText(aviso.getMensaje())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

        // Android 13+ requiere permiso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // No hay permiso: no hacemos nada, o podrías loggear
                return;
            }
        }

        NotificationManagerCompat.from(context)
                .notify((int)System.currentTimeMillis(), builder.build());
    }
}
