package com.pastillerodigital.cuidamedpill.modelo.enumerados;

public enum TipoAviso {

    CADUCIDAD,
    COMPRA,
    OLVIDO,
    FIN_TRATAMIENTO,
    INFO_GENERAL;

    public static TipoAviso fromString(String value) {
        if (value == null) return null;
        return TipoAviso.valueOf(value);
    }
}
