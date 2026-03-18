package com.pastillerodigital.cuidamedpill.controlador.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoAviso;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos.Aviso;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvisosAdapter extends RecyclerView.Adapter<AvisosAdapter.AvisoVH> {

    public interface OnAvisoClickListener {
        void onIgnorar(Aviso aviso);
        void onResolver(Aviso aviso);
    }

    private List<Aviso> avisos;
    private OnAvisoClickListener listener;
    private Map<String, Medicamento> medicamentosCache = new HashMap<>();
    private MedicamentoDAO medDAO;

    public AvisosAdapter(List<Aviso> avisos,String uid ,OnAvisoClickListener listener) {
        this.avisos = avisos;
        this.listener = listener;
        this.medDAO = new MedicamentoDAO(uid);
    }

    @NonNull
    @Override
    public AvisoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aviso, parent, false);
        return new AvisoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AvisoVH holder, int position) {
        Aviso aviso = avisos.get(position);

        holder.tvTitulo.setText(aviso.getTitulo());
        holder.tvMensaje.setText(aviso.getMensaje() != null ? aviso.getMensaje() : "");

        Context context = holder.itemView.getContext();

        String medId = aviso.getMedId();

        //Icono med
        holder.imgIcono.setVisibility(View.GONE);
        holder.cardView.setVisibility(View.GONE);
        holder.btnResolver.setVisibility(View.GONE);
        if(aviso.getTipoAviso() != TipoAviso.OLVIDOASISTIDO){
            holder.imgIcono.setVisibility(View.VISIBLE);
            holder.cardView.setVisibility(View.VISIBLE);
            holder.btnResolver.setVisibility(View.VISIBLE);

            if (medicamentosCache.containsKey(medId)) {
                Medicamento med = medicamentosCache.get(medId);
                bindIcon(holder, context, med);
            } else{

                medDAO.getBasic(medId, new OnDataLoadedCallback<Medicamento>() {
                    @Override
                    public void onSuccess(Medicamento med) {
                        int pos = holder.getBindingAdapterPosition();
                        if (pos == RecyclerView.NO_POSITION) return;

                        medicamentosCache.put(medId, med);
                        aviso.setMed(med);
                        notifyItemChanged(pos);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        medicamentosCache.put(medId, null);
                    }
                });

            }
        }
        else{

        }


        //Color aviso
        switch (aviso.getTipoAviso()) {
            case CADUCIDAD:
                holder.card.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.avisoCaducidad));
                break;

            case COMPRA:
                holder.card.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.avisoCompra));
                break;

            case FINTRATAMIENTO:
                holder.card.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.avisoFinTratamiento));
                break;

            case OLVIDOASISTIDO:
                holder.card.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.avisoOlvidoAsistido));
                break;

            default:
                holder.card.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.avisoGeneral));
                break;
        }


        holder.btnIgnorar.setOnClickListener(v -> {
            if (listener != null) listener.onIgnorar(aviso);
        });

        holder.btnResolver.setOnClickListener(v -> {
            if (listener != null) listener.onResolver(aviso);
        });
    }

    @Override
    public int getItemCount() {
        return avisos.size();
    }

    static class AvisoVH extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvMensaje;
        Button btnIgnorar, btnResolver;
        CardView card;
        ImageView imgIcono;
        MaterialCardView cardView;

        public AvisoVH(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloAviso);
            tvMensaje = itemView.findViewById(R.id.tvMensajeAviso);
            btnIgnorar = itemView.findViewById(R.id.btnIgnorar);
            btnResolver = itemView.findViewById(R.id.btnResolver);
            imgIcono = itemView.findViewById(R.id.imgIconoAviso);
            cardView = itemView.findViewById(R.id.cardIconoAviso);
            card = (CardView) itemView;
        }
    }

    private void bindIcon(AvisoVH holder, Context context, Medicamento med) {
        if (med != null && med.getTipoMed() != null) {
            UiUtils.setMedicamentoIcon(context, holder.imgIcono, med.getTipoMed(), med.getColorSimb());

        } else {
            UiUtils.setMedicamentoIcon(context, holder.imgIcono, TipoMed.CAPSULA, "md_primary");
        }
    }
}
