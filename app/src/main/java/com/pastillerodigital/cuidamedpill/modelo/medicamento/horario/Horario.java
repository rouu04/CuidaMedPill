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

    public boolean hayIngestaDia(Calendar fechaSeleccionada){
        if(sigIngesta == null) return false;
        Calendar sigIngestaCal = Calendar.getInstance();
        sigIngestaCal.setTime(sigIngesta.toDate());
        Utils.limpiarHora(sigIngestaCal);

        //Para evitar ciclos infinitos
        Calendar limite = (Calendar) fechaSeleccionada.clone();
        limite.add(Calendar.YEAR, 2);

        while(!sigIngestaCal.after(fechaSeleccionada) && sigIngestaCal.before(limite)){
            if(mismoDia(sigIngestaCal, fechaSeleccionada)) return true;
            avanzarIntervalo(sigIngestaCal);
        }

        return mismoDia(sigIngestaCal, fechaSeleccionada);
    }

    private void avanzarIntervalo(Calendar cal){
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

    private boolean mismoDia(Calendar c1, Calendar c2){
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Devuelve una lista de Strings con las horas programadas para un día específico.
     * Formato HH:mm
     * @param fechaSeleccionada El día a consultar
     * @return Lista de horas en formato HH:mm
     */
    public List<String> getHorasDiaStr(Calendar fechaSeleccionada){
        List<String> horasDelDia = new ArrayList<>();
        if(horas == null || horas.isEmpty() || sigIngesta == null) return horasDelDia;

        // Clonamos la fecha para no modificarla
        Calendar fecha = (Calendar) fechaSeleccionada.clone();
        Utils.limpiarHora(fecha);

        Calendar sigCal = Calendar.getInstance();
        sigCal.setTime(sigIngesta.toDate());
        Utils.limpiarHora(sigCal);

        // Evitamos bubles infinitos
        Calendar limite = (Calendar) fecha.clone();
        limite.add(Calendar.YEAR, 2);

        while(!sigCal.after(fecha) && sigCal.before(limite)){
            if(mismoDia(sigCal, fecha)){
                for(Hora h : horas){ // Añadimos las horas de ese día
                    String horaStr = String.format("%02d:%02d", h.getHora(), h.getMin());
                    horasDelDia.add(horaStr);
                }
                break;
            }
            avanzarIntervalo(sigCal);
        }

        return horasDelDia;
    }

    public List<Hora> getHorasDia(Calendar fechaSeleccionada){
        List<Hora> horasDelDia = new ArrayList<>();
        if(horas == null || horas.isEmpty() || sigIngesta == null) return horasDelDia;

        // Clonamos la fecha para no modificarla
        Calendar fecha = (Calendar) fechaSeleccionada.clone();
        Utils.limpiarHora(fecha);

        Calendar sigCal = Calendar.getInstance();
        sigCal.setTime(sigIngesta.toDate());
        Utils.limpiarHora(sigCal);

        // Evitamos bubles infinitos
        Calendar limite = (Calendar) fecha.clone();
        limite.add(Calendar.YEAR, 2);

        while(!sigCal.after(fecha) && sigCal.before(limite)){
            if(mismoDia(sigCal, fecha)) horasDelDia.addAll(horas);
            avanzarIntervalo(sigCal);
        }

        return horasDelDia;
    }

    public List<Timestamp> getFechaHorasDia(Calendar fechaSeleccionada){
        List<Timestamp> fechas = new ArrayList<>();

        if (horas == null || horas.isEmpty() || sigIngesta == null || fechaSeleccionada == null) {
            return fechas;
        }

        // Clonamos la fecha para no modificarla
        Calendar fecha = (Calendar) fechaSeleccionada.clone();
        Utils.limpiarHora(fecha);

        // Creamos un calendario con la fecha de la próxima ingesta
        Calendar sigCal = Calendar.getInstance();
        sigCal.setTime(sigIngesta.toDate());
        Utils.limpiarHora(sigCal);

        // Evitamos bucles infinitos
        Calendar limite = (Calendar) fecha.clone();
        limite.add(Calendar.YEAR, 2);

        while (!sigCal.after(fecha) && sigCal.before(limite)) {
            if (mismoDia(sigCal, fecha)) {
                for (Hora h : horas) { //ponemos las fechas
                    Calendar calHora = (Calendar) fecha.clone();
                    calHora.set(Calendar.HOUR_OF_DAY, h.getHora());
                    calHora.set(Calendar.MINUTE, h.getMin());
                    calHora.set(Calendar.SECOND, 0);
                    calHora.set(Calendar.MILLISECOND, 0);

                    fechas.add(new Timestamp(calHora.getTime()));
                }
                break;
            }
            avanzarIntervalo(sigCal);
        }

        return fechas;
    }





    //todo llamar cuando usuario indique que se ha tomado x pastilla
    public void actualizarSigIngesta() {
        if (sigIngesta == null) return;

        Calendar cal = Calendar.getInstance(); //instanciamos calendario
        cal.setTime(sigIngesta.toDate());
        avanzarIntervalo(cal); //avanzamos en el intervalo

        sigIngesta = new Timestamp(cal.getTime());
    }
}
