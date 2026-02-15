package com.pastillerodigital.cuidamedpill.modelo.enumerados;

import com.pastillerodigital.cuidamedpill.utils.Constantes;

public enum TipoMed {

    PASTILLA(Constantes.TIPOMED_PASTILLA, Constantes.RES_IC_MED_PASTILLA_CIRCULAR_LIST, Constantes.RES_IC_MED_PASTILLA_CIRCULAR_COLOR),
    CAPSULA(Constantes.TIPOMED_CAPSULA, Constantes.RES_IC_MED_PASTILLA_CAPSULA_LIST, Constantes.RES_IC_MED_PASTILLA_CAPSULA_INFERIOR),
    INYECCION(Constantes.TIPOMED_INYECCION, Constantes.RES_IC_MED_INYECCION_LIST, Constantes.RES_IC_MED_INYECCION_COLOR),
    INHALADOR(Constantes.TIPOMED_INHALADOR, Constantes.RES_IC_MED_INHALADOR_LIST, Constantes.RES_IC_MED_INHALADOR_COLOR),
    CREMA(Constantes.TIPOMED_CREMA, Constantes.RES_IC_MED_CREMA_LIST, Constantes.RES_IC_MED_CREMA_COLOR),
    JARABE(Constantes.TIPOMED_JARABE, Constantes.RES_IC_MED_JARABE_LIST, Constantes.RES_IC_MED_JARABE_COLOR),
    GOTAS(Constantes.TIPOMED_GOTAS, Constantes.RES_IC_MED_GOTAS, Constantes.RES_IC_MED_GOTAS);

    private final String descripcion;
    private final int drawableRes;
    private final int drawableResColoreable;

    TipoMed(String tipoMed, int drawableRes, int drawableResColoreable) {
        this.descripcion = tipoMed;
        this.drawableRes = drawableRes;
        this.drawableResColoreable = drawableResColoreable;
    }

    public static String[] getAllTipos() {
        TipoMed[] values = TipoMed.values();
        String[] tipos = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            tipos[i] = values[i].descripcion;
        }

        return tipos;
    }

    @Override
    public String toString(){
        return descripcion;
    }

    public int getDrawableRes() {
        return drawableRes;
    }

    public int getDrawableResColoreable() {
        return drawableResColoreable;
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
