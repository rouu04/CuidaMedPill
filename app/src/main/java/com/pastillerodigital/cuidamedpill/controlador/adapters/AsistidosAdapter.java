package com.pastillerodigital.cuidamedpill.controlador.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioAsistido;

import java.util.List;

public class AsistidosAdapter extends RecyclerView.Adapter<AsistidosAdapter.AsistidoVH>{
    private List<UsuarioAsistido> lista;
    private OnClickListener listener;

    public interface OnClickListener {
        void onClick(UsuarioAsistido ua);
    }

    public AsistidosAdapter(List<UsuarioAsistido> lista, OnClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AsistidoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asistido, parent, false);
        return new AsistidoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AsistidoVH holder, int position) {
        UsuarioAsistido ua = lista.get(position);
        holder.tvNombre.setText(ua.getAliasU());
        // holder.imgFoto.setImageResource(...) o Glide
        holder.btnSupervisar.setOnClickListener(v -> listener.onClick(ua));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class AsistidoVH extends RecyclerView.ViewHolder {
        ImageView imgFoto;
        TextView tvNombre;
        Button btnSupervisar;

        public AsistidoVH(@NonNull View itemView) {
            super(itemView);
            imgFoto = itemView.findViewById(R.id.imgFotoAsistido);
            tvNombre = itemView.findViewById(R.id.tvNombreAsistido);
            btnSupervisar = itemView.findViewById(R.id.btnSupervisar);
        }
    }
}
