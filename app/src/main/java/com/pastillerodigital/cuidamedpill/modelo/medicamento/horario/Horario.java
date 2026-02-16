package com.pastillerodigital.cuidamedpill.modelo.medicamento.horario;

import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoIntervalo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Horario {

    private  List<Hora> horas; // varias horas del día (todos los días 9am y 8pm por ejemplo)

    private TipoIntervalo tipoIntervalo;
    private int intervalo; // número de días/semanas/meses según el tipo (cada 2 días)

    public Horario(){}

    public Horario(TipoIntervalo tipoIntervalo, int intervalo, List<Hora> horas) {
        this.tipoIntervalo = tipoIntervalo;
        this.intervalo = intervalo;
        this.horas = horas;
    }

    public List<Hora> getHoras() {
        return horas;
    }

    public void setHoras(List<Hora> horas) {
        this.horas = horas;
    }

    public TipoIntervalo getTipoIntervalo() {
        return tipoIntervalo;
    }

    public void setTipoIntervalo(TipoIntervalo tipoIntervalo) {
        this.tipoIntervalo = tipoIntervalo;
    }

    public int getIntervalo() {
        return intervalo;
    }

    public void setIntervalo(int intervalo) {
        this.intervalo = intervalo;
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
