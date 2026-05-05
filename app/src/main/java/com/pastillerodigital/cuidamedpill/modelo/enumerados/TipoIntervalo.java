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

    public static String[] getAllTiposOutput(int intervalo) {
        TipoIntervalo[] values = TipoIntervalo.values();
        String[] tipos = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            switch (values[i]) {
                case DIARIO:
                    tipos[i] = (intervalo == 1) ? Constantes.INTERVALO_DIA : Constantes.INTERVALO_DIAS;
                    break;
                case SEMANAL:
                    tipos[i] = (intervalo == 1) ? Constantes.INTERVALO_SEMANA : Constantes.INTERVALO_SEMANAS;
                    break;
                case QUINCENAL:
                    tipos[i] = (intervalo == 1) ? Constantes.INTERVALO_QUINCENA : Constantes.INTERVALO_QUINCENAS;
                    break;
                case MENSUAL:
                    tipos[i] = (intervalo == 1) ? Constantes.INTERVALO_MES : Constantes.INTERVALO_MESES;
                    break;
                case TRIMESTRAL:
                    tipos[i] = (intervalo == 1) ? Constantes.INTERVALO_TRIMESTRE : Constantes.INTERVALO_TRIMESTRES;
                    break;
                case ANUAL:
                    tipos[i] = (intervalo == 1) ? Constantes.INTERVALO_ANIO : Constantes.INTERVALO_ANIOS;
                    break;
            }
        }

        return tipos;
    }

    public static TipoIntervalo fromUnidad(String unidad) {
        if (unidad == null) return null;

        switch (unidad.toLowerCase()) {
            case Constantes.INTERVALO_DIA:
            case Constantes.INTERVALO_DIAS:
                return DIARIO;

            case Constantes.INTERVALO_SEMANA:
            case Constantes.INTERVALO_SEMANAS:
                return SEMANAL;

            case Constantes.INTERVALO_QUINCENA:
            case Constantes.INTERVALO_QUINCENAS:
                return QUINCENAL;

            case Constantes.INTERVALO_MES:
            case Constantes.INTERVALO_MESES:
                return MENSUAL;

            case Constantes.INTERVALO_TRIMESTRE:
            case Constantes.INTERVALO_TRIMESTRES:
                return TRIMESTRAL;

            case Constantes.INTERVALO_ANIO:
            case Constantes.INTERVALO_ANIOS:
                return ANUAL;

            default:
                return null;
        }
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

    public static String tipoToStringIndividual(int intervalo, TipoIntervalo tipo){
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

        return unidad;
    }


}
