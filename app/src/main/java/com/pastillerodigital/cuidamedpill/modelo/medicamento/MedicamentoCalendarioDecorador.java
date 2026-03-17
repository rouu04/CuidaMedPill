package com.pastillerodigital.cuidamedpill.modelo.medicamento;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Calendar;
import java.util.List;

public class MedicamentoCalendarioDecorador implements DayViewDecorator {
    private final int color;
    private final List<Medicamento> medicamentos;

    public MedicamentoCalendarioDecorador(int color, List<Medicamento> medicamentos) {
        this.color = color;
        this.medicamentos = medicamentos;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        // Convertimos el CalendarDay de la librería a el Calendar de Java
        Calendar cal = Calendar.getInstance();
        cal.set(day.getYear(), day.getMonth() - 1, day.getDay());

        for (Medicamento med : medicamentos) {
            if (med.hayIngestaDia(cal)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void decorate(DayViewFacade view) {
        // Añade un punto debajo del número (tamaño 8px, color personalizado)
        view.addSpan(new DotSpan(8, color));
    }
}