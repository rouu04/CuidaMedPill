package com.pastillerodigital.cuidamedpill.modelo.usuario;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

import java.util.ArrayList;
import java.util.List;

/**
 * Usuario estándar recibe los servicios completos de la aplicación y además puede ser el tutor
 * de otros usuarios asistidos
 */
public class UsuarioEstandar extends Usuario{

    private List<String> idUsrAsistAsig = new ArrayList<>(); //ids de los usuarios asistidos a los que supervisará

    @Exclude
    private List<UsuarioAsistido> usrAsistidoAsig = new ArrayList<>();

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

    public static UsuarioEstandar doctoObj(DocumentSnapshot doc) {
        UsuarioEstandar ue = new UsuarioEstandar();
        ue.setIdUsrAsistAsig((List<String>) doc.get(Constantes.USUARIO_ESTANDAR_IDUSRASIST));
        return ue;
    }
}
