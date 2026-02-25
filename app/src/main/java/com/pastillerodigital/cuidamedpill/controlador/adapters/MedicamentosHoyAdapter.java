package com.pastillerodigital.cuidamedpill.controlador.adapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.MedHorasDisplay;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;

import java.util.List;

public class MedicamentosHoyAdapter extends RecyclerView.Adapter<MedicamentosHoyAdapter.ViewHolder> {

    /**
     * Clase interna auxiliar para simplificar la ui
     */

    public interface OnClickListener {
        void onItemClick(MedHorasDisplay medHora);
        void onCheckClick(MedHorasDisplay medHora);
    }

    private List<MedHorasDisplay> lista;
    private OnClickListener listener;

    public MedicamentosHoyAdapter(List<MedHorasDisplay> lista, OnClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicamento_hoy, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MedHorasDisplay item = lista.get(position);
        Medicamento med = item.getMedicamento();

        holder.tvNombre.setText(med.getNombreMed());
        holder.tvHora.setText(item.getHora());

        //todo cambios en funcion estado

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

        holder.btnCheck.setOnClickListener(v -> {

            /*
             AQUÍ irá:
             - crear Ingesta
             - actualizar estado
             - animación check
             */
        });
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void update(List<MedHorasDisplay> nueva) {
        lista = nueva;
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre;
        TextView tvHora;
        ImageView imgTipoMed;
        MaterialButton btnCheck;

        public ViewHolder(View v) {
            super(v);
            tvNombre = v.findViewById(R.id.tvNombreMedHoy);
            tvHora = v.findViewById(R.id.tvHora);
            imgTipoMed = v.findViewById(R.id.imgTipoMedHoy);
            btnCheck = v.findViewById(R.id.btnCheck);
        }
    }
}
