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
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoNotificacion;

/**
 * Clase que realmente muestra la notificación cuando llega la hora
 * Worker indica que trabaja en segundo plano
 */
public class NotificacionWorker extends Worker {

    public NotificacionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Métod*o que se ejecuta cuando llega el momento programado
     * @return
     */
    @NonNull
    @Override
    public Result doWork() {
        String titulo = getInputData().getString("titulo");
        String mensaje = getInputData().getString("mensaje");
        String tipoStr = getInputData().getString("tipoNotificacion");
        String nombreMed = getInputData().getString("nombreMed");
        boolean antipro = getInputData().getBoolean("antiprocrastinador", false);

        if (tipoStr == null) {
            tipoStr = TipoNotificacion.ESTANDAR.toString(); // valor por defecto
        }

        if (titulo == null || mensaje == null) return Result.failure();

        TipoNotificacion tipo = TipoNotificacion.valueOf(tipoStr);

        // Permisos Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return Result.failure();
            }
        }

        NotificationHelper.mostrarNotificacion(getApplicationContext(), titulo, mensaje, tipo, nombreMed, antipro);
        return Result.success();
    }
}