package com.pastillerodigital.cuidamedpill.modelo.medicamento;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.Persistible;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Horario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

/**
 * Medicamentos
 * Numerosos atributos son opcionales
 */
public class Medicamento implements Persistible {

    //Atributos que aparecerán en la base de datos
    private String nombreMed;
    private String colorSimb;
    private float pauta;
    private TipoMed tipoMed;

    //Opcionales:
    private Timestamp fechaCad;
    private Timestamp fechaFin;
    private int nMedRestantes; //medicinas restantes que le quedan al usuario
    private Horario horario; //todo revisar como se ve en firebase

    //Atributos que NO estarán en firebase directamente pero que el objeto almacenará.
    @Exclude
    private String idMed;

    public Medicamento(){}

    public Medicamento(String colorSimb, float pauta, TipoMed tipoM, Timestamp fechaCad,
                       String nombreM, Timestamp fechaFin, int nMedRestantes, Horario horario, String idM) {
        this.colorSimb = colorSimb;
        this.pauta = pauta;
        this.tipoMed = tipoM;
        this.fechaCad = fechaCad;
        this.nombreMed = nombreM;
        this.fechaFin = fechaFin;
        this.nMedRestantes = nMedRestantes;
        this.horario = horario;
        this.idMed = idM;
    }

    public String getNombreMed() {
        return nombreMed;
    }

    public void setNombreMed(String nombreMed) {
        this.nombreMed = nombreMed;
    }

    public String getColorSimb() {
        return colorSimb;
    }

    public void setColorSimb(String colorSimb) {
        this.colorSimb = colorSimb;
    }

    public float getPauta() {
        return pauta;
    }

    public void setPauta(float pauta) {
        this.pauta = pauta;
    }

    public TipoMed getTipoMed() {
        return tipoMed;
    }

    public void setTipoMed(TipoMed tipoMed) {
        this.tipoMed = tipoMed;
    }

    public Timestamp getFechaCad() {
        return fechaCad;
    }

    public void setFechaCad(Timestamp fechaCad) {
        this.fechaCad = fechaCad;
    }

    public Timestamp getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Timestamp fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getnMedRestantes() {
        return nMedRestantes;
    }

    public void setnMedRestantes(int nMedRestantes) {
        this.nMedRestantes = nMedRestantes;
    }

    public Horario getHorario() {
        return horario;
    }

    public void setHorario(Horario horario) {
        this.horario = horario;
    }

    @Exclude
    @Override
    public String getId() {
        return idMed;
    }

    @Exclude
    @Override
    public void setId(String idM) {
        this.idMed = idM;
    }

    public static Medicamento doctoObj(DocumentSnapshot doc){
        Medicamento med = new Medicamento();

        med.setNombreMed(doc.getString(Constantes.MED_NOMBREMED));
        med.setColorSimb(doc.getString(Constantes.MED_COLORSIMB));
        med.setPauta(doc.getDouble(Constantes.MED_PAUTA).floatValue());
        med.setTipoMed(TipoMed.tipoMedFromString(doc.getString(Constantes.MED_TIPOMED)));

        med.setId(doc.getId());

        //Atributos opcionales:
        med.setFechaCad(doc.getTimestamp(Constantes.MED_FECHACAD));
        med.setFechaFin(doc.getTimestamp(Constantes.MED_FECHAFIN));
        Long nCajasMed = doc.getLong(Constantes.MED_NMEDRESTANTES);
        if (nCajasMed != null) med.setnMedRestantes(nCajasMed.intValue());
        else med.setnMedRestantes(-1);

        med.setHorario(doc.get(Constantes.MED_HORARIO, Horario.class)); //todo sino funciona revisar que esto es problematico


        return med;
    }



}
