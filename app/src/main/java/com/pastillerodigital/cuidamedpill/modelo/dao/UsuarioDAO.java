package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioAsistido;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioEstandar;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Override
    public Usuario docToObj(DocumentSnapshot doc) {
        return Usuario.doctoObj(doc);
    }

    //-------------FUNCIONES GET
    @Override
    public void get(String id, OnDataLoadedCallback<Usuario> callback) {
        db.collection(collectionName)
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Usuario u = docToObj(documentSnapshot);

                    //Lista de medicamentos asociados
                    //todo get lista medicamentos (llamar funcion y en el onsucces se guarda)
                    //todo mirar qué método de obtener usuarios usas (habrá que reescribir)


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
     * Devuelve simplemente el objeto de la colección usuarios, sin entrar en las subcolecciones
     * que pueda tener. No siempre se necesitan las subcolecciones entonces lo hacemos más eficiente.
     * @param id
     * @param callback
     */
    public void getBasic(String id, OnDataLoadedCallback<Usuario> callback){
        db.collection(collectionName)
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Usuario u = docToObj(documentSnapshot);
                    //Obtenemos los usuarios (básicos) asistidos en caso de ser tutor
                    if(u.getTipoUsuario().equals(TipoUsuario.ESTANDAR)){
                        UsuarioEstandar ue = (UsuarioEstandar) u;
                        getySetUsrsAsist(ue, callback);
                    }
                    else{ callback.onSuccess(u);}
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {callback.onFailure(e);}
                });
    }

    /**
    Devuelve un objeto usuario a partir del documento devuelto por firebase
    Imprescindible que los strings marcados coincidan exactamente con la base de datos
     */


    /**
     * A partir del parámetro único del objeto devuelve el id en vez de devolver el objeto entero
     * Se usará para ver si el objeto existe o no.
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
    public void getBasicWithParameter(String paramBD, Object param, OnDataLoadedCallback<Usuario> callback){
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
                        Usuario u = docToObj(doc);
                        if(u.getTipoUsuario().equals(TipoUsuario.ESTANDAR)){
                            UsuarioEstandar ue = (UsuarioEstandar) u;
                            getySetUsrsAsist(ue, callback);
                        }
                        else{ callback.onSuccess(u);}
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
     * Obtiene una lista de usuarios asistidos a partir de una lista de ids de usuarios
     * asistidos asignados a un tutor.
     * @param ids
     * @param callback
     */
    public void getListAsistAsig(List<String> ids, OnDataLoadedCallback<List<UsuarioAsistido>> callback){
        if (ids == null || ids.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<UsuarioAsistido> usuarios = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(ids.size()); //herramientas concurrentes porque
        //firebase es asíncrono

        for (String id : ids) {
            db.collection(collectionName)
                    .document(id)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            usuarios.add((UsuarioAsistido) docToObj(doc));
                        }
                        if (counter.decrementAndGet() == 0) {
                            callback.onSuccess(usuarios);
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.onFailure(e);
                    });
        }
    }

    /**
     * Función auxiliar que llama a la base de datos para obtener los usuarios asignados al tutor
     * @param ue
     * @param callback recibe usuario porque es una función auxiliar para dar claridad al código.
     *                 se llamará al obtener un usuario
     */
    //todo reescribir para sacar los medicamentos y más cosas
    private void getySetUsrsAsist(UsuarioEstandar ue, OnDataLoadedCallback<Usuario> callback){
        getListAsistAsig(ue.getIdUsrAsistAsig(), new OnDataLoadedCallback<List<UsuarioAsistido>>() {
            @Override
            public void onSuccess(List<UsuarioAsistido> asistidos) {
                ue.setUsrAsistidoAsig(asistidos);
                callback.onSuccess(ue);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    //------------OTRAS FUNCIONES DAO
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
