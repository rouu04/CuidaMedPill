package com.pastillerodigital.cuidamedpill.modelo.medicamento.horario;

import com.pastillerodigital.cuidamedpill.utils.Constantes;

import java.util.Calendar;
import java.util.Map;

public class Hora implements Comparable<Hora>{

    //En firebase:
    protected int hora;
    protected int min;

    public Hora(){}

    public Hora(int hora, int min) {
        this.hora = hora;
        this.min = min;
    }

    public int getHora() {
        return hora;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    /**
     * Para poder ordenar horas fácilmente
     * @param otra the object to be compared.
     * @return
     */
    @Override
    public int compareTo(Hora otra) {
        if(this.hora != otra.hora){
            return this.hora - otra.hora;
        }

        return this.min - otra.min;
    }


    public static Hora mapToObj(Map<String, Object> data) {
        if (data == null) return null;

        if (data.containsKey(Constantes.HORA_MOMENTODIASTR)) { //es un momento, delegamos
            return HoraMomentoDia.mapToObj(data);
        }

        Hora h = new Hora();
        h.setHora(((Long) data.get(Constantes.HORA_HORA)).intValue());
        h.setMin(((Long) data.get(Constantes.HORA_MIN)).intValue());
        return h;
    }

}
