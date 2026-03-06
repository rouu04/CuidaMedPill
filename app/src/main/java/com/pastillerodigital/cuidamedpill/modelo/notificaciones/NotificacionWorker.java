package com.pastillerodigital.cuidamedpill.modelo.notificaciones;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoNotificacion;

import java.util.concurrent.TimeUnit;

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
        String idMed = getInputData().getString("idMed");
        String titulo = getInputData().getString("titulo");
        String mensaje = getInputData().getString("mensaje");
        String tipoStr = getInputData().getString("tipoNotificacion");
        String nombreMed = getInputData().getString("nombreMed");
        boolean antipro = getInputData().getBoolean("antiprocrastinador", false);
        long tiempoProgramado = getInputData().getLong("tiempoProgramado", 0);

        if (idMed == null || nombreMed == null) return Result.failure();

        if (tipoStr == null) {
            tipoStr = TipoNotificacion.ESTANDAR.toString(); // valor por defecto
        }

        if (titulo == null || mensaje == null) return Result.failure();

        TipoNotificacion tipo = TipoNotificacion.tipoNotiFromString(tipoStr);

        // Permisos Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return Result.failure();
            }
        }

        NotificationHelper.mostrarNotificacion(getApplicationContext(), titulo, mensaje, tipo, nombreMed, antipro);

        // AUTO-RECARGA
        if (tiempoProgramado > 0) {
            long unaSemanaEnMillis = 7L * 24 * 60 * 60 * 1000;
            long proximaSemana = tiempoProgramado + unaSemanaEnMillis;

            // Reprogramamos usando una versión ligera del manager
            // Creamos un objeto Medicamento "ficticio" con los datos que ya tenemos
            // para reutilizar la lógica de programación.
            reprogramarSiguienteDosis(idMed, nombreMed, tipo, antipro, proximaSemana);
        }

        return Result.success();
    }

    /**
     * Programa la misma dosis para la semana que viene de forma autónoma
     */
    private void reprogramarSiguienteDosis(String id, String nombre, TipoNotificacion tipo, boolean anti, long proximoTiempo) {
        long delay = proximoTiempo - System.currentTimeMillis();
        if (delay <= 0) return;

        Data data = new Data.Builder()
                .putString("idMed", id)
                .putString("nombreMed", nombre)
                .putLong("tiempoProgramado", proximoTiempo)
                .putString("titulo", "Hora de tu medicación")
                .putString("mensaje", "Es momento de tomar: " + nombre)
                .putString("tipoNotificacion", tipo.toString())
                .putBoolean("antiprocrastinador", anti)
                .build();

        androidx.work.OneTimeWorkRequest request = new androidx.work.OneTimeWorkRequest.Builder(NotificacionWorker.class)
                .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(id) // Vital para poder cancelarlo si el usuario borra el medicamento
                .build();

        androidx.work.WorkManager.getInstance(getApplicationContext()).enqueue(request);
    }
}