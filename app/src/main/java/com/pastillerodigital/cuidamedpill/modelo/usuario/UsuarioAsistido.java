package com.pastillerodigital.cuidamedpill.modelo.usuario;

import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;

import java.util.List;

/**
 * Un usuario asistido es aquel que necesitará, como mínimo, un tutor (cualquier usuario estándar) para realizar
 * determinadas operaciones
 */
public class UsuarioAsistido extends Usuario{

    private List<String> idUsrTutoresAsig; //ids de los usuarios que le ayudarán en la app

    @Exclude
    private List<Usuario> usrTutoresAsig;
    public UsuarioAsistido(){
        super();
        this.tipoUsuario = TipoUsuario.ASISTIDO;
        this.tipoUsuarioStr = TipoUsuario.ASISTIDO.toString();
    }

    public List<String> getIdUsrTutoresAsig() {
        return idUsrTutoresAsig;
    }

    public void setIdUsrTutoresAsig(List<String> idUsrTutoresAsig) {
        this.idUsrTutoresAsig = idUsrTutoresAsig;
    }
    @Exclude
    public List<Usuario> getUsrTutoresAsig() {
        return usrTutoresAsig;
    }
    @Exclude
    public void setUsrTutoresAsig(List<Usuario> usrTutoresAsig) {
        this.usrTutoresAsig = usrTutoresAsig;
    }
}
