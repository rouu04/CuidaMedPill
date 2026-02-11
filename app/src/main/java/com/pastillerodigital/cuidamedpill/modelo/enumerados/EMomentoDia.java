package com.pastillerodigital.cuidamedpill.modelo.enumerados;

import com.pastillerodigital.cuidamedpill.utils.Constantes;

public enum EMomentoDia {

    DESAYUNO(Constantes.TIPOMOM_DESAYUNO, 8, 0),
    ALMUERZO(Constantes.TIPOMOM_ALMUERZO, 11, 0),
    COMIDA(Constantes.TIPOMOM_COMIDA, 14, 0),
    MERIENDA(Constantes.TIPOMOM_MERIENDA, 18, 0),
    CENA(Constantes.TIPOMOM_CENA, 21, 0);

    private final String descripcion;
    private final int horaDefault;
    private final int minDefault;


    EMomentoDia(String momentoDia, int horaDefault, int minDefault) {
        this.descripcion = momentoDia;
        this.horaDefault = horaDefault;
        this.minDefault = minDefault;
    }

    @Override
    public String toString(){
        return descripcion;
    }

    public static EMomentoDia momentoDiaFromString(String tipo) {
        if (tipo == null) return null;
        for (EMomentoDia t : EMomentoDia.values()) {
            if (t.descripcion.equals(tipo)) {
                return t;
            }
        }
        return null;
    }

    public int getHoraDefault() {
        return horaDefault;
    }

    public int getMinDefault() {
        return minDefault;
    }
}
