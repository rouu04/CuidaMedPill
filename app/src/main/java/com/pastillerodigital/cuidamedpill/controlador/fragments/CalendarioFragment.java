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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.adapters.MedicamentoCalendarioAdapter;
import com.pastillerodigital.cuidamedpill.controlador.fragments.extra.MedicamentoDetalleFragment;
import com.pastillerodigital.cuidamedpill.modelo.dao.IngestaDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarioFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private RecyclerView rvMedicamentos;
    private TextView tvFecha, tvDiaEmpty, tvHintCalAsist;
    private MaterialToolbar tbTtitleCal;
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
    private Calendar fechaSeleccionada = Calendar.getInstance();

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
        tvDiaEmpty = view.findViewById(R.id.tvDiaEmpty);
        tbTtitleCal = view.findViewById(R.id.topAppBarCal);
        chipVistaSemanal = view.findViewById(R.id.chipVistaSemanal);
        tvHintCalAsist = view.findViewById(R.id.tvHintCalAsist);

        configCalDefault();
        lecturaArgumentosYConsec();
        setButtonListeners();
        setAdapterMeds();
        cargarMedicamentos();
    }

    private void configCalDefault() {
        calendarView.state().edit()
                .setCalendarDisplayMode(CalendarMode.WEEKS) // modo semanal
                .commit();

        chipVistaSemanal.setChecked(true);
        chipVistaSemanal.setText(Mensajes.CAL_VISTA_SEMANAL);
        chipVistaSemanal.setChipIconVisible(true);

        CalendarDay hoy = CalendarDay.today(); // obtiene el día actual
        calendarView.setSelectedDate(hoy);//para que se marque el día de hoy como seleccionado al principio

        chipVistaSemanal.setOnCheckedChangeListener((buttonView, isChecked) -> {
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
                tbTtitleCal.setTitle(Mensajes.CAL_TITLE);
            }

            if(modo == Modo.ASISTIDO){
                tvHintCalAsist.setVisibility(View.VISIBLE);
            }
            else{
                tvHintCalAsist.setVisibility(View.GONE);
            }
        }
    }

    private void setButtonListeners(){
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(date.getYear(), date.getMonth() - 1, date.getDay());

            fechaSeleccionada = selectedCalendar;
            filtrarPorFecha(fechaSeleccionada);
            actualizarTextoFecha();
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
                for(Medicamento med: data){
                    if(med.isFinTratamiento(Calendar.getInstance())) medDAO.updateFinTratamientoFinalizado(med);
                    listaCompleta.add(med);
                }
                marcarIngestasPasadasComoOlvido();

                actualizarPuntosCalendario(); //colorea puntos calendario
                actualizarTextoFecha();
                filtrarPorFecha(fechaSeleccionada); //carga las pastillas del día seleccionado (inicializado a hoy) sin tener que darle
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
                tbTtitleCal.setTitle(String.format(Mensajes.CAL_TITLE_SUPERVISOR, data.getAliasU()));
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void marcarIngestasPasadasComoOlvido() {
        Calendar hoy = Calendar.getInstance();
        Utils.limpiarHora(hoy); // Solo queremos comparar días, no horas

        for (Medicamento med : listaCompleta) {
            if (med.getlIngestas() == null) continue;

            for (Ingesta ing : med.getlIngestas()) {
                Timestamp ts = ing.getFechaProgramada();
                if (ts == null) continue;

                Calendar fechaIng = Calendar.getInstance();
                fechaIng.setTime(ts.toDate());
                Utils.limpiarHora(fechaIng);

                // Solo ingestas del pasado
                if (fechaIng.before(hoy)) {
                    // Si estaba pendiente, marcar como olvido
                    if (EstadoIngesta.PENDIENTE.equals(ing.getEstadoIngesta())) {
                        ing.setEstadoIngesta(EstadoIngesta.OLVIDO);
                        ing.setEstadoIngestaStr(EstadoIngesta.OLVIDO.toString());

                        // Guardar cambio en Firebase
                        IngestaDAO ingDAO = new IngestaDAO(uid, med.getId());
                        ingDAO.edit(ing, new OnOperationCallback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onFailure(Exception e) {
                            }
                        });
                    }
                }
            }
        }
    }


    private void filtrarPorFecha(Calendar fecha){
        listaFiltrada.clear();
        TipoDia tipoDia = clasificarDia(fecha);

        for(Medicamento med : listaCompleta){
            List<Ingesta> resultado = new ArrayList<>();
            if(tipoDia == TipoDia.PRESENTE || tipoDia == TipoDia.PASADO) resultado = med.getIngestasPorDia(fecha);
            else resultado = med.generarIngestasFecha(fecha, tipoDia);

            if(!resultado.isEmpty())listaFiltrada.add(med);
        }

        // Ordenar alfabéticamente por nombre del medicamento
        listaFiltrada.sort((m1, m2) -> m1.getNombreMed().compareToIgnoreCase(m2.getNombreMed()));
        adapter.setFechaSeleccionada(fecha);
        adapter.notifyDataSetChanged();

        if(listaFiltrada.isEmpty()){
            tvDiaEmpty.setVisibility(View.VISIBLE);
            rvMedicamentos.setVisibility(View.GONE);
        }else{
            tvDiaEmpty.setVisibility(View.GONE);
            rvMedicamentos.setVisibility(View.VISIBLE);
        }
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

        //if(sel.equals(hoy) || sel.equals(ayer))
            //return TipoDia.PRESENTE;
        if(sel.equals(hoy))
            return TipoDia.PRESENTE;

        return TipoDia.PASADO;
    }

    private boolean esHoy(Calendar fecha){
        Calendar hoy = Calendar.getInstance();
        return fecha.get(Calendar.YEAR) == hoy.get(Calendar.YEAR) &&
                fecha.get(Calendar.MONTH) == hoy.get(Calendar.MONTH) &&
                fecha.get(Calendar.DAY_OF_MONTH) == hoy.get(Calendar.DAY_OF_MONTH);
    }

    private void actualizarTextoFecha(){
        if(esHoy(fechaSeleccionada)){
            tvFecha.setText(Mensajes.CAL_DIA_SEL_HOY);
        }else{
            tvFecha.setText(String.format(Locale.getDefault(), Mensajes.CAL_DIA_SEL,
                    fechaSeleccionada.get(Calendar.DAY_OF_MONTH),
                    fechaSeleccionada.get(Calendar.MONTH)+1,
                    fechaSeleccionada.get(Calendar.YEAR)
            ));
        }
    }

}
