package com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos;

import com.google.firebase.Timestamp;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoAviso;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;

public class AvisoFactory {

    public static Aviso crearAvisoCaducidad(Medicamento med, Timestamp fecha) {
        return new Aviso(
                TipoAviso.CADUCIDAD,
                Mensajes.AVISO_TITULO_CADUCIDAD,
                String.format(Mensajes.AVISO_MSG_CADUCIDAD, med.getNombreMed()),
                med.getId(),
                fecha
        );
    }

    public static Aviso crearAvisoCompra(Medicamento med, Timestamp fecha) {
        return new Aviso(
                TipoAviso.COMPRA,
                Mensajes.AVISO_TITULO_COMPRA,
                String.format(Mensajes.AVISO_MSG_COMPRA, med.getNombreMed()),
                med.getId(),
                fecha
        );
    }

    public static Aviso crearAvisoOlvido(Medicamento med, Timestamp fecha) {
        return new Aviso(
                TipoAviso.OLVIDOASISTIDO,
                Mensajes.AVISO_TITULO_OLVIDO,
                String.format(Mensajes.AVISO_MSG_OLVIDO, med.getNombreMed()),
                med.getId(),
                fecha
        );
    }

    public static Aviso crearAvisoFinTratamiento(Medicamento med, Timestamp fecha) {
        return new Aviso(
                TipoAviso.FINTRATAMIENTO,
                Mensajes.AVISO_TITULO_FIN_TRATAMIENTO,
                String.format(Mensajes.AVISO_MSG_FIN_TRATAMIENTO, med.getNombreMed()),
                med.getId(),
                fecha
        );
    }
}
