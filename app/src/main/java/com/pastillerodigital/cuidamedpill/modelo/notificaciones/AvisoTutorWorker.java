package com.pastillerodigital.cuidamedpill.modelo.notificaciones;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoNotificacion;

import java.util.Arrays;
import java.util.List;

public class AvisoTutorWorker extends Worker {

    public AvisoTutorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        // Obtenemos datos del asistido y medicamento
        String idAsistido = getInputData().getString("idAsistido");
        String nombreMed = getInputData().getString("nombreMed");
        String[] tutoresArray = getInputData().getStringArray("tutores");
        if (tutoresArray == null || tutoresArray.length == 0) return Result.failure();
        List<String> tutores = Arrays.asList(tutoresArray);

        if (idAsistido == null || nombreMed == null || tutores == null || tutores.isEmpty()) {
            return Result.failure();
        }

        // Por cada tutor, mostrar notificación
        for (String tutorId : tutores) {
            // Aquí puedes crear una lógica para obtener el Usuario del tutor
            // o simplemente enviar la notificación genérica
            NotificationHelper.mostrarNotificacion(
                    getApplicationContext(),
                    "Medicación no registrada",
                    "El asistido " + idAsistido + " no registró la medicación " + nombreMed + " en 1,5h",
                    TipoNotificacion.ESTANDAR,
                    nombreMed,
                    false, // no antiprocrastinador
                    tutorId // id para distinguir la notificación
            );
        }

        return Result.success();
    }
}