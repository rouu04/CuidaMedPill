package com.pastillerodigital.cuidamedpill.modelo.medicamento.horario;

import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EMomentoDia;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoIntervalo;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Horario {

    private  List<Hora> horas; // varias horas del día (todos los días 9am y 8pm por ejemplo)

    private String tipoIntervaloStr;
    @Exclude
    private TipoIntervalo tipoIntervalo;
    private int intervalo; // número de días/semanas/meses según el tipo (cada 2 días)

    public Horario(){}

    public Horario(String tipoIntervaloStr, int intervalo, List<Hora> horas) {
        this.tipoIntervaloStr = tipoIntervaloStr;
        this.tipoIntervalo = TipoIntervalo.tipoIntervaloFromString(tipoIntervaloStr);
        this.intervalo = intervalo;
        this.horas = horas;
    }

    public List<Hora> getHoras() {
        return horas;
    }

    public void setHoras(List<Hora> horas) {
        this.horas = horas;
    }

    @Exclude
    public TipoIntervalo getTipoIntervalo() {
        return tipoIntervalo;
    }
    @Exclude
    public void setTipoIntervalo(TipoIntervalo tipoIntervalo) {
        this.tipoIntervalo = tipoIntervalo;
    }


    public int getIntervalo() {
        return intervalo;
    }

    public void setIntervalo(int intervalo) {
        this.intervalo = intervalo;
    }

    public String getTipoIntervaloStr() {
        return tipoIntervaloStr;
    }

    public void setTipoIntervaloStr(String tipoIntervaloStr) {
        this.tipoIntervaloStr = tipoIntervaloStr;
    }

    public static Horario mapToObj(Map<String, Object> map) {
        if (map == null) return null;

        Horario horario = new Horario();
        horario.setTipoIntervaloStr(map.get(Constantes.HORARIO_TIPOINTERVALOSTR).toString());
        horario.setTipoIntervalo(TipoIntervalo.tipoIntervaloFromString(horario.getTipoIntervaloStr()));
        horario.setIntervalo(((Long) map.get(Constantes.HORARIO_INTERVALO)).intValue());

        List<Map<String, Object>> listaMaps = (List<Map<String, Object>>) map.get(Constantes.HORARIO_HORAS);
        List<Hora> listaProcesada = new ArrayList<>();

        if (listaMaps != null) {
            for (Map<String, Object> horaMap : listaMaps) {
                listaProcesada.add(Hora.mapToObj(horaMap)); //delega en los hijos
            }
        }

        horario.setHoras(listaProcesada);
        return horario;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constantes.HORARIO_TIPOINTERVALOSTR, tipoIntervaloStr);
        map.put(Constantes.HORARIO_INTERVALO, intervalo);

        List<Map<String,Object>> horasList = new ArrayList<>();
        if(horas != null) {
            for(Hora h : horas) {
                Map<String,Object> horaMap = new HashMap<>();
                if(h instanceof HoraMomentoDia) {
                    horaMap.put(Constantes.HORA_MOMENTODIASTR, ((HoraMomentoDia) h).getMomentoDiaStr());
                } else {
                    horaMap.put(Constantes.HORA_HORA, h.getHora());
                    horaMap.put(Constantes.HORA_MIN, h.getMin());
                }
                horasList.add(horaMap);
            }
        }
        map.put(Constantes.HORARIO_HORAS, horasList);

        return map;
    }


    /**
     * Métod*o que devuelve las próximas fechas de la toma.
     * todo revisar parametros por si necesito más cosas
     * @param desde
     * @param cantidad
     * @return
     */
    public List<Calendar> calcularProximasFechas(Calendar desde, int cantidad){
        List<Calendar> proximas = new ArrayList<>();

        Calendar actual = (Calendar) desde.clone();

        for (int i = 0; i < cantidad; i++) {
            for (Hora h : horas) {
                Calendar c = (Calendar) actual.clone();
                c.set(Calendar.HOUR_OF_DAY, h.getHora());
                c.set(Calendar.MINUTE, h.getMin());
                c.set(Calendar.SECOND, 0);
                proximas.add(c);
            }

            switch (tipoIntervalo) {
                case DIARIO:
                    actual.add(Calendar.DAY_OF_YEAR, intervalo);
                    break;
                case SEMANAL:
                    actual.add(Calendar.WEEK_OF_YEAR, intervalo);
                    break;
                case QUINCENAL:
                    actual.add(Calendar.WEEK_OF_YEAR, 2 * intervalo);
                    break;
                case MENSUAL:
                    actual.add(Calendar.MONTH, intervalo);
                case TRIMESTRAL:
                    actual.add(Calendar.MONTH, 3 * intervalo);
                    break;
                case ANUAL:
                    actual.add(Calendar.YEAR, intervalo);
                    break;
            }
        }

        return proximas;
    }
}
