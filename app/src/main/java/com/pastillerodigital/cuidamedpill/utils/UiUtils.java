package com.pastillerodigital.cuidamedpill.utils;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
Clase que incluirá funciones que pueden usar distintas clases relacionadas con la interfaz
Por ejemplo, mostrar un mensaje de error
 */
public class UiUtils {

    /**
     * Muestra un error genérico y reinicia la Activity actual
     */
    public static void mostrarErrorYReiniciar(@NonNull Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(Mensajes.ERROR_HAYERROR)
                .setMessage(Mensajes.ERROR_REINTENTAR)
                .setCancelable(false)
                .setPositiveButton(Mensajes.ACEPTAR, (dialog, which) -> reiniciarActivity(activity))
                .show();
    }

    private static void reiniciarActivity(Activity activity) {
        Intent intent = activity.getIntent();
        activity.finish();
        activity.startActivity(intent);
    }
}
