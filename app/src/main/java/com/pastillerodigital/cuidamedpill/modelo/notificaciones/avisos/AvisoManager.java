package com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos;

import android.content.Context;

import com.pastillerodigital.cuidamedpill.modelo.dao.AvisoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoAviso;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.ConfNoti;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;

public class AvisoManager {

    public static void comprobarAvisos(Context context, Usuario usuario, Medicamento med) {
        ConfNoti conf = usuario.getConfNoti();
        if(conf == null) return;
        AvisoDAO aDAO = new AvisoDAO(usuario.getId());


        // CADUCIDAD
        if(conf.isAvisoCaducidad() && med.estaCaducado()){
            Aviso aviso = AvisoFactory.crearAvisoCaducidad(med);
            aDAO.add(aviso, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    AvisoNotificacionHelper.mostrarAviso(context, aviso);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }

        // COMPRA
        if(conf.isAvisoCompra() && med.getnMedRestantes() <= 5){
            Aviso aviso = AvisoFactory.crearAvisoCompra(med);
            aDAO.add(aviso, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    AvisoNotificacionHelper.mostrarAviso(context, aviso);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }

        // FIN TRATAMIENTO
        if(conf.isAvisoFinTratamiento() && med.tratamientoFinalizado()){
            //todo ponerlo en factory
            Aviso aviso = new Aviso(
                    TipoAviso.FIN_TRATAMIENTO,
                    "Tratamiento finalizado",
                    "El tratamiento de " + med.getNombreMed() + " ha terminado",
                    med.getId()
            );
            aviso.setuDestId(usuario.getId());

            aDAO.add(aviso, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    AvisoNotificacionHelper.mostrarAviso(context, aviso);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
            AvisoNotificacionHelper.mostrarAviso(context, aviso);
        }
    }
}
