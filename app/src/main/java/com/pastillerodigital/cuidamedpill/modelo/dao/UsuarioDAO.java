package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.ConfNoti;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioAsistido;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioEstandar;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
Funciones de acceso a la base de datos de usuarios
 */
public class UsuarioDAO extends AbstractDAO<Usuario>{

    public UsuarioDAO(){
        super();
        this.path = new String[] {Constantes.COLLECTION_USUARIOS};
    }

    //MÉTODOS SOBREESCRITOS DE LA CLASE ABSTRACTA

    @Override
    public Usuario docToObj(DocumentSnapshot doc) {
        return Usuario.doctoObj(doc);
    }

    //-------------FUNCIONES GET
    @Override
    public void get(String id, OnDataLoadedCallback<Usuario> callback) {

    }

    /**
     * Devuelve simplemente el objeto de la colección usuarios, sin entrar en las subcolecciones
     * que pueda tener. No siempre se necesitan las subcolecciones entonces lo hacemos más eficiente.
     * @param id
     * @param callback
     */
    @Override
    public void getBasic(String id, OnDataLoadedCallback<Usuario> callback){
        getCollection()
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

        getCollection()
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

        getCollection()
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
            getCollection()
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
    private void getySetUsrsAsist(UsuarioEstandar ue, OnDataLoadedCallback<Usuario> callback){
        getListAsistAsig(ue.getIdUsrAsistAsig(), new OnDataLoadedCallback<List<UsuarioAsistido>>() {
            @Override
            public void onSuccess(List<UsuarioAsistido> asistidos) {
                ue.setUsrAsistidoAsig(asistidos);
                getySetUsrsNotificados(ue, callback);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    private void getySetUsrsNotificados(UsuarioEstandar ue, OnDataLoadedCallback<Usuario> callback){
        ConfNoti conf = ue.getConfNoti();
        List<String> idsNotificados = conf.getUsrsNotificadosId();

        if (idsNotificados == null || idsNotificados.isEmpty()) {
            conf.setUsrsNotificados(new ArrayList<>());
            callback.onSuccess(ue);
            return;
        }

        List<Usuario> usuarios = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(idsNotificados.size());
        AtomicBoolean fallo = new AtomicBoolean(false);

        for (String id : idsNotificados) {
            getCollection()
                    .document(id)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            usuarios.add(docToObj(doc)); // Obtenemos el objeto Usuario completo
                        }
                        if (counter.decrementAndGet() == 0) {
                            if (!fallo.get()) {
                                conf.setUsrsNotificados(usuarios);
                                callback.onSuccess(ue);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        fallo.set(true);
                        callback.onFailure(e);
                    });
        }
    }

    //------------OTRAS FUNCIONES DAO

    @Override
    public void add(Usuario obj, OnOperationCallback callback) {
        String newId = getCollection().document().getId(); // Genero ID primero para poder ponerlo en la config
        obj.setId(newId);
        obj.setConfNotiDefault(); //notificaciones default

        getCollection()
                .document(newId)
                .set(obj)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Añade un usuario asistido a la aplicación, en consecuencia se actualizan las listas del tutor
     * @param ua
     * @param idue
     * @param callback
     */
    public void add(UsuarioAsistido ua, String idue, OnOperationCallback callback){
        ua.addTutorAAsistido(idue);

        String newId = getCollection().document().getId();
        ua.setId(newId);
        ua.setConfNotiDefault();

        getCollection()
                .document(newId)
                .set(ua)
                .addOnSuccessListener(documentReference -> {
                    addAsistidoATutor(ua.getId(), idue, callback);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Añade un usuario asistido al usuario estándar, el usuario estándar será un nuevo tutor del
     * usuario asistido. Los cambios se guardan en la base de datos, el objeto se actualizará en cuando
     * el tutor realice cualquier acción de la aplicación.
     * @param idua
     * @param idue
     * @param callback
     */
    public void addAsistidoATutor(String idua, String idue, OnOperationCallback callback){
        //Actualiza el campo añadiendo el id (sin duplicados)
        getCollection()
                .document(idue)
                .update( Constantes.USUARIO_ESTANDAR_IDUSRASIST, FieldValue.arrayUnion(idua))
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }


    /**
     * Actualiza tanto la lista de asistido como la del tutor. Se usará cuando un tutor añada a un asistido que ya
     * existe
     * @param idua
     * @param idue
     * @param callback
     */
    public void vincularAsistATutor(String idua, String idue, OnOperationCallback callback){
        getCollection()
                .document(idua)
                .update( Constantes.USUARIO_ASIST_IDUSRTUTORESASIG, FieldValue.arrayUnion(idue))
                .addOnSuccessListener(v ->{
                    getCollection()
                            .document(idue)
                            .update( Constantes.USUARIO_ESTANDAR_IDUSRASIST, FieldValue.arrayUnion(idua))
                            .addOnSuccessListener(vo -> callback.onSuccess())
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Desvincula un asistido de un tutor
     * @param idua
     * @param idue
     * @param callback
     */
    public void desvincular(String idua, String idue, OnOperationCallback callback){
        getCollection()
                .document(idua)
                .update(Constantes.USUARIO_ASIST_IDUSRTUTORESASIG, FieldValue.arrayRemove(idue))
                .addOnSuccessListener(v -> {
                    getCollection()
                            .document(idue)
                            .update(Constantes.USUARIO_ESTANDAR_IDUSRASIST, FieldValue.arrayRemove(idua))
                            .addOnSuccessListener(vo -> callback.onSuccess())
                            .addOnFailureListener(callback::onFailure);

                })
                .addOnFailureListener(callback::onFailure);

    }

    /**
     * Al borrar un usuario estándar, si tiene usuarios asistidos a su cargo, hay que actualizar
     * que ya no estarán a su cargo. Si era el único, no podrá eliminarse.
     * @param ue
     * @param callback deuvelve true si se ha eliminado sin problemas, false si no puede eliminarse porque es
     *                 el único tutor
     */
    public void delete(UsuarioEstandar ue, OnDataLoadedCallback<Boolean> callback){
        List<UsuarioAsistido> listUa = ue.getUsrAsistidoAsig();

        if(listUa.isEmpty()) { //Se puede borrar directamente
            super.delete(ue.getId(), new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    callback.onSuccess(true);
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        }
        else{ //No puede borrarse si es el único tutor del asistido
            boolean borrable = true;
            for(UsuarioAsistido ua: listUa){
                if(ua.getIdUsrTutoresAsig().size() == 1) borrable = false;
            }
            if(borrable){
                super.delete(ue.getId(), new OnOperationCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }
            else{
                callback.onSuccess(false);
            }
        }
    }

    /**
     * Borra un usuario asistido, y en consecuencia lo borra también de las listas de sus tutores
     * @param ua
     * @param callback
     */
    public void delete(UsuarioAsistido ua, OnOperationCallback callback){
        List<String> idsTutores = ua.getIdUsrTutoresAsig();

        AtomicInteger counter = new AtomicInteger(idsTutores.size());
        AtomicBoolean fallo = new AtomicBoolean(false);

        for (String idTutor : idsTutores) {
            getCollection()
                    .document(idTutor)
                    .update(Constantes.USUARIO_ESTANDAR_IDUSRASIST, FieldValue.arrayRemove(ua.getId()))
                    .addOnSuccessListener(v -> {
                        if (counter.decrementAndGet() == 0) { // Todos los tutores actualizados, borramos el asistido
                            if (!fallo.get()) {
                                super.delete(ua.getId(), callback);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        fallo.set(true);
                        callback.onFailure(e);
                    });
        }
    }

}
