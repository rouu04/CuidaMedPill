package com.pastillerodigital.cuidamedpill.modelo.notificaciones;

import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

import com.google.firebase.Timestamp;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoNotificacion;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Ingesta;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Programa y cancela recordatorios usando workmanager
 */
public class RecordatorioManager {

    public static void programarRecordatoriosMedicamento(Context context, Medicamento med) {
        if (med.getHorario() == null) return;
        Calendar hoy = Calendar.getInstance();

        for (int i = 0; i < 7; i++) { // Programamos por ejemplo los próximos 7 días
            List<Timestamp> horas = med.getFechaHorasDia(hoy);
            for (Timestamp ts : horas) {
                long tiempo = ts.toDate().getTime();
                if (tiempo > System.currentTimeMillis()) {
                    programarRecordatorio(context, med, tiempo);
                }
            }
            hoy.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    /**
     * Crea work request
     * @param context
     * @param med
     * @param tiempoNotificacion
     */
    public static void programarRecordatorio(Context context, Medicamento med, long tiempoNotificacion) {
        long delay = tiempoNotificacion - System.currentTimeMillis();
        if (delay <= 0) return;

        TipoNotificacion tipo = TipoNotificacion.ESTANDAR;
        boolean antiproc = true;
        if (med.getConfNoti() != null) {
            tipo = med.getConfNoti().getTipoNoti();
            antiproc = med.getConfNoti().isAntiprocrastinador();
        }

        //Crea datos para worker
        Data data = new Data.Builder()
                .putString("idMed", med.getId())
                .putString("nombreMed", med.getNombreMed())
                .putLong("tiempoProgramado", tiempoNotificacion)
                .putString("titulo", "Hora de tu medicación")  // título por defecto
                .putString("mensaje", "Es momento de tomar: " + med.getNombreMed()) // mensaje por defecto
                .putString("tipoNotificacion", tipo.toString())
                .putBoolean("antiprocrastinador", antiproc)
                .build();

        //Crea tarea
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(NotificacionWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .addTag(med.getId())
                        .build();

        WorkManager.getInstance(context).enqueue(request); //encola tarea
    }

    /**
     * Candela todos los recordatorios asociados a ese medicamento
     * @param context
     * @param med
     */
    public static void cancelarRecordatoriosMedicamento(Context context, Medicamento med) {
        WorkManager.getInstance(context).cancelAllWorkByTag(med.getId());
    }

    /**
     *
     * @param context
     * @param listaMeds lista con medicamentos con config general
     */
    public static void reprogramarMedsGenerales(Context context, List<Medicamento> listaMeds) {
        for (Medicamento med : listaMeds) {
            if (med.getIsNotiGeneral()) {
                // Cancelamos lo viejo y ponemos lo nuevo con la nueva configuración general
                cancelarRecordatoriosMedicamento(context, med);
                programarRecordatoriosMedicamento(context, med);
            }
        }
    }
}
