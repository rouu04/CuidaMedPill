package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioAsistido;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioEstandar;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;

import java.util.ArrayList;
import java.util.List;

/**
Funciones de acceso a la base de datos de usuarios
 */
public class UsuarioDAO extends AbstractDAO<Usuario>{

    public UsuarioDAO(){
        super();
        setCollectionName();
    }

    //MÉTODOS SOBREESCRITOS DE LA CLASE ABSTRACTA
    @Override
    protected void setCollectionName() {
        this.collectionName = Constantes.COLLECTION_USUARIOS;
    }

    //todo reescribir para usuarioEstandar y usuarioAsistido
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


                    callback.onSuccess(u);
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
    }



    /**
    Devuelve un objeto usuario a partir del documento devuelto por firebase
    Imprescindible que los strings marcados coincidan exactamente con la base de datos
     todo reescribir para usuarios estandar y asistidos
     */
    @Override
    public Usuario docToObj(DocumentSnapshot doc) {
        Usuario u = new Usuario();
        u.setNombreUsuario(doc.getString(Constantes.USUARIO_NOMBREUSUARIO));
        u.setAliasU(doc.getString(Constantes.USUARIO_ALIAS));
        u.setTipoUsuarioStr(doc.getString(Constantes.USUARIO_TIPOUSR));
        u.setFotoPerfil((int) doc.get(Constantes.USUARIO_FOTO));
        u.setMedAsigId((List<String>) doc.get(Constantes.USUARIO_MEDLISTSTR));
        u.setPasswordHash(doc.getString(Constantes.USUARIO_PASSWORDHASH));
        u.setSalt(doc.getString(Constantes.USUARIO_SALT));

        return u;
    }

    /**
     * A partir del parámetro único del objeto devuelve el id
     * @param paramBD es el string del campo en la base de datos
     * @param param el parámetro a filtrar
     * @param callback
     */
    public void getIdWithParameter(String paramBD, Object param, OnDataLoadedCallback<String> callback){
        if (param == null) {
            callback.onSuccess(null);
            return;
        }

        db.collection(collectionName)
                .whereEqualTo(paramBD, param)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0); //primer y único documento de la consulta
                        callback.onSuccess(doc.getId()); //
                    } else {
                        callback.onSuccess(null); // Tiene éxito en la consulta (no da error)
                        //pero no existe
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onFailure(new Exception(Mensajes.EX_EXISTE));
                });
    }


    /**
    Obtiene objeto filtrando por un parámetro
     */
    public void getWithParameter(String paramBD, Object param, OnDataLoadedCallback<Usuario> callback){
        if (param == null) {
            callback.onSuccess(null);
            return;
        }

        db.collection(collectionName)
                .whereEqualTo(paramBD, param)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0); //primer y único documento de la consulta
                        Usuario usuario = docToObj(doc);
                        usuario.setId(doc.getId()); // Asignamos el ID del documento
                        //todo get lista de medicamentos
                        callback.onSuccess(usuario); // Devolvemos el usuario
                    } else {
                        callback.onSuccess(null); // Tiene éxito en la consulta (no da error)
                        //pero no existe
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onFailure(new Exception(Mensajes.EX_EXISTE));
                });
    }

    /**
     * Añade un usuario asistido a la aplicación, en consecuencia se actualizan las listas del tutor
     * @param ua
     * @param idue
     * @param callback
     */
    public void add(UsuarioAsistido ua, String idue, OnOperationCallback callback){
        ua.addTutorAAsistido(idue);
        db.collection(collectionName)
                .add(ua)
                .addOnSuccessListener(documentReference -> {
                    ua.setId(documentReference.getId());
                    addAsistidoATutor(ua.getId(), idue, callback);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Añade un usuario asistido al usuario estándar, el usuario estándar será un nuevo tutor del
     * usuario asistido. Los cambios se guardan en la base de datos, el objeto se actualizará en cuando
     * el tutor realice cualquier app de la aplicación.
     * todo revisar eso (mirar en docs lo de los listeners)
     * @param idua
     * @param idue
     * @param callback
     */
    public void addAsistidoATutor(String idua, String idue, OnOperationCallback callback){
        //Actualiza el campo añadiendo el id (sin duplicados)
        db.collection(collectionName)
                .document(idue)
                .update( Constantes.USUARIO_ESTANDAR_IDUSRASIST, FieldValue.arrayUnion(idua))
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

}
