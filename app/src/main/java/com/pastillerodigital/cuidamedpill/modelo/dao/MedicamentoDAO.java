package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Ingesta;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioEstandar;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MedicamentoDAO extends AbstractDAO<Medicamento> {

    private String uid;

    public MedicamentoDAO(String id){
        super();
        this.uid = id;
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

    public void getConIngestasHome(OnDataLoadedCallback<List<Medicamento>> callback){
        getListBasic(new OnDataLoadedCallback<List<Medicamento>>() {// Primero get todos los medicamentos
            @Override
            public void onSuccess(List<Medicamento> medicamentos) {
                if (medicamentos.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                List<Medicamento> resultado = new ArrayList<>();
                int[] cont = {0}; // contador de callbacks completados

                for (Medicamento med : medicamentos) {
                    if (med.getHorario() == null) {
                        // No tiene horario, asignamos lista vacía y seguimos
                        med.setlIngestas(new ArrayList<>());
                        resultado.add(med);
                        cont[0]++;
                        if (cont[0] == medicamentos.size()) {
                            callback.onSuccess(resultado);
                        }
                        continue;
                    }

                    IngestaDAO ingDAO = new IngestaDAO(uid, med.getId());
                    ingDAO.getListBasicUltimosDosDias(new OnDataLoadedCallback<List<Ingesta>>() {
                        @Override
                        public void onSuccess(List<Ingesta> ingestas) {
                            med.setlIngestas(ingestas); // asignamos ingestas al medicamento
                            resultado.add(med);
                            cont[0]++;
                            if (cont[0] == medicamentos.size()) { // Todos los medicamentos ya tienen sus ingestas
                                callback.onSuccess(resultado);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
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
