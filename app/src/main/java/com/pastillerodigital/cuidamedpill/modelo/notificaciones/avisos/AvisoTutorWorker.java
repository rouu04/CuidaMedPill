package com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoAviso;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        String uidSelf = getInputData().getString("uidSelf");
        String aliasU = getInputData().getString("aliasU");
        long tiempoProgramado = getInputData().getLong("tiempoProgramado", 0);

        String[] tutoresArray = getInputData().getStringArray("tutores");
        if (tutoresArray == null) return Result.failure();

        List<String> tutores = Arrays.asList(tutoresArray);

        // Verificar si el asistido no registró la medicación y avisar a los tutores
        verificarTomaNoRegistrada(idAsistido, medId, uidSelf, nombreMed, tiempoProgramado, tutores, aliasU);

        return Result.success();
    }

    private void verificarTomaNoRegistrada(String idAsistido, String medId, String uidSelf, String nombreMed, long tiempoProgramado, List<String> tutores, String aliasU) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Margen de 2 minutos
            long margen = 2 * 60 * 1000; //lo que decide si la toma fue tomada o no
            //verificará si hay alguna ingesta registrada entre esas horas

            // Consultar si existe registro de ingesta dentro del margen
            QuerySnapshot snapshot = Tasks.await(
                    db.collection("usuarios")
                            .document(idAsistido)
                            .collection("medicamentos")
                            .document(medId)
                            .collection("ingestas")
                            .whereGreaterThan("fechaProgramada", new Date(tiempoProgramado - margen))
                            .whereLessThan("fechaProgramada", new Date(tiempoProgramado + margen))
                            .get()
            );

            if (!snapshot.isEmpty()) {
                for (QueryDocumentSnapshot document : snapshot) {
                    String estado = document.getString("estadoIngestaStr");
                    if (EstadoIngesta.TOMADA.toString().equals(estado)) {
                        return;
                    }
                }
            }

            // Formatear la hora de la toma programada
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String fechaAviso = sdf.format(new Date(tiempoProgramado));

            // Iterar sobre cada tutor
            for (String tutorId : tutores) {

                // Crear aviso
                Aviso aviso = new Aviso(TipoAviso.OLVIDOASISTIDO,
                        "Olvido de: " + aliasU,
                        aliasU+ " no registró la medicación " + nombreMed + " (" + fechaAviso + ")",
                        medId
                );

                aviso.setuDestId(tutorId);
                aviso.setuOrigId(idAsistido);

                // Guardar en Firestore
                db.collection("usuarios")
                        .document(tutorId)
                        .collection("avisos")
                        .add(aviso);

                // Mostrar notificación local al tutor (1 notificación por dispositivo)
                if (tutorId.equals(uidSelf)) {
                    AvisoNotificacionHelper.mostrarAviso(getApplicationContext(), aviso);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}