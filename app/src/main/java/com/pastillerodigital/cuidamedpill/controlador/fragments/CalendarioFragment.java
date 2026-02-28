package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.adapters.MedicamentoCalendarioAdapter;
import com.pastillerodigital.cuidamedpill.controlador.fragments.extra.MedicamentoDetalleFragment;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Ingesta;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.MedicamentoCalendarioDecorador;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;
import com.pastillerodigital.cuidamedpill.utils.Utils;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarioFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private RecyclerView rvMedicamentos;
    private TextView tvFecha, tvTtitle;
    private Chip chipVistaSemanal;

    //Elementos lógica
    private MedicamentoDAO medDAO;
    private UsuarioDAO uDAO;
    private MedicamentoCalendarioAdapter adapter;

    private List<Medicamento> listaCompleta = new ArrayList<>(); //lista medicamentos con horario del usuario
    private List<Medicamento> listaFiltrada = new ArrayList<>();

    private String uid;
    private String uidSelf;
    private Modo modo;
    private boolean vistaSemanal = false;

    private enum TipoDia {
        PASADO,
        PRESENTE,
        FUTURO;
    }

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
        chipVistaSemanal = view.findViewById(R.id.chipVistaSemanal);

        configCalDefault();
        lecturaArgumentosYConsec();
        setButtonListeners();
        setAdapterMeds();
        cargarMedicamentos();
    }

    private void configCalDefault() {
        vistaSemanal = true;
        calendarView.state().edit()
                .setCalendarDisplayMode(CalendarMode.WEEKS) // modo semanal
                .commit();

        chipVistaSemanal.setChecked(true);
        chipVistaSemanal.setText(Mensajes.CAL_VISTA_SEMANAL);
        chipVistaSemanal.setChipIconVisible(true);

        CalendarDay hoy = CalendarDay.today(); // obtiene el día actual
        calendarView.setSelectedDate(hoy);//para que se marque el día de hoy como seleccionado al principio

        chipVistaSemanal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            vistaSemanal = isChecked;
            calendarView.state().edit()
                    .setCalendarDisplayMode(isChecked ? CalendarMode.WEEKS : CalendarMode.MONTHS)
                    .commit();

            if (isChecked) {
                chipVistaSemanal.setText(Mensajes.CAL_VISTA_SEMANAL);
                chipVistaSemanal.setChipIconVisible(true);
            } else {
                chipVistaSemanal.setText(Mensajes.CAL_VISTA_MENSUAL);
                chipVistaSemanal.setChipIconVisible(false);
            }
        });
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
                tvTtitle.setText(Mensajes.CAL_TITLE);
            }
        }
    }

    private void setButtonListeners(){
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(date.getYear(), date.getMonth() - 1, date.getDay());

            filtrarPorFecha(selectedCalendar);

            // (date.getMonth() en esta librería es 1-12, no 0-11)
            tvFecha.setText(String.format(Locale.getDefault(), Mensajes.CAL_DIA_SEL,
                    date.getDay(),
                    date.getMonth(),
                    date.getYear()
            ));
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
        medDAO.getListConIngestas(new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> data) {
                listaCompleta.clear();
                listaCompleta.addAll(data);
                actualizarPuntosCalendario(); //colorea puntos calendario
                tvFecha.setText(Mensajes.CAL_DIA_SEL_HOY);
                filtrarPorFecha(Calendar.getInstance()); //para que salgan las pastillas de hoy sin tener que darle
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
                tvTtitle.setText(String.format(Mensajes.CAL_TITLE_SUPERVISOR, data.getAliasU()));
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }


    private void filtrarPorFecha(Calendar fecha){
        listaFiltrada.clear();
        TipoDia tipoDia = clasificarDia(fecha);

        for(Medicamento med : listaCompleta){
            List<Ingesta> resultado = med.generarIngestasFecha(fecha, tipoDia);

            if(!resultado.isEmpty())listaFiltrada.add(med);
        }

        // Ordenar alfabéticamente por nombre del medicamento
        listaFiltrada.sort((m1, m2) -> m1.getNombreMed().compareToIgnoreCase(m2.getNombreMed()));

        adapter.notifyDataSetChanged();
    }

    private void actualizarPuntosCalendario() {
        if (getContext() == null) return;
        calendarView.removeDecorators();

        int color = ContextCompat.getColor(getContext(), R.color.md_primary_dark);
        calendarView.addDecorator(new MedicamentoCalendarioDecorador(color, listaCompleta));
    }

    private TipoDia clasificarDia(Calendar seleccionado){
        Calendar hoy = Calendar.getInstance();
        Utils.limpiarHora(hoy);
        Calendar sel = (Calendar) seleccionado.clone();
        Utils.limpiarHora(sel);
        Calendar ayer = (Calendar) hoy.clone();
        ayer.add(Calendar.DAY_OF_YEAR, -1);

        if(sel.after(hoy)) return TipoDia.FUTURO;

        if(sel.equals(hoy) || sel.equals(ayer))
            return TipoDia.PRESENTE;

        return TipoDia.PASADO;
    }

}
