package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.Timestamp;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.activities.MainActivity;
import com.pastillerodigital.cuidamedpill.controlador.adapters.IngestasAdapter;
import com.pastillerodigital.cuidamedpill.controlador.adapters.MedicamentoAdapter;
import com.pastillerodigital.cuidamedpill.controlador.fragments.extra.MedicamentoDetalleFragment;
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

    private RecyclerView rvMedicamentosHoy;
    private TextView tvTitleHome, tvTitleAvisos, tvMedsHoy;
    private ExtendedFloatingActionButton fab;
    private View progressHome;
    private LinearLayout layoutFormHome;

    //Logica
    private String uidSelf, uid, uAlias = "";
    private Modo modo;
    private MedicamentoDAO medDAO;
    private UsuarioDAO uDAO;
    private Usuario usr;
    private IngestasAdapter medHoyAdapter;
    private List<Medicamento> lMed = new ArrayList<>();
    private List<Ingesta> ingPendientes = new ArrayList<>();

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

        mostrarCarga();
        setUpRecyclerView();
        leerArgsYConsec();
        setButtonListeners();

        cargarMedsConIngestas();
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

    private void cargarMedsConIngestas() {
        medDAO.getListConIngestas(new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> medicamentos) {
                lMed.clear();
                for (Medicamento med : medicamentos) {
                    if (med.getHorario() == null) continue;
                    lMed.add(med);
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

    private void getIngPendientesPresente(){
        ingPendientes.clear();

        Calendar hoy = Calendar.getInstance();
        Calendar ayer = (Calendar) hoy.clone();
        ayer.add(Calendar.DAY_OF_MONTH, -1);
        Utils.limpiarHora(ayer);

        for(Medicamento med: lMed){
            if (med.getHorario() == null) continue;
            if(!med.checkAndUpdateFinTratamiento()){
                ingPendientes.addAll(med.getIngestasPendientesDia(ayer, med.getFechaHorasDia(ayer)));
                ingPendientes.addAll(med.getIngestasPendientesDia(hoy, med.getFechaHorasDia(hoy)));
            }
        }

        ordenarFechaHora(ingPendientes);
        medHoyAdapter.update(ingPendientes);
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
                    AvisoManager.comprobarAvisos(getContext(), usr, med);
                    //puede haber medicamentos con horario que tengan ingestas fuera de horario
                    if(med.getHorario() != null && fechaProgramada != null){
                        medHoyAdapter.notifyDataSetChanged();
                        mostrarCarga();
                        cargarMedsConIngestas();
                        med.ingestaTomada(ingesta);
                        //Guardamos en medicamento la nueva sigtoma del horario
                        medDAO.edit(med, new OnOperationCallback() {
                            @Override
                            public void onSuccess() {
                                // Recargar ingestas pendientes
                                mostrarCarga();
                                cargarMedsConIngestas();
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



}
