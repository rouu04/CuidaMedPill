package com.pastillerodigital.cuidamedpill.modelo.dao;
/*
Interfaz que define métodos de acceso a la base de datos de forma generalizada

Los métodos no pueden devolver nada porque firebase es asíncrono entonces el resultado no estaría
listo al devolverse y por tanto no funcionaría.
 */
public interface DAO<T> {

    public void get(String id, OnDataLoadedCallback<T> callback);
    public void add(T obj, OnOperationCallback callback);
    public void edit(T nuevo, OnOperationCallback callback);
    public void delete(String id, OnOperationCallback callback);
}
