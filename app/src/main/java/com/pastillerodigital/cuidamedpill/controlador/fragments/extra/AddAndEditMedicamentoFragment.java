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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.Timestamp;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EMomentoDia;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoIntervalo;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Hora;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Horario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;
import com.pastillerodigital.cuidamedpill.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AddAndEditMedicamentoFragment extends Fragment {

    private TextInputLayout layoutNombre, layoutPauta, layoutTipoIntervalo, layoutIntervaloNum, layoutFechaCad, layoutFechaFin, layoutNMedRestantes;
    private TextInputEditText edtNombre, edtPauta, edtTipoIntervalo, edtIntervaloNum, edtFechaCad, edtFechaFin, edtNMedRestantes;
    private ImageView imgMedicamento;
    private MaterialButton btnGuardar, btnAgregarHora;
    private CircularProgressIndicator progressIndicator;
    private View viewColor;
    private TextView tvTitulo;
    private List<Hora> listaHoras = new ArrayList<>();
    private ChipGroup layoutHorasContainer; //como un layout pero más profesional
    private SwitchMaterial switchHorario; //toggle
    private LinearLayout layoutHorarioContainer;
    private MaterialCardView cardTipoMed;

    //Elementos lógicos
    private MedicamentoDAO medDAO;
    private Medicamento medEdit;
    private boolean isEdit, horarioActivo;
    private String uid, uidSelf, medId;
    private Modo modo;
    private int selectedColorRes = R.color.md_primary;
    private TipoMed selectedTipo = TipoMed.CAPSULA;
    private int colorMed;
    private TipoIntervalo tipoIntervaloSel;

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

    public static AddAndEditMedicamentoFragment newInstance(Medicamento med, String uid, String uidSelf, Modo modo){
        AddAndEditMedicamentoFragment fragment = new AddAndEditMedicamentoFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_MEDID, med.getId());
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
        imgMedicamento = view.findViewById(R.id.imgMedicamento);
        layoutNombre = view.findViewById(R.id.layoutNombreMed);
        edtNombre = view.findViewById(R.id.edtNombreMed);
        cardTipoMed = view.findViewById(R.id.cardTipoMed);

        //Horario:
        layoutTipoIntervalo = view.findViewById(R.id.layoutTipoIntervalo);
        edtTipoIntervalo = view.findViewById(R.id.edtTipoIntervalo);
        layoutIntervaloNum = view.findViewById(R.id.layoutIntervaloNum);
        edtIntervaloNum = view.findViewById(R.id.edtIntervaloNum);
        layoutPauta = view.findViewById(R.id.layoutPauta);
        edtPauta = view.findViewById(R.id.edtPauta);
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

        tvTitulo = view.findViewById(R.id.tvTitle);

        viewColor = view.findViewById(R.id.viewColor);

        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnAgregarHora = view.findViewById(R.id.btnAgregarHora);
        progressIndicator = view.findViewById(R.id.progressIndicator);

        //Lógica:
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
            medId = getArguments().getString(Constantes.ARG_MEDID);

            //Modo edicion o añadir medicamento
            if(medId != null){ //edición
                tvTitulo.setText("Editar medicamento");
                isEdit = true;
                cargarDatos(medId);
            }
            else{
                tvTitulo.setText("Añadir un medicamento");
                colorMed = R.color.md_primary;
                tipoIntervaloSel = TipoIntervalo.DIARIO;
                selectedTipo = TipoMed.CAPSULA;
                actualizarImagenTipo(TipoMed.CAPSULA);
            }
        }
    }

    private void setButtonListeners(){
        cardTipoMed.setOnClickListener(v -> mostrarSelectorTipo());

        edtTipoIntervalo.setOnClickListener(v -> mostrarSelectorIntervalo());
        edtFechaCad.setOnClickListener(v -> mostrarDatePicker());
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
                edtTipoIntervalo.setText(TipoIntervalo.DIARIO.toString());

            } else {
                layoutHorarioContainer.setVisibility(View.GONE);
                // Se limpian los datos del horario
                listaHoras.clear();
                layoutHorasContainer.removeAllViews();
                edtIntervaloNum.setText("");
                edtPauta.setText("");
                edtTipoIntervalo.setText("");
            }
        });

    }

    private void cargarDatos(String medId){
        // todo
        //todo poner color atrib y tipo intervalo (sacalo del horario) como el del medicamento
    }

    /**
     * Abre un date picker para los atributos con fecha, para que no tenga que ser introducido manualmente
     * por el usuario y así evitar errores, además de añadir un aspecto profesional a la interfaz
     */
    private void mostrarDatePicker(){
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    edtFechaCad.setText(Utils.calendarToString(calendar));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void mostrarSelectorIntervalo(){
        String[] tipos = TipoIntervalo.getAllTipos();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleccionar intervalo")
                .setItems(tipos, (dialog, which) -> {
                    edtTipoIntervalo.setText(tipos[which]);
                    TipoIntervalo tipoSeleccionado = TipoIntervalo.tipoIntervaloFromString(tipos[which]);
                    //todo que hacer cuando se selecciona
                })
                .show();
    }

    private void mostrarSelectorTipo(){
        String[] tipos = TipoMed.getAllTipos();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleccionar tipo")
                .setItems(tipos, (dialog, which) -> {
                    TipoMed tipoSeleccionado = TipoMed.tipoMedFromString(tipos[which]);
                    actualizarImagenTipo(tipoSeleccionado);
                })
                .show();
    }

    private void mostrarMenuSeleccionHora() {

        String[] opciones = {
                "Seleccionar hora manualmente",
                "Seleccionar por momentos del día"
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Añadir hora")
                .setItems(opciones, (dialog, which) -> {

                    if (which == 0) {
                        mostrarSelectorHora();
                    } else {
                        mostrarSelectorMomentos();
                    }

                })
                .show();
    }


    private void mostrarSelectorHora() {

        MaterialTimePicker picker =
                new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(8)
                        .setMinute(0)
                        .setTitleText("Seleccionar hora")
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

        picker.show(getParentFragmentManager(), "TIME_PICKER");
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
                .setTitle("Seleccionar momento del día")
                .setItems(textos, (dialog, which) -> {

                    EMomentoDia momento = momentos[which];

                    Hora nuevaHora = new Hora(
                            momento.getHoraDefault(),
                            momento.getMinDefault()
                    );

                    if(!existeHora(nuevaHora)){
                        listaHoras.add(nuevaHora);
                        ordenarYRepintarHoras();
                    }

                })
                .show();
    }



    private void guardarOEditarMedicamento(){
        UiUtils.limpiarErroresLayouts((ViewGroup) getView().findViewById(R.id.formLayout));

        String nombre = edtNombre.getText().toString().trim();
        String pautaStr = edtPauta.getText().toString().trim();
        String intervaloNumStr = edtIntervaloNum.getText().toString().trim();
        String fechaCadStr = edtFechaCad.getText().toString().trim();
        String fechaFinStr = edtFechaFin.getText().toString().trim();
        String nMedRestantesStr = edtNMedRestantes.getText().toString().trim();
        String colorString = getResources().getResourceEntryName(selectedColorRes);

        if(!validaciones(nombre)) return;
        if(!validacionesOpcionales(fechaCadStr, fechaFinStr, nMedRestantesStr)) return;


        int nCajas = nMedRestantesStr.isEmpty() ? -1 : Integer.parseInt(nMedRestantesStr);
        int intervaloNum = intervaloNumStr.isEmpty() ? -1 : Integer.parseInt(intervaloNumStr); //-1 por simplicidad
        Timestamp fechaCad = fechaCadStr.isEmpty() ? null : Utils.stringToTimestamp(fechaCadStr);
        Timestamp fechaFin = fechaFinStr.isEmpty() ? null : Utils.stringToTimestamp(fechaFinStr);

        Horario horario = null;
        if(horarioActivo){
            if(!validacionesHorario(intervaloNumStr, pautaStr)) return;
            horario = new Horario(
                    tipoIntervaloSel,
                    intervaloNum,
                    Float.parseFloat(pautaStr),
                    listaHoras
            );
        }

        Medicamento medActual = new Medicamento(colorString, selectedTipo, fechaCad , nombre,
                fechaFin, nCajas, horario, null);
        if(isEdit) medActual.setId(medEdit.getId());

        //No pueden haber dos medicamentos con el mismo nombre para el mismo usuario:
        medDAO.getListBasic(uid, new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> data) {
                if(data.isEmpty()){ //si no tiene medicamentos registrados
                    if(isEdit){ //todo

                    }
                    else addMedicamento(medActual);
                }

                for(Medicamento med: data){
                    if(med.getNombreMed().equals(nombre)) return;

                    if(isEdit){ //todo

                    }
                    else addMedicamento(medActual);
                }
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
            layoutNombre.setError("Ingrese el nombre del medicamento");
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
                    layoutFechaFin.setError("Fecha inválida");
                    valid = false;
                } else {
                    Timestamp hoy = Timestamp.now();

                    if(fechaFin.toDate().before(hoy.toDate())){
                        layoutFechaFin.setError("La fecha fin no puede ser anterior a hoy");
                        valid = false;
                    }
                }
            }catch (Exception e){
                layoutFechaFin.setError("Formato de fecha incorrecto");
                valid = false;
            }
        }

        if(!fechaCadStr.isEmpty()){
            try{
                Timestamp fechaCad = Utils.stringToTimestamp(fechaFinStr);

                if(fechaCad == null){
                    layoutFechaCad.setError("Fecha inválida");
                    valid = false;
                } else {
                    Timestamp hoy = Timestamp.now();

                    if(fechaCad.toDate().before(hoy.toDate())){
                        layoutFechaCad.setError("La fecha fin no puede ser anterior a hoy");
                        valid = false;
                    }
                }
            }catch (Exception e){
                layoutFechaCad.setError("Formato de fecha incorrecto");
                valid = false;
            }
        }

        if(!nMedRestantesStr.isEmpty()){
            try{
                int nCajas = Integer.parseInt(nMedRestantesStr);
                if(nCajas < 0){
                    layoutNMedRestantes.setError("Debe ser un número más grande o igual que 0");
                    valid = false;
                }
            }catch (NumberFormatException e){
                layoutNMedRestantes.setError("Número inválido");
                valid = false;
            }
        }



        return valid;
    }

    private boolean validacionesHorario(String intervaloNumStr, String pautaStr){
        boolean valid = true;

        if(!intervaloNumStr.isEmpty()){
            try{
                int intervaloNum = Integer.parseInt(intervaloNumStr);
                if(intervaloNum <= 0){
                    layoutIntervaloNum.setError("Debe ser un número más grande que 0");
                    valid = false;
                }
            }catch (NumberFormatException e){
                layoutIntervaloNum.setError("Número inválido");
                valid = false;
            }
        }

        if(!pautaStr.isEmpty()){
            try{
                float pautaF = Float.parseFloat(pautaStr);
                if(pautaF <= 0.0){
                    layoutPauta.setError("Debe ser un número más grande que 0.0");
                    valid = false;
                }
            }catch (NumberFormatException e){
                layoutPauta.setError("Número inválido");
                valid = false;
            }
        }

        if(horarioActivo && listaHoras.isEmpty()){
            UiUtils.mostrarConfirmacion(requireActivity(), "Si el horario está activo necesita al menos una hora");
            return false;
        }


        return valid;
    }

    private void addMedicamento(Medicamento med){
        medDAO.add(med, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                //medicamento añadido, volvemos a la lista
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



    //--------FUNCIONES PARA EL SIMBOLO Y COLOR DEL ICONO DEL MEDICAMENTO
    private void actualizarImagenTipo(TipoMed tipo){
        selectedTipo = tipo;
        imgMedicamento.setImageResource(tipo.getDrawableRes());
        //cambio también el selector de colores al default para mantener el mismo color que el icono
        selectedColorRes = colorMed; //default si es añadir
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
                .setTitle("Seleccionar color")
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
    private void pintarHora(Hora hora){

        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        fila.setPadding(0,16,0,16);

        TextView tvHora = new TextView(requireContext());
        tvHora.setText(String.format(Locale.getDefault(),
                "%02d:%02d",
                hora.getHora(),
                hora.getMin()));
        tvHora.setTextSize(16f);
        tvHora.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));

        ImageView btnDelete = new ImageView(requireContext());
        btnDelete.setImageResource(R.drawable.ic_basura);

        btnDelete.setOnClickListener(v -> {
            listaHoras.remove(hora);
            layoutHorasContainer.removeView(fila);
        });

        fila.addView(tvHora);
        fila.addView(btnDelete);

        layoutHorasContainer.addView(fila);
    }

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

        chip.setCloseIconVisible(true);
        chip.setClickable(false);
        chip.setCheckable(false);

        /*
        Más bonito
        chip.setChipIconResource(R.drawable.ic_clock);
        chip.setChipIconVisible(true);

         */

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




}
