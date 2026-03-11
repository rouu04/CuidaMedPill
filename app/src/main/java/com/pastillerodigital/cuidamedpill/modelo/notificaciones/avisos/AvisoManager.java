package com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos;

import android.content.Context;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pastillerodigital.cuidamedpill.modelo.dao.AvisoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.ConfNoti;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;

import java.util.List;

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
        }
    }

    public static void comprobarAvisos(Context context, Usuario usuario, Medicamento med) {
        ConfNoti conf = usuario.getConfNoti();
        if(conf == null) return;
        AvisoDAO aDAO = new AvisoDAO(usuario.getId());

        // CADUCIDAD
        if(conf.isAvisoCaducidad() && med.esSemanaACaducado()){
            Aviso aviso = AvisoFactory.crearAvisoCaducidad(med);
            aDAO.gestionarAvisoExistente(aviso, new OnOperationCallback() {
                @Override
                public void onSuccess() {}

                @Override
                public void onFailure(Exception e) {}
            });
        }

        // COMPRA
        if(conf.isAvisoCompra() && med.getnMedRestantes() <= 5){
            Aviso aviso = AvisoFactory.crearAvisoCompra(med);
            aDAO.gestionarAvisoExistente(aviso, new OnOperationCallback() {
                @Override
                public void onSuccess() {}

                @Override
                public void onFailure(Exception e) {}
            });
        }

        // FIN TRATAMIENTO
        if(conf.isAvisoFinTratamiento() && med.esSemanaAFinTratamiento()){
            Aviso aviso = AvisoFactory.crearAvisoFinTratamiento(med);
            aDAO.gestionarAvisoExistente(aviso, new OnOperationCallback() {
                @Override
                public void onSuccess() {}

                @Override
                public void onFailure(Exception e) {}
            });
        }
    }

    public static void comprobarYMostrarAvisosEdicion(Context context, Usuario usuario, Medicamento med){
        ConfNoti conf = usuario.getConfNoti();
        if(conf == null) return;

        AvisoDAO aDAO = new AvisoDAO(usuario.getId());

        // CADUCIDAD
        if(conf.isAvisoCaducidad() && med.esSemanaACaducado()){
            Aviso avisoNuevo = AvisoFactory.crearAvisoCaducidad(med);
            aDAO.gestionarAvisoExistente(avisoNuevo, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    AvisoNotificacionHelper.mostrarAviso(context, avisoNuevo);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });

        }

        // COMPRA
        if(conf.isAvisoCompra() && med.getnMedRestantes() <= 5){
            Aviso avisoNuevo = AvisoFactory.crearAvisoCompra(med);
            aDAO.gestionarAvisoExistente(avisoNuevo, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    AvisoNotificacionHelper.mostrarAviso(context, avisoNuevo);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }

        // FIN TRATAMIENTO
        if(conf.isAvisoFinTratamiento() && med.esSemanaAFinTratamiento()){
            Aviso avisoNuevo = AvisoFactory.crearAvisoFinTratamiento(med);
            avisoNuevo.setuDestId(usuario.getId());
            aDAO.gestionarAvisoExistente(avisoNuevo, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    AvisoNotificacionHelper.mostrarAviso(context, avisoNuevo);
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

    public static void comprobarAvisosInicio(Context context, Usuario usuario, List<Medicamento> meds){
        for(Medicamento med : meds){
            comprobarYMostrarAvisosEdicion(context, usuario, med);
        }
    }
}
