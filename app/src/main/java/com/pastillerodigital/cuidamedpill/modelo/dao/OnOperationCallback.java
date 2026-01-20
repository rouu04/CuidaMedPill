package com.pastillerodigital.cuidamedpill.modelo.dao;
/*
Devuelve si una llamada a base de datos a firebase tuvo éxito o no
 */
public interface OnOperationCallback {
    void onSuccess();
    void onFailure(Exception e);
}
