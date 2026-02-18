package com.pastillerodigital.cuidamedpill.modelo.enumerados;

import com.pastillerodigital.cuidamedpill.utils.Constantes;

public enum TipoIntervalo {

    DIARIO(Constantes.TIPOINTERVALO_DIARIO),
    SEMANAL(Constantes.TIPOINTERVALO_SEMANAL),
    QUINCENAL(Constantes.TIPOINTERVALO_QUINCENAL),
    MENSUAL(Constantes.TIPOINTERVALO_MENSUAL),
    TRIMESTRAL(Constantes.TIPOINTERVALO_TRIMESTRAL),
    ANUAL(Constantes.TIPOINTERVALO_ANUAL);

    private final String descripcion;

    TipoIntervalo(String intervalo){
        this.descripcion = intervalo;
    }

    @Override
    public String toString(){
        return descripcion;
    }

    public static String[] getAllTipos() {
        TipoIntervalo[] values = TipoIntervalo.values();
        String[] tipos = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            tipos[i] = values[i].descripcion;
        }

        return tipos;
    }

    /**
     * Convierte el string al tipo del enumerado
     * @param tipo
     * @return
     */
    public static TipoIntervalo tipoIntervaloFromString(String tipo) {
        if (tipo == null) return null;
        for (TipoIntervalo t : TipoIntervalo.values()) {
            if (t.descripcion.equals(tipo)) {
                return t;
            }
        }
        return null;
    }

    public static String tipoToStringVista(int intervalo, TipoIntervalo tipo){
        if (tipo == null || intervalo <= 0) return "";

        String unidad;
        switch (tipo){
            case DIARIO:
                unidad = (intervalo == 1) ? Constantes.INTERVALO_DIA : Constantes.INTERVALO_DIAS;
                break;
            case SEMANAL:
                unidad = (intervalo == 1) ? Constantes.INTERVALO_SEMANA : Constantes.INTERVALO_SEMANAS;
                break;
            case QUINCENAL:
                unidad = (intervalo == 1) ? Constantes.INTERVALO_QUINCENA : Constantes.INTERVALO_QUINCENAS;
                break;
            case MENSUAL:
                unidad = (intervalo == 1) ? Constantes.INTERVALO_MES : Constantes.INTERVALO_MESES;
                break;
            case TRIMESTRAL:
                unidad = (intervalo == 1) ? Constantes.INTERVALO_TRIMESTRE : Constantes.INTERVALO_TRIMESTRES;
                break;
            case ANUAL:
                unidad = (intervalo == 1) ? Constantes.INTERVALO_ANIO : Constantes.INTERVALO_ANIOS;
                break;
            default:
                unidad = "";
        }

        return "Cada " + intervalo + " " + unidad;
    }


}
