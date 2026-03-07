package com.pastillerodigital.cuidamedpill.controlador.fragments.extra;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoNotificacion;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.ConfNoti;

import java.util.Arrays;

public class NotificacionesFragment extends Fragment {

    private MaterialSwitch switchAvisoCaducidad;
    private MaterialSwitch switchAvisoCompra;
    private MaterialSwitch switchFinTratamiento;
    private MaterialSwitch switchAntiprocrastinador;
    private MaterialSwitch switchAvisoTutores;
    private LinearLayout layoutAvisoTutores;
    private Spinner spTipoNoti;

    private TextView tvTitulo;

    private ConfNoti originalConf; // Para restaurar si se cancela
    private boolean modoEdicion = false, asist = false, isVer = true;

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

        switchAvisoCaducidad = view.findViewById(R.id.switchAvisoCaducidad);
        switchAvisoCompra = view.findViewById(R.id.switchAvisoCompra);
        switchFinTratamiento = view.findViewById(R.id.switchFinTratamiento);
        switchAntiprocrastinador = view.findViewById(R.id.switchAntiprocrastinador);
        tvTitulo = view.findViewById(R.id.tvTituloNotificaciones);
        spTipoNoti = view.findViewById(R.id.spTipoNoti);

        switchAvisoTutores = view.findViewById(R.id.switchAvisoTutores);
        layoutAvisoTutores = view.findViewById(R.id.layoutAvisoTutores);

        setVistasModoEdicion(modoEdicion);
        if(!isVer) tvTitulo.setVisibility(View.GONE);
    }

    // El padre llama a esto para pasarle los datos
    public void cargarDatosEnPantalla(ConfNoti conf) {
        if (getView() == null || getContext() == null) return;

        originalConf = new ConfNoti();
        originalConf.setAvisoCaducidad(conf.isAvisoCaducidad());
        originalConf.setAvisoCompra(conf.isAvisoCompra());
        originalConf.setAvisoFinTratamiento(conf.isAvisoFinTratamiento());
        originalConf.setAntiprocrastinador(conf.isAntiprocrastinador());
        originalConf.setTipoNoti(conf.getTipoNoti());

        switchAvisoCaducidad.setChecked(conf.isAvisoCaducidad());
        switchAvisoCompra.setChecked(conf.isAvisoCompra());
        switchFinTratamiento.setChecked(conf.isAvisoFinTratamiento());
        switchAntiprocrastinador.setChecked(conf.isAntiprocrastinador());

        originalConf.setAvisoTutoresOlvido(conf.isAvisoTutoresOlvido());
        switchAvisoTutores.setChecked(conf.isAvisoTutoresOlvido());

        // Configurar Spinner
        String[] tipos = Arrays.stream(TipoNotificacion.values()).map(Enum::toString).toArray(String[]::new);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, tipos);
        spTipoNoti.setAdapter(adapter);

        int position = Arrays.asList(tipos).indexOf(conf.getTipoNoti().toString());
        if (position >= 0) spTipoNoti.setSelection(position);
    }


    /**
     * Habilita/deshabilita elementos según modo edición
     */
    public void setVistasModoEdicion(boolean editar) {
        //Para que se vea bien aunque esté disabled
        float alpha = editar ? 1.0f : 0.85f; // casi opaco cuando está deshabilitado
        int visText = editar ? View.GONE : View.VISIBLE;

        View[] controles = {switchAvisoCaducidad, switchAvisoCompra,
                switchFinTratamiento, switchAntiprocrastinador, spTipoNoti, switchAvisoTutores};
        for (View v : controles) {
            v.setEnabled(editar);
            v.setAlpha(alpha); // forzamos que se vea más fuerte (aunque esté disabled)
        }
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public void setIsVer(boolean isVer){
        this.isVer = isVer;
    }

    public void setAsistido(boolean asistido) {
        this.asist = asistido;
        layoutAvisoTutores.setVisibility(asistido ? View.VISIBLE : View.GONE);
    }

    public ConfNoti obtenerConfiguracion() {
        ConfNoti conf = new ConfNoti();

        conf.setAvisoCaducidad(switchAvisoCaducidad.isChecked());
        conf.setAvisoCompra(switchAvisoCompra.isChecked());
        conf.setAvisoFinTratamiento(switchFinTratamiento.isChecked());
        conf.setAntiprocrastinador(switchAntiprocrastinador.isChecked());

        String seleccion = spTipoNoti.getSelectedItem().toString();
        conf.setTipoNotiStr(seleccion);
        conf.setTipoNoti(TipoNotificacion.tipoNotiFromString(seleccion));

        if(asist){
            conf.setAvisoTutoresOlvido(switchAvisoTutores.isChecked());
        } else {
            conf.setAvisoTutoresOlvido(false); // por defecto
        }

        return conf;
    }

    /**
     * Será llamado para que al pasar de configuración general a personalizada empiece con estos valores
     * en vez de tenerlas todas desactivadas
     */
    public void cargarConfiguracionPorDefecto() {
        ConfNoti conf = new ConfNoti();
        conf.setAvisoCaducidad(true);
        conf.setAvisoCompra(true);
        conf.setAvisoFinTratamiento(true);
        conf.setAntiprocrastinador(true);
        conf.setTipoNoti(TipoNotificacion.ESTANDAR);

        cargarDatosEnPantalla(conf);
    }


}