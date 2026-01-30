package com.pastillerodigital.cuidamedpill.modelo.enumerados;

import com.pastillerodigital.cuidamedpill.utils.Constantes;

/**
 * Enumerado que define el modo de la interfaz, se usará para determinar lo que ve un usuario
 */
public enum Modo {

    ESTANDAR(Constantes.MODO_ESTANDAR),
    ASISTIDO(Constantes.MODO_ASISTIDO),
    SUPERVISOR(Constantes.MODO_SUPERVISOR);

    private final String descripcion;

    Modo(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }

    /**
     * Convierte un string a su valor correspondiente del enumerado Modo.
     * @param modo String que representa el modo
     * @return Modo correspondiente, o null si no existe
     */
    public static Modo modoFromString(String modo) {
        if (modo == null) return null;
        for (Modo m : Modo.values()) {
            if (m.descripcion.equals(modo)) {
                return m;
            }
        }
        return null;
    }
}
