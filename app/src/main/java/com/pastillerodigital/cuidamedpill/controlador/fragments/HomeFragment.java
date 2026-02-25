package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.adapters.MedicamentosHoyAdapter;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.MedHorasDisplay;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvMedicamentosHoy;
    private TextView tvTitleHome;
    private FloatingActionButton fab;
    private View progressHome;
    private LinearLayout layoutFormHome;

    //Logica
    private String uidSelf, uid;
    private Modo modo;
    private MedicamentoDAO medDAO;
    private UsuarioDAO uDAO;
    private MedicamentosHoyAdapter medHoyAdapter;
    private List<Medicamento> medsUsr = new ArrayList<>();
    private List<MedHorasDisplay> medsHoyDisplay = new ArrayList<>();

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

        mostrarCarga();
        setUpRecyclerView();
        leerArgsYConsec();
        setButtonListeners();

        cargarMeds();
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
                tvTitleHome.setText("Tus medicamentos hoy");
            }
            else{
                cargaUsr();
            }
        }
    }

    private void setButtonListeners(){
        this.fab.setOnClickListener(v->{
            //todo add ingesta
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

    private void cargarMeds(){
        medDAO.getListBasic(new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> data) {
                for(Medicamento med : data){
                    if(med.getHorario() != null) medsUsr.add(med);
                }
                cargarMedicamentosHoy();
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    /**
     *
     */
    private void cargarMedicamentosHoy() {
        Calendar hoy = Calendar.getInstance();
        medsHoyDisplay.clear();

        for (Medicamento med : medsUsr) {
            if (med.getHorario() == null) continue;

            // Obtenemos las horas programadas para este medicamento hoy
            List<String> horas = med.getHorario().getHorasDia(hoy);
            for (String hora : horas) {
                // Aquí es donde instanciamos tu nueva clase de UI
                // Por ahora pasamos null en la Ingesta hasta que implementes su lectura
                medsHoyDisplay.add(new MedHorasDisplay(med, hora, null));
            }
        }

        ordenarPorHora(medsHoyDisplay);
        medHoyAdapter.update(medsHoyDisplay);
        ocultarCarga();
    }

    private void cargaUsr(){
        uDAO.getBasic(uid, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                tvTitleHome.setText(String.format("Medicamentos de %s hoy", data.getAliasU()));
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void setUpRecyclerView(){
        medHoyAdapter = new MedicamentosHoyAdapter(medsHoyDisplay, new MedicamentosHoyAdapter.OnClickListener() {
            @Override
            public void onItemClick(MedHorasDisplay item) {
                // todo ir al med
            }

            @Override
            public void onCheckClick(MedHorasDisplay item) {
                // todo ingesta
            }
        });

        rvMedicamentosHoy.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMedicamentosHoy.setAdapter(medHoyAdapter);
    }

    private void ordenarPorHora(List<MedHorasDisplay> lista) {
        Collections.sort(lista, (a, b) -> {
            int minutosA = horaToMinutos(a.getHora());
            int minutosB = horaToMinutos(b.getHora());
            return Integer.compare(minutosA, minutosB);
        });
    }

    private int horaToMinutos(String hora) {
        // Formato esperado: HH:mm
        String[] partes = hora.split(":");
        int h = Integer.parseInt(partes[0]);
        int m = Integer.parseInt(partes[1]);
        return h * 60 + m;
    }
}
