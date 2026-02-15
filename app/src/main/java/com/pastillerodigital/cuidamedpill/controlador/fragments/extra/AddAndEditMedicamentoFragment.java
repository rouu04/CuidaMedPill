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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;
import com.pastillerodigital.cuidamedpill.utils.Utils;

import java.util.Calendar;
import java.util.List;

public class AddAndEditMedicamentoFragment extends Fragment {

    private TextInputLayout layoutNombre, layoutTipoMed, layoutPauta, layoutFechaCad, layoutFechaFin, layoutNCajas, layoutMedPorCaja, layoutMedActualCaja;
    private TextInputEditText edtNombre, edtTipoMed, edtPauta, edtFechaCad, edtFechaFin,edtNCajas, edtMedPorCaja, edtMedActualCaja;
    private ImageView imgMedicamento;
    private MaterialButton btnGuardar, btnAgregarHora;
    private CircularProgressIndicator progressIndicator;
    private View viewColor;

    private TextView tvTitulo;

    //Elementos lógicos
    private MedicamentoDAO medDAO;
    private Medicamento medEdit;
    private boolean isEdit;
    private String uid, uidSelf, medId;
    private Modo modo;
    private int selectedColorRes = R.color.md_primary;
    private TipoMed selectedTipo = TipoMed.CAPSULA;

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
        layoutTipoMed = view.findViewById(R.id.layoutTipoMed);
        edtTipoMed = view.findViewById(R.id.edtTipoMed);
        layoutPauta = view.findViewById(R.id.layoutPauta);
        edtPauta = view.findViewById(R.id.edtPauta);
        layoutFechaCad = view.findViewById(R.id.layoutFechaCad);
        edtFechaCad = view.findViewById(R.id.edtFechaCad);
        layoutFechaFin = view.findViewById(R.id.layoutFechaFin);
        edtFechaFin = view.findViewById(R.id.edtFechaFin);
        layoutNCajas = view.findViewById(R.id.layoutNCajas);
        edtNCajas = view.findViewById(R.id.edtNCajas);
        layoutMedPorCaja = view.findViewById(R.id.layoutMedPorCaja);
        edtMedPorCaja = view.findViewById(R.id.edtMedPorCaja);
        layoutMedActualCaja = view.findViewById(R.id.layoutMedActualCaja);
        edtMedActualCaja = view.findViewById(R.id.edtMedActualCaja);

        tvTitulo = view.findViewById(R.id.tvTitle);

        viewColor = view.findViewById(R.id.viewColor);

        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnAgregarHora = view.findViewById(R.id.btnAgregarHora);
        progressIndicator = view.findViewById(R.id.progressIndicator);

