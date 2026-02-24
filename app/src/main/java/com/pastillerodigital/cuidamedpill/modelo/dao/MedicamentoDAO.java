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
import java.util.Map;

public class MedicamentoDAO extends AbstractDAO<Medicamento> {

    public MedicamentoDAO(String id){
        super();
        this.path = new String[]{Constantes.COLLECTION_USUARIOS, id, Constantes.COLLECTION_MEDICAMENTOS};
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
        getCollection()
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Medicamento med = docToObj(documentSnapshot);

                    callback.onSuccess(med);
                })
                .addOnFailureListener(callback::onFailure);
    }


    public void getListBasic(OnDataLoadedCallback<List<Medicamento>> callback) {
        getCollection()
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


    //-----------ADD
    /*
    @Override
    public void add(Medicamento obj, OnOperationCallback callback) {
        Map<String,Object> mapObj = Medicamento.toMap(obj);

        db.collection(this.collectionName)
                .document(this.idCollection)
                .collection(this.subColName)
                .add(mapObj)
                .addOnSuccessListener(documentReference -> {
                    obj.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }

     */
}
