package com.pastillerodigital.cuidamedpill.modelo.usuario;

import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;

import java.util.List;

/**
 * Usuario estándar recibe los servicios completos de la aplicación y además puede ser el tutor
 * de otros usuarios asistidos
 */
public class UsuarioEstandar extends Usuario{

    private List<String> idUsrAsistidoAsig; //ids de los usuarios asistidos a los que supervisará

    @Exclude
    private List<Usuario> usrAsistidoAsig;

    public UsuarioEstandar(){
        super();
        this.tipoUsuario = TipoUsuario.ESTANDAR;
        this.tipoUsuarioStr = TipoUsuario.ESTANDAR.toString();
    }

    public List<String> getIdUsrAsistidoAsig() {
        return idUsrAsistidoAsig;
    }


    public void setIdUsrAsistidoAsig(List<String> idUsrAsistidoAsig) {
        this.idUsrAsistidoAsig = idUsrAsistidoAsig;
    }
    @Exclude
    public List<Usuario> getUsrAsistidoAsig() {
        return usrAsistidoAsig;
    }
    @Exclude
    public void setUsrAsistidoAsig(List<Usuario> usrAsistidoAsig) {
        this.usrAsistidoAsig = usrAsistidoAsig;
    }
}
