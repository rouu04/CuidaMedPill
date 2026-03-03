package com.pastillerodigital.cuidamedpill.modelo.notificaciones;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //todo volver a cargar medicamentos
            // y reprogramar alarmas
        }
    }
}