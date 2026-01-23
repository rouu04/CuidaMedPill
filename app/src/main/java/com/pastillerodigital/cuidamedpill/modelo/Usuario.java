package com.pastillerodigital.cuidamedpill.modelo;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;

import java.util.List;

public class Usuario implements Persistible {

    public Usuario(){

    }

    //Atributos que aparecerán en la base de datos
    private String nombreUsuario;
    private String aliasU; //nombre (como se dirigirá la app)
    private String fotoURL;
    private String tipoUsuarioStr;
    private List<String> medAsigId; //lista de ids de medicamentos de este usuario

    private String passwordHash;
    private String salt;

    //Atributos que NO quiero que estén en firebase directamente pero son necesarios para el objeto
    @DocumentId
    private String idU; //indica id del documento
    @Exclude
    private TipoUsuario tipoUsuario;
    @Exclude
    private List<Medicamento> medList;
    @Exclude
    private String passwordPlano;

    //GETTERS Y SETTERS

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreU) {
        this.nombreUsuario = nombreU;
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

    public List<String> getMedAsigId() {
        return medAsigId;
    }

    public void setMedAsigId(List<String> medAsigId) {
        this.medAsigId = medAsigId;
    }

    public String getAliasU() {
        return aliasU;
    }

    public void setAliasU(String aliasU) {
        this.aliasU = aliasU;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
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
    @Exclude
    public String getPasswordPlano() {
        return passwordPlano;
    }
    @Exclude
    public void setPasswordPlano(String passwordPlano) {
        this.passwordPlano = passwordPlano;
    }
}
