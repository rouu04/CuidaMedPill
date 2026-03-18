package com.pastillerodigital.cuidamedpill.modelo.notificaciones.medicacion;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoNotificacion;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos.Aviso;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos.AvisoNotificacionHelper;
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

        cancelarRecordatoriosMedicamento(context, med);

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

        SharedPreferences prefs = context.getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, Context.MODE_PRIVATE);
        String idSelf = prefs.getString(Constantes.PERSIST_KEYUSERSELFID, null);
        String idUsuario = prefs.getString(Constantes.PERSIST_KEYUSERID, idSelf);
        String modoStr = prefs.getString(Constantes.PERSIST_KEYMODO, null);
        Modo modo = Modo.modoFromString(modoStr);
        if (idUsuario == null || modo == null || idSelf == null) return;

        UsuarioDAO uDAO = new UsuarioDAO();
        uDAO.getBasic(idUsuario, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario user) {
                MedicamentoDAO medDAO = new MedicamentoDAO(idUsuario);
                medDAO.esMedicamentoDeUsuario(med.getId(), new OnDataLoadedCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean esPropio) {
                        if (esPropio) {
                            if(user instanceof UsuarioAsistido){
                                UsuarioAsistido asistido = (UsuarioAsistido) user;
                                if(asistido.getConfNoti().isAvisoTutoresOlvido()){
                                    programarAvisoTutoreSiNoRegistra(context, asistido, idSelf, med, tiempoNotificacion);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                    }
                });


                if(modo != Modo.SUPERVISOR){
                    if (med.getConfNoti() != null && !med.getIsNotiGeneral()) {
                        programarConConfig(context, med, tiempoNotificacion, med.getConfNoti().getTipoNoti(), med.getConfNoti().isAntiprocrastinador(), idUsuario);
                    }
                    else{
                        programarConConfig(context, med, tiempoNotificacion, user.getConfNoti().getTipoNoti(), user.getConfNoti().isAntiprocrastinador(), idUsuario);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) { //notificacion normal
                programarConConfig(context, med, tiempoNotificacion, tipo, antiproc, null);
            }
        });
    }
    private static void programarConConfig(Context context, Medicamento med, long tiempoNotificacion,
                                           TipoNotificacion tipo, boolean antiproc, String uid) {
        long delay = tiempoNotificacion - System.currentTimeMillis();
        if (delay <= 0) return;

        Data data = new Data.Builder()
                .putString("idMed", med.getId())
                .putString("nombreMed", med.getNombreMed())
                .putLong("tiempoProgramado", tiempoNotificacion)
                .putString("titulo", "Hora de tomar: " + med.getNombreMed())
                .putString("mensaje", "Pulse para registrar ingesta")
                .putString("tipoNotificacion", tipo.toString())
                .putBoolean("antiprocrastinador", antiproc)
                .putString("tipoMed", med.getTipoMed().toString())
                .putString("colorSimb", med.getColorSimb())
                .putString("uid", uid)
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

    /**
     *
     * @param context
     * @param asistido
     * @param med
     * @param tiempoNotificacion hora exacta en la que el asistido debe tomarse la medicación
     */
    private static void programarAvisoTutoreSiNoRegistra(Context context, UsuarioAsistido asistido, String uidSelf, Medicamento med, long tiempoNotificacion) {
        if (!asistido.getConfNoti().isAvisoTutoresOlvido()) return;

        List<String> tutores = asistido.getIdUsrTutoresAsig();
        if (tutores == null || tutores.isEmpty()) return;

        long tiempoAviso = tiempoNotificacion + 60 * 1000; //1 mins, cuando disparará el aviso
        long delay = tiempoAviso - System.currentTimeMillis();
        if (delay <= 0) return;


        Data data = new Data.Builder()
                .putString("idAsistido", asistido.getId())
                .putString("nombreMed", med.getNombreMed())
                .putStringArray("tutores", tutores.toArray(new String[0]))
                .putLong("tiempoProgramado", tiempoNotificacion)
                .putString("medId", med.getId())
                .putString("uidSelf", uidSelf)
                .putString("aliasU", asistido.getAliasU())
                .build();

        String tag = "aviso_tutores_" + med.getId() + "_" + tiempoNotificacion;

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(AvisoTutorWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(tag)
                .addTag(med.getId())
                .build();


        WorkManager.getInstance(context).enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request);

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

    /**
     * Cancela todas las notificaciones de un usuario usando los DAO.
     * @param context
     * @param uid id del usuario
     */
    public static void cancelarTodasNotificaciones(Context context, String uid) {
        MedicamentoDAO medDAO = new MedicamentoDAO(uid);

        medDAO.getListBasic(new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> medicamentos) {
                for (Medicamento med : medicamentos) {
                    //notificaciones estándar
                    cancelarRecordatoriosMedicamento(context, med);
                    //avisos a tutores
                    if (med.getWorkTags() != null) {
                        for (String tag : med.getWorkTags()) {
                            WorkManager.getInstance(context).cancelAllWorkByTag(tag);
                        }
                        med.getWorkTags().clear();
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

}
