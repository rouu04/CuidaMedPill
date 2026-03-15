package com.pastillerodigital.cuidamedpill.modelo.notificaciones;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.medicacion.RecordatorioManager;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        // Leer sesión guardada
        SharedPreferences prefs = context.getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, Context.MODE_PRIVATE);
        boolean sesionActiva = prefs.getBoolean(Constantes.PERSIST_KEYSESIONACTIVA, false);
        if (!sesionActiva) return; // No hay sesión, no hacemos nada

        String uidSelf = prefs.getString(Constantes.PERSIST_KEYUSERSELFID, null);
        if (uidSelf == null) return;
        MedicamentoDAO medDAO = new MedicamentoDAO(uidSelf);

        // Cargar medicamentos básicos (no hace falta ingestas)
        medDAO.getListBasic(new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> medicamentos) {
                for (Medicamento med : medicamentos) {
                    if (med.getHorario() != null) {
                        RecordatorioManager.programarRecordatoriosMedicamento(context, med);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }
}