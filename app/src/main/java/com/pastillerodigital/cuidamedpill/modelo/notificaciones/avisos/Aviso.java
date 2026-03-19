package com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.Persistible;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoAviso;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class Aviso implements Persistible {
    private String tipoAvisoStr;
    private String titulo;
    private String mensaje;
    private Timestamp fechaProgramada;
    private boolean leido;
    private boolean notiMostrada;
    private String medId;
    private String uDestId; //por si nos dirijimos a otro usuario
    private String uOrigId; // generador del aviso (asistido util)

    // ----- Campo no persistente -----

    @Exclude
    private String idAviso;
    @Exclude
    private Medicamento med;
    @Exclude
    private TipoAviso tipoAviso;

    public Aviso() {}// Necesario para Firebase

    public Aviso(TipoAviso tipo, String titulo, String mensaje, String medicamentoId, Timestamp fecha) {

        this.tipoAvisoStr = tipo.toString();
        this.tipoAviso = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fechaProgramada = fecha;
        this.leido = false;
        this.notiMostrada = false;
        this.medId = medicamentoId;
    }

    @Override
    @Exclude
    public String getId() {
        return idAviso;
    }

    @Override
    @Exclude
    public void setId(String id) {
        this.idAviso = id;
    }

    public String getTipoAvisoStr() {
        return tipoAvisoStr;
    }

    public void setTipoAvisoStr(String tipoAvisoStr) {
        this.tipoAvisoStr = tipoAvisoStr;
    }

    @Exclude
    public TipoAviso getTipoAviso() {
        return TipoAviso.fromString(tipoAvisoStr);
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Timestamp getFechaProgramada() {
        return fechaProgramada;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public void setFechaProgramada(Timestamp fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public void setMedId(String medId) {
        this.medId = medId;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }

    public boolean isNotiMostrada() {
        return notiMostrada;
    }

    public void setNotiMostrada(boolean notiMostrada) {
        this.notiMostrada = notiMostrada;
    }

    public String getMedId() {
        return medId;
    }

    public String getuDestId() {
        return uDestId;
    }

    public void setuDestId(String uDestId) {
        this.uDestId = uDestId;
    }

    public String getuOrigId() {
        return uOrigId;
    }

    public void setuOrigId(String uOrigId) {
        this.uOrigId = uOrigId;
    }

    @Exclude
    public Medicamento getMed() {
        return med;
    }

    @Exclude
    public void setMed(Medicamento med) {
        this.med = med;
    }

    public static Aviso doctoObj(DocumentSnapshot doc){
        Aviso aviso = new Aviso();
        aviso.setId(doc.getId());

        //Campos obligatorios
        aviso.setTipoAvisoStr(doc.getString(Constantes.AVISO_TIPOAVISOSTR));
        aviso.titulo = doc.getString(Constantes.AVISO_TITULO);
        aviso.mensaje = doc.getString(Constantes.AVISO_MENSAJE);
        aviso.medId = doc.getString(Constantes.AVISO_MEDID);
        aviso.uDestId = doc.getString(Constantes.AVISO_UDESTID);
        aviso.uOrigId = doc.getString(Constantes.AVISO_UORIGID);

        Timestamp fecha = doc.getTimestamp(Constantes.AVISO_FECHACREACION);
        aviso.fechaProgramada = (fecha != null) ? fecha : Timestamp.now();

        Boolean leido = doc.getBoolean(Constantes.AVISO_LEIDO);
        aviso.leido = (leido != null) ? leido : false;

        Boolean notifMostrada = doc.getBoolean(Constantes.AVISO_NOTIMOSTRADA);
        aviso.notiMostrada = (notifMostrada != null) ? notifMostrada : false;

        aviso.tipoAviso = TipoAviso.fromString(aviso.getTipoAvisoStr());

        return aviso;
    }

}
