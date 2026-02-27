package com.pastillerodigital.cuidamedpill.modelo.medicamento.horario;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoIntervalo;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Horario {

    private  List<Hora> horas; // varias horas del día (todos los días 9am y 8pm por ejemplo)

    private String tipoIntervaloStr;
    private Timestamp sigIngesta;
    @Exclude
    private TipoIntervalo tipoIntervalo;
    private int intervalo; // número de días/semanas/meses según el tipo (cada 2 días)

    public Horario(){}

    public Horario(String tipoIntervaloStr, int intervalo, List<Hora> horas, Timestamp sigIngesta) {
        this.tipoIntervaloStr = tipoIntervaloStr;
        this.tipoIntervalo = TipoIntervalo.tipoIntervaloFromString(tipoIntervaloStr);
        this.intervalo = intervalo;
        this.horas = horas;
        this.sigIngesta = sigIngesta;
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

    public Timestamp getSigIngesta() {
        return sigIngesta;
    }

    public void setSigIngesta(Timestamp sigIngesta) {
        this.sigIngesta = sigIngesta;
    }

    public static Horario mapToObj(Map<String, Object> map) {
        if (map == null) return null;

        Horario horario = new Horario();
        horario.setTipoIntervaloStr(map.get(Constantes.HORARIO_TIPOINTERVALOSTR).toString());
        horario.setTipoIntervalo(TipoIntervalo.tipoIntervaloFromString(horario.getTipoIntervaloStr()));
        horario.setIntervalo(((Long) map.get(Constantes.HORARIO_INTERVALO)).intValue());
        horario.setSigIngesta((Timestamp) map.get(Constantes.HORARIO_SIGINGESTA));

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


    /**
     * Devuelve una lista de Timestamps de todas las horas programadas para una fecha específica
     * @param fechaSeleccionada La fecha a calcular
     * @param fechaInicio Opcional: primer día del medicamento para calcular pasado
     */
    public List<Timestamp> getFechaHorasDia(Calendar fechaSeleccionada, Calendar fechaInicio) {
        List<Timestamp> fechas = new ArrayList<>();
        if (horas == null || horas.isEmpty() || fechaSeleccionada == null) return fechas;

        Calendar check = (fechaInicio != null) ? (Calendar) fechaInicio.clone() : Calendar.getInstance();
        Utils.limpiarHora(check);

        Calendar fecha = (Calendar) fechaSeleccionada.clone();
        Utils.limpiarHora(fecha);

        Calendar limite = (Calendar) fecha.clone();
        limite.add(Calendar.YEAR, 2);

        while (!check.after(fecha) && check.before(limite)) {
            if (Utils.mismoDia(check, fecha)) {
                for (Hora h : horas) {
                    Calendar calHora = (Calendar) fecha.clone();
                    calHora.set(Calendar.HOUR_OF_DAY, h.getHora());
                    calHora.set(Calendar.MINUTE, h.getMin());
                    calHora.set(Calendar.SECOND, 0);
                    calHora.set(Calendar.MILLISECOND, 0);
                    fechas.add(new Timestamp(calHora.getTime()));
                }
                break;
            }
            avanzarIntervalo(check);
        }
        return fechas;
    }


    public void avanzarIntervalo(Calendar cal){
        switch (tipoIntervalo) {
            case DIARIO:
                cal.add(Calendar.DAY_OF_YEAR, intervalo);
                break;
            case SEMANAL:
                cal.add(Calendar.WEEK_OF_YEAR, intervalo);
                break;
            case QUINCENAL:
                cal.add(Calendar.WEEK_OF_YEAR, 2 * intervalo);
                break;
            case MENSUAL:
                cal.add(Calendar.MONTH, intervalo);
                break;
            case TRIMESTRAL:
                cal.add(Calendar.MONTH, 3 * intervalo);
                break;
            case ANUAL:
                cal.add(Calendar.YEAR, intervalo);
                break;
        }
    }

}
