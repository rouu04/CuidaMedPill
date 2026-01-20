package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pastillerodigital.cuidamedpill.modelo.Persistible;

/*
Clase abstracta que implementa el dao y hará las funciones generales
 */
public abstract class AbstractDAO<T extends Persistible> implements DAO<T>{

    protected final FirebaseFirestore db;
    protected String collectionName;

    public AbstractDAO() {
        this.db = FirebaseFirestore.getInstance();
    }

    protected abstract void setCollectionName();

    public abstract void get(String id, OnDataLoadedCallback<T> callback);
    public abstract T docToObj(DocumentSnapshot doc);


    @Override
    public void add(T obj, OnOperationCallback callback) {
        //todo comprobar si existe
        db.collection(this.collectionName)
                .add(obj)
                .addOnSuccessListener(documentReference -> {
                    obj.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }

    /*
    Usamos set, que reemplaza el documento con el id por el nuevo que le pasamos
     */
    @Override
    public void edit(T nuevo, OnOperationCallback callback) {
        String id = nuevo.getId();
        if(id == null || id.isEmpty()){
            callback.onFailure(new Exception("ID nulo o inválido"));
            return;
        }

        db.collection(collectionName)
            .document(id)
            .set(nuevo)
            .addOnSuccessListener(v -> callback.onSuccess())
            .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void delete(String id, OnOperationCallback callback) {
        if (id == null || id.isEmpty()) {
            callback.onFailure(new Exception("ID nulo o inválido"));
            return;
        }

        db.collection(collectionName)
                .document(id)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

}
