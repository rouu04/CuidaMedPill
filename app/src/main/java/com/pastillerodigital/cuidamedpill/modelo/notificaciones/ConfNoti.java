package com.pastillerodigital.cuidamedpill.modelo.notificaciones;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.Persistible;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoNotificacion;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Horario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

import java.util.List;
import java.util.Map;

/**
 * Define las notificaciones del usuario generales por defecto
 */
public class ConfNoti implements Persistible {
    private boolean avisoCaducidad;
    private boolean avisoCompra;
    private boolean avisoFinTratamiento;
    private boolean antiprocrastinador;
    private String tipoNotiStr;

    @Exclude
    private String confNotiId;
    @Exclude
    private TipoNotificacion tipoNoti = TipoNotificacion.ESTANDAR;


    public ConfNoti(){
        this.avisoCaducidad = true;
        this.avisoCompra = true;
        this.avisoFinTratamiento = true;
        this.antiprocrastinador = true;
        this.tipoNotiStr = TipoNotificacion.ESTANDAR.toString();
    }

    public ConfNoti(boolean avisoCaducidad, boolean avisoCompra, boolean avisoFinTratamiento, TipoNotificacion tipoNotis,
                    boolean antiprocrastinador) {
        this.avisoCaducidad = avisoCaducidad;
        this.avisoCompra = avisoCompra;
        this.avisoFinTratamiento = avisoFinTratamiento;
        this.tipoNoti = tipoNotis;
        this.tipoNotiStr = tipoNotis.toString();
        this.antiprocrastinador = antiprocrastinador;
    }

    public boolean isAvisoCaducidad() {
        return avisoCaducidad;
    }

    public void setAvisoCaducidad(boolean avisoCaducidad) {
        this.avisoCaducidad = avisoCaducidad;
    }

    public boolean isAvisoCompra() {
        return avisoCompra;
    }

    public void setAvisoCompra(boolean avisoCompra) {
        this.avisoCompra = avisoCompra;
    }

    public boolean isAvisoFinTratamiento() {
        return avisoFinTratamiento;
    }

    public void setAvisoFinTratamiento(boolean avisoFinTratamiento) {
        this.avisoFinTratamiento = avisoFinTratamiento;
    }

    public String getTipoNotiStr() {
        return tipoNotiStr;
    }

    public void setTipoNotiStr(String tipoNotiStr) {
        this.tipoNotiStr = tipoNotiStr;
    }

    public boolean isAntiprocrastinador() {
        return antiprocrastinador;
    }

    public void setAntiprocrastinador(boolean antiprocrastinador) {
        this.antiprocrastinador = antiprocrastinador;
    }

    @Override
    @Exclude
    public void setId(String id) {
        this.confNotiId = id;
    }

    @Override
    @Exclude
    public String getId() {
        return this.confNotiId;
    }
    @Exclude
    public TipoNotificacion getTipoNoti() {
        return tipoNoti;
    }
    @Exclude
    public void setTipoNoti(TipoNotificacion tipoNoti) {
        this.tipoNoti = tipoNoti;
    }

    public static ConfNoti doctoObj(DocumentSnapshot doc){
        ConfNoti confNoti = new ConfNoti();
        confNoti.setId(doc.getId());

        Boolean avisoCad = doc.getBoolean(Constantes.CONFNOTI_AVISOCADUCIDAD);
        if(avisoCad != null) confNoti.setAvisoCaducidad(avisoCad);
        Boolean avisoCompra = doc.getBoolean(Constantes.CONFNOTI_AVISOCOMPRA);
        if(avisoCompra != null) confNoti.setAvisoCompra(avisoCompra);
        Boolean avisoFinTrat = doc.getBoolean(Constantes.CONFNOTI_AVISOFINTRATAMIENTO);
        if(avisoFinTrat != null) confNoti.setAvisoFinTratamiento(avisoFinTrat);
        Boolean antipro = doc.getBoolean(Constantes.CONFNOTI_ANTIPROCRASTINADOR);
        if(antipro != null) confNoti.setAntiprocrastinador(antipro);


        String tipoNotiStr = doc.getString(Constantes.CONFNOTI_TIPONOTISTR);
        confNoti.setTipoNotiStr(tipoNotiStr);
        if(tipoNotiStr != null){
            confNoti.setTipoNoti(TipoNotificacion.valueOf(tipoNotiStr));
        }

        return confNoti;
    }

    public static ConfNoti fromMap(Map<String, Object> map) {
        if (map == null) return new ConfNoti();

        ConfNoti conf = new ConfNoti();

        conf.setAvisoCaducidad((Boolean) map.get(Constantes.CONFNOTI_AVISOCADUCIDAD));
        conf.setAvisoCompra((Boolean) map.get(Constantes.CONFNOTI_AVISOCOMPRA));
        conf.setAvisoFinTratamiento((Boolean) map.get(Constantes.CONFNOTI_AVISOFINTRATAMIENTO));
        conf.setAntiprocrastinador((Boolean) map.get(Constantes.CONFNOTI_ANTIPROCRASTINADOR));

        String tipo = (String) map.get(Constantes.CONFNOTI_TIPONOTISTR);
        if (tipo != null) {
            conf.setTipoNotiStr(tipo);
            conf.setTipoNoti(TipoNotificacion.tipoNotiFromString(tipo));
        }


        return conf;
    }
}
