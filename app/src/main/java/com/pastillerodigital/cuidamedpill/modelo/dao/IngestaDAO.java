package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Ingesta;

public class IngestaDAO extends AbstractDAO<Ingesta>{



    @Override
    public void getBasic(String id, OnDataLoadedCallback<Ingesta> callback) {

    }

    @Override
    public Ingesta docToObj(DocumentSnapshot doc) {
        return null;
    }

    @Override
    public void get(String id, OnDataLoadedCallback<Ingesta> callback) {

    }
}
