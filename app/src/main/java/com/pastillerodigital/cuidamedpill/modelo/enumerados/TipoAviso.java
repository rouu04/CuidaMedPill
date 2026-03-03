package com.pastillerodigital.cuidamedpill.modelo.enumerados;

public enum TipoAviso {

    CADUCIDAD,
    COMPRA,
    OLVIDO,
    INFO_GENERAL;

    public static TipoAviso fromString(String value) {
        if (value == null) return null;
        return TipoAviso.valueOf(value);
    }
}
