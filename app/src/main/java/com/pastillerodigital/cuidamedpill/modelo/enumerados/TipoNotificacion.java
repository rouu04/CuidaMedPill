package com.pastillerodigital.cuidamedpill.modelo.enumerados;

import com.pastillerodigital.cuidamedpill.utils.Constantes;

public enum TipoNotificacion {

    ALARMA(Constantes.TIPONOTI_ALARMA),
    ESTANDAR(Constantes.TIPONOTI_ESTANDAR),
    SILENCIOSA(Constantes.TIPONOTI_SILENCIOSA);

    private final String descripcion;

    TipoNotificacion(String tipoNoti) {
        this.descripcion = tipoNoti;
    }

    @Override
    public String toString(){
        return descripcion;
    }

    public static TipoNotificacion tipoNotiFromString(String tipo) {
        if (tipo == null) return null;
        for (TipoNotificacion t : TipoNotificacion.values()) {
            if (t.descripcion.equals(tipo)) {
                return t;
            }
        }
        return null;
    }


}
