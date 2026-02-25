package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.activities.MainActivity;
import com.pastillerodigital.cuidamedpill.controlador.adapters.MedicamentosHoyAdapter;
import com.pastillerodigital.cuidamedpill.controlador.fragments.extra.MedicamentoDetalleFragment;
import com.pastillerodigital.cuidamedpill.modelo.dao.IngestaDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Ingesta;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.MedHorasDisplay;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;
import com.pastillerodigital.cuidamedpill.utils.Utils;

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
    private List<Medicamento> lMedHoras = new ArrayList<>();
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

        //cargarMeds();
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

    /*
    private void cargarMeds(){
        medDAO.getListBasic(new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> data) {
                lMedHoras.clear();
                for(Medicamento med : data){
                    if(med.getHorario() != null) lMedHoras.add(med);
                }
                cargarIngestas();
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }


    private void cargarIngestas(){
        List<Ingesta> todasIngestas = new ArrayList<>();
        if (lMedHoras.isEmpty()) {
            cargarIngPendientes(todasIngestas);
            return;
        }

        int[] cont = {0};
        for (Medicamento med : lMedHoras) { //por cada med carga sus ingestas de las ultimas 48h
            IngestaDAO ingDAO = new IngestaDAO(uid, med.getId());

            ingDAO.getListBasicUltimosDosDias(new OnDataLoadedCallback<List<Ingesta>>() {
                @Override
                public void onSuccess(List<Ingesta> data) {
                    todasIngestas.addAll(data);
                    cont[0]++;
                    if (cont[0] == lMedHoras.size()) { // Todas las llamadas terminaron
                        cargarIngPendientes(todasIngestas);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    UiUtils.mostrarErrorYReiniciar(requireActivity());
                }
            });
        }
    }


    private void cargarIngPendientes(List<Ingesta> ingestas) { //Selecciona de entre unas ingestas cuales no se han tomado
        medsHoyDisplay.clear();

        Calendar hoy = Calendar.getInstance();
        Calendar ayer = (Calendar) hoy.clone();
        ayer.add(Calendar.DAY_OF_MONTH, -1);

        for (Medicamento med : lMedHoras){
            if(med.getHorario() == null) continue;
            procesarHorasDia(med, ayer, ingestas);
            procesarHorasDia(med, hoy, ingestas);
        }

        ordenarFechaHora(medsHoyDisplay);
        medHoyAdapter.update(medsHoyDisplay);
        ocultarCarga();
    }ç

     */

    private void cargarMedsConIngestas() {
        medDAO.getConIngestasHome(new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> medicamentos) {
                lMedHoras.clear();
                for (Medicamento med : medicamentos) {
                    if (med.getHorario() == null) continue;
                    lMedHoras.add(med);
                }
                cargarIngPendientes();
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void cargarIngPendientes() {
        medsHoyDisplay.clear();
        Calendar hoy = Calendar.getInstance();
        Calendar ayer = (Calendar) hoy.clone();
        ayer.add(Calendar.DAY_OF_MONTH, -1);

        for (Medicamento med : lMedHoras) { //por cada medicamento
            if (med.getHorario() == null) continue;

            List<Ingesta> ingestas = med.getlIngestas() != null ? med.getlIngestas() : new ArrayList<>();
            List<Calendar> dias = List.of(ayer, hoy);

            for (Calendar dia : dias) {
                List<Timestamp> fechasProgramadas = med.getHorario().getFechaHorasDia(dia);

                for (Timestamp fechaProgramada : fechasProgramadas) {
                    boolean yaTomada = ingestas.stream().anyMatch(ing -> ing.getIdMed().equals(med.getId())
                                    && mismaFechaHoraMinuto(ing.getFechaProgramada(), fechaProgramada)
                                    && ing.getEstadoIngesta() != EstadoIngesta.PENDIENTE);

                    if (!yaTomada) {
                        Timestamp ahora = new Timestamp(Calendar.getInstance().getTime());
                        Ingesta ingNoTomada = new Ingesta(fechaProgramada, ahora,
                                EstadoIngesta.PENDIENTE.toString(), med.getId());
                        medsHoyDisplay.add(new MedHorasDisplay(med, Utils.timestampAHoraHome(fechaProgramada), ingNoTomada));
                    }
                }
            }
        }

        ordenarFechaHora(medsHoyDisplay);
        medHoyAdapter.update(medsHoyDisplay);
        ocultarCarga();
    }

    /**
     * usamos las ingestas que vienen del DAO.
     */
    private void procesarHorasDiaConIngestas(Medicamento med, Calendar dia, List<Ingesta> ingestas) {
        List<String> horas = med.getHorario().getHorasDiaStr(dia);
        Calendar ahora = Calendar.getInstance();

        for (String hora : horas) {
            Timestamp fechaProgramada = Utils.construirTimestamp(dia, hora);
            boolean yaTomada = false;

            for (Ingesta ing : ingestas) {
                if (ing.getIdMed().equals(med.getId()) && mismaFechaHoraMinuto(ing.getFechaProgramada(), fechaProgramada)
                        && ing.getEstadoIngesta() != EstadoIngesta.PENDIENTE) {
                    yaTomada = true;
                    break;
                }
            }

            if (!yaTomada) {
                Ingesta ingNoTomada = new Ingesta(fechaProgramada, new Timestamp(ahora.getTime()),
                        EstadoIngesta.PENDIENTE.toString(), med.getId());
                medsHoyDisplay.add(new MedHorasDisplay(med, hora, ingNoTomada));
            }
        }
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
                MedicamentoDetalleFragment detalleFragment = MedicamentoDetalleFragment.newInstance(item.getMedicamento().getId(),uid, uidSelf, modo);

                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentApp, detalleFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onCheckClick(MedHorasDisplay item) {
                mostrarDialogoIngesta(item);
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

    /**
     * Por cada hora programada para ese día del medicamento, comprueba las ingestas para ver si alguna coincide
     * @param med
     * @param dia
     * @param ingestas
     */
    private void procesarHorasDia(Medicamento med, Calendar dia, List<Ingesta> ingestas){
        List<String> horas = med.getHorario().getHorasDiaStr(dia);

        for(String hora : horas){
            Timestamp fechaProgramada = Utils.construirTimestamp(dia, hora);
            boolean yaTomada = false;

            for(Ingesta ing : ingestas){
                if(ing.getIdMed().equals(med.getId()) && mismaFechaHoraMinuto(ing.getFechaProgramada(), fechaProgramada) &&
                        ing.getEstadoIngesta() != EstadoIngesta.PENDIENTE){
                    yaTomada = true;
                    break;
                }
            }

            // Si no existe ingesta tomada es pendiente
            if(!yaTomada){
                Calendar aux = Calendar.getInstance();
                Ingesta ingNoTomada = new Ingesta(fechaProgramada, new Timestamp(aux.getTime()),
                        EstadoIngesta.PENDIENTE.toString(), med.getId());
                medsHoyDisplay.add(new MedHorasDisplay(med, hora, ingNoTomada));
            }
        }
    }

    private void ordenarFechaHora(List<MedHorasDisplay> lista) {
        Collections.sort(lista, (a, b) -> {
            Timestamp t1 = a.getIngesta().getFechaProgramada();
            Timestamp t2 = b.getIngesta().getFechaProgramada();
            return Long.compare(t1.getSeconds(), t2.getSeconds());
        });
    }


    //----------INGESTAS

    private void mostrarDialogoIngesta(MedHorasDisplay item){
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_confirm_ingesta, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        ImageView imgTipo = dialogView.findViewById(R.id.imgTipoMedDialog);
        TextView tvNombre = dialogView.findViewById(R.id.tvNombreMedDialog);
        Button btnSi = dialogView.findViewById(R.id.btnSi);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        Medicamento med = item.getMedicamento();
        tvNombre.setText(med.getNombreMed());

        TipoMed tipoMed = TipoMed.tipoMedFromString(med.getTipoMedStr());
        Drawable drawable = ContextCompat.getDrawable(getContext(), tipoMed.getDrawableRes());
        int colorResId = getResources().getIdentifier(med.getColorSimb(), "color", getContext().getPackageName());
        int color = ContextCompat.getColor(getContext(), colorResId);

        if(drawable instanceof LayerDrawable){
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            Drawable capaColor = layerDrawable.findDrawableByLayerId(tipoMed.getDrawableResColoreable());
            if(capaColor != null){
                capaColor.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
            imgTipo.setImageDrawable(layerDrawable);
        } else {
            drawable = drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            imgTipo.setImageDrawable(drawable);
        }

        btnSi.setOnClickListener(v -> {
            Calendar ahora = Calendar.getInstance();
            Timestamp fechaIngesta = new Timestamp(ahora.getTime());
            Timestamp fechaProgramada = item.getIngesta().getFechaProgramada();

            EstadoIngesta estado = calcularEstadoIngesta(fechaProgramada);
            Ingesta ingesta = new Ingesta(fechaProgramada, fechaIngesta, estado.toString(), item.getMedicamento().getId());

            IngestaDAO ingestaDAO = new IngestaDAO(uid, item.getMedicamento().getId());
            ingestaDAO.add(ingesta, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    medHoyAdapter.notifyDataSetChanged();
                    mostrarCarga();
                    //cargarMeds();
                    cargarMedsConIngestas();
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
        long diffMinutos = (System.currentTimeMillis() - fechaProgramada.toDate().getTime()) / 60000;

        if(diffMinutos <= 60){ //todo cambiar 60 por algo coded
            //todo por default antes de indicar la ingesta será margen y luego ya se cambia
            //mayor a 24 h será olvido
            return EstadoIngesta.OK;
        } else {
            return EstadoIngesta.RETRASO;
        }
    }

    private boolean mismaFechaHoraMinuto(Timestamp t1, Timestamp t2){
        Calendar c1 = Calendar.getInstance();
        c1.setTime(t1.toDate());
        Calendar c2 = Calendar.getInstance();
        c2.setTime(t2.toDate());

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) &&
                c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY) &&
                c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE);
    }


}
