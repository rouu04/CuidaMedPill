package com.pastillerodigital.cuidamedpill.controlador.adapters;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
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

import com.google.firebase.Timestamp;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Ingesta;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Horario;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MedicamentoCalendarioAdapter extends RecyclerView.Adapter<MedicamentoCalendarioAdapter.MedCalViewHolder> {

    private List<Medicamento> lista;
    private Calendar fecha;
    private OnItemClickListener listener;

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
        holder.llHoras.removeAllViews();
        List<Ingesta> ingestasDia = med.getIngestasPorDia(fecha);

        // Ordenar por hora ascendente
        ingestasDia.sort((i1, i2) -> {
            Timestamp t1 = (i1.getFechaProgramada() != null) ? i1.getFechaProgramada() : i1.getFechaIngesta();
            Timestamp t2 = (i2.getFechaProgramada() != null) ? i2.getFechaProgramada() : i2.getFechaIngesta();
            return t1.compareTo(t2); // compara por fecha/hora
        });

        for (Ingesta ing : ingestasDia) {
            Calendar cal = Calendar.getInstance();
            Timestamp fechaBase = (ing.getFechaProgramada() != null) ? ing.getFechaProgramada() : ing.getFechaIngesta();
            cal.setTime(fechaBase.toDate());

            String horaStr = String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            String estado = ing.getEstadoIngestaStr();
            String nota = ing.getNotas();

            // Contenedor horizontal por ingesta
            LinearLayout container = new LinearLayout(holder.itemView.getContext());
            container.setOrientation(LinearLayout.HORIZONTAL);
            container.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            container.setPadding(0, 2, 0, 2);

            // Hora + estado
            TextView tvHoraEstado = new TextView(holder.itemView.getContext());
            tvHoraEstado.setText(String.format("%s - %s", horaStr, estado));
            tvHoraEstado.setTextSize(14);
            tvHoraEstado.setTypeface(null, Typeface.BOLD);
            tvHoraEstado.setTextColor(colorSegunEstado(holder, estado));

            container.addView(tvHoraEstado);

            // Notas
            if(nota != null && !nota.isEmpty()){
                TextView tvNota = new TextView(holder.itemView.getContext());
                tvNota.setText(nota);
                tvNota.setTextSize(12);
                tvNota.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.md_on_surface));
                tvNota.setPadding(8,0,0,0);
                container.addView(tvNota);
            }

            holder.llHoras.addView(container);
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

    public void setFechaSeleccionada(Calendar nuevaFecha) {
        this.fecha = (Calendar) nuevaFecha.clone(); // clon para evitar mutaciones externas
        notifyDataSetChanged();
    }

    private int colorSegunEstado(MedCalViewHolder holder, String estadoStr){
        EstadoIngesta estado = EstadoIngesta.estadoIngestaFromString(estadoStr);
        if (estado == null) {
            return ContextCompat.getColor(holder.itemView.getContext(), R.color.md_on_surface); // gris por defecto
        }

        switch (estado) {
            case TOMADA:
                return ContextCompat.getColor(holder.itemView.getContext(), R.color.estadoTomado);
            case PENDIENTE:
                return ContextCompat.getColor(holder.itemView.getContext(), R.color.estadoPendiente);
            case OLVIDO:
            case RETRASO:
                return ContextCompat.getColor(holder.itemView.getContext(), R.color.estadoRetraso);
            case NO_PROGRAMADA:
                return ContextCompat.getColor(holder.itemView.getContext(), R.color.estadoNoProgramado);
            default:
                return ContextCompat.getColor(holder.itemView.getContext(), R.color.md_on_surface);
        }
    }
}