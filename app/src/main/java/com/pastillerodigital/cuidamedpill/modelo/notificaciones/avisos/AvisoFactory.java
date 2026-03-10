package com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos;

import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoAviso;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;

public class AvisoFactory {

    public static Aviso crearAvisoCaducidad(Medicamento med) {
        return new Aviso(TipoAviso.CADUCIDAD,
                "Medicamento caducado",
                "El medicamento " + med.getNombreMed() + " ha caducado.",
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
                TipoAviso.OLVIDO,
                "Toma olvidada",
                "Has olvidado una toma de " + med.getNombreMed(),
                med.getId()
        );
    }
}
