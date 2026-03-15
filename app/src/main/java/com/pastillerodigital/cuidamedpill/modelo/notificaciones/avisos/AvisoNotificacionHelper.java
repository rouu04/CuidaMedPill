package com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.dao.AvisoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.medicacion.NotificationHelper;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class AvisoNotificacionHelper {

    public static final String CHANNEL_AVISOS = "canal_avisos";

    public static void mostrarAviso(Context context, Aviso aviso){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_AVISOS)
                    .setSmallIcon(R.drawable.ic_warning)
                    .setContentTitle(aviso.getTitulo())
                    .setContentText(aviso.getMensaje())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

        // Android 13+ requiere permiso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // No hay permiso: no hacemos nada, o podrías loggear
                return;
            }
        }

        SharedPreferences prefs = context.getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, Context.MODE_PRIVATE);
        String idSelf = prefs.getString(Constantes.PERSIST_KEYUSERSELFID, null);
        String idUsuario = prefs.getString(Constantes.PERSIST_KEYUSERID, idSelf);
        if (idUsuario == null) return;

        AvisoDAO aDAO = new AvisoDAO(idUsuario);
        aviso.setNotiMostrada(true);
        aDAO.edit(aviso, new OnOperationCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(Exception e) {

            }
        });

        Log.d("WORKER_AVISO", "Mostrar noti");

        int notificationId = (aviso.getuDestId() + aviso.getMedId()).hashCode();
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}
