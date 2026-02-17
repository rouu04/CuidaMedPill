package com.pastillerodigital.cuidamedpill.controlador.adapters;


import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;

import java.util.List;

public class MedicamentoAdapter extends RecyclerView.Adapter<MedicamentoAdapter.MedViewHolder>{
    private List<Medicamento> lista;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Medicamento medicamento);
    }

    public MedicamentoAdapter(List<Medicamento> lista, OnItemClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicamento, parent, false);
        return new MedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedViewHolder holder, int position) {
        Medicamento med = lista.get(position);
        holder.tvNombre.setText(med.getNombreMed());

        //SIMBOLO MEDICAMENTO
        TipoMed tipoMed = TipoMed.tipoMedFromString(med.getTipoMedStr());

        holder.imgTipoMed.setImageResource(tipoMed.getDrawableRes());
        int colorResId = holder.itemView.getContext().getResources().getIdentifier(med.getColorSimb(), "color", holder.itemView.getContext().getPackageName());
        int color = ContextCompat.getColor(holder.itemView.getContext(), colorResId);

        // Obtener drawable
        Drawable drawable = ContextCompat.getDrawable(holder.itemView.getContext(), tipoMed.getDrawableRes());
        if(drawable == null) return;

        if (drawable instanceof LayerDrawable) { //si es un layout con capa fija y otra con color se cambia solo la capa color
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            Drawable capaColor = layerDrawable.findDrawableByLayerId(tipoMed.getDrawableResColoreable());
            if (capaColor != null) {
                capaColor.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
            holder.imgTipoMed.setImageDrawable(layerDrawable); //asigna dawable colorado a imageview

        } else { //en caso de tener un icono simple con tod*o coloreable
            drawable = drawable.mutate(); // importante para no afectar otras instancias
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            holder.imgTipoMed.setImageDrawable(drawable);
        }



        //Al hacer click en un medicamento
        holder.itemView.setOnClickListener(v -> {
            if(listener != null){
                listener.onItemClick(med);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class MedViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        View viewColor;
        ImageView imgTipoMed;

        public MedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreMed);
            viewColor = itemView.findViewById(R.id.viewColor);
            imgTipoMed = itemView.findViewById(R.id.imgTipoMed);
        }
    }
}
