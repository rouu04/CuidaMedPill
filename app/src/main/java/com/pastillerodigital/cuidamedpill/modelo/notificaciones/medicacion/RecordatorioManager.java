package com.pastillerodigital.cuidamedpill.modelo.notificaciones.medicacion;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoNotificacion;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos.AvisoTutorWorker;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioAsistido;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

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
        TipoNotificacion tipo = TipoNotificacion.ESTANDAR;
        boolean antiproc = true;


        if (med.getConfNoti() != null) {
            programarConConfig(context, med, tiempoNotificacion, med.getConfNoti().getTipoNoti(), med.getConfNoti().isAntiprocrastinador());
        } else if (med.getIsNotiGeneral()) {

            SharedPreferences prefs = context.getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, Context.MODE_PRIVATE);
            String idSelf = prefs.getString(Constantes.PERSIST_KEYUSERSELFID, null);
            String idUsuario = prefs.getString(Constantes.PERSIST_KEYUSERID, idSelf);
            if (idUsuario == null) return;

            UsuarioDAO uDAO = new UsuarioDAO();
            uDAO.getBasic(idUsuario, new OnDataLoadedCallback<Usuario>() {
                @Override
                public void onSuccess(Usuario user) {
                    if(user instanceof UsuarioAsistido){
                        UsuarioAsistido asistido = (UsuarioAsistido) user;
                        if(asistido.getConfNoti().isAvisoTutoresOlvido()){
                            programarAvisoTutoreSiNoRegistra(context, asistido, med, tiempoNotificacion);
                        }
                    }
                    // luego programar la notificación normal
                    programarConConfig(context, med, tiempoNotificacion, user.getConfNoti().getTipoNoti(), user.getConfNoti().isAntiprocrastinador());
                }

                @Override
                public void onFailure(Exception e) {
                    // fallback a notificación normal
                    programarConConfig(context, med, tiempoNotificacion, tipo, antiproc);
                }
            });
        } else {
            programarConConfig(context, med, tiempoNotificacion, tipo, antiproc);
        }
    }
    private static void programarConConfig(Context context, Medicamento med, long tiempoNotificacion,
                                           TipoNotificacion tipo, boolean antiproc) {
        long delay = tiempoNotificacion - System.currentTimeMillis();
        if (delay <= 0) return;

        Data data = new Data.Builder()
                .putString("idMed", med.getId())
                .putString("nombreMed", med.getNombreMed())
                .putLong("tiempoProgramado", tiempoNotificacion)
                .putString("titulo", "Hora de tu medicación")
                .putString("mensaje", "Es momento de tomar: " + med.getNombreMed())
                .putString("tipoNotificacion", tipo.toString())
                .putBoolean("antiprocrastinador", antiproc)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(NotificacionWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(med.getId())
                .build();

        WorkManager.getInstance(context).enqueue(request);
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

    private static void programarAvisoTutoreSiNoRegistra(Context context, UsuarioAsistido asistido, Medicamento med, long tiempoNotificacion) {
        if (!asistido.getConfNoti().isAvisoTutoresOlvido()) return;

        List<String> tutores = asistido.getIdUsrTutoresAsig();
        if (tutores == null || tutores.isEmpty()) return;

        long delay = tiempoNotificacion + 90 * 60 * 1000 - System.currentTimeMillis(); // 1.5h
        if (delay <= 0) return;

        Data data = new Data.Builder()
                .putString("idAsistido", asistido.getId())
                .putString("nombreMed", med.getNombreMed())
                .putStringArray("tutores", tutores.toArray(new String[0]))
                .build();

        String tag = "aviso_tutores_" + med.getId() + "_" + asistido.getId();

        androidx.work.OneTimeWorkRequest request = new androidx.work.OneTimeWorkRequest.Builder(AvisoTutorWorker.class)
                .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(tag)
                .build();

        androidx.work.WorkManager.getInstance(context).enqueue(request);

        // Guardar el tag para poder cancelarlo más tarde si se registra la ingesta
        med.addWorkTag(tag); // suponiendo que agregamos un ArrayList<String> en Medicamento para estos tags
    }

    public static void sincronizarRecordatorios(Context context, String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios")
                .document(uid)
                .collection("medicamentos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Medicamento med = Medicamento.doctoObj(doc);
                        if (med == null) continue;
                        med.setId(doc.getId());
                        // cancelar por si existía
                        cancelarRecordatoriosMedicamento(context, med);
                        // volver a programar
                        programarRecordatoriosMedicamento(context, med);
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

}
