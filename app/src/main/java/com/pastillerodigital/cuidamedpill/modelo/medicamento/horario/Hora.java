package com.pastillerodigital.cuidamedpill.modelo.medicamento.horario;

public class Hora {

    //En firebase:
    private int hora;
    private int min;

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
}
