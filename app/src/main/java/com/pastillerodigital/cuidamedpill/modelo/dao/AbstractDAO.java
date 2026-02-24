package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pastillerodigital.cuidamedpill.modelo.Persistible;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

/**
Clase abstracta que implementa el dao y hará las funciones generales
 */
public abstract class AbstractDAO<T extends Persistible> implements DAO<T>{

    protected final FirebaseFirestore db;
    protected String[] path;
    public AbstractDAO() {
        this.db = FirebaseFirestore.getInstance();
    }

    protected CollectionReference getCollection() {
        if (path == null || path.length == 0) {
            throw new IllegalArgumentException("Path vacío");
        }

        CollectionReference ref = db.collection(path[0]);
        for (int i = 1; i < path.length; i += 2) {
            ref = ref
                    .document(path[i])
                    .collection(path[i + 1]);
        }

        return ref;
    }

    public abstract T docToObj(DocumentSnapshot doc);
    public abstract void get(String id, OnDataLoadedCallback<T> callback);

    /**
    Función que añade un elemento a la coleccion. Cada objeto debería preguntar si existe en función
    de sus restricciones.
     */
    @Override
    public void add(T obj, OnOperationCallback callback) {
        getCollection()
                .add(obj)
                .addOnSuccessListener(documentReference -> {
                    obj.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
    Usamos set, que reemplaza el documento con el id por el nuevo que le pasamos
     */
    @Override
    public void edit(T nuevo, OnOperationCallback callback) {
        String id = nuevo.getId();
        if(id == null || id.isEmpty()){
            callback.onFailure(new Exception(Constantes.EX_ID_INVALIDO));
            return;
        }

        getCollection()
            .document(id)
            .set(nuevo)
            .addOnSuccessListener(v -> callback.onSuccess())
            .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void delete(String id, OnOperationCallback callback) {
        if (id == null || id.isEmpty()) {
            callback.onFailure(new Exception(Constantes.EX_ID_INVALIDO));
            return;
        }

        getCollection()
                .document(id)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

}
