package com.pastillerodigital.cuidamedpill.controlador.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pastillerodigital.cuidamedpill.R;
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

        //todo revisar color simbolo
        try {
            int color = Color.parseColor(med.getColorSimb());
            ((GradientDrawable) holder.viewColor.getBackground()).setColor(color);
        } catch (Exception e) {
            holder.viewColor.setBackgroundColor(Color.GRAY);
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

        public MedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreMed);
            viewColor = itemView.findViewById(R.id.viewColor);
        }
    }
}
