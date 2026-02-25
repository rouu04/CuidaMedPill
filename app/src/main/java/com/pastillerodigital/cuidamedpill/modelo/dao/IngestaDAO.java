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

    public void getListBasicUltimosDosDias(OnDataLoadedCallback<List<Ingesta>> callback) {

        Calendar inicio = Calendar.getInstance();
        inicio.set(Calendar.HOUR_OF_DAY, 0);
        inicio.set(Calendar.MINUTE, 0);
        inicio.set(Calendar.SECOND, 0);
        inicio.set(Calendar.MILLISECOND, 0);
        inicio.add(Calendar.DAY_OF_MONTH, -1); // ayer 00:00

        Calendar fin = (Calendar) inicio.clone();
        fin.add(Calendar.DAY_OF_MONTH, 2); // mañana 00:00

        Timestamp tsInicio = new Timestamp(inicio.getTime());
        Timestamp tsFin = new Timestamp(fin.getTime());

        getCollection()
                .whereGreaterThanOrEqualTo(Constantes.ING_FECHAPROGRAMADA, tsInicio)
                .whereLessThan(Constantes.ING_FECHAPROGRAMADA, tsFin)
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
