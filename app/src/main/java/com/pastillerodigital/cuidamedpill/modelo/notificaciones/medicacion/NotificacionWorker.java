package com.pastillerodigital.cuidamedpill.modelo.notificaciones.medicacion;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoNotificacion;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;

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
        String idMed = getInputData().getString(Constantes.NOTI_INPUT_IDMED);
        String titulo = getInputData().getString(Constantes.NOTI_INPUT_TITULO);
        String mensaje = getInputData().getString(Constantes.NOTI_INPUT_MENSAJE);
        String tipoStr = getInputData().getString(Constantes.NOTI_INPUT_TIPO_NOTIFICACION);
        String nombreMed = getInputData().getString(Constantes.NOTI_INPUT_NOMBRE_MED);
        String uid = getInputData().getString(Constantes.NOTI_INPUT_UID);
        String tipoMedStr = getInputData().getString(Constantes.NOTI_INPUT_TIPO_MED);
        String colorSimb = getInputData().getString(Constantes.NOTI_INPUT_COLOR_SIMB);
        boolean antipro = getInputData().getBoolean(Constantes.ARG_ANTIPROCRASTINADOR, false);
        long tiempoProgramado = getInputData().getLong(Constantes.NOTI_INPUT_TIEMPO_PROGRAMADO, 0);

        if (idMed == null || nombreMed == null || uid == null) return Result.failure();

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

        NotificationHelper.mostrarNotificacion(getApplicationContext(), titulo, mensaje, tipo, antipro,idMed, tipoMedStr, colorSimb);

        // AUTO-RECARGA
        if (tiempoProgramado > 0) {
            long unaSemanaEnMillis = Constantes.UNA_SEMANA_MILLS;
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
                .putString(Constantes.NOTI_INPUT_IDMED, id)
                .putString(Constantes.NOTI_INPUT_NOMBRE_MED, nombre)
                .putLong(Constantes.NOTI_INPUT_TIEMPO_PROGRAMADO, proximoTiempo)
                .putString(Constantes.NOTI_INPUT_TITULO, Mensajes.NOTI_HORAMED)
                .putString(Constantes.NOTI_INPUT_MENSAJE, String.format(Mensajes.NOTI_TOMARMED, nombre))
                .putString(Constantes.NOTI_INPUT_TIPO_NOTIFICACION, tipo.toString())
                .putBoolean(Constantes.ARG_ANTIPROCRASTINADOR, anti)
                .build();

        androidx.work.OneTimeWorkRequest request = new androidx.work.OneTimeWorkRequest.Builder(NotificacionWorker.class)
                .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(id) // para poder cancelarlo si el usuario borra el medicamento
                .build();

        androidx.work.WorkManager.getInstance(getApplicationContext()).enqueue(request);
    }
}