package com.pastillerodigital.cuidamedpill.controlador.adapters;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Horario;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MedicamentoCalendarioAdapter extends RecyclerView.Adapter<MedicamentoCalendarioAdapter.MedCalViewHolder> {

    private List<Medicamento> lista;
    private Calendar fecha;
    private OnItemClickListener listener;

    private List<Medicamento> medsExpandida = new ArrayList<>();
    private List<String> horasExpandida = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(Medicamento medicamento);
    }

    public MedicamentoCalendarioAdapter(List<Medicamento> lista, Calendar fecha, OnItemClickListener listener) {
        this.lista = lista;
        this.fecha = fecha;
        this.listener = listener;
    }


    @NonNull
    @Override
    public MedCalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicamento_calendario, parent, false);
        return new MedCalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedCalViewHolder holder, int position) {
        Medicamento med = lista.get(position);

        holder.tvNombre.setText(med.getNombreMed());

        // Icono y color
        TipoMed tipoMed = TipoMed.tipoMedFromString(med.getTipoMedStr());
        holder.imgTipoMed.setImageResource(tipoMed.getDrawableRes());
        int colorResId = holder.itemView.getContext().getResources().getIdentifier(med.getColorSimb(), "color", holder.itemView.getContext().getPackageName());
        int color = ContextCompat.getColor(holder.itemView.getContext(), colorResId);

        Drawable drawable = ContextCompat.getDrawable(holder.itemView.getContext(), tipoMed.getDrawableRes());
        if(drawable != null){
            if(drawable instanceof LayerDrawable){
                LayerDrawable layerDrawable = (LayerDrawable) drawable;
                Drawable capaColor = layerDrawable.findDrawableByLayerId(tipoMed.getDrawableResColoreable());
                if(capaColor != null){
                    capaColor.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
                holder.imgTipoMed.setImageDrawable(layerDrawable);
            } else {
                drawable = drawable.mutate();
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                holder.imgTipoMed.setImageDrawable(drawable);
            }
        }

        // Horarios del día
        Horario horario = med.getHorario();
        holder.llHoras.removeAllViews(); // Limpiar antes de reutilizar la vista

        if (horario != null) {
            List<String> horas = horario.getHorasDiaStr(fecha); // Lista HH:mm

            for (String hora : horas) {
                String estado = "<estado>"; // todo poner bien cuando haya ingestas
                //todo mostrar momento si coincide la hora con algun momento
                TextView tvHoraEstado = new TextView(holder.itemView.getContext());
                tvHoraEstado.setText(hora + " - " + estado);
                holder.llHoras.addView(tvHoraEstado);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if(listener != null) listener.onItemClick(med);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class MedCalViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTipoMed;
        TextView tvNombre;
        LinearLayout llHoras;

        public MedCalViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTipoMed = itemView.findViewById(R.id.imgTipoMed);
            tvNombre = itemView.findViewById(R.id.tvNombreMed);
            llHoras = itemView.findViewById(R.id.llHoras);
        }
    }
}