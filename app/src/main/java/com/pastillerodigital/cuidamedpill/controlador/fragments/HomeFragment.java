package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ListenerRegistration;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.activities.MainActivity;
import com.pastillerodigital.cuidamedpill.controlador.adapters.AvisosAdapter;
import com.pastillerodigital.cuidamedpill.controlador.adapters.IngestasAdapter;
import com.pastillerodigital.cuidamedpill.controlador.adapters.MedicamentoAdapter;
import com.pastillerodigital.cuidamedpill.controlador.fragments.extra.MedicamentoDetalleFragment;
import com.pastillerodigital.cuidamedpill.modelo.dao.AvisoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.IngestaDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EMomentoDia;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Ingesta;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos.Aviso;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos.AvisoManager;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;
import com.pastillerodigital.cuidamedpill.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvMedicamentosHoy, rvAvisos;
    private TextView tvTitleHome, tvTitleAvisos, tvMedsHoy, tvEmptyAvisos, tvEmptyMedsHoy;
    private ExtendedFloatingActionButton fab;
    private View progressHome;
    private LinearLayout layoutFormHome;

    //Logica
    private String uidSelf, uid, uAlias = "";
    private Modo modo;
    private MedicamentoDAO medDAO;
    private UsuarioDAO uDAO;
    private AvisoDAO aDAO;
    private Usuario usr;
    private IngestasAdapter medHoyAdapter;
    private List<Medicamento> lMedHorario = new ArrayList<>();
    private List<Medicamento> lMedTodos = new ArrayList<>();
    private List<Ingesta> ingPendientes = new ArrayList<>();
    private AvisosAdapter avisosAdapter;
    private List<Aviso> listaAvisos = new ArrayList<>();
    private ListenerRegistration avisosListener;

    public static HomeFragment newInstance(String userIdSelf, Modo modo) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSelf);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public static HomeFragment newInstance(String userIdSelf,String userId, Modo modo) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSelf);
        args.putString(Constantes.ARG_UID, userId);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        rvMedicamentosHoy = view.findViewById(R.id.rvMedicamentosHoy);
        tvTitleHome = view.findViewById(R.id.tvTituloHome);
        fab = view.findViewById(R.id.fabAddIngesta);
        progressHome = view.findViewById(R.id.progressHome);
        layoutFormHome = view.findViewById(R.id.formLayoutHome);
        tvTitleAvisos = view.findViewById(R.id.tvTituloAvisosHome);
        tvMedsHoy = view.findViewById(R.id.tvMedsHoyHome);

        tvEmptyAvisos = view.findViewById(R.id.tvEmptyAvisos);
        tvEmptyMedsHoy = view.findViewById(R.id.tvEmptyMedsHoy);
        rvAvisos = view.findViewById(R.id.rvAvisosHome);

        mostrarCarga();
        setUpRecyclerView();
        leerArgsYConsec();
        setButtonListeners();

        cargarMedsYConIngestas();
    }

    private void leerArgsYConsec(){
        if(getArguments() != null){
            uidSelf = getArguments().getString(Constantes.ARG_UIDSELF);
            uid = getArguments().getString(Constantes.ARG_UID);
            modo = Modo.modoFromString(getArguments().getString(Constantes.ARG_MODO));
            if(uid == null){
                uid = uidSelf;
            }

            medDAO = new MedicamentoDAO(uid); //uid (sea el supervisado o no) será del que se obtengan los datos
            uDAO = new UsuarioDAO();
            aDAO = new AvisoDAO(uid);

            if(modo != Modo.SUPERVISOR){
                tvTitleHome.setText(Mensajes.HOME_TITLE);
                tvTitleAvisos.setText(Mensajes.HOME_TITLE_AVISOS);
                tvMedsHoy.setText(Mensajes.HOME_MEDS_HOY);
            }
            cargaUsr();
        }
    }


    private void setButtonListeners(){
        this.fab.setOnClickListener(v->{
            mostrarDialogoSelMed();
        });
    }

    private void mostrarCarga(){
        progressHome.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);
        layoutFormHome.setVisibility(View.GONE);
    }

    private void ocultarCarga(){
        progressHome.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);
        layoutFormHome.setVisibility(View.VISIBLE);
        //ocultar algo si fuese necesario
    }

    private void cargarMedsYConIngestas() {
        medDAO.getListConIngestas(new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> medicamentos) {
                lMedTodos.clear();
                lMedTodos.addAll(medicamentos);
                AvisoManager.comprobarAvisosGeneral(getContext(), usr, medicamentos);
                escucharAvisos();
                gestionarFinTratamientoMeds(); //importante que se haga después de comprobar avisos

                lMedHorario.clear();
                for (Medicamento med : medicamentos) {
                    if (med.getHorario() == null) continue;
                    lMedHorario.add(med);
                }
                //Tenemos la lista de medicamentos con ingestas.
                //Necesitamos ver las ingestas pendientes de ayer y hoy
                getIngPendientesPresente();
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void escucharAvisos(){
        avisosListener = aDAO.listenNoLeidos(new OnDataLoadedCallback<List<Aviso>>() {
            @Override
            public void onSuccess(List<Aviso> data) {
                listaAvisos.clear();
                listaAvisos.addAll(data);
                avisosAdapter.notifyDataSetChanged();
                isVistaNoAvisos();
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });

    }

    private void getIngPendientesPresente(){
        ingPendientes.clear();

        Calendar hoy = Calendar.getInstance();
        Calendar ayer = (Calendar) hoy.clone();
        ayer.add(Calendar.DAY_OF_MONTH, -1);
        Utils.limpiarHora(ayer);

        for(Medicamento med: lMedHorario){
            if (med.getHorario() == null) continue;
            if(!med.isFinTratamiento(Calendar.getInstance())){
                //ingPendientes.addAll(med.getIngestasPendientesDia(ayer, med.getFechaHorasDia(ayer)));
                ingPendientes.addAll(med.getIngestasPendientesDia(hoy, med.getFechaHorasDia(hoy)));
            }
        }

        updateIngestasPendientesDAO();

        ordenarFechaHora(ingPendientes);
        medHoyAdapter.update(ingPendientes);
        isVistaNoMedsHoy();
        ocultarCarga();
    }


    private void cargaUsr(){
        uDAO.getBasic(uid, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                uAlias = data.getAliasU();
                if(modo == Modo.SUPERVISOR){
                    tvTitleHome.setText(String.format(Mensajes.HOME_TITLE_SUPERVISOR, uAlias));
                    tvTitleAvisos.setText(String.format(Mensajes.HOME_TITLE_AVISOS_SUPERVISOR));
                    tvMedsHoy.setText(String.format(Mensajes.HOME_MEDS_HOY_SUPERVISOR));
                }
                usr = data;
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void editaMedAviso(Medicamento med, Aviso aviso){
        medDAO.edit(med, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                AvisoManager.comprobarAvisos(getContext(), usr, med);
                marcarAvisoLeido(aviso);
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void updateIngestasPendientesDAO(){
        for (Ingesta ing : ingPendientes) {
            if (ing.getFechaProgramada() == null) continue;
            Medicamento med = ing.getMed();
            if (med == null) continue;

            IngestaDAO ingestaDAO = new IngestaDAO(uid, med.getId());

            //ingestas actuales del medicamento
            ingestaDAO.getListBasic(new OnDataLoadedCallback<List<Ingesta>>() {
                @Override
                public void onSuccess(List<Ingesta> listaExistente) {
                    boolean yaExiste = false;
                    for (Ingesta existente : listaExistente) {
                        if (existente.getFechaProgramada() != null && existente.getFechaProgramada().equals(ing.getFechaProgramada()) &&
                                EstadoIngesta.PENDIENTE.equals(existente.getEstadoIngesta())) {
                            yaExiste = true;
                            break;
                        }
                    }

                    if (!yaExiste) {
                        // Solo añadimos si no existe
                        Ingesta ingPendiente = new Ingesta(ing.getFechaProgramada(), null, EstadoIngesta.PENDIENTE.toString(), med, "");

                        ingestaDAO.add(ingPendiente, new OnOperationCallback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onFailure(Exception e) {
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    // opcional: manejar error
                }
            });
        }
    }

    private void setUpRecyclerView(){
        medHoyAdapter = new IngestasAdapter(ingPendientes, new IngestasAdapter.OnClickListener() {
            @Override
            public void onItemClick(Ingesta item) {
                MedicamentoDetalleFragment detalleFragment = MedicamentoDetalleFragment.newInstance(item.getMed().getId(),uid, uidSelf, modo);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentApp, detalleFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onCheckClick(Ingesta item) {
                mostrarDialogoIngesta(item.getMed(), item);
            }
        });
        rvMedicamentosHoy.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMedicamentosHoy.setAdapter(medHoyAdapter);
        rvMedicamentosHoy.setNestedScrollingEnabled(false);


        //Adapter avisos

        rvAvisos.setLayoutManager(new LinearLayoutManager(getContext()));
        avisosAdapter = new AvisosAdapter(listaAvisos, new AvisosAdapter.OnAvisoClickListener() {
            @Override
            public void onIgnorar(Aviso aviso) {
                aviso.setLeido(true);
                new AvisoDAO(uid).edit(aviso, new OnOperationCallback() {
                    @Override
                    public void onSuccess() {
                        listaAvisos.remove(aviso);
                        avisosAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        UiUtils.mostrarErrorYReiniciar(requireActivity());
                    }
                });
            }

            @Override
            public void onResolver(Aviso aviso) {
                mostrarDialogoResolverAviso(aviso);
            }
        });
        rvAvisos.setAdapter(avisosAdapter);
        rvAvisos.setNestedScrollingEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Si está supervisando a alguien se actualiza la vista correspondiente.
        MainActivity ma = (MainActivity) requireActivity();
        modo = ma.getModo();
        uidSelf = ma.getUidSelf();

    }

    private void ordenarFechaHora(List<Ingesta> lista) {
        Collections.sort(lista, (a, b) -> {
            if (a.getFechaProgramada() == null) return 1;
            if (b.getFechaProgramada() == null) return -1;

            return a.getFechaProgramada().compareTo(b.getFechaProgramada());
        });
    }

    private Medicamento buscarMedicamento(String id){
        for(Medicamento m : lMedTodos){
            if(m.getId().equals(id)) return m;
        }
        return null;
    }

    private void isVistaNoMedsHoy(){
        if(ingPendientes.isEmpty()){
            tvEmptyMedsHoy.setVisibility(View.VISIBLE);
            rvMedicamentosHoy.setVisibility(View.GONE);
        }else{
            tvEmptyMedsHoy.setVisibility(View.GONE);
            rvMedicamentosHoy.setVisibility(View.VISIBLE);
        }
    }

    private void isVistaNoAvisos(){
        if(listaAvisos.isEmpty()){
            if(modo == Modo.ASISTIDO){ //ocultamos sección de avisos vacíos
                tvTitleAvisos.setVisibility(View.GONE);
                tvEmptyAvisos.setVisibility(View.GONE);
                rvAvisos.setVisibility(View.GONE);
            }else{
                tvTitleAvisos.setVisibility(View.VISIBLE);
                tvEmptyAvisos.setVisibility(View.VISIBLE);
                rvAvisos.setVisibility(View.GONE);
            }
        }else{
            tvTitleAvisos.setVisibility(View.VISIBLE);
            tvEmptyAvisos.setVisibility(View.GONE);
            rvAvisos.setVisibility(View.VISIBLE);
        }
    }

    //----------INGESTAS
    private void mostrarDialogoIngesta(Medicamento med, @Nullable Ingesta ing){
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm_ingesta, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        ImageView imgTipo = dialogView.findViewById(R.id.imgTipoMedDialog);
        TextView tvNombre = dialogView.findViewById(R.id.tvNombreMedDialog);
        TextView tvTituloDialog = dialogView.findViewById(R.id.tvTituloDialogConfIng);
        TextView tvHora = dialogView.findViewById(R.id.tvHoraConfIng);
        Button btnSi = dialogView.findViewById(R.id.btnSi);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        TipoMed tipoMed = TipoMed.tipoMedFromString(med.getTipoMedStr());
        UiUtils.setDrawableTipoMed(getContext(), imgTipo, tipoMed, med.getColorSimb());

        if(modo == Modo.SUPERVISOR){
            tvTituloDialog.setText(String.format(Mensajes.HOME_CONFING_TITULO_SUPERVISOR, uAlias));
        }
        else{
            tvTituloDialog.setText(Mensajes.HOME_CONFING_TITULO);
        }

        tvNombre.setText(med.getNombreMed());
        if(med.getTipoMedStr() != null) tvHora.setText(med.getTipoMedStr());

        EditText etNotasDialog = dialogView.findViewById(R.id.etNotasConfirmIng);
        Timestamp fechaProgramada = ing != null ? ing.getFechaProgramada() : null;
        if(fechaProgramada== null){ //no programada
            etNotasDialog.setVisibility(View.VISIBLE);
            tvHora.setVisibility(View.GONE);
        } else {
            etNotasDialog.setVisibility(View.GONE);
            tvHora.setVisibility(View.VISIBLE);
            EMomentoDia mom = med.getMomentoDiaFromIngesta(ing);
            if(mom != null) tvHora.setText(mom.toString());
            else tvHora.setText(Utils.timestampToString(fechaProgramada));

            // Cancelar avisos a tutores si existían
            if (med.getWorkTags() != null) {
                for (String tag : med.getWorkTags()) {
                    androidx.work.WorkManager.getInstance(requireContext()).cancelAllWorkByTag(tag);
                }
                med.getWorkTags().clear(); // limpiar tags
            }
        }


        btnSi.setOnClickListener(v -> {
            if (fechaProgramada != null) {
                String tag = "aviso_tutores_" + med.getId() + "_" + fechaProgramada.toDate().getTime();
                androidx.work.WorkManager.getInstance(requireContext()).cancelUniqueWork(tag);
            }

            Calendar ahora = Calendar.getInstance();
            Timestamp fechaIngesta = new Timestamp(ahora.getTime());

            EstadoIngesta estado = calcularEstadoIngesta(fechaProgramada);
            String nota = "";
            if(estado.equals(EstadoIngesta.NO_PROGRAMADA)){
                nota = etNotasDialog.getText().toString().trim();
            }

            Ingesta ingesta = new Ingesta(fechaProgramada, fechaIngesta, estado.toString(), med, nota);
            IngestaDAO ingestaDAO = new IngestaDAO(uid, med.getId());

            ingestaDAO.add(ingesta, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    //puede haber medicamentos con horario que tengan ingestas fuera de horario

                    med.ingestaTomada(ingesta); //actualiza sig ingesta y resta med
                    medDAO.edit(med, new OnOperationCallback() {
                        @Override
                        public void onSuccess() {
                            if(med.getHorario() == null || fechaProgramada == null) AvisoManager.comprobarAvisos(getContext(), usr, med);
                            else{
                                // Recargar ingestas pendientes
                                mostrarCarga();
                                cargarMedsYConIngestas();
                                medHoyAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            UiUtils.mostrarErrorYReiniciar(requireActivity());
                        }
                    });



                }

                @Override
                public void onFailure(Exception e) {
                    UiUtils.mostrarErrorYReiniciar(requireActivity());
                }
            });
            dialog.dismiss();
        });

        btnNo.setOnClickListener(v -> {
            dialog.dismiss(); //ingesta no tomada, no cambiamos nada
        });

        dialog.show();
    }

    private EstadoIngesta calcularEstadoIngesta(Timestamp fechaProgramada){
        if(fechaProgramada == null) return EstadoIngesta.NO_PROGRAMADA;
        long diffMinutos = (System.currentTimeMillis() - fechaProgramada.toDate().getTime()) / 60000;
        if(diffMinutos <= Constantes.MINS_RETRASO)return EstadoIngesta.TOMADA;
        else return EstadoIngesta.RETRASO;
    }

    private void mostrarDialogoSelMed(){
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lista_medicamentos, null);
        RecyclerView rv = dialogView.findViewById(R.id.rvListaMedicamentos);
        TextView tvTitle = dialogView.findViewById(R.id.tvTituloSelMedHome);

        String titulo = "";
        if(modo == Modo.SUPERVISOR)titulo = Mensajes.HOME_SELMED_TITULO;
        else titulo = String.format(Mensajes.HOME_SELMED_TITULO_SUPERVISOR, uAlias);
        tvTitle.setText(titulo);

        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
                .setView(dialogView)
                .create();
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        //Cargar medicamentos
        medDAO.getListBasic(new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> lista) {
                MedicamentoAdapter adapter = new MedicamentoAdapter(lista, med -> {
                    dialog.dismiss();
                    mostrarDialogoIngesta(med, null);
                });
                rv.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
        dialog.show();
    }

    //AVISOS
    private void mostrarDialogoResolverAviso(Aviso aviso){
        switch (aviso.getTipoAviso()){
            case CADUCIDAD:
                mostrarDialogoCaducidad(aviso);
                break;
            case COMPRA:
                mostrarDialogoCompra(aviso);
                break;
            case FINTRATAMIENTO:
                mostrarDialogoFinTratamiento(aviso);
                break;
        }
    }

    private void mostrarDialogoCaducidad(Aviso aviso){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_resolver_caducidad, null);

        ImageView imgTipo = view.findViewById(R.id.imgTipoMedCad);
        TextView tvNombre = view.findViewById(R.id.tvNombreMedCad);
        TextView tvFecha = view.findViewById(R.id.tvFechaCadCad);
        MaterialButton btnFecha = view.findViewById(R.id.btnSeleccionarFechaCad);
        MaterialButton btnQuitar = view.findViewById(R.id.btnQuitarFechaCad);

        Medicamento med = buscarMedicamento(aviso.getMedId());
        if(med == null) return;
        tvNombre.setText(med.getNombreMed());
        TipoMed tipo = TipoMed.tipoMedFromString(med.getTipoMedStr());
        UiUtils.setDrawableTipoMed(getContext(), imgTipo, tipo, med.getColorSimb());
        Calendar nuevaFecha = Calendar.getInstance();

        btnFecha.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(getContext(), (view1, year, month, day) -> {
                        nuevaFecha.set(year, month, day);
                        tvFecha.setText(Utils.calendarToString(nuevaFecha));
                    },
                    nuevaFecha.get(Calendar.YEAR),
                    nuevaFecha.get(Calendar.MONTH),
                    nuevaFecha.get(Calendar.DAY_OF_MONTH)
            );
            picker.show();
        });

        btnQuitar.setOnClickListener(v -> {
            tvFecha.setText("Sin fecha de caducidad");
            nuevaFecha.clear();
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
                .setView(view)
                .setPositiveButton("Guardar", (d, w) -> {

                    if(nuevaFecha.isSet(Calendar.YEAR)){
                        med.setFechaCad(new Timestamp(nuevaFecha.getTime()));
                    }else{
                        med.setFechaCad(null);
                    }
                    editaMedAviso(med, aviso);
                })
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();
    }

    private void mostrarDialogoCompra(Aviso aviso){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_resolver_compra, null);

        ImageView imgTipo = view.findViewById(R.id.imgTipoMed);
        TextView tvNombre = view.findViewById(R.id.tvNombreMed);
        EditText etCantidad = view.findViewById(R.id.etCantidad);

        Medicamento med = buscarMedicamento(aviso.getMedId());

        if(med == null) return;
        tvNombre.setText(med.getNombreMed());
        TipoMed tipo = TipoMed.tipoMedFromString(med.getTipoMedStr());
        UiUtils.setDrawableTipoMed(getContext(), imgTipo, tipo, med.getColorSimb());

        new MaterialAlertDialogBuilder(getContext())
                .setView(view)
                .setPositiveButton("Guardar", (d, w) -> {
                    String texto = etCantidad.getText().toString().trim();

                    if(!texto.isEmpty()){
                        int cantidad = Integer.parseInt(texto);
                        med.setnMedRestantes(med.getnMedRestantes() + cantidad);
                        editaMedAviso(med, aviso);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoFinTratamiento(Aviso aviso){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_resolver_fintratamiento, null);

        ImageView imgTipo = view.findViewById(R.id.imgTipoMed);
        TextView tvNombre = view.findViewById(R.id.tvNombreMed);
        MaterialButton btnFecha = view.findViewById(R.id.btnSeleccionarFechaFin);

        Medicamento med = buscarMedicamento(aviso.getMedId());

        if(med == null) return;
        tvNombre.setText(med.getNombreMed());
        TipoMed tipo = TipoMed.tipoMedFromString(med.getTipoMedStr());
        UiUtils.setDrawableTipoMed(getContext(), imgTipo, tipo, med.getColorSimb());

        Calendar nuevaFecha = Calendar.getInstance();
        btnFecha.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(getContext(), (view1, year, month, day) -> {
                        nuevaFecha.set(year, month, day);
                        },
                        nuevaFecha.get(Calendar.YEAR),
                        nuevaFecha.get(Calendar.MONTH),
                        nuevaFecha.get(Calendar.DAY_OF_MONTH)
            );

            picker.show();
        });

        new MaterialAlertDialogBuilder(getContext())
                .setView(view)
                .setPositiveButton("Guardar", (d, w) -> {

                    if(nuevaFecha.isSet(Calendar.YEAR)){
                        med.setFechaFin(new Timestamp(nuevaFecha.getTime()));
                        editaMedAviso(med, aviso);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void marcarAvisoLeido(Aviso aviso){
        aviso.setLeido(true);

        aDAO.edit(aviso, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                listaAvisos.remove(aviso);
                avisosAdapter.notifyDataSetChanged();

                isVistaNoAvisos();
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void gestionarFinTratamientoMeds(){
        for(Medicamento med: lMedTodos){
            if(med.isFinTratamiento(Calendar.getInstance())) medDAO.updateFinTratamientoFinalizado(med);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (avisosListener != null) {
            avisosListener.remove();
        }
    }

}
