package com.pastillerodigital.cuidamedpill.modelo.medicamento.horario;

public class Hora implements Comparable<Hora>{

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

}
