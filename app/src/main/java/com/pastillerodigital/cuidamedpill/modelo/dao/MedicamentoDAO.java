package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.firebase.firestore.DocumentSnapshot;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class MedicamentoDAO extends AbstractDAOSubcol<Medicamento> {

    public MedicamentoDAO(String id){
        super();
        setSubColName();
        setIdCollection(id);
        setCollectionName();
    }
    @Override
    protected void setSubColName() {
        this.subColName = Constantes.COLLECTION_MEDICAMENTOS;
    }

    @Override
    protected void setIdCollection(String id) {
        this.idCollection = id;
    }

    @Override
    protected void setCollectionName() {
        this.collectionName = Constantes.COLLECTION_USUARIOS;
    }

    @Override
    public Medicamento docToObj(DocumentSnapshot doc) {
        //todo
        return null;
    }

    @Override
    public void get(String id, OnDataLoadedCallback<Medicamento> callback) {

    }
}
