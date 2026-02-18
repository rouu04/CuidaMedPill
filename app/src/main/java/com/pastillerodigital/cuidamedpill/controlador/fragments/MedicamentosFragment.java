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
import com.pastillerodigital.cuidamedpill.controlador.activities.MainActivity;
import com.pastillerodigital.cuidamedpill.controlador.adapters.MedicamentoAdapter;
import com.pastillerodigital.cuidamedpill.controlador.fragments.extra.AddAndEditMedicamentoFragment;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class MedicamentosFragment extends Fragment {

    //Elementos del diseño
    private View progressMed;
    private RecyclerView rvMed;
    private FloatingActionButton fab;
    private TextView tvTitleMeds;
    private LinearLayout layoutFormMeds;

    //Elementos lógicos
    String uid, uidSelf;
    private MedicamentoAdapter medAdapter;
    private MedicamentoDAO medDAO;
    private UsuarioDAO uDAO;
    private List<Medicamento> lMed = new ArrayList<>();
    private Modo modo;
    private boolean updateNeeded = false;

    public static MedicamentosFragment newInstance(String userIdSellf, Modo modo) {
        MedicamentosFragment fragment = new MedicamentosFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSellf);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public static MedicamentosFragment newInstance(String userIdSelf,String userId, Modo modo) {
        MedicamentosFragment fragment = new MedicamentosFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSelf);
        args.putString(Constantes.ARG_UID, userId);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }

    //---------INICIALIZACIONES
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medicamentos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        //Elementos diseño:
        progressMed = view.findViewById(R.id.progressMed);
        rvMed = view.findViewById(R.id.rvMedicamentos);
        fab = view.findViewById(R.id.fabAddMedicamento);
        tvTitleMeds = view.findViewById(R.id.tvTitleMeds);
        layoutFormMeds = view.findViewById(R.id.formLayoutMeds);

        //Lógica:
        mostrarCarga();

        lecturaArgumentosYConsec();
        setButtonListeners();

        cargarMeds();
    }

    private void lecturaArgumentosYConsec(){
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
                tvTitleMeds.setText(Mensajes.MEDS_TITLE);
            }
            else{
                cargaUsr();
            }
        }
    }

    private void ocultaVistaModo(){
        if(modo == Modo.ASISTIDO){
            fab.setVisibility(View.GONE);
        }
        else{
            fab.setVisibility(View.VISIBLE);
        }
    }

    private void setButtonListeners(){
        this.fab.setOnClickListener(v->{
            AddAndEditMedicamentoFragment addEditMedFragment = AddAndEditMedicamentoFragment.newInstance(uid, uidSelf, modo);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentApp, addEditMedFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void mostrarCarga(){
        progressMed.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);
        layoutFormMeds.setVisibility(View.GONE);
    }

    private void ocultarCarga(){
        progressMed.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);
        layoutFormMeds.setVisibility(View.VISIBLE);
        ocultaVistaModo();
    }

    //---------FUNCIONES CON GRAN FUNCIONALIDAD
    private void cargarMeds(){
        medDAO.getListBasic(uid, new OnDataLoadedCallback<List<Medicamento>>() {
            @Override
            public void onSuccess(List<Medicamento> data) {
                lMed = data;

                setUpRecyclerView();
                ocultarCarga();
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void cargaUsr(){
        uDAO.getBasic(uid, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                tvTitleMeds.setText(String.format(Mensajes.MEDS_TITULO_SUPERV, data.getAliasU()));
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void setUpRecyclerView(){
        medAdapter = new MedicamentoAdapter(lMed, medicamento -> {
            /*
            todo pulir lógica con lo que necesite en el futuro
            MedicamentoDetalleFragment detalleFragment = MedicamentoDetalleFragment.newInstance(medicamento.getId());

            // Navegación usando FragmentManager
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, detalleFragment)
                    .addToBackStack(null)
                    .commit();

             */
        });
        rvMed.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMed.setAdapter(medAdapter);
    }


    @Override
    public void onResume() {
        super.onResume();

        if (updateNeeded) {
            updateNeeded = false;
            mostrarCarga();
            cargarMeds();
        }

        //Si está supervisando a alguien se actualiza la vista correspondiente.
        MainActivity ma = (MainActivity) requireActivity();
        modo = ma.getModo();
        uidSelf = ma.getUidSelf();

    }
}
