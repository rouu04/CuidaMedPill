package com.pastillerodigital.cuidamedpill.controlador.fragments.extra;

import android.app.DatePickerDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.Timestamp;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.dao.AvisoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.IngestaDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EMomentoDia;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoIntervalo;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Ingesta;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Hora;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.HoraMomentoDia;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Horario;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.ConfNoti;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos.AvisoManager;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.medicacion.RecordatorioManager;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;
import com.pastillerodigital.cuidamedpill.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AddAndEditMedicamentoFragment extends Fragment {

    private TextInputLayout layoutNombre, layoutIntervaloNum, layoutFechaCad, layoutFechaFin, layoutNMedRestantes, layoutNotasMed, layoutSigToma;
    private TextInputEditText edtNombre, edtTipoIntervalo, edtIntervaloNum, edtFechaCad, edtFechaFin, edtNMedRestantes, edtNotasMed, edtSigToma;
    private ImageView imgMedicamento;
    private MaterialButton btnGuardar, btnAgregarHora;
    private View viewColor, progressMedEdit;
    private List<Hora> listaHoras = new ArrayList<>();
    private ChipGroup layoutHorasContainer; //layout profesional de las horas
    private SwitchMaterial switchHorario, switchNotiGeneral; //toggle
    private LinearLayout layoutHorarioContainer, layoutFormMedEditAdd;
    private MaterialCardView cardTipoMed;
    private MaterialToolbar toolbarSup;
    private TextView tvNotiGeneralInfo;
    private NotificacionesFragment notisFragment;
    private boolean isNotiGeneral = true;

    //Elementos lógicos
    private MedicamentoDAO medDAO;
    private UsuarioDAO uDAO;
    private Medicamento medEdit;
    private boolean isEdit, horarioActivo;
    private String uid, uidSelf, medId;
    private Modo modo;
    private int selectedColorRes = R.color.md_primary;
    private TipoMed selectedTipo = TipoMed.CAPSULA;
    private int intervalo = 1;
    private TipoIntervalo tipoIntervaloSel;
    private Usuario usr;

    private final int[] coloresDisponibles = {
            R.color.md_primary,
            R.color.md_rojo,
            R.color.md_naranja,
            R.color.md_amarillo,
            R.color.md_verde,
            R.color.md_azul,
            R.color.md_azul_oscuro,
            R.color.md_violeta,
            R.color.md_black,
            R.color.md_blanco
    };

    public static AddAndEditMedicamentoFragment newInstance(String medId, String uid, String uidSelf, Modo modo){
        AddAndEditMedicamentoFragment fragment = new AddAndEditMedicamentoFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_MEDID, medId);
        args.putString(Constantes.ARG_UID, uid);
        args.putString(Constantes.ARG_UIDSELF, uidSelf);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public static AddAndEditMedicamentoFragment newInstance(String uid, String uidSelf, Modo modo){
        AddAndEditMedicamentoFragment fragment = new AddAndEditMedicamentoFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UID, uid);
        args.putString(Constantes.ARG_UIDSELF, uidSelf);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_add_edit_medicamento, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        // Elementos del layout
        progressMedEdit = view.findViewById(R.id.progressMedEdit);
        layoutFormMedEditAdd = view.findViewById(R.id.layoutFormMedEditAdd);
        toolbarSup = view.findViewById(R.id.topAppBar);

        imgMedicamento = view.findViewById(R.id.imgMedicamento);
        layoutNombre = view.findViewById(R.id.layoutNombreMed);
        edtNombre = view.findViewById(R.id.edtNombreMed);
        cardTipoMed = view.findViewById(R.id.cardTipoMed);

        //Horario:
        edtTipoIntervalo = view.findViewById(R.id.edtTipoIntervalo);
        layoutIntervaloNum = view.findViewById(R.id.layoutIntervaloNum);
        edtIntervaloNum = view.findViewById(R.id.edtIntervaloNum);
        layoutSigToma = view.findViewById(R.id.layoutSigToma);
        edtSigToma = view.findViewById(R.id.edtSigToma);
        layoutHorasContainer = view.findViewById(R.id.layoutHorasContainer);
        switchHorario = view.findViewById(R.id.switchHorario);
        layoutHorarioContainer = view.findViewById(R.id.layoutHorarioContainer);

        //detalles:
        layoutFechaCad = view.findViewById(R.id.layoutFechaCad);
        edtFechaCad = view.findViewById(R.id.edtFechaCad);
        layoutFechaFin = view.findViewById(R.id.layoutFechaFin);
        edtFechaFin = view.findViewById(R.id.edtFechaFin);
        layoutNMedRestantes = view.findViewById(R.id.layoutNMedRestantes);
        edtNMedRestantes = view.findViewById(R.id.edtNMedRestantes);
        layoutNotasMed = view.findViewById(R.id.layoutNotasMed);
        edtNotasMed = view.findViewById(R.id.edtNotasMed);

        //Notificaciones
        switchNotiGeneral = view.findViewById(R.id.switchNotiGeneral);
        tvNotiGeneralInfo = view.findViewById(R.id.tvNotiGeneralInfo);

        viewColor = view.findViewById(R.id.viewColor);

        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnAgregarHora = view.findViewById(R.id.btnAgregarHora);

        //Lógica:
        mostrarCarga();
        leerArgumentosYConsec();
        setButtonListeners();
    }

    /**
     * Lee los argumentos y en consecuencia define atributos y carga datos si es necesario.
     */
    private void leerArgumentosYConsec(){
        if(getArguments() != null){
            uidSelf = getArguments().getString(Constantes.ARG_UIDSELF);
            uid = getArguments().getString(Constantes.ARG_UID);
            modo = Modo.modoFromString(getArguments().getString(Constantes.ARG_MODO));

            if(uid == null) uid = uidSelf;
            medDAO = new MedicamentoDAO(uid);
            uDAO = new UsuarioDAO();
            medId = getArguments().getString(Constantes.ARG_MEDID);

            //Modo edicion o añadir medicamento
            if(medId != null){ //edición
                toolbarSup.setTitle(R.string.text_title_edit_med);
                isEdit = true;
                if(modo != Modo.SUPERVISOR) toolbarSup.setTitle(R.string.text_title_edit_med);
                cargarMed(medId);
            }
            else{
                selectedColorRes = R.color.md_primary;
                tipoIntervaloSel = TipoIntervalo.DIARIO;
                Calendar hoy = Calendar.getInstance();
                edtSigToma.setText(Utils.calendarToString(hoy));
                selectedTipo = TipoMed.CAPSULA;
                actualizarImagenTipo(TipoMed.CAPSULA);
                if(modo == Modo.SUPERVISOR) toolbarSup.setTitle(R.string.text_title_add_med);
                ocultarCarga();
            }

            cargarUsr();
        }
    }

    private void setButtonListeners(){
        cardTipoMed.setOnClickListener(v -> mostrarSelectorTipo());

        edtTipoIntervalo.setOnClickListener(v -> mostrarSelectorIntervalo(intervalo));
        edtFechaCad.setOnClickListener(v -> mostrarDatePicker(edtFechaCad));
        edtFechaFin.setOnClickListener(v -> mostrarDatePicker(edtFechaFin));
        edtSigToma.setOnClickListener(v -> mostrarDatePicker(edtSigToma));
        btnGuardar.setOnClickListener(v -> guardarOEditarMedicamento());
        btnAgregarHora.setOnClickListener(v -> mostrarMenuSeleccionHora());
        viewColor.setOnClickListener(v -> mostrarSelectorColor());

        switchHorario.setChecked(false);
        horarioActivo = false;
        switchHorario.setOnCheckedChangeListener((buttonView, isChecked) -> {
            horarioActivo = isChecked;

            if(isChecked){ //si está activo
                layoutHorarioContainer.setVisibility(View.VISIBLE);
                // Valores por defecto
                tipoIntervaloSel = TipoIntervalo.DIARIO;
                edtTipoIntervalo.setText(Constantes.INTERVALO_DIA);
                edtIntervaloNum.setText(String.valueOf(1));
                Calendar hoy = Calendar.getInstance();
                edtSigToma.setText(Utils.calendarToString(hoy));

            } else {
                layoutHorarioContainer.setVisibility(View.GONE);
                // Se limpian los datos del horario
                listaHoras.clear();
                layoutHorasContainer.removeAllViews();
                edtIntervaloNum.setText("");
                edtTipoIntervalo.setText("");
            }
        });

        // Acción botón atrás
        toolbarSup.setNavigationOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );

        //Notificaciones
        switchNotiGeneral.setChecked(true); // o false según el estado inicial
        actualizarVistaNotificaciones(switchNotiGeneral.isChecked());

        switchNotiGeneral.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isNotiGeneral = isChecked;
            actualizarVistaNotificaciones(isChecked);
        });

        layoutFechaCad.setEndIconOnClickListener(v -> {
            edtFechaCad.setText("");
        });

        layoutFechaFin.setEndIconOnClickListener(v -> {
            edtFechaFin.setText("");
        });

    }

    private void mostrarCarga(){
        progressMedEdit.setVisibility(View.VISIBLE);
        layoutFormMedEditAdd.setVisibility(View.GONE);
    }

    private void ocultarCarga(){
        progressMedEdit.setVisibility(View.GONE);
        layoutFormMedEditAdd.setVisibility(View.VISIBLE);
    }

    private void cargarMed(String medId){
        medDAO.getBasic(medId, new OnDataLoadedCallback<Medicamento>() {
            @Override
            public void onSuccess(Medicamento med) {
                if (med == null) {
                    UiUtils.mostrarErrorYReiniciar(requireActivity());
                    return;
                }
                medEdit = med;
                fillViewMed(med);
                ocultarCarga();
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void cargarUsr(){
        uDAO.getBasic(uid, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                if(modo == Modo.SUPERVISOR){
                    if(medId == null) toolbarSup.setTitle(String.format(Mensajes.MED_ADD_SUPERV, data.getAliasU()));
                    else toolbarSup.setTitle(String.format(Mensajes.MED_EDIT_SUPERV, data.getAliasU()));
                }
                usr = data;
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    /**
     * Abre un date picker para los atributos con fecha, para que no tenga que ser introducido manualmente
     * por el usuario y así evitar errores, además de añadir un aspecto profesional a la interfaz
     */
    private void mostrarDatePicker(TextInputEditText edtFecha){
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    edtFecha.setText(Utils.calendarToString(calendar));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void mostrarSelectorIntervalo(int intervalo){
        String[] tipos = TipoIntervalo.getAllTiposOutput(intervalo);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(Mensajes.MED_EDITADD_SEL_INTERVALO)
                .setItems(tipos, (dialog, which) -> {
                    edtTipoIntervalo.setText(tipos[which]);
                    tipoIntervaloSel = TipoIntervalo.fromUnidad(tipos[which]);
                })
                .show();
    }

    private void mostrarSelectorTipo(){
        String[] tipos = TipoMed.getAllTipos();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(Mensajes.MED_EDITADD_SEL_TIPO)
                .setItems(tipos, (dialog, which) -> {
                    actualizarImagenTipo(TipoMed.tipoMedFromString(tipos[which]));
                })
                .show();
    }

    private void mostrarMenuSeleccionHora() {

        String[] opciones = {
                Mensajes.MED_EDITADD_SEL_HORA_ESPEC,
                Mensajes.MED_EDITADD_SEL_HORA_MOMENT
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(Mensajes.MED_EDITADD_ADD_HORA)
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) mostrarSelectorHora();
                    else mostrarSelectorMomentos();
                })
                .show();
    }


    private void mostrarSelectorHora() {

        MaterialTimePicker picker =
                new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(8)
                        .setMinute(0)
                        .setTitleText(Mensajes.MED_EDITADD_SEL_HORA_TITLE)
                        .setPositiveButtonText(Mensajes.BASIC_ACEPTAR)
                        .setNegativeButtonText(Mensajes.BASIC_CANCELAR)
                        .build();

        picker.addOnPositiveButtonClickListener(view -> {
            int hour = picker.getHour();
            int minute = picker.getMinute();
            Hora nuevaHora = new Hora(hour, minute);
            if(!existeHora(nuevaHora)){
                listaHoras.add(nuevaHora);
                ordenarYRepintarHoras();
            }
        });

        picker.show(getParentFragmentManager(), Constantes.PICKER_TIME);
    }


    private void mostrarSelectorMomentos() {

        EMomentoDia[] momentos = EMomentoDia.values();
        String[] textos = new String[momentos.length];

        for(int i = 0; i < momentos.length; i++){
            textos[i] = momentos[i].toString() + " (" +
                    String.format(Locale.getDefault(),
                            "%02d:%02d",
                            momentos[i].getHoraDefault(),
                            momentos[i].getMinDefault()) + ")";
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(Mensajes.MED_EDITADD_SEL_HORA_MOMENT_TITLE)
                .setItems(textos, (dialog, which) -> {

                    EMomentoDia momento = momentos[which];
                    HoraMomentoDia nuevaHoraMom = new HoraMomentoDia(momento.toString());
                    if(!existeHora(nuevaHoraMom)){
                        listaHoras.add(nuevaHoraMom);
                        ordenarYRepintarHoras();
                    }

                })
                .show();
    }


    private void guardarOEditarMedicamento(){
        UiUtils.limpiarErroresLayouts((ViewGroup) getView().findViewById(R.id.layoutFormMedEditAdd));

        String nombre = edtNombre.getText().toString().trim().toUpperCase();
        String intervaloNumStr = edtIntervaloNum.getText().toString().trim();
        String fechaCadStr = edtFechaCad.getText().toString().trim();
        String fechaFinStr = edtFechaFin.getText().toString().trim();
        String nMedRestantesStr = edtNMedRestantes.getText().toString().trim();
        String colorString = getResources().getResourceEntryName(selectedColorRes);
        String notasMed = edtNotasMed.getText().toString().trim();
        String sigTomaStr = edtSigToma.getText().toString().trim();

        if(!validaciones(nombre)) return;
        if(!validacionesOpcionales(fechaCadStr, fechaFinStr, nMedRestantesStr)) return;

        int nCajas = nMedRestantesStr.isEmpty() ? -1 : Integer.parseInt(nMedRestantesStr);
        int intervaloNum = intervaloNumStr.isEmpty() ? -1 : Integer.parseInt(intervaloNumStr); //-1 por simplicidad
        Timestamp fechaCad = fechaCadStr.isEmpty() ? null : Utils.stringToTimestamp(fechaCadStr);
        Timestamp fechaFin = fechaFinStr.isEmpty() ? null : Utils.stringToTimestamp(fechaFinStr);
        Timestamp sigToma = sigTomaStr.isEmpty() ? null : Utils.stringToTimestamp(sigTomaStr);


        Horario horario = null;
        if(horarioActivo){
            if(!validacionesHorario(intervaloNumStr, sigTomaStr)) return;
            sigToma = aplicarPrimeraHora(sigToma, listaHoras); //guarda la hora de la sig toma
            horario = new Horario(tipoIntervaloSel.toString(), intervaloNum, listaHoras, sigToma);
        }

        Timestamp fechaInicio;
        if(medEdit != null && medEdit.getFechaInicio() != null) fechaInicio = medEdit.getFechaInicio();
        else fechaInicio = sigToma;

        //Notificaciones
        ConfNoti confNoti = null;
        if(!isNotiGeneral && notisFragment != null){
            confNoti = notisFragment.obtenerConfiguracion();
        }

        Medicamento medActual = new Medicamento(colorString, selectedTipo.toString(), fechaCad , nombre,
                fechaFin, fechaInicio, nCajas, horario, null, notasMed, isNotiGeneral, confNoti);
        if(isEdit){
            medActual.setId(medEdit.getId());
        }

        //No pueden haber dos medicamentos con el mismo nombre para el mismo usuario:
        medDAO.getListBasic(new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> data) {
                for(Medicamento med: data){
                    if(!med.getId().equals(medActual.getId()) && med.getNombreMed().equals(nombre)) {
                        layoutNombre.setError(Mensajes.MED_EDITADD_VAL_NOMBRE);
                        return;
                    }
                }

                if(isEdit){
                    //si han quitado el horario:
                    if(medEdit.getHorario() != null && medActual.getHorario() == null){
                        RecordatorioManager.cancelarRecordatoriosMedicamento(requireContext(), medEdit);
                    }

                    editMedicamento(medActual);
                }
                else addMedicamento(medActual);
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }


    private boolean validaciones(String nombre){
        boolean valid = true;

        if(nombre.isEmpty()){
            layoutNombre.setError(Mensajes.MED_EDITADD_ERR_NOMBRE);
            valid = false;
        }
        return valid;
    }

    private boolean validacionesOpcionales(String fechaCadStr, String fechaFinStr, String nMedRestantesStr){
        boolean valid = true;

        if(!fechaFinStr.isEmpty()){
            try{
                Timestamp fechaFin = Utils.stringToTimestamp(fechaFinStr);

                if(fechaFin == null){
                    layoutFechaFin.setError(Mensajes.MED_EDITADD_ERR_FECHA_INVALIDA);
                    valid = false;
                } else {
                    Timestamp hoy = Timestamp.now();

                    if(fechaFin.toDate().before(hoy.toDate())){
                        layoutFechaFin.setError(Mensajes.MED_EDITADD_ERR_FECHA_ANTIGUALHOY);
                        valid = false;
                    }
                }
            }catch (Exception e){
                layoutFechaFin.setError(Mensajes.MED_EDITADD_ERR_FECHA_MALFORMATO);
                valid = false;
            }
        }

        if(!fechaCadStr.isEmpty()){
            try{
                Timestamp fechaCad = Utils.stringToTimestamp(fechaCadStr);

                if(fechaCad == null){
                    layoutFechaCad.setError(Mensajes.MED_EDITADD_ERR_FECHA_INVALIDA);
                    valid = false;
                } else {
                    Timestamp hoy = Timestamp.now();

                    if(fechaCad.toDate().before(hoy.toDate())){
                        layoutFechaCad.setError(Mensajes.MED_EDITADD_ERR_FECHA_ANTIGUALHOY);
                        valid = false;
                    }
                }
            }catch (Exception e){
                layoutFechaCad.setError(Mensajes.MED_EDITADD_ERR_FECHA_MALFORMATO);
                valid = false;
            }
        }

        if(!nMedRestantesStr.isEmpty()){
            try{
                int nCajas = Integer.parseInt(nMedRestantesStr);
                if(nCajas < 0){
                    layoutNMedRestantes.setError(Mensajes.MED_EDITADD_ERR_NUM_MAYORO0);
                    valid = false;
                }
            }catch (NumberFormatException e){
                layoutNMedRestantes.setError(Mensajes.MED_EDITADD_ERR_NUM_INVALIDO);
                valid = false;
            }
        }


        return valid;
    }

    private boolean validacionesHorario(String intervaloNumStr, String sigTomaStr){
        boolean valid = true;

        if(!intervaloNumStr.isEmpty()){
            try{
                int intervaloNum = Integer.parseInt(intervaloNumStr);
                if(intervaloNum <= 0){
                    layoutIntervaloNum.setError(Mensajes.MED_EDITADD_ERR_NUM_MAYOR0);
                    valid = false;
                }
            }catch (NumberFormatException e){
                layoutIntervaloNum.setError(Mensajes.MED_EDITADD_ERR_NUM_INVALIDO);
                valid = false;
            }
        }
        else{
            layoutIntervaloNum.setError(Mensajes.MED_EDITADD_ERR_HORARIO_INTERVALO);
            valid = false;
        }

        if(!sigTomaStr.isEmpty()){
            try{
                Timestamp sigToma = Utils.stringToTimestamp(sigTomaStr);

                if(sigToma == null){
                    layoutSigToma.setError(Mensajes.MED_EDITADD_ERR_FECHA_INVALIDA);
                    valid = false;
                } else if(esFechaAnteriorAHoy(sigToma)){
                    layoutSigToma.setError(Mensajes.MED_EDITADD_ERR_FECHA_ANTHOY);
                    valid = false;
                }
            }catch (Exception e){
                layoutSigToma.setError(Mensajes.MED_EDITADD_ERR_FECHA_MALFORMATO);
                valid = false;
            }
        }

        if(horarioActivo && listaHoras.isEmpty()){
            UiUtils.mostrarNegConfirmacion(requireActivity(), Mensajes.MED_EDITADD_ERR_HORARIO_HORA);
            return false;
        }

        return valid;
    }

    private void fillViewMed(Medicamento med){
        edtNombre.setText(med.getNombreMed());

        // Color
        String colorString = med.getColorSimb();
        int resColor = getResources().getIdentifier(colorString, Constantes.COLOR, requireContext().getPackageName());
        if (resColor != 0) {
            selectedColorRes = resColor;
            viewColor.getBackground().setTint(getResources().getColor(selectedColorRes));
        } else {
            selectedColorRes = R.color.md_primary;
        }

        if (med.getTipoMed() != null) {
            selectedTipo = med.getTipoMed();
            actualizarImagenTipo(selectedTipo);
        } else selectedTipo = TipoMed.CAPSULA;

        // Fechas
        if (med.getFechaCad() != null) edtFechaCad.setText(Utils.timestampToString(med.getFechaCad()));
        if (med.getFechaFin() != null) edtFechaFin.setText(Utils.timestampToString(med.getFechaFin()));

        if (med.getnMedRestantes() >= 0) edtNMedRestantes.setText(String.valueOf(med.getnMedRestantes()));
        else {
            edtNMedRestantes.setText("");
        }

        // Notas
        if (med.getNotasMed() != null) edtNotasMed.setText(med.getNotasMed());

        // Horario
        if (med.getHorario() != null) {
            horarioActivo = true;
            switchHorario.setChecked(true);
            layoutHorarioContainer.setVisibility(View.VISIBLE);

            listaHoras.clear();
            if (med.getHorario().getHoras() != null) {
                listaHoras.addAll(med.getHorario().getHoras());
            }
            ordenarYRepintarHoras();

            intervalo = med.getHorario().getIntervalo();
            tipoIntervaloSel = med.getHorario().getTipoIntervalo();
            edtTipoIntervalo.setText(TipoIntervalo.tipoToStringIndividual(intervalo, tipoIntervaloSel));
            edtIntervaloNum.setText(String.valueOf(med.getHorario().getIntervalo()));
            edtSigToma.setText(Utils.timestampToString(med.getHorario().getSigIngesta()));
        } else {
            horarioActivo = false;
            switchHorario.setChecked(false);
            layoutHorarioContainer.setVisibility(View.GONE);
        }

        if (med.getIsNotiGeneral()) {
            switchNotiGeneral.setChecked(true);
        } else {
            switchNotiGeneral.setChecked(false);
            if(med.getConfNoti() != null){
                notisFragment.cargarDatosEnPantalla(med.getConfNoti());
            } else {
                notisFragment.cargarConfiguracionPorDefecto();
            }
        }
    }

    private void addMedicamento(Medicamento med){
        medDAO.add(med, new OnOperationCallback() {
            @Override
            public void onSuccess() {//medicamento añadido, volvemos a la lista
                if(med.getHorario() != null) RecordatorioManager.programarRecordatoriosMedicamento(requireContext(), med);

                AvisoManager.comprobarAvisos(getContext(), usr, med);
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack();
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void editMedicamento(Medicamento med){
        medDAO.edit(med, new OnOperationCallback() {
            @Override
            public void onSuccess() { // Medicamento actualizado, volvemos atrás
                if(med.getHorario() != null) {
                    RecordatorioManager.cancelarRecordatoriosMedicamento(requireContext(), med);
                    RecordatorioManager.programarRecordatoriosMedicamento(requireContext(), med);
                }

                AvisoDAO aDAO = new AvisoDAO(uid);
                aDAO.eliminarAvisosMedicamento(med.getId());
                AvisoManager.comprobarAvisos(getContext(), usr, med);

                deleteIngIncoherentes();
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void deleteIngIncoherentes() {
        if (medEdit == null || medEdit.getId() == null) return; // nada que borrar

        IngestaDAO ingDAO = new IngestaDAO(uid, medEdit.getId());

        ingDAO.getListBasic(new OnDataLoadedCallback<List<Ingesta>>() {
            @Override
            public void onSuccess(List<Ingesta> ingestas) {
                if (ingestas == null || ingestas.isEmpty()) return;

                //solo las pendientes
                List<Ingesta> pendientes = new ArrayList<>();
                for (Ingesta ing : ingestas) {
                    if (ing.getEstadoIngesta() == EstadoIngesta.PENDIENTE) {
                        pendientes.add(ing);
                    }
                }

                if (pendientes.isEmpty()) {
                    requireActivity()
                            .getSupportFragmentManager()
                            .popBackStack();
                }

                final int total = pendientes.size();
                final int[] contador = {0};

                // Borrar cada ingesta pendiente
                for (Ingesta ingPend : pendientes) {
                    ingDAO.delete(ingPend.getId(), new OnOperationCallback() {
                        @Override
                        public void onSuccess() {
                            contador[0]++;
                            if (contador[0] == total) {
                                requireActivity()
                                        .getSupportFragmentManager()
                                        .popBackStack();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            UiUtils.mostrarErrorYReiniciar(requireActivity());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void actualizarVistaNotificaciones(boolean isNotiGeneral) {
        tvNotiGeneralInfo.setVisibility(isNotiGeneral ? View.VISIBLE : View.GONE);
        Fragment existing = getChildFragmentManager().findFragmentById(R.id.containerNotificacionesMed);

        if (isNotiGeneral) {
            // Si es notificación general, eliminamos cualquier fragmento de notificaciones
            if (existing != null) {
                getChildFragmentManager()
                        .beginTransaction()
                        .remove(existing)
                        .commitNow();
            }
        } else {
            // Si NO es notificación general, añadimos el fragmento
            if (existing == null) {
                notisFragment = new NotificacionesFragment();
                notisFragment.setModoEdicion(true);
                notisFragment.setMostrarTitulo(false);

                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.containerNotificacionesMed, notisFragment)
                        .commitNow();

                if (medEdit != null && medEdit.getConfNoti() != null) {
                    notisFragment.cargarDatosEnPantalla(medEdit.getConfNoti());
                } else {
                    notisFragment.cargarConfiguracionPorDefecto();
                }
            }
        }
    }

    //--------FUNCIONES PARA EL SIMBOLO Y COLOR DEL ICONO DEL MEDICAMENTO
    private void actualizarImagenTipo(TipoMed tipo){
        selectedTipo = tipo;
        imgMedicamento.setImageResource(tipo.getDrawableRes());
        //cambio también el selector de colores al default para mantener el mismo color que el icono
        viewColor.getBackground().setTint(
                ContextCompat.getColor(requireContext(), selectedColorRes)
        );
        actualizarImagenColor(tipo, selectedColorRes);
    }

    private void mostrarSelectorColor(){

        // Grid View para enseñar los posibles colores
        GridView gridView = new GridView(requireContext());
        gridView.setNumColumns(5); // ajustable
        gridView.setHorizontalSpacing(16);
        gridView.setVerticalSpacing(16);
        gridView.setPadding(16,16,16,16);

        // Adaptador simple para mostrar los colores
        gridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return coloresDisponibles.length;
            }

            @Override
            public Object getItem(int position) {
                return coloresDisponibles[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = new View(requireContext());
                int size = 120; // tamaño de cada círculo
                GridView.LayoutParams params = new GridView.LayoutParams(size, size);
                v.setLayoutParams(params);
                v.setBackgroundResource(R.drawable.bg_circle_color);
                v.getBackground().setTint(getResources().getColor(coloresDisponibles[position]));
                return v;
            }
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(Mensajes.MED_EDITADD_SEL_COLOR)
                .setView(gridView)
                .create();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            selectedColorRes = coloresDisponibles[position];

            // Actualiza el viewColor
            viewColor.getBackground().setTint(getResources().getColor(selectedColorRes));
            actualizarImagenColor(selectedTipo, selectedColorRes);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void actualizarImagenColor(TipoMed tipo, int colorRes) {
        // Carga drawable correspondiente al tipo de medicamento
        Drawable drawable = ContextCompat.getDrawable(requireContext(), tipo.getDrawableRes());
        if(drawable == null) return;

        int color = ContextCompat.getColor(requireContext(), colorRes);

        if (drawable instanceof LayerDrawable) { //si es un layout con capa fija y otra con color se cambia solo la capa color
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            Drawable capaColor = layerDrawable.findDrawableByLayerId(tipo.getDrawableResColoreable());

            if (capaColor != null) {
                capaColor.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
            imgMedicamento.setImageDrawable(layerDrawable); //asigna dawable colorado a imageview

        } else { //en caso de tener un icono simple con tod*o coloreable
            drawable = drawable.mutate(); // importante para no afectar otras instancias
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            imgMedicamento.setImageDrawable(drawable);
        }
    }

    //--------FUNCIONES DE LAS HORAS

    private boolean existeHora(Hora nueva){

        for(Hora h : listaHoras){
            if(h.getHora() == nueva.getHora() &&
                    h.getMin() == nueva.getMin()){
                return true;
            }
        }
        return false;
    }

    private void pintarChipHora(Hora hora){

        Chip chip = new Chip(requireContext());
        chip.setText(String.format(Locale.getDefault(),
                "%02d:%02d",
                hora.getHora(),
                hora.getMin()));

        if (hora instanceof HoraMomentoDia) {
            chip.setText(hora.toString());
        } else {
            chip.setText(String.format(Locale.getDefault(), "%02d:%02d",
                    hora.getHora(), hora.getMin()));
        }

        chip.setCloseIconVisible(true);
        chip.setClickable(false);
        chip.setCheckable(false);

        chip.setOnCloseIconClickListener(v -> {
            listaHoras.remove(hora);
            layoutHorasContainer.removeView(chip);
        });

        layoutHorasContainer.addView(chip);
    }

    private void ordenarYRepintarHoras(){
        Collections.sort(listaHoras);
        layoutHorasContainer.removeAllViews();
        for(Hora h : listaHoras){
            pintarChipHora(h);
        }
    }

    private Timestamp aplicarPrimeraHora(Timestamp fecha, List<Hora> horas){
        if(fecha == null || horas == null || horas.isEmpty()) return fecha;

        Hora primera = horas.get(0); //están ordenadas

        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha.toDate());
        cal.set(Calendar.HOUR_OF_DAY, primera.getHora());
        cal.set(Calendar.MINUTE, primera.getMin());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return new Timestamp(cal.getTime());
    }

    private boolean esFechaAnteriorAHoy(Timestamp ts){
        if(ts == null) return false;

        Calendar hoy = Calendar.getInstance();
        Utils.limpiarHora(hoy);

        Calendar fecha = Calendar.getInstance();
        fecha.setTime(ts.toDate());
        Utils.limpiarHora(fecha);

        return fecha.before(hoy);
    }

}
