package com.pastillerodigital.cuidamedpill.controlador.fragments.extra;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.appbar.MaterialToolbar;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoIntervalo;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Hora;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Horario;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.RecordatorioManager;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioEstandar;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;
import com.pastillerodigital.cuidamedpill.utils.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MedicamentoDetalleFragment extends Fragment {

    private MaterialToolbar toolbarSup;
    private ImageView imgMedicamento;
    private TextView tvNombre, tvFechaCad, tvFechaFin, tvRestantes, tvNotas, tvIntervalo, tvSigToma;
    private ChipGroup chipGroupHoras;
    private LinearLayout layoutNotificaciones, layoutFormMedDetalle;
    private MaterialButton btnEditar, btnEliminar;
    private View progressMedDetalle;
    private LinearLayout layoutFormNotificaciones, layoutFormOpciones;

    //LOGICA
    private MedicamentoDAO medDAO;
    private UsuarioDAO uDAO;
    private String medId, uid, uidSelf;
    private Medicamento medicamento;
    private Modo modo;

    public static MedicamentoDetalleFragment newInstance(String medId, String uid, String uidSelf, Modo modo){
        MedicamentoDetalleFragment fragment = new MedicamentoDetalleFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_MEDID, medId);
        args.putString(Constantes.ARG_UID, uid);
        args.putString(Constantes.ARG_UIDSELF, uidSelf);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medicamento_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Vistas
        toolbarSup = view.findViewById(R.id.topAppBarMedDetalle);
        progressMedDetalle = view.findViewById(R.id.progressMedDetalle);
        layoutFormMedDetalle = view.findViewById(R.id.layoutFormMedDetalle);
        layoutFormNotificaciones = view.findViewById(R.id.layoutFormNotificacionesDetalle);
        layoutFormOpciones = view.findViewById(R.id.layoutFormOpcionesDetalle);

        imgMedicamento = view.findViewById(R.id.imgMedicamentoDet);
        tvNombre = view.findViewById(R.id.tvNombreMedDetalle);

        tvIntervalo = view.findViewById(R.id.tvIntervaloDet);
        tvSigToma = view.findViewById(R.id.tvSigTomaDet);
        chipGroupHoras = view.findViewById(R.id.chipGroupHoras);
        layoutNotificaciones = view.findViewById(R.id.layoutNotificacionesDetalle);

        tvFechaCad = view.findViewById(R.id.tvFechaCad);
        tvFechaFin = view.findViewById(R.id.tvFechaFin);
        tvRestantes = view.findViewById(R.id.tvRestantes);
        tvNotas = view.findViewById(R.id.tvNotas);

        btnEditar = view.findViewById(R.id.btnEditar);
        btnEliminar = view.findViewById(R.id.btnEliminar);

        //Lógica
        mostrarCarga();
        leerArgumentosYConsec();
        cargarMed();
        setButtonListeners();

    }

    private void leerArgumentosYConsec(){
        if(getArguments() != null){
            uidSelf = getArguments().getString(Constantes.ARG_UIDSELF);
            uid = getArguments().getString(Constantes.ARG_UID);
            modo = Modo.modoFromString(getArguments().getString(Constantes.ARG_MODO));
            medId = getArguments().getString(Constantes.ARG_MEDID);

            if(uid == null) uid = uidSelf;
            medDAO = new MedicamentoDAO(uid);
            uDAO = new UsuarioDAO();

            if(modo == Modo.SUPERVISOR){
                cargarUsr();
            }
            else if(modo == Modo.ASISTIDO){
                ocultarVistasAsistido();
            }

        }
    }

    private void setButtonListeners(){
        toolbarSup.setNavigationOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        btnEditar.setOnClickListener(v -> {
            AddAndEditMedicamentoFragment addEditMedFragment = AddAndEditMedicamentoFragment.newInstance(medId,uid, uidSelf, modo);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentApp, addEditMedFragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnEliminar.setOnClickListener(v -> {
            eliminarMed(medId);
        });
    }

    private void cargarMed() {
        medDAO.getBasic(medId, new OnDataLoadedCallback<Medicamento>() {
            @Override
            public void onSuccess(Medicamento med) {
                if(med == null){
                    UiUtils.mostrarErrorYReiniciar(requireActivity());
                    return;
                }
                medicamento = med;
                fillView();
                ocultarCarga();
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void cargarUsr(){
        uDAO.getBasic(uid, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                if(medId == null) toolbarSup.setTitle(String.format(Mensajes.MED_ADD_SUPERV, data.getAliasU()));
                else toolbarSup.setTitle(String.format(Mensajes.MED_EDIT_SUPERV, data.getAliasU()));
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void eliminarMed(String medId){
        new AlertDialog.Builder(requireContext())
                .setTitle(Mensajes.MED_DET_ELIM)
                .setMessage(Mensajes.MED_DET_PREG_ELIM)
                .setPositiveButton(Mensajes.BASIC_ELIMINAR, (d, w) -> {
                    //Borramos las notificaciones que pueda haber
                    RecordatorioManager.cancelarRecordatoriosMedicamento(requireContext(), medicamento);

                    medDAO.delete(medId, new OnOperationCallback() {
                        @Override
                        public void onSuccess() {
                            UiUtils.mostrarConfirmacion(requireActivity(), Mensajes.MED_DET_CONF_ELIMINADO);
                            requireActivity().getSupportFragmentManager().popBackStack();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            UiUtils.mostrarErrorYReiniciar(requireActivity());
                        }
                    });
                })
                .setNegativeButton(Mensajes.BASIC_CANCELAR, null)
                .show();
    }

    private void fillView(){
        mostrarSiHayContenido(tvFechaCad, medicamento.getFechaCad() != null ? Utils.timestampToString(medicamento.getFechaCad()) : null);
        mostrarSiHayContenido(tvFechaFin, medicamento.getFechaFin() != null ? Utils.timestampToString(medicamento.getFechaFin()) : null);
        mostrarSiHayContenido(tvRestantes, medicamento.getnMedRestantes());
        mostrarSiHayContenido(tvNotas, medicamento.getNotasMed());

        tvNombre.setText(medicamento.getNombreMed());

        // Tipo y color
        UiUtils.setMedicamentoIcon(requireContext(), imgMedicamento, medicamento.getTipoMed(), medicamento.getColorSimb());

        // Horas
        chipGroupHoras.removeAllViews();
        Horario horario = medicamento.getHorario();
        if(horario != null && horario.getHoras() != null){
            List<Hora> horas = horario.getHoras();
            Collections.sort(horas);
            for(Hora h: horas){
                Chip chip = new Chip(requireContext());
                chip.setText(h instanceof com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.HoraMomentoDia
                        ? h.toString()
                        : String.format(Locale.getDefault(), "%02d:%02d", h.getHora(), h.getMin()));
                chip.setClickable(false);
                chip.setCheckable(false);
                chipGroupHoras.addView(chip);
            }
        }

        if (horario != null) tvIntervalo.setText(TipoIntervalo.tipoToStringVista(horario.getIntervalo(),
                TipoIntervalo.tipoIntervaloFromString(horario.getTipoIntervaloStr())));
        else tvIntervalo.setText("-");

        if (horario != null && horario.getSigIngesta() != null)tvSigToma.setText(Utils.timestampToTextoSigToma(horario.getSigIngesta()));
        else tvSigToma.setText("-");


        // Notificaciones placeholder
        layoutNotificaciones.removeAllViews();
        TextView tvNotif = new TextView(requireContext());
        tvNotif.setText(Mensajes.MED_DET_NOTISLIKEGEN);
        layoutNotificaciones.addView(tvNotif);
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

    private void ocultarVistasAsistido(){
        layoutFormNotificaciones.setVisibility(View.GONE);
        layoutFormOpciones.setVisibility(View.GONE);
    }

    private void mostrarCarga(){
        progressMedDetalle.setVisibility(View.VISIBLE);
        layoutFormMedDetalle.setVisibility(View.GONE);
    }

    private void ocultarCarga(){
        progressMedDetalle.setVisibility(View.GONE);
        layoutFormMedDetalle.setVisibility(View.VISIBLE);
    }

    /**
     * Muestra u oculta un TextView o ChipGroup dependiendo de si hay contenido
     * Solo aplica en modo ASISTIDO
     */
    private void mostrarSiHayContenido(View view, Object contenido) {
        if (modo == Modo.ASISTIDO) {
            boolean tieneContenido = false;

            if (contenido instanceof String) {
                String texto = (String) contenido;
                tieneContenido = texto != null && !texto.isEmpty();
                if (view instanceof TextView) ((TextView) view).setText(texto);
            } else if (contenido instanceof Integer) { // para números
                int valor = (Integer) contenido;
                tieneContenido = valor >= 0;
                if (view instanceof TextView) ((TextView) view).setText(String.valueOf(valor));
            } else if (contenido instanceof List) { // para ChipGroup
                List<?> lista = (List<?>) contenido;
                tieneContenido = lista != null && !lista.isEmpty();
            } else if (contenido != null) { // otros tipos
                tieneContenido = true;
            }

            view.setVisibility(tieneContenido ? View.VISIBLE : View.GONE);
        } else {
            // En modo SUPERVISOR o normal siempre mostrar
            view.setVisibility(View.VISIBLE);
        }
    }
}
