package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioEstandar;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;

import java.util.ArrayList;
import java.util.List;

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
        return Medicamento.doctoObj(doc);
    }

    @Override
    public void get(String id, OnDataLoadedCallback<Medicamento> callback) {

    }

    @Override
    public void getBasic(String id, OnDataLoadedCallback<Medicamento> callback) {
        db.collection(this.collectionName)
                .document(this.idCollection)
                .collection(this.subColName)
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Medicamento med = docToObj(documentSnapshot);

                    callback.onSuccess(med);
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {callback.onFailure(e);}
                });
    }

    @Override
    public void getList(String idCollection, OnDataLoadedCallback<List<Medicamento>> callback) {

    }

    @Override
    public void getListBasic(String idCollection, OnDataLoadedCallback<List<Medicamento>> callback) {
        db.collection(this.collectionName)
                .document(idCollection)
                .collection(this.subColName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Medicamento> listaMedicamentos = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Medicamento med = docToObj(doc);
                        if (med != null) {
                            listaMedicamentos.add(med);
                        }
                    }
                    callback.onSuccess(listaMedicamentos);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getBasicWithParameter(String paramBD, Object param, OnDataLoadedCallback<Medicamento> callback){
        if (param == null) {
            callback.onSuccess(null);
            return;
        }

        db.collection(collectionName)
                .document(idCollection)
                .collection(this.subColName)
                .whereEqualTo(paramBD, param)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0); //primer y único documento de la consulta
                        callback.onSuccess(Medicamento.doctoObj(doc));
                    } else {
                        callback.onSuccess(null); // Tiene éxito en la consulta (no da error)
                        //pero no existe
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onFailure(new Exception(Mensajes.EX_EXISTE));
                });
    }
}
