package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.adapters.MedicamentoAdapter;
import com.pastillerodigital.cuidamedpill.controlador.adapters.MedicamentoCalendarioAdapter;
import com.pastillerodigital.cuidamedpill.controlador.fragments.extra.MedicamentoDetalleFragment;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Horario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarioFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView rvMedicamentos;
    private TextView tvFecha, tvTtitle;

    //Elementos lógica
    private MedicamentoDAO medDAO;
    private UsuarioDAO uDAO;
    private MedicamentoCalendarioAdapter adapter;

    private List<Medicamento> listaCompleta = new ArrayList<>(); //lista medicamentos con horario del usuario
    private List<Medicamento> listaFiltrada = new ArrayList<>();

    private String uid;
    private String uidSelf;
    private Modo modo;

    public static CalendarioFragment newInstance(String userIdSelf, Modo modo) {
        CalendarioFragment fragment = new CalendarioFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSelf);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public static CalendarioFragment newInstance(String userIdSelf,String userId, Modo modo) {
        CalendarioFragment fragment = new CalendarioFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSelf);
        args.putString(Constantes.ARG_UID, userId);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        rvMedicamentos = view.findViewById(R.id.rvMedicamentosDia);
        tvFecha = view.findViewById(R.id.tvFechaSeleccionada);
        tvTtitle = view.findViewById(R.id.tvTitleCal);

        lecturaArgumentosYConsec();
        setButtonListeners();
        setAdapterMeds();
        cargarMedicamentos();
    }

    private void lecturaArgumentosYConsec(){
        if(getArguments() != null){
            uidSelf = getArguments().getString(Constantes.ARG_UIDSELF);
            uid = getArguments().getString(Constantes.ARG_UID);
            modo = Modo.modoFromString(getArguments().getString(Constantes.ARG_MODO));
            if(uid == null){
                uid = uidSelf;
            }
            medDAO = new MedicamentoDAO(uid);
            uDAO = new UsuarioDAO();

            if(modo == Modo.SUPERVISOR){
                cargaUsr(uid);
            }
            else{
                tvTtitle.setText("Tu calendario");
            }


        }
    }

    private void setButtonListeners(){
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            filtrarPorFecha(selected);
            tvFecha.setText("Medicamentos a tomar el día: " + dayOfMonth + "/" + (month+1) + "/" + year);
        });
    }

    private void setAdapterMeds(){
        rvMedicamentos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MedicamentoCalendarioAdapter(
                listaFiltrada,
                Calendar.getInstance(), // Fecha inicial, luego se actualiza con el calendario
                medicamento -> {
                    MedicamentoDetalleFragment detalleFragment = MedicamentoDetalleFragment.newInstance(medicamento.getId(), uid, uidSelf, modo);
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentApp, detalleFragment)
                            .addToBackStack(null)
                            .commit();
                }
        );
        rvMedicamentos.setAdapter(adapter);
    }


    private void cargarMedicamentos(){
        medDAO.getListBasic(uid, new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> data) {
                for(Medicamento med: data){
                    if(med.getHorario() != null) listaCompleta.add(med);
                }
                tvFecha.setText("Medicamentos a tomar hoy");
                filtrarPorFecha(Calendar.getInstance()); //para que salgan las pastillas de hoy
                //sin tener que darle
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void cargaUsr(String uid){
        uDAO.getBasic(uid, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                tvTtitle.setText(String.format("Calendario de %s", data.getAliasU()));
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }



    /**
     * Filtra los medicamentos que tienen alguna ingesta ese día
     */
    private void filtrarPorFecha(Calendar fecha){
        listaFiltrada.clear();

        for(Medicamento med : listaCompleta){
            if(med.getHorario() == null) continue;

            if(med.getHorario().hayIngestaDia(fecha)){
                listaFiltrada.add(med);
            }
        }

        adapter.notifyDataSetChanged();
    }


}
