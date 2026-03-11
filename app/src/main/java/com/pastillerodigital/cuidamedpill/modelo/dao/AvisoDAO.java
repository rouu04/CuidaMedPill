package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos.Aviso;
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

    /**
     * Crea o actualiza un aviso evitando duplicados para el mismo medicamento y tipo.
     * Si existe uno no leído se actualiza, si no se crea uno nuevo.
     */
    public void gestionarAvisoExistente(Aviso aviso, OnOperationCallback callback){

        getCollection()
                .whereEqualTo(Constantes.AVISO_TIPOAVISOSTR, aviso.getTipoAvisoStr())
                .whereEqualTo(Constantes.AVISO_MEDID, aviso.getMedId())
                .get()
                .addOnSuccessListener(query -> {
                    boolean actualizado = false;

                    for(DocumentSnapshot doc : query.getDocuments()){
                        Boolean leido = doc.getBoolean(Constantes.AVISO_LEIDO);
                        if(leido != null && !leido){  // actualizar existente
                            getCollection()
                                    .document(doc.getId())
                                    .set(aviso)
                                    .addOnSuccessListener(v -> callback.onSuccess())
                                    .addOnFailureListener(callback::onFailure);

                            actualizado = true;
                            break;
                        }
                    }

                    if(!actualizado){
                        add(aviso, callback); // crear nuevo aviso
                    }

                })
                .addOnFailureListener(callback::onFailure);
    }
}
