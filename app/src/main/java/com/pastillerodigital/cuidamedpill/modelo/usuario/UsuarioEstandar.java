package com.pastillerodigital.cuidamedpill.modelo.usuario;

import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;

import java.util.List;

/**
 * Usuario estándar recibe los servicios completos de la aplicación y además puede ser el tutor
 * de otros usuarios asistidos
 */
public class UsuarioEstandar extends Usuario{

    private List<String> idUsrAsistAsig; //ids de los usuarios asistidos a los que supervisará

    @Exclude
    private List<UsuarioAsistido> usrAsistidoAsig;

    public UsuarioEstandar(){
        super();
        this.tipoUsuario = TipoUsuario.ESTANDAR;
        this.tipoUsuarioStr = TipoUsuario.ESTANDAR.toString();
    }

    public List<String> getIdUsrAsistAsig() {
        return idUsrAsistAsig;
    }


    public void setIdUsrAsistAsig(List<String> idUsrAsistAsig) {
        this.idUsrAsistAsig = idUsrAsistAsig;
    }
    @Exclude
    public List<UsuarioAsistido> getUsrAsistidoAsig() {
        return usrAsistidoAsig;
    }
    @Exclude
    public void setUsrAsistidoAsig(List<UsuarioAsistido> usrAsistidoAsig) {
        this.usrAsistidoAsig = usrAsistidoAsig;
    }
}
