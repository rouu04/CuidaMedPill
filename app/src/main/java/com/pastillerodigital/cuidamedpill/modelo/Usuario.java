package com.pastillerodigital.cuidamedpill.modelo;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;

import java.util.List;

public class Usuario implements Persistible {

    public Usuario(){

    }

    //Atributos que aparecerán en la base de datos
    private String nombreU;
    private String telefono;
    private String fotoURL;
    private String tipoUsuarioStr;
    private List<String> medAsigId; //lista de ids de medicamentos de este usuario

    //Atributos que NO quiero que estén en firebase directamente pero son necesarios para el objeto
    @DocumentId
    private String idU; //indica id del documento
    @Exclude
    private TipoUsuario tipoUsuario;
    @Exclude
    private List<Medicamento> medList;

    //GETTERS Y SETTERS

    public String getNombreU() {
        return nombreU;
    }

    public void setNombreU(String nombreU) {
        this.nombreU = nombreU;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }


    public String getFotoURL() {
        return fotoURL;
    }

    public void setFotoURL(String fotoURL) {
        this.fotoURL = fotoURL;
    }

    public String getTipoUsuarioStr() {
        return tipoUsuarioStr;
    }

    public void setTipoUsuarioStr(String tipoUsuarioStr) {
        this.tipoUsuarioStr = tipoUsuarioStr;
    }

    public List<String> getMedListStr() {
        return medAsigId;
    }

    public void setMedListStr(List<String> medAsigId) {
        this.medAsigId = medAsigId;
    }

    @DocumentId
    @Override
    public String getId() {
        return idU;
    }
    @DocumentId
    @Override
    public void setId(String id) {
        this.idU = id;
    }
    @Exclude
    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }
    @Exclude
    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
    @Exclude
    public List<Medicamento> getMedList() {
        return medList;
    }
    @Exclude
    public void setMedList(List<Medicamento> medList) {
        this.medList = medList;
    }



}
