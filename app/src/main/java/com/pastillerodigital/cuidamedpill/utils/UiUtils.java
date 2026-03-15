package com.pastillerodigital.cuidamedpill.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;

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
                .setPositiveButton(Mensajes.BASIC_ACEPTAR, (dialog, which) -> reiniciarActivity(activity))
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

    public static void mostrarConfirmacion(@NonNull Activity activity, @NonNull String mensaje) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(Mensajes.BASIC_CONFIRMACION)
                .setMessage(mensaje)
                .setPositiveButton(Mensajes.BASIC_ACEPTAR, null)
                .show();
    }

    public static void mostrarNegConfirmacion(@NonNull Activity activity, @NonNull String mensaje) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(Mensajes.BASIC_NEG_CONFIRMACION)
                .setMessage(mensaje)
                .setPositiveButton(Mensajes.BASIC_ACEPTAR, null)
                .show();
    }

    public static void setDrawableTipoMed(@NonNull android.content.Context context, @NonNull android.widget.ImageView imgTipo,
                                          @NonNull TipoMed tipoMed, @NonNull String colorSimb) {

        // Obtener drawable del tipo de medicamento
        Drawable drawable = ContextCompat.getDrawable(context, tipoMed.getDrawableRes());
        // Obtener color según el color simbólico
        int colorResId = context.getResources().getIdentifier(colorSimb, Constantes.COLOR, context.getPackageName());
        int color = ContextCompat.getColor(context, colorResId);

        if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            Drawable capaColor = layerDrawable.findDrawableByLayerId(tipoMed.getDrawableResColoreable());
            if (capaColor != null) {
                capaColor.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
            imgTipo.setImageDrawable(layerDrawable);
        } else {
            drawable = drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            imgTipo.setImageDrawable(drawable);
        }
    }

    public static void setMedicamentoIcon(Context context, ImageView imageView, TipoMed tipoMed, String colorSimb) {
        if(tipoMed == null) tipoMed = TipoMed.CAPSULA;
        Drawable drawable = ContextCompat.getDrawable(context, tipoMed.getDrawableRes());
        if(drawable == null) return;

        if(colorSimb != null){
            int resColor = context.getResources().getIdentifier(colorSimb, Constantes.COLOR, context.getPackageName());
            int color = ContextCompat.getColor(context, resColor);

            if (drawable instanceof LayerDrawable) {
                LayerDrawable layerDrawable = (LayerDrawable) drawable;
                Drawable capaColor = layerDrawable.findDrawableByLayerId(tipoMed.getDrawableResColoreable());
                if (capaColor != null) capaColor.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                imageView.setImageDrawable(layerDrawable);
            } else {
                drawable = drawable.mutate();
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                imageView.setImageDrawable(drawable);
            }
        } else imageView.setImageDrawable(drawable);
    }

    /**
     * Obtiene bitmap del medicamento, sirve para mostrar el icono en las notificaciones
     * @param context
     * @param tipoMedStr
     * @param colorSimb
     * @return
     */
    public static Bitmap getMedicamentoBitmap(Context context, String tipoMedStr, String colorSimb) {
        TipoMed tipoMed;
        try {
            tipoMed = TipoMed.tipoMedFromString(tipoMedStr);
        } catch (Exception e) {
            tipoMed = TipoMed.CAPSULA;
        }

        // 1. Cargar el drawable y mutarlo inmediatamente
        Drawable drawable = ContextCompat.getDrawable(context, tipoMed.getDrawableRes());
        if (drawable == null) return null;
        drawable = drawable.mutate();

        // 2. Obtener el color
        int color;
        if (colorSimb != null && !colorSimb.isEmpty()) {
            int resColor = context.getResources().getIdentifier(colorSimb, "color", context.getPackageName());
            color = (resColor != 0) ? ContextCompat.getColor(context, resColor) : ContextCompat.getColor(context, R.color.md_primary);
        } else {
            color = ContextCompat.getColor(context, R.color.md_primary);
        }

        // 3. Aplicar el tinte según el tipo de drawable
        if (drawable instanceof LayerDrawable) {
            LayerDrawable ld = (LayerDrawable) drawable;
            Drawable capaColoreable = ld.findDrawableByLayerId(tipoMed.getDrawableResColoreable());

            if (capaColoreable != null) {
                // Envolvemos la capa para aplicar el tinte de forma compatible
                Drawable wrapped = androidx.core.graphics.drawable.DrawableCompat.wrap(capaColoreable.mutate());
                androidx.core.graphics.drawable.DrawableCompat.setTint(wrapped, color);
                androidx.core.graphics.drawable.DrawableCompat.setTintMode(wrapped, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                // Si no encuentra la capa, teñimos tod*o el conjunto por seguridad
                androidx.core.graphics.drawable.DrawableCompat.setTint(drawable, color);
            }
        } else {
            // Para drawables simples (como GOTAS que no es un list)
            androidx.core.graphics.drawable.DrawableCompat.setTint(drawable, color);
        }

        // 4. "Baking": Dibujar el resultado en un Bitmap físico
        int size = (int) (48 * context.getResources().getDisplayMetrics().density);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }



}
