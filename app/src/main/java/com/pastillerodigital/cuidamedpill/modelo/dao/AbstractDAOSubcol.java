package com.pastillerodigital.cuidamedpill.modelo.dao;

import com.pastillerodigital.cuidamedpill.modelo.Persistible;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

import java.util.List;

/**
 * Clase del dao abstracta de una subcolección. Aunque la funcionalidad es parecida a la del AbstractDAO
 * es necesaria por firebase porque necesita saber la ubicación en las colecciones.
 */
public abstract class AbstractDAOSubcol<T extends Persistible> extends GeneralDAO<T> implements DAO<T> {

    protected String subColName;
    protected String idCollection; //el id del elemento de la colección padre
    public AbstractDAOSubcol(){
        super();
    }

    protected abstract void setSubColName();
    protected abstract void setIdCollection(String id);


    public abstract void getList(String idCollection, OnDataLoadedCallback<List<Medicamento>> callback);
    /**
     * Recoge lista de todos los items de forma básica de la coleccion padre con id idCollection
     * @param idCollection
     */
    public abstract void getListBasic(String idCollection, OnDataLoadedCallback<List<Medicamento>> callback);

    /**
     Función que añade un elemento a la coleccion. Cada objeto debería preguntar si existe en función
     de sus restricciones.
     */
    @Override
    public void add(T obj, OnOperationCallback callback) {
        db.collection(this.collectionName)
                .document(this.idCollection)
                .collection(this.subColName)
                .add(obj)
                .addOnSuccessListener(documentReference -> {
                    obj.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     Usamos set, que reemplaza el documento con el id por el nuevo que le pasamos
     */
    @Override
    public void edit(T nuevo, OnOperationCallback callback) {
        String id = nuevo.getId();
        if(id == null || id.isEmpty()){
            callback.onFailure(new Exception(Constantes.EX_ID_INVALIDO));
            return;
        }

        db.collection(collectionName)
                .document(this.idCollection)
                .collection(this.subColName)
                .document(id)
                .set(nuevo)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void delete(String id, OnOperationCallback callback) {
        if (id == null || id.isEmpty()) {
            callback.onFailure(new Exception(Constantes.EX_ID_INVALIDO));
            return;
        }

        db.collection(collectionName)
                .document(this.idCollection)
                .collection(this.subColName)
                .document(id)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

}
