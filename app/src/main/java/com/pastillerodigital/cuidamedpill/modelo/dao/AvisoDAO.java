package com.pastillerodigital.cuidamedpill.modelo.dao;

import android.content.Context;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos.Aviso;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos.AvisoNotificacionHelper;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AvisoDAO extends AbstractDAO<Aviso>{

    private final String uid; // Usuario al que pertenecen los avisos

    public AvisoDAO(String userId) {
        super();
        this.uid = userId;
        this.path = new String[]{Constantes.COLLECTION_USUARIOS, uid, Constantes.COLLECTION_AVISOS};
    }

    @Override
    public Aviso docToObj(DocumentSnapshot doc) { return Aviso.doctoObj(doc);}

    @Override
    public void get(String id, OnDataLoadedCallback<Aviso> callback) {
        getCollection()
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    Aviso aviso = docToObj(doc);
                    callback.onSuccess(aviso);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getBasic(String id, OnDataLoadedCallback<Aviso> callback) {
        getCollection()
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    Aviso aviso = docToObj(doc);
                    callback.onSuccess(aviso);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Devuelve todos los avisos del usuario
     */
    public void getAll(OnDataLoadedCallback<List<Aviso>> callback) {
        getCollection()
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Aviso> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Aviso aviso = docToObj(doc);
                        if (aviso != null) lista.add(aviso);
                    }
                    callback.onSuccess(lista);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Devuelve solo los avisos no leídos
     */
    public void getNoLeidos(OnDataLoadedCallback<List<Aviso>> callback) {
        getCollection()
                .whereEqualTo(Constantes.AVISO_LEIDO, false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Aviso> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Aviso aviso = docToObj(doc);
                        if (aviso != null) lista.add(aviso);
                    }
                    lista.sort(Comparator.comparing(Aviso::getFechaCreacion));
                    callback.onSuccess(lista);
                })
                .addOnFailureListener(callback::onFailure);
    }


    public void getAvisoPendiente(Aviso aviso, OnDataLoadedCallback<Aviso> callback){
        getCollection()
                .whereEqualTo("tipoAvisoStr", aviso.getTipoAvisoStr())
                .whereEqualTo("medId", aviso.getMedId())
                .whereEqualTo("leido", false)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        callback.onSuccess(null); // no hay aviso pendiente
                        return;
                    }
                    DocumentSnapshot doc = query.getDocuments().get(0);
                    Aviso avisoBD = doc.toObject(Aviso.class);
                    if (avisoBD != null) {
                        avisoBD.setId(doc.getId());
                    }
                    callback.onSuccess(avisoBD);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public ListenerRegistration listenNoLeidos(OnDataLoadedCallback<List<Aviso>> callback) {
        return getCollection()
                .whereEqualTo(Constantes.AVISO_LEIDO, false)

                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        callback.onFailure(e);
                        return;
                    }

                    List<Aviso> lista = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Aviso aviso = docToObj(doc);
                            if (aviso != null) {
                                lista.add(aviso);
                            }
                        }
                    }

                    callback.onSuccess(lista);
                });
    }

    public void eliminarAvisosMedicamento(String medId){
        db.collection("usuarios")
                .document(uid)
                .collection("avisos")
                .whereEqualTo("medId", medId)
                .get()
                .addOnSuccessListener(query -> {
                    for(DocumentSnapshot doc : query){
                        doc.getReference().delete();
                    }
                });
    }
}
