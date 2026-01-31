package com.pastillerodigital.cuidamedpill.controlador.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioAsistido;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

public class AsistidosAdapter extends RecyclerView.Adapter<AsistidosAdapter.AsistidoVH>{
    private List<UsuarioAsistido> lista;
    private OnClickListener listener;
    private String uidSupervisando = null; // id del asistido actualmente supervisado

    public interface OnClickListener {
        void onSupervisar(UsuarioAsistido ua);
        void onDejarDeSupervisar();
        void onEditarPerfil(UsuarioAsistido ua);
        void onBorrarCuenta(UsuarioAsistido ua);
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
        //Cargar foto de perfil:
        String nombreDrawable = ua.getFotoPerfil();
        int resId = holder.itemView.getContext().getResources().getIdentifier(nombreDrawable, Constantes.RES_TIPO,
                        holder.itemView.getContext().getPackageName());

        if (resId != 0) {
            holder.imgFoto.setImageResource(resId);
        } else {
            holder.imgFoto.setImageResource(R.drawable.usuario_fotoperfil_default);
        }

        boolean estaSupervisando = ua.getId().equals(uidSupervisando);
        holder.btnSupervisar.setText(estaSupervisando ? "Dejar de supervisar" : "Supervisar");
        holder.layoutOpciones.setVisibility(estaSupervisando ? View.VISIBLE : View.GONE);

        holder.btnSupervisar.setOnClickListener(v -> {
            if (estaSupervisando) { //si estaba supervisando, deja de hacerlo
                uidSupervisando = null;
                listener.onDejarDeSupervisar();
            } else {
                uidSupervisando = ua.getId();
                listener.onSupervisar(ua);
            }
            notifyDataSetChanged();
        });

        holder.btnEditarPerfil.setOnClickListener(v -> listener.onEditarPerfil(ua));
        holder.btnBorrarCuenta.setOnClickListener(v -> listener.onBorrarCuenta(ua));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class AsistidoVH extends RecyclerView.ViewHolder {
        ImageView imgFoto;
        TextView tvNombre;
        Button btnSupervisar, btnEditarPerfil, btnBorrarCuenta;
        LinearLayout layoutOpciones;

        public AsistidoVH(@NonNull View itemView) {
            super(itemView);
            imgFoto = itemView.findViewById(R.id.imgFotoAsistido);
            tvNombre = itemView.findViewById(R.id.tvNombreAsistido);
            btnSupervisar = itemView.findViewById(R.id.btnSupervisar);
            layoutOpciones = itemView.findViewById(R.id.layoutOpciones);
            btnEditarPerfil = itemView.findViewById(R.id.btnEditarPerfilAsist);
            btnBorrarCuenta = itemView.findViewById(R.id.btnEliminarCuentaAsist);
        }
    }
}
