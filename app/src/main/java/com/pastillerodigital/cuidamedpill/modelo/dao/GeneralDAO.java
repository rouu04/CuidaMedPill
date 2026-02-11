package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pastillerodigital.cuidamedpill.modelo.Persistible;

public abstract class GeneralDAO<T extends Persistible> {
    protected final FirebaseFirestore db;
    protected String collectionName;

    public GeneralDAO() {
        this.db = FirebaseFirestore.getInstance();
    }

    protected abstract void setCollectionName();
    public abstract T docToObj(DocumentSnapshot doc);
    public abstract void get(String id, OnDataLoadedCallback<T> callback);
}
