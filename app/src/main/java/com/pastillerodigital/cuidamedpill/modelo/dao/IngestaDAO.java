package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Ingesta;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class IngestaDAO extends AbstractDAO<Ingesta>{

    public IngestaDAO(String idUsr, String idMed){
        super();
        this.path = new String[]{Constantes.COLLECTION_USUARIOS, idUsr, Constantes.COLLECTION_MEDICAMENTOS,
                idMed, Constantes.COLLECTION_INGESTAS};
    }


    @Override
    public Ingesta docToObj(DocumentSnapshot doc) {return Ingesta.doctoObj(doc);}

    @Override
    public void getBasic(String id, OnDataLoadedCallback<Ingesta> callback) {
        getCollection()
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Ingesta ingesta = docToObj(documentSnapshot);

                    callback.onSuccess(ingesta);
                })
                .addOnFailureListener(callback::onFailure);
    }



    @Override
    public void get(String id, OnDataLoadedCallback<Ingesta> callback) {

    }

    public void getListBasic(OnDataLoadedCallback<List<Ingesta>> callback) {
        getCollection()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Ingesta> listaIng = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Ingesta ing = docToObj(doc);
                        if (ing != null) {
                            listaIng.add(ing);
                        }
                    }
                    callback.onSuccess(listaIng);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
