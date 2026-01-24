package com.pastillerodigital.cuidamedpill.modelo.enumerados;

import com.pastillerodigital.cuidamedpill.utils.Constantes;

/*
Enumerado que describe el tipo de usuario
 */
public enum TipoUsuario {
    ESTANDAR(Constantes.TIPOUSR_ESTANDAR),
    ASISTIDO(Constantes.TIPOUSR_ASISTIDO);

    private final String descripcion;

    TipoUsuario(String tipoUsr) {
        this.descripcion = tipoUsr;
    }

    @Override
    public String toString(){
        return descripcion;
    }

    /**
    Convierte el string al valor del enumerado
     */
    public static TipoUsuario tipoUsrFromString(String tipo) {
        if (tipo == null) return null;
        for (TipoUsuario t : TipoUsuario.values()) {
            if (t.descripcion.equals(tipo)) {
                return t;
            }
        }
        return null;
    }
}
