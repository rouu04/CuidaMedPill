package com.pastillerodigital.cuidamedpill.modelo.notificaciones;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ListenableWorker;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.activities.AlarmaMedicacionActivity;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoNotificacion;

/**
 * Gestiona notificaciones, crea el canal y muesta la notificacion
 */
public class NotificationHelper {

    public static final String CHANNEL_ID = "canal_medicacion"; //identificador del canal
    //tiene que ser el mimsmo que el que usa notification worker
    public static final String CHANNEL_NORMAL = "canal_normal";
    public static final String CHANNEL_ALARMA = "canal_alarma";
    public static final String CHANNEL_SILENCIOSO = "canal_silencioso";

    /**
     * Crea canal, llamado en main al iniciar app
     * @param context
     */
    public static void crearCanales(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            // Canal NORMAL
            NotificationChannel normalChannel = new NotificationChannel(
                    CHANNEL_NORMAL,
                    "Recordatorios normales",
                    NotificationManager.IMPORTANCE_HIGH
            );
            normalChannel.setDescription("Recordatorios estándar");
            normalChannel.enableVibration(true);
            normalChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, null);
            notificationManager.createNotificationChannel(normalChannel);

            // Canal ALARMA
            NotificationChannel alarmaChannel = new NotificationChannel(
                    CHANNEL_ALARMA,
                    "Alarmas de medicación",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alarmaChannel.setDescription("Alarmas importantes");
            alarmaChannel.enableVibration(true);
            alarmaChannel.setSound(Settings.System.DEFAULT_ALARM_ALERT_URI,
                    new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
            notificationManager.createNotificationChannel(alarmaChannel);

            // Canal SILENCIOSO
            NotificationChannel silenciosoChannel = new NotificationChannel(
                    CHANNEL_SILENCIOSO,
                    "Recordatorios silenciosos",
                    NotificationManager.IMPORTANCE_LOW
            );
            silenciosoChannel.setDescription("Sin sonido ni vibración");
            silenciosoChannel.setSound(null, null);
            silenciosoChannel.enableVibration(false);
            notificationManager.createNotificationChannel(silenciosoChannel);
        }
    }

    public static void mostrarNotificacion(Context context, String titulo, String mensaje, TipoNotificacion tipo,
                                           String nombreMed, boolean antiprocrastinador) {
        // Android 13+ requiere permiso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // No hay permiso: no hacemos nada, o podrías loggear
                return;
            }
        }

        String channelId;
        switch (tipo) {
            case ALARMA:
                channelId = CHANNEL_ALARMA;
                break;
            case SILENCIOSA:
                channelId = CHANNEL_SILENCIOSO;
                break;
            default:
                channelId = CHANNEL_NORMAL;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_pastilla_capsula)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (tipo == TipoNotificacion.ALARMA) {
            Intent intent = new Intent(context, AlarmaMedicacionActivity.class);
            intent.putExtra("nombreMed", nombreMed);
            intent.putExtra("antiprocrastinador", antiprocrastinador);

            // Flags para forzar la aparición
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    (int) System.currentTimeMillis(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setFullScreenIntent(pendingIntent, true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // Visible en cualquier momento

            // Forzamos la apertura de la actividad manualmente (para que se muestre incluso si está encendido)
            context.startActivity(intent);
        }

        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
    }
}