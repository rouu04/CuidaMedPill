package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos.Aviso;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

import java.util.ArrayList;
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
                .whereEqualTo("leido", false)
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
     * Marca un aviso como leído
     */
    public void marcarComoLeido(String avisoId, OnOperationCallback callback) {
        getCollection()
                .document(avisoId)
                .update("leido", true)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}