        //Lógica:
        medDAO = new MedicamentoDAO(uid);
        leerArgumentos();
        setButtonListeners();

    }

    private void leerArgumentos(){
        if(getArguments() != null){
            uidSelf = getArguments().getString(Constantes.ARG_UIDSELF);
            uid = getArguments().getString(Constantes.ARG_UID);
            modo = Modo.modoFromString(getArguments().getString(Constantes.ARG_MODO));
            if(uid == null) uid = uidSelf;
            medId = getArguments().getString(Constantes.ARG_MEDID);
            if(medId != null){ //edición
                tvTitulo.setText("Editar medicamento");
                isEdit = true;
                cargarDatos(medId);
            }
            else{
                tvTitulo.setText("Añadir un medicamento");
                edtTipoMed.setText(TipoMed.CAPSULA.toString());
                actualizarImagenTipo(TipoMed.CAPSULA);
            }
        }
    }

    private void setButtonListeners(){
        edtTipoMed.setOnClickListener(v -> mostrarSelectorTipo());
        edtFechaCad.setOnClickListener(v -> mostrarDatePicker());
        btnGuardar.setOnClickListener(v -> guardarOEditarMedicamento());
        viewColor.setOnClickListener(v -> mostrarSelectorColor());

    }

    private void cargarDatos(String medId){
        // todo
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

    private void guardarOEditarMedicamento(){
        // Validar campos
        // Crear nuevo Medicamento o actualizar medEdit
        // Llamar medicamentoDAO.add() o medicamentoDAO.edit()
        UiUtils.limpiarErroresLayouts((ViewGroup) getView().findViewById(R.id.formLayout));
        String nombre = edtNombre.getText().toString().trim();
        String tipoStr = edtTipoMed.getText().toString().trim();
        String pautaStr = edtPauta.getText().toString().trim();
        String fechaCadStr = edtFechaCad.getText().toString().trim();
        String fechaFinStr = edtFechaFin.getText().toString().trim();
        String nCajasStr = edtNCajas.getText().toString().trim();
        String nMedPorCajaStr = edtMedPorCaja.getText().toString().trim();
        String nMedActualStr = edtMedActualCaja.getText().toString().trim();
        String colorString = getResources().getResourceEntryName(selectedColorRes);

        if(!validaciones(nombre, tipoStr, pautaStr)) return;
        if(!validacionesOpcionales(fechaCadStr, fechaFinStr, nCajasStr, nMedPorCajaStr, nMedActualStr)) return;

        int nCajas = nCajasStr.isEmpty() ? -1 : Integer.parseInt(nCajasStr);
        int nMedPorCaja = nMedPorCajaStr.isEmpty() ? -1 : Integer.parseInt(nMedPorCajaStr);
        int nMedActual = nMedActualStr.isEmpty() ? -1 : Integer.parseInt(nMedActualStr);

        Timestamp fechaCad = fechaCadStr.isEmpty() ? null : Utils.stringToTimestamp(fechaCadStr);
        Timestamp fechaFin = fechaFinStr.isEmpty() ? null : Utils.stringToTimestamp(fechaFinStr);


        //todo revisar y hacer horario

        Medicamento medActual = new Medicamento(colorString, Float.parseFloat(pautaStr), TipoMed.tipoMedFromString(tipoStr),
                fechaCad , nombre, fechaFin, nCajas, nMedPorCaja, nMedActual, null, null);

        if(isEdit) medActual.setId(medEdit.getId());

        //No pueden haber dos medicamentos con el mismo nombre para el mismo usuario:
        medDAO.getListBasic(uid, new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> data) {
                for(Medicamento med: data){
                    if(med.getNombreMed().equals(nombre)) return;

                    if(isEdit){ //todo

                    }
                    else{

                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }


    private boolean validaciones(String nombre, String tipoStr, String pautaStr){
        boolean valid = true;

        if(nombre.isEmpty()){
            layoutNombre.setError("Ingrese el nombre del medicamento");
            valid = false;
        }
        if(tipoStr.isEmpty()){
            layoutTipoMed.setError("Seleccione un tipo de medicamento");
            valid = false;
        }
        if(pautaStr.isEmpty()){
            layoutPauta.setError("Ingrese la pauta");
            valid = false;
        }
        return valid;
    }

    private boolean validacionesOpcionales(String fechaCadStr, String fechaFinStr, String nCajasStr,
                                           String nMedPorCajaStr, String nMedActualStr){
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

        if(!nCajasStr.isEmpty()){
            try{
                int nCajas = Integer.parseInt(nCajasStr);
                if(nCajas < 0){
                    layoutNCajas.setError("Debe ser un número más grande que 0");
                    valid = false;
                }
            }catch (NumberFormatException e){
                layoutNCajas.setError("Número inválido");
                valid = false;
            }
        }

        if(!nMedPorCajaStr.isEmpty()){
            try{
                int nMedPorCaja = Integer.parseInt(nMedPorCajaStr);
                if(nMedPorCaja <= 0){
                    layoutMedPorCaja.setError("Debe ser más grande que 0");
                    valid = false;
                }
            }catch (NumberFormatException e){
                layoutMedPorCaja.setError("Número inválido");
                valid = false;
            }
        }

        if(!nMedActualStr.isEmpty()){
            try{
                int nMedActual = Integer.parseInt(nMedActualStr);

                if(nMedActual < 0){
                    layoutMedActualCaja.setError("Debe ser un número más grande que 0");
                    valid = false;
                }

                if(!nMedPorCajaStr.isEmpty()){
                    int nMedPorCaja = Integer.parseInt(nMedPorCajaStr);

                    if(nMedActual > nMedPorCaja){
                        layoutMedActualCaja.setError("No puede ser mayor que los medicamentos por caja");
                        valid = false;
                    }
                }

            }catch (NumberFormatException e){
                layoutMedActualCaja.setError("Número inválido");
                valid = false;
            }
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
    }

    private void mostrarSelectorTipo(){
        String[] tipos = TipoMed.getAllTipos();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleccionar tipo")
                .setItems(tipos, (dialog, which) -> {
                    edtTipoMed.setText(tipos[which]);
                    TipoMed tipoSeleccionado = TipoMed.tipoMedFromString(tipos[which]);
                    actualizarImagenTipo(tipoSeleccionado);
                })
                .show();
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
        LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(requireContext(), tipo.getDrawableRes());

        if (layerDrawable != null) {
            // Capa coloreable (color dinámico)
            Drawable capaColor = layerDrawable.findDrawableByLayerId(tipo.getDrawableResColoreable());
            if (capaColor != null) {//Aplicamos color seleccionado
                capaColor.setColorFilter(getResources().getColor(colorRes), PorterDuff.Mode.SRC_IN);
            }
            // Asigna drawable coloreado al ImageView
            imgMedicamento.setImageDrawable(layerDrawable);
        }
    }
}
