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

    private String tipoMedStr;

    //Opcionales:
    private Timestamp fechaCad;
    private Timestamp fechaFin;
    private int nMedRestantes; //medicinas restantes que le quedan al usuario
    private Horario horario; //todo revisar como se ve en firebase

    //Atributos que NO estarán en firebase directamente pero que el objeto almacenará.
    @Exclude
    private String idMed;
    @Exclude
    private TipoMed tipoMed;

    public Medicamento(){}

    public Medicamento(String colorSimb, String tipoMedStr, Timestamp fechaCad, String nombreM,
                       Timestamp fechaFin, int nMedRestantes, Horario horario, String idM) {
        this.colorSimb = colorSimb;
        this.tipoMedStr = tipoMedStr;
        this.tipoMed = TipoMed.tipoMedFromString(tipoMedStr);
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

    public String getTipoMedStr() {
        return tipoMedStr;
    }

    public void setTipoMedStr(String tipoMedStr) {
        this.tipoMedStr = tipoMedStr;
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
    @Exclude
    public TipoMed getTipoMed() {
        return tipoMed;
    }
    @Exclude
    public void setTipoMed(TipoMed tipoMed) {
        this.tipoMed = tipoMed;
    }

    public static Medicamento doctoObj(DocumentSnapshot doc){
        Medicamento med = new Medicamento();

        med.setNombreMed(doc.getString(Constantes.MED_NOMBREMED));
        med.setColorSimb(doc.getString(Constantes.MED_COLORSIMB));
        med.setTipoMedStr(doc.getString(Constantes.MED_TIPOMEDSTR));
        med.setTipoMed(TipoMed.tipoMedFromString(med.getTipoMedStr()));

        med.setId(doc.getId());

        //Atributos opcionales:
        med.setFechaCad(doc.getTimestamp(Constantes.MED_FECHACAD));
        med.setFechaFin(doc.getTimestamp(Constantes.MED_FECHAFIN));
        Long nCajasMed = doc.getLong(Constantes.MED_NMEDRESTANTES);
        if (nCajasMed != null) med.setnMedRestantes(nCajasMed.intValue());
        else med.setnMedRestantes(-1);

        med.setHorario(doc.get(Constantes.MED_HORARIO, Horario.class));

        return med;
    }

}
