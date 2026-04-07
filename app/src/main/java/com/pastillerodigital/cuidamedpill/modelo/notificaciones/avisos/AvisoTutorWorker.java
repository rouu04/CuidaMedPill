package com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoAviso;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;

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
        Log.d("AvisoTutorWorker", "En do work");

        String idAsistido = getInputData().getString(Constantes.NOTI_INPUT_ID_ASISTIDO);
        String nombreMed = getInputData().getString(Constantes.NOTI_INPUT_NOMBRE_MED);
        String medId = getInputData().getString(Constantes.NOTI_INPUT_IDMED);
        String uidSelf = getInputData().getString(Constantes.ARG_UIDSELF);
        String aliasU = getInputData().getString(Constantes.USUARIO_ALIAS);
        long tiempoProgramado = getInputData().getLong(Constantes.NOTI_INPUT_TIEMPO_PROGRAMADO, 0);

        String[] tutoresArray = getInputData().getStringArray(Constantes.NOTI_INPUT_TUTORES);
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

            Log.d("AvisoTutorWorker", "En verifica");

            Date inicio = new Date(tiempoProgramado - margen);
            Date fin = new Date(tiempoProgramado + margen);
            /*

            // Consultar si existe registro de ingesta dentro del margen
            QuerySnapshot snapshot = Tasks.await(
                    db.collection(Constantes.COLLECTION_USUARIOS)
                            .document(idAsistido)
                            .collection(Constantes.COLLECTION_MEDICAMENTOS)
                            .document(medId)
                            .collection(Constantes.COLLECTION_INGESTAS)
                            .whereGreaterThan(Constantes.ING_FECHAPROGRAMADA, inicio)
                            .whereLessThan(Constantes.ING_FECHAPROGRAMADA, fin)
                            .get()
            );

            Log.d("AvisoTutorWorker", "Antes empty");
            if (!snapshot.isEmpty()) {
                for (QueryDocumentSnapshot document : snapshot) {
                    Log.d("AvisoTutorWorker", "dentro empty");
                    String estado = document.getString(Constantes.ING_ESTADOINGESTASTR);
                    if (EstadoIngesta.TOMADA.toString().equals(estado)) {
                        return;
                    }
                }
            }

             */

            // Formatear la hora de la toma programada
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String fechaAviso = sdf.format(new Date(tiempoProgramado));

            // Iterar sobre cada tutor
            for (String tutorId : tutores) {
                Log.d("AvisoTutorWorker", "tutor");
                /*
                QuerySnapshot existing = Tasks.await(
                        db.collection(Constantes.COLLECTION_USUARIOS)
                                .document(tutorId)
                                .collection(Constantes.COLLECTION_AVISOS)
                                .whereEqualTo(Constantes.AVISO_MEDID, medId)
                                .whereEqualTo(Constantes.AVISO_UORIGID, idAsistido)
                                .whereEqualTo(Constantes.AVISO_TIPOAVISOSTR, TipoAviso.OLVIDOASISTIDO.toString())
                                .whereEqualTo(Constantes.AVISO_FECHAPROGRAMADA, new Date(tiempoProgramado))
                                .get()
                );

                if (!existing.isEmpty()) {
                    Log.d("AvisoTutorWorker", "Existe para " + tutorId);
                    continue; // ya existe
                }

                 */

                // Crear aviso
                Aviso aviso = new Aviso(
                        TipoAviso.OLVIDOASISTIDO,
                        String.format(Mensajes.AVISO_TITULO_OLVIDO_ASISTIDO, aliasU),
                        String.format(Mensajes.AVISO_MSG_OLVIDO_ASISTIDO, aliasU, nombreMed, fechaAviso),
                        medId,
                        new Timestamp(new Date(tiempoProgramado))
                );

                aviso.setuDestId(tutorId);
                aviso.setuOrigId(idAsistido);

                // Guardar en Firestore
                db.collection(Constantes.COLLECTION_USUARIOS)
                        .document(tutorId)
                        .collection(Constantes.COLLECTION_AVISOS)
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