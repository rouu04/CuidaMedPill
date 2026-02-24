package com.pastillerodigital.cuidamedpill.modelo.medicamento;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.Persistible;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;

public class Ingesta implements Persistible {

    private Timestamp fechaProgramada;
    private Timestamp fechaIngesta;
    private String estadoIngestaStr;
    @Exclude
    private EstadoIngesta estadoIngesta;
    @Exclude
    private String idIng;

    public Ingesta(){}

    public Ingesta(Timestamp fechaProgramada, Timestamp fechaIngesta, String estadoIngestaStr) {
        this.fechaProgramada = fechaProgramada;
        this.fechaIngesta = fechaIngesta;
        this.estadoIngestaStr = estadoIngestaStr;
        this.estadoIngesta = EstadoIngesta.estadoIngestaFromString(estadoIngestaStr);
    }

    public Timestamp getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(Timestamp fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public Timestamp getFechaIngesta() {
        return fechaIngesta;
    }

    public void setFechaIngesta(Timestamp fechaIngesta) {
        this.fechaIngesta = fechaIngesta;
    }

    public String getEstadoIngestaStr() {
        return estadoIngestaStr;
    }

    public void setEstadoIngestaStr(String estadoIngestaStr) {
        this.estadoIngestaStr = estadoIngestaStr;
    }
    @Exclude
    public EstadoIngesta getEstadoIngesta() {
        return estadoIngesta;
    }
    @Exclude
    public void setEstadoIngesta(EstadoIngesta estadoIngesta) {
        this.estadoIngesta = estadoIngesta;
    }

    @Override
    @Exclude
    public void setId(String id) {
        this.idIng = id;
    }

    @Override
    @Exclude
    public String getId() {
        return this.idIng;
    }
}
