package com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos;

import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoAviso;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;

public class AvisoFactory {

    public static Aviso crearAvisoCaducidad(Medicamento med) {
        return new Aviso(TipoAviso.CADUCIDAD,
                "Medicamento va a caducar",
                "El medicamento " + med.getNombreMed() + " está a punto de caducar",
                med.getId()
        );
    }

    public static Aviso crearAvisoCompra(Medicamento med) {
        return new Aviso(
                TipoAviso.COMPRA,
                "Pocas unidades restantes",
                "Quedan pocas unidades de " + med.getNombreMed(),
                med.getId()
        );
    }

    public static Aviso crearAvisoOlvido(Medicamento med) {
        return new Aviso(
                TipoAviso.OLVIDOASISTIDO,
                "Toma olvidada",
                "Has olvidado una toma de " + med.getNombreMed(),
                med.getId()
        );
    }

    public static Aviso crearAvisoFinTratamiento(Medicamento med) {
        return new Aviso(
                TipoAviso.FINTRATAMIENTO,
                "Tratamiento va a finalizar",
                "El tratamiento de " + med.getNombreMed() + " va a terminar en una semana",
                med.getId()
        );
    }
}
