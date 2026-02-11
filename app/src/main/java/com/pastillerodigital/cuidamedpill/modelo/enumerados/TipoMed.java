package com.pastillerodigital.cuidamedpill.modelo.enumerados;

import com.pastillerodigital.cuidamedpill.utils.Constantes;

public enum TipoMed {

    PASTILLA(Constantes.TIPOMED_PASTILLA),
    VACUNA(Constantes.TIPOMED_VACUNA),
    INYECCION(Constantes.TIPOMED_INYECCION),
    INHALADOR(Constantes.TIPOMED_INHALADOR),
    CREMA(Constantes.TIPOMED_CREMA),
    JARABE(Constantes.TIPOMED_JARABE),
    GOTAS(Constantes.TIPOMED_GOTAS),
    AEROSOL(Constantes.TIPOMED_AEROSOL),
    PARCHE(Constantes.TIPOMED_PARCHE);

    private final String descripcion;

    TipoMed(String tipoMed) { this.descripcion = tipoMed;}

    @Override
    public String toString(){
        return descripcion;
    }

    /**
     * Convierte el string al tipo del enumerado
     * @param tipo
     * @return
     */
    public static TipoMed tipoMedFromString(String tipo) {
        if (tipo == null) return null;
        for (TipoMed t : TipoMed.values()) {
            if (t.descripcion.equals(tipo)) {
                return t;
            }
        }
        return null;
    }
}
