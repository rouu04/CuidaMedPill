package com.pastillerodigital.cuidamedpill.utils;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pastillerodigital.cuidamedpill.R;

/**
Clase que incluirá funciones que pueden usar distintas clases relacionadas con la interfaz
Por ejemplo, mostrar un mensaje de error
 */
public class UiUtils {

    /**
     * Representación de las posibles fotos de perfil
     */
    public static final int[] fotosPerfil = {
            R.drawable.usuario_fotoperfil_default,
            R.drawable.usuario_fotoperfil_personaje_verde,
            R.drawable.usuario_fotoperfil_personajeazul,
            R.drawable.usuario_fotoperfil_personajerojo,
            R.drawable.usuario_fotoperfil_siluetamujer,
            R.drawable.usuario_fotoperfil_siluetahombre,
            R.drawable.usuario_fotoperfil_gato,
            R.drawable.usuario_fotoperfil_perro,
            R.drawable.usuario_fotoperfil_flor,
            R.drawable.usuario_fotoperfil_osopeluche
    };

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

    /**
     * Función dinámica que limpia errores que puedan haber en los layouts para que no persistan cuando
     * el usuario los corrige
     * @param group contenedor donde limpiará los textinputlayout
     */
    public static void limpiarErroresLayouts(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof TextInputLayout) {
                ((TextInputLayout) child).setError(null);
            } else if (child instanceof ViewGroup) {
                limpiarErroresLayouts((ViewGroup) child); // recursivo
            }
        }
    }


    /**
     * Configura manualmente el icono del ojo en las contraseñas que permite ocultarla y visualizarla
     * @param layout
     * @param editText
     * @param context
     */
    public static void setupPasswordToggle(@NonNull final TextInputLayout layout, @NonNull final TextInputEditText editText,
                                           @NonNull final android.content.Context context) {
        // Contraseña empieza oculta, ícono ojo cerrado
        editText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
        layout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        layout.setEndIconDrawable(ContextCompat.getDrawable(context, R.drawable.ic_ojo_cerrado));

        layout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getTransformationMethod() instanceof android.text.method.PasswordTransformationMethod) {
                    // Mostrar contraseña
                    editText.setTransformationMethod(android.text.method.HideReturnsTransformationMethod.getInstance());
                    layout.setEndIconDrawable(ContextCompat.getDrawable(context, R.drawable.ic_ojo_abierto));
                } else {
                    // Ocultar contraseña
                    editText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
                    layout.setEndIconDrawable(ContextCompat.getDrawable(context, R.drawable.ic_ojo_cerrado));
                }
                // Mantener el cursor al final
                editText.setSelection(editText.getText().length());
            }
        });
    }



}
