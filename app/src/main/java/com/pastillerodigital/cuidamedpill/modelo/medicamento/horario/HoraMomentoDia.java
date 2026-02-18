package com.pastillerodigital.cuidamedpill.modelo.medicamento.horario;

import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EMomentoDia;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

import java.util.Map;

public class HoraMomentoDia extends Hora{

    private String momentoDiaStr;

    public HoraMomentoDia(){super();}

    public HoraMomentoDia(String momentoDiaStr) {
        this.momentoDiaStr = momentoDiaStr;
        EMomentoDia momDia = EMomentoDia.momentoDiaFromString(this.momentoDiaStr);
        this.hora = momDia.getHoraDefault();
        this.min = momDia.getMinDefault();
    }


    @Override
    public String toString(){
        return this.momentoDiaStr;
    }

    public String getMomentoDiaStr() {
        return momentoDiaStr;
    }

    public void setMomentoDiaStr(String momentoDiaStr) {
        this.momentoDiaStr = momentoDiaStr;
    }

    public static HoraMomentoDia mapToObj(Map<String, Object> data) {
        String momentoStr = (String) data.get(Constantes.HORA_MOMENTODIASTR);
        return  new HoraMomentoDia(momentoStr);
    }
}
