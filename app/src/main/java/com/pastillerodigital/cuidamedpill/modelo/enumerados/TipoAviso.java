package com.pastillerodigital.cuidamedpill.modelo.enumerados;

public enum TipoAviso {

    CADUCIDAD,
    COMPRA,
    OLVIDOASISTIDO,
    FINTRATAMIENTO,
    INFOGENERAL;

    public static TipoAviso fromString(String value) {
        if (value == null) return null;
        return TipoAviso.valueOf(value);
    }
}
