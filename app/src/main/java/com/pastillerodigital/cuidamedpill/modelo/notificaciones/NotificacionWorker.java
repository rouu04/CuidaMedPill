package com.pastillerodigital.cuidamedpill.modelo.notificaciones;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pastillerodigital.cuidamedpill.R;

public class NotificacionWorker extends Worker {

    public NotificacionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String nombreMed = getInputData().getString("nombreMed");

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), NotificationHelper.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_pastilla_capsula)
                        .setContentTitle("Hora de tu medicación")
                        .setContentText("Es momento de tomar: " + nombreMed)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (getApplicationContext().checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return Result.failure();
            }
        }

        NotificationManagerCompat.from(getApplicationContext()).notify((int) System.currentTimeMillis(), builder.build());
        return Result.success();
    }
}