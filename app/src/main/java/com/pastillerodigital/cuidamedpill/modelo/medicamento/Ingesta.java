package com.pastillerodigital.cuidamedpill.modelo.medicamento;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.Persistible;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class Ingesta implements Persistible {

    private Timestamp fechaProgramada;
    private Timestamp fechaIngesta;
    private String estadoIngestaStr;
    private String notas;
    @Exclude
    private EstadoIngesta estadoIngesta;
    @Exclude
    private String idIng;
    @Exclude
    private Medicamento med; //guardo el medicamento al que pertenece por simplicidad

    public Ingesta(){}

    public Ingesta(Timestamp fechaProgramada, Timestamp fechaIngesta, String estadoIngestaStr,
                   Medicamento med, String notas) {
        this.fechaProgramada = fechaProgramada;
        this.fechaIngesta = fechaIngesta;
        this.estadoIngestaStr = estadoIngestaStr;
        this.estadoIngesta = EstadoIngesta.estadoIngestaFromString(estadoIngestaStr);
        this.med = med;
        this.notas = notas;
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

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
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
    @Exclude
    public Medicamento getMed() {
        return med;
    }
    @Exclude
    public void setMed(Medicamento med) {
        this.med = med;
    }

    public static Ingesta doctoObj(DocumentSnapshot doc){
        Ingesta ingesta = new Ingesta();
        ingesta.setId(doc.getId());

        ingesta.setEstadoIngestaStr(doc.getString(Constantes.ING_ESTADOINGESTASTR));
        ingesta.setEstadoIngesta(EstadoIngesta.estadoIngestaFromString(ingesta.getEstadoIngestaStr()));

        ingesta.setFechaProgramada(doc.getTimestamp(Constantes.ING_FECHAPROGRAMADA));
        ingesta.setFechaIngesta(doc.getTimestamp(Constantes.ING_FECHAINGESTA));

        ingesta.setNotas(doc.getString(Constantes.ING_NOTAS));

        return ingesta;
    }
}
