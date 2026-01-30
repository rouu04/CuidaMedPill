package com.pastillerodigital.cuidamedpill.modelo.usuario;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.Persistible;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.ConfNotificacionGeneral;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

import java.util.ArrayList;
import java.util.List;

public class Usuario implements Persistible {

    //Atributos que aparecerán en la base de datos
    private String nombreUsuario;
    private String aliasU; //nombre (como se dirigirá la app)
    private String fotoPerfil; //guardará el nombre del drawable
    protected String tipoUsuarioStr;
    private List<String> medAsigId = new ArrayList<>(); //lista de ids de medicamentos de este usuario

    private String passwordHash;
    private String salt;
    private ConfNotificacionGeneral confNotiGeneral;

    //Atributos que NO quiero que estén en firebase directamente pero son necesarios para el objeto
    @DocumentId
    private String idU; //indica id del documento
    @Exclude
    protected TipoUsuario tipoUsuario;
    @Exclude
    private List<Medicamento> medList = new ArrayList<>();
    @Exclude
    private String passwordPlano;

    public Usuario(){
        this.confNotiGeneral = new ConfNotificacionGeneral();
    }

    //GETTERS Y SETTERS

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreU) {
        this.nombreUsuario = nombreU;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
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

    /**
     * Devuelve el usuario sacado de la base de datos, se encarga de delegar a los hijos los campos y
     * asignar los campos comunes
     * @param doc
     * @return
     */
    public static Usuario doctoObj(DocumentSnapshot doc) {
        String tipoStr = doc.getString(Constantes.USUARIO_TIPOUSR);
        TipoUsuario tipo = TipoUsuario.tipoUsrFromString(tipoStr);

        Usuario u;

        //Establece los campos de los hijos
        if (tipo == TipoUsuario.ASISTIDO) {
            u = UsuarioAsistido.doctoObj(doc); // delega al hijo
        } else {
            u = UsuarioEstandar.doctoObj(doc); // delega al hijo
        }

        // Campos comunes
        u.setId(doc.getId());
        u.setNombreUsuario(doc.getString(Constantes.USUARIO_NOMBREUSUARIO));
        u.setAliasU(doc.getString(Constantes.USUARIO_ALIAS));
        u.setTipoUsuarioStr(tipoStr);
        u.setTipoUsuario(tipo);
        u.setFotoPerfil(doc.getString(Constantes.USUARIO_FOTO));
        u.setMedAsigId((List<String>) doc.get(Constantes.USUARIO_MEDLISTSTR));
        u.setPasswordHash(doc.getString(Constantes.USUARIO_PASSWORDHASH));
        u.setSalt(doc.getString(Constantes.USUARIO_SALT));

        return u;
    }
}
