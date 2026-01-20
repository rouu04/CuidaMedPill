package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.firebase.firestore.DocumentSnapshot;
import com.pastillerodigital.cuidamedpill.modelo.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

import java.util.List;

/*
Funciones de acceso a la base de datos de usuarios
 */
public class UsuarioDAO extends AbstractDAO<Usuario>{

    public UsuarioDAO(){
        super();
        setCollectionName();
    }
    @Override
    protected void setCollectionName() {
        this.collectionName = Constantes.COLLECTION_USUARIOS;
    }

    @Override
    public void get(String id, OnDataLoadedCallback<Usuario> callback) {
        db.collection(collectionName)
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Usuario u = docToObj(documentSnapshot);
                    //Hay que asignar los parámetros que no están directamente en firebase
                    u.setId(id);
                    u.setTipoUsuario(TipoUsuario.tipoUsrFromString(u.getTipoUsuarioStr()));

                    //Lista de medicamentos asociados
                    //todo get lista medicamentos (llamar funcion y en el onsucces se guarda)

                })
                .addOnFailureListener(callback::onFailure);
    }

    /*
    Devuelve un objeto usuario a partir del documento devuelto por firebase
    Imprescindible que los strings marcados coincidan exactamente con la base de datos
     */
    @Override
    public Usuario docToObj(DocumentSnapshot doc) {
        Usuario u = new Usuario();
        u.setNombreU(doc.getString(Constantes.USUARIO_NOMBRE));
        u.setTelefono(doc.getString(Constantes.USUARIO_TELEFONO));
        u.setTipoUsuarioStr(doc.getString(Constantes.USUARIO_TIPOUSR));
        u.setFotoURL(doc.getString(Constantes.USUARIO_FOTO));
        u.setMedListStr((List<String>) doc.get(Constantes.USUARIO_MEDLISTSTR));

        return u;
    }


}
