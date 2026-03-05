package com.pastillerodigital.cuidamedpill.controlador.fragments.extra;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoNotificacion;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.ConfNoti;

import java.util.Arrays;

public class NotificacionesFragment extends Fragment {

    private SwitchMaterial switchAvisoCaducidad;
    private SwitchMaterial switchAvisoCompra;
    private SwitchMaterial switchFinTratamiento;
    private SwitchMaterial switchAntiprocrastinador;

    private Spinner spTipoNoti;
    private MaterialButton btnEditarNotis;
    private MaterialButton btnGuardarNotis;
    private MaterialButton btnCancelarNotis;

    private ConfNoti originalConf; // Para restaurar si se cancela

    // Definimos una interfaz para avisar al Perfil cuando hay que guardar
    public interface OnNotificacionesListener {
        void onGuardarConfiguracion(ConfNoti nuevaConf);
    }

    private OnNotificacionesListener listener;

    // El padre llama a esto para pasarle los datos
    public void cargarDatosEnPantalla(ConfNoti conf) {
        if (getView() == null || getContext() == null) return;

        switchAvisoCaducidad.setChecked(conf.isAvisoCaducidad());
        switchAvisoCompra.setChecked(conf.isAvisoCompra());
        switchFinTratamiento.setChecked(conf.isAvisoFinTratamiento());
        switchAntiprocrastinador.setChecked(conf.isAntiprocrastinador());

        // Configurar Spinner
        String[] tipos = Arrays.stream(TipoNotificacion.values()).map(Enum::toString).toArray(String[]::new);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, tipos);
        spTipoNoti.setAdapter(adapter);

        int position = Arrays.asList(tipos).indexOf(conf.getTipoNoti().toString());
        if (position >= 0) spTipoNoti.setSelection(position);
    }

    public NotificacionesFragment() {
        // Constructor vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notificaciones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializamos
        switchAvisoCaducidad = view.findViewById(R.id.switchAvisoCaducidad);
        switchAvisoCompra = view.findViewById(R.id.switchAvisoCompra);
        switchFinTratamiento = view.findViewById(R.id.switchFinTratamiento);
        switchAntiprocrastinador = view.findViewById(R.id.switchAntiprocrastinador);
        spTipoNoti = view.findViewById(R.id.spTipoNoti);

        btnEditarNotis = view.findViewById(R.id.btnEditarNotis);
        btnGuardarNotis = view.findViewById(R.id.btnGuardarNotis);
        btnCancelarNotis = view.findViewById(R.id.btnCancelarNotis);

        setModoEdicion(false); //tod*o quitado menos el botón de editar

        // Botones
        btnEditarNotis.setOnClickListener(v -> setModoEdicion(true));
        btnCancelarNotis.setOnClickListener(v -> setModoEdicion(false));
        btnGuardarNotis.setOnClickListener(v -> {
            if (listener != null) {
                ConfNoti nuevaConf = new ConfNoti();
                nuevaConf.setAvisoCaducidad(switchAvisoCaducidad.isChecked());
                nuevaConf.setAvisoCompra(switchAvisoCompra.isChecked());
                nuevaConf.setAvisoFinTratamiento(switchFinTratamiento.isChecked());
                nuevaConf.setAntiprocrastinador(switchAntiprocrastinador.isChecked());

                // Obtener valor del spinner
                String seleccion = spTipoNoti.getSelectedItem().toString();
                nuevaConf.setTipoNoti(TipoNotificacion.tipoNotiFromString(seleccion));

                listener.onGuardarConfiguracion(nuevaConf);
            }
            setModoEdicion(false);
        });

        btnCancelarNotis.setOnClickListener(v -> {
            if (originalConf != null) {
                cargarDatosEnPantalla(originalConf); // Revertimos a los datos originales
            }
            setModoEdicion(false);
        });
    }

    /**
     * Habilita/deshabilita elementos según modo edición
     */
    public void setModoEdicion(boolean editar) {
        //Para que se vea bien aunque esté disabled
        float alpha = editar ? 1.0f : 0.85f; // casi opaco cuando está deshabilitado
        View[] controles = {switchAvisoCaducidad, switchAvisoCompra,
                switchFinTratamiento, switchAntiprocrastinador, spTipoNoti};
        for (View v : controles) {
            v.setEnabled(editar);
            v.setAlpha(alpha); // forzamos que se vea más fuerte (aunque esté disabled)
        }

        switchAvisoCaducidad.setEnabled(editar);
        switchAvisoCompra.setEnabled(editar);
        switchFinTratamiento.setEnabled(editar);
        switchAntiprocrastinador.setEnabled(editar);
        spTipoNoti.setEnabled(editar);

        btnEditarNotis.setVisibility(editar ? View.GONE : View.VISIBLE);
        btnGuardarNotis.setVisibility(editar ? View.VISIBLE : View.GONE);
        btnCancelarNotis.setVisibility(editar ? View.VISIBLE : View.GONE);
    }

    public void setListener(OnNotificacionesListener listener) {
        this.listener = listener;
    }

}