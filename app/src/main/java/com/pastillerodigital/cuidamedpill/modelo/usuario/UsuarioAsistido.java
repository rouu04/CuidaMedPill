package com.pastillerodigital.cuidamedpill.modelo.usuario;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

/**
 * Un usuario asistido es aquel que necesitará, como mínimo, un tutor (cualquier usuario estándar) para realizar
 * determinadas operaciones
 */
public class UsuarioAsistido extends Usuario{

    private List<String> idUsrTutoresAsig = new ArrayList<>(); //ids de los usuarios que le ayudarán en la app

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

    public void addTutorAAsistido(String  idue){
        this.idUsrTutoresAsig.add(idue);
    }

    public static UsuarioAsistido doctoObj(DocumentSnapshot doc) {
        UsuarioAsistido ua = new UsuarioAsistido();
        // Campos específicos
        ua.setIdUsrTutoresAsig((List<String>) doc.get(Constantes.USUARIO_ASIST_IDUSRTUTORESASIG));
        return ua;
    }
}
