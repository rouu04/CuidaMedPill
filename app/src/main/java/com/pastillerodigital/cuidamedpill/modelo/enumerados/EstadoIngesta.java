package com.pastillerodigital.cuidamedpill.modelo.enumerados;

import com.pastillerodigital.cuidamedpill.utils.Constantes;

public enum EstadoIngesta {

    OK(Constantes.ESTADO_INGESTA_OK),
    RETRASO(Constantes.ESTADO_INGESTA_RETRASO),
    OLVIDO(Constantes.ESTADO_INGESTA_OLVIDO),
    PENDIENTE(Constantes.ESTADO_INGESTA_PENDIENTE),
    NO_PROGRAMADA(Constantes.ESTADO_INGESTA_NO_PROGRAMADA);

    private final String descripcion;

    EstadoIngesta(String intervalo){
        this.descripcion = intervalo;
    }

    @Override
    public String toString(){
        return descripcion;
    }

    public static String[] getAllEstadosIngesta() {
        EstadoIngesta[] values = EstadoIngesta.values();
        String[] tipos = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            tipos[i] = values[i].descripcion;
        }

        return tipos;
    }

    /**
     * Convierte el string al estado del enumerado
     * @param tipo
     * @return
     */
    public static EstadoIngesta estadoIngestaFromString(String tipo) {
        if (tipo == null) return null;
        for (EstadoIngesta t : EstadoIngesta.values()) {
            if (t.descripcion.equals(tipo)) {
                return t;
            }
        }
        return null;
    }
}
