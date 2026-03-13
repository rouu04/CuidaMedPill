package com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos;

import android.content.Context;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pastillerodigital.cuidamedpill.modelo.dao.AvisoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.ConfNoti;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;

import java.util.List;

public class AvisoManager {

    public static void comprobarAvisosGeneral(Context context, Usuario usuario, List<Medicamento> meds){
        for(Medicamento med : meds){
            comprobarAvisos(context, usuario, med);
        }
    }

    public static void comprobarAvisos(Context context, Usuario usuario, Medicamento med){
        ConfNoti conf = usuario.getConfNoti();
        if(conf == null) return;
        AvisoDAO aDAO = new AvisoDAO(usuario.getId());

        // CADUCIDAD
        if(conf.isAvisoCaducidad() && med.esSemanaACaducado()){
            Aviso aviso = AvisoFactory.crearAvisoCaducidad(med);
            gestionarAviso(context, aDAO, aviso);
        }

        // COMPRA
        if(conf.isAvisoCompra() && med.getnMedRestantes() <= 5){
            Aviso aviso = AvisoFactory.crearAvisoCompra(med);
            gestionarAviso(context, aDAO, aviso);
        }

        // FIN TRATAMIENTO
        if(conf.isAvisoFinTratamiento() && med.esSemanaAFinTratamiento()){
            Aviso aviso = AvisoFactory.crearAvisoFinTratamiento(med);
            aviso.setuDestId(usuario.getId());
            gestionarAviso(context, aDAO, aviso);
        }
    }


    private static void gestionarAviso(Context context, AvisoDAO aDAO, Aviso aviso) {
        aDAO.getAvisoPendiente(aviso, new OnDataLoadedCallback<Aviso>() {
            @Override
            public void onSuccess(Aviso avisoExistente) {
                if (avisoExistente != null) {
                    if (!avisoExistente.isNotiMostrada()) {
                        dispararNotificacion(context, aDAO, avisoExistente);
                    }
                } else {
                    aDAO.add(aviso, new OnOperationCallback() {
                        @Override
                        public void onSuccess() {
                            dispararNotificacion(context, aDAO, aviso);
                        }
                        @Override
                        public void onFailure(Exception e) {}
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    private static void dispararNotificacion(Context context, AvisoDAO aDAO, Aviso aviso) {
        aviso.setNotiMostrada(true);
        aDAO.edit(aviso, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                AvisoNotificacionHelper.mostrarAviso(context, aviso);
            }
            @Override
            public void onFailure(Exception e) {}
        });
    }


    public static void sincronizarAvisos(Context context, String uid) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usuarios")
                .document(uid)
                .collection("avisos")
                .whereEqualTo("notiMostrada", false)
                .get()
                .addOnSuccessListener(query -> {

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        Aviso aviso = doc.toObject(Aviso.class);

                        if(aviso == null) continue;

                        aviso.setId(doc.getId());

                        AvisoNotificacionHelper.mostrarAviso(context, aviso);

                        doc.getReference().update("notiMostrada", true);
                    }

                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

}
