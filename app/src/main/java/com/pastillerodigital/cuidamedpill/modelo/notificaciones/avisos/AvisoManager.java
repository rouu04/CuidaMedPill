package com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos;

import android.content.Context;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pastillerodigital.cuidamedpill.modelo.dao.AvisoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.ConfNoti;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;

public class AvisoManager {

    public static void comprobarYMostrarAvisos(Context context, Usuario usuario, Medicamento med) {
        ConfNoti conf = usuario.getConfNoti();
        if(conf == null) return;
        AvisoDAO aDAO = new AvisoDAO(usuario.getId());

        // CADUCIDAD
        if(conf.isAvisoCaducidad() && med.esSemanaACaducado()){
            Aviso aviso = AvisoFactory.crearAvisoCaducidad(med);
            aDAO.add(aviso, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    AvisoNotificacionHelper.mostrarAviso(context, aviso);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }

        // COMPRA
        //todo quitar hardcoded
        if(conf.isAvisoCompra() && med.getnMedRestantes() <= 5){
            Aviso aviso = AvisoFactory.crearAvisoCompra(med);
            aDAO.add(aviso, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    AvisoNotificacionHelper.mostrarAviso(context, aviso);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }

        // FIN TRATAMIENTO
        if(conf.isAvisoFinTratamiento() && med.esSemanaAFinTratamiento()){
            Aviso aviso = AvisoFactory.crearAvisoFinTratamiento(med);
            aviso.setuDestId(usuario.getId());

            aDAO.add(aviso, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    AvisoNotificacionHelper.mostrarAviso(context, aviso);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
            AvisoNotificacionHelper.mostrarAviso(context, aviso);
        }
    }

    public static void comprobarAvisos(Context context, Usuario usuario, Medicamento med) {
        ConfNoti conf = usuario.getConfNoti();
        if(conf == null) return;
        AvisoDAO aDAO = new AvisoDAO(usuario.getId());

        // CADUCIDAD
        if(conf.isAvisoCaducidad() && med.esSemanaACaducado()){
            Aviso aviso = AvisoFactory.crearAvisoCaducidad(med);
            aDAO.add(aviso, new OnOperationCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }

        // COMPRA
        //todo quitar hardcoded
        if(conf.isAvisoCompra() && med.getnMedRestantes() <= 5){
            Aviso aviso = AvisoFactory.crearAvisoCompra(med);
            aDAO.add(aviso, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }

        // FIN TRATAMIENTO
        if(conf.isAvisoFinTratamiento() && med.esSemanaAFinTratamiento()){
            Aviso aviso = AvisoFactory.crearAvisoFinTratamiento(med);
            aviso.setuDestId(usuario.getId());

            aDAO.add(aviso, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }
    }



    public static void sincronizarAvisos(Context context, String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios")
                .document(uid)
                .collection("avisos")
                .whereEqualTo("notificado", false)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Aviso aviso = doc.toObject(Aviso.class);
                        if(aviso == null) continue;
                        aviso.setId(doc.getId());
                        // Mostrar notificación
                        AvisoNotificacionHelper.mostrarAviso(context, aviso);
                        // Marcar como notificado
                        doc.getReference().update("notificado", true);
                    }

                })
                .addOnFailureListener(Throwable::printStackTrace);
    }
}
