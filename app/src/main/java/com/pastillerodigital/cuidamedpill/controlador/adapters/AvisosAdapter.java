package com.pastillerodigital.cuidamedpill.controlador.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos.Aviso;

import java.util.List;

public class AvisosAdapter extends RecyclerView.Adapter<AvisosAdapter.AvisoVH> {

    public interface OnAvisoClickListener {
        void onIgnorar(Aviso aviso);
        void onResolver(Aviso aviso);
    }

    private List<Aviso> avisos;
    private OnAvisoClickListener listener;

    public AvisosAdapter(List<Aviso> avisos, OnAvisoClickListener listener) {
        this.avisos = avisos;
        this.listener = listener;
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

        // Cambiar color según tipo de aviso
        switch (aviso.getTipoAviso()) {
            case CADUCIDAD:
                holder.card.setCardBackgroundColor(Color.parseColor("#E53935")); // rojo
                break;
            case COMPRA:
                holder.card.setCardBackgroundColor(Color.parseColor("#FDD835")); // amarillo
                break;
            case FIN_TRATAMIENTO:
                holder.card.setCardBackgroundColor(Color.parseColor("#43A047")); // verde
                break;
            default:
                holder.card.setCardBackgroundColor(Color.LTGRAY);
        }

        holder.btnIgnorar.setOnClickListener(v -> {
            if(listener != null) listener.onIgnorar(aviso);
        });

        holder.btnResolver.setOnClickListener(v -> {
            if(listener != null) listener.onResolver(aviso);
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

        public AvisoVH(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloAviso);
            tvMensaje = itemView.findViewById(R.id.tvMensajeAviso);
            btnIgnorar = itemView.findViewById(R.id.btnIgnorar);
            btnResolver = itemView.findViewById(R.id.btnResolver);
            card = (CardView) itemView;
        }
    }
}
