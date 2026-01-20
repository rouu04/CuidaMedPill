package com.pastillerodigital.cuidamedpill.modelo.dao;
/*
Devuelve objetos pedidos si la operacion tiene exito o no en firebase
Necesario porque firebase es asíncrono
 */
public interface OnDataLoadedCallback<T> {
    void onSuccess(T data);
    void onFailure(Exception e);
}
