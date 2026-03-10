package com.pastillerodigital.cuidamedpill.modelo.medicamento;

import android.content.Context;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.medicacion.RecordatorioManager;

public class MedicamentoRealTimeManager {
    private ListenerRegistration listener;

    public void iniciarListener(Context context, String uid) {
        //todo cambiar y ponerlo sin cosas hardocded
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        listener = db.collection("usuarios")
                .document(uid)
                .collection("medicamentos")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        Medicamento med = Medicamento.doctoObj(dc.getDocument());
                        med.setId(dc.getDocument().getId());

                        switch (dc.getType()) {
                            case ADDED:
                                RecordatorioManager.programarRecordatoriosMedicamento(context, med);
                                break;
                            case MODIFIED:
                                RecordatorioManager.cancelarRecordatoriosMedicamento(context, med);
                                RecordatorioManager.programarRecordatoriosMedicamento(context, med);
                                break;
                            case REMOVED:
                                RecordatorioManager.cancelarRecordatoriosMedicamento(context, med);
                                break;
                        }
                    }
                });
    }

    public void detenerListener() {
        if (listener != null) {
            listener.remove();
        }
    }
}
