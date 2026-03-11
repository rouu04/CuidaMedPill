package com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoAviso;

import java.util.Arrays;
import java.util.List;

public class AvisoTutorWorker extends Worker {

    public AvisoTutorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String idAsistido = getInputData().getString("idAsistido");
        String nombreMed = getInputData().getString("nombreMed");
        String medId = getInputData().getString("medId");

        String[] tutoresArray = getInputData().getStringArray("tutores");
        if (tutoresArray == null) return Result.failure();
        List<String> tutores = Arrays.asList(tutoresArray);

        for(String tutorId : tutores){
            Aviso aviso = new Aviso(
                    TipoAviso.OLVIDOASISTIDO,
                    "Toma olvidada",
                    "El asistido no registró la medicación " + nombreMed,
                    medId
            );

            aviso.setuDestId(tutorId);
            aviso.setuOrigId(idAsistido);

            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(tutorId)
                    .collection("avisos")
                    .add(aviso);

            AvisoNotificacionHelper.mostrarAviso(getApplicationContext(), aviso);
        }

        return Result.success();
    }
}