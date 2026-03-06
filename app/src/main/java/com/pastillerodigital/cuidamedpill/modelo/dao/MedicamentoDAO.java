package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.firebase.firestore.DocumentSnapshot;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Ingesta;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

import java.util.ArrayList;
import java.util.List;

public class MedicamentoDAO extends AbstractDAO<Medicamento> {

    private String uid;
    private UsuarioDAO uDAO;

    public MedicamentoDAO(String id){
        super();
        this.uid = id;
        this.path = new String[]{Constantes.COLLECTION_USUARIOS, id, Constantes.COLLECTION_MEDICAMENTOS};
        this.uDAO = new UsuarioDAO();
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

                    if(med.getIsNotiGeneral()){ //obtengo la configuracion del usuario
                        uDAO.getBasic(uid, new OnDataLoadedCallback<Usuario>() {
                            @Override
                            public void onSuccess(Usuario data) {
                                med.setConfNoti(data.getConfNoti());
                                callback.onSuccess(med);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(e);
                            }
                        });
                    }

                    callback.onSuccess(med);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getListBasic(OnDataLoadedCallback<List<Medicamento>> callback) {
        uDAO.getBasic(uid, new OnDataLoadedCallback<Usuario>() { //para obtener su configuracion
            @Override
            public void onSuccess(Usuario user) {
                getCollection().get().addOnSuccessListener(querySnapshot -> { //get medicamento
                    List<Medicamento> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Medicamento med = docToObj(doc);
                        if (med != null) {
                            if (med.getIsNotiGeneral()) { //si es general obtenemos la del usuario
                                med.setConfNoti(user.getConfNoti());
                            }
                            lista.add(med);
                        }
                    }
                    callback.onSuccess(lista);
                }).addOnFailureListener(callback::onFailure);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void getListConIngestas(OnDataLoadedCallback<List<Medicamento>> callback){
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
                    ingDAO.getListBasic(new OnDataLoadedCallback<List<Ingesta>>() {
                        @Override
                        public void onSuccess(List<Ingesta> ingestas) {
                            med.setlIngestas(ingestas); // asignamos ingestas al medicamento
                            for (Ingesta ing : ingestas) {
                                ing.setMed(med); //para que ingesta también tenga el medicamento por simplicidad
                            }

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

}
