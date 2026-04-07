package com.pastillerodigital.cuidamedpill.modelo.notificaciones.medicacion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoNotificacion;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class NotificacionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String idMed = intent.getStringExtra(Constantes.NOTI_INPUT_IDMED);
        String titulo = intent.getStringExtra(Constantes.NOTI_INPUT_TITULO);
        String mensaje = intent.getStringExtra(Constantes.NOTI_INPUT_MENSAJE);
        String tipoStr = intent.getStringExtra(Constantes.NOTI_INPUT_TIPO_NOTIFICACION);
        String tipoMedStr = intent.getStringExtra(Constantes.NOTI_INPUT_TIPO_MED);
        String colorSimb = intent.getStringExtra(Constantes.NOTI_INPUT_COLOR_SIMB);
        boolean antipro = intent.getBooleanExtra(Constantes.ARG_ANTIPROCRASTINADOR, false);

        TipoNotificacion tipo = TipoNotificacion.tipoNotiFromString(tipoStr);

        NotificationHelper.mostrarNotificacion(
                context,
                titulo,
                mensaje,
                tipo,
                antipro,
                idMed,
                tipoMedStr,
                colorSimb
        );
    }
}
