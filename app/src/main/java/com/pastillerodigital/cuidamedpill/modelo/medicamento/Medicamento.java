package com.pastillerodigital.cuidamedpill.modelo.medicamento;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.Persistible;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Hora;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Horario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Utils;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Medicamentos
 * Numerosos atributos son opcionales
 */
public class Medicamento implements Persistible {

    //Atributos que aparecerán en la base de datos
    private String nombreMed;
    private String colorSimb;

    private String tipoMedStr;

    //Opcionales:
    private Timestamp fechaCad;
    private Timestamp fechaFin;
    private Timestamp fechaInicio;
    private int nMedRestantes; //medicinas restantes que le quedan al usuario
    private Horario horario;
    private String notasMed;

    //Atributos que NO estarán en firebase directamente pero que el objeto almacenará.
    @Exclude
    private String idMed;
    @Exclude
    private TipoMed tipoMed;
    @Exclude
    private List<Ingesta> lIngestas =  new ArrayList<>();

    public Medicamento(){}

    public Medicamento(String colorSimb, String tipoMedStr, Timestamp fechaCad, String nombreM,
                       Timestamp fechaFin, Timestamp fechaInicio, int nMedRestantes, Horario horario, String idM, String notasMed) {
        this.colorSimb = colorSimb;
        this.tipoMedStr = tipoMedStr;
        this.tipoMed = TipoMed.tipoMedFromString(tipoMedStr);
        this.fechaCad = fechaCad;
        this.nombreMed = nombreM;
        this.fechaFin = fechaFin;
        this.nMedRestantes = nMedRestantes;
        this.horario = horario;
        this.idMed = idM;
        this.notasMed = notasMed;
        this.fechaInicio = fechaInicio;
    }

    public String getNombreMed() {
        return nombreMed;
    }

    public void setNombreMed(String nombreMed) {
        this.nombreMed = nombreMed;
    }

    public String getColorSimb() {
        return colorSimb;
    }

    public void setColorSimb(String colorSimb) {
        this.colorSimb = colorSimb;
    }
    public Timestamp getFechaCad() {
        return fechaCad;
    }

    public void setFechaCad(Timestamp fechaCad) {
        this.fechaCad = fechaCad;
    }

    public Timestamp getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Timestamp fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getnMedRestantes() {
        return nMedRestantes;
    }

    public void setnMedRestantes(int nMedRestantes) {
        this.nMedRestantes = nMedRestantes;
    }

    public Horario getHorario() {
        return horario;
    }

    public void setHorario(Horario horario) {
        this.horario = horario;
    }

    public String getTipoMedStr() {
        return tipoMedStr;
    }

    public void setTipoMedStr(String tipoMedStr) {
        this.tipoMedStr = tipoMedStr;
    }

    public String getNotasMed() {
        return notasMed;
    }

    public void setNotasMed(String notasMed) {
        this.notasMed = notasMed;
    }

    public Timestamp getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Timestamp fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    @Exclude
    @Override
    public String getId() {
        return idMed;
    }

    @Exclude
    @Override
    public void setId(String idM) {
        this.idMed = idM;
    }
    @Exclude
    public TipoMed getTipoMed() {
        return tipoMed;
    }
    @Exclude
    public void setTipoMed(TipoMed tipoMed) {
        this.tipoMed = tipoMed;
    }
    @Exclude
    public List<Ingesta> getlIngestas() {
        return lIngestas;
    }
    @Exclude
    public void setlIngestas(List<Ingesta> lIngestas) {
        this.lIngestas = lIngestas;
    }

    public static Medicamento doctoObj(DocumentSnapshot doc){
        Medicamento med = new Medicamento();

        med.setNombreMed(doc.getString(Constantes.MED_NOMBREMED));
        med.setColorSimb(doc.getString(Constantes.MED_COLORSIMB));
        med.setTipoMedStr(doc.getString(Constantes.MED_TIPOMEDSTR));
        med.setTipoMed(TipoMed.tipoMedFromString(med.getTipoMedStr()));

        med.setId(doc.getId());

        //Atributos opcionales:
        med.setNotasMed(doc.getString(Constantes.MED_NOTASMED));
        med.setFechaCad(doc.getTimestamp(Constantes.MED_FECHACAD));
        med.setFechaFin(doc.getTimestamp(Constantes.MED_FECHAFIN));
        med.setFechaInicio(doc.getTimestamp(Constantes.MED_FECHAINICIO));
        Long nCajasMed = doc.getLong(Constantes.MED_NMEDRESTANTES);
        if (nCajasMed != null) med.setnMedRestantes(nCajasMed.intValue());
        else med.setnMedRestantes(-1);

        Map<String, Object> horarioMap = doc.getData().containsKey(Constantes.MED_HORARIO) ?
                (Map<String, Object>) doc.get(Constantes.MED_HORARIO) : null;
        Horario horarioObj = Horario.mapToObj(horarioMap);
        med.setHorario(horarioObj);

        return med;
    }


    /**
     *Devuelve true si hay alguna ingesta programada o registrada para el dia dado
     * @param fechaSeleccionada
     * @return
     */
    public boolean hayIngestaDia(Calendar fechaSeleccionada) {
        if (fechaSeleccionada == null) return false;

        for (Ingesta ing : lIngestas) {//Revisar ingestas registradas
            if (ing.getFechaProgramada() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(ing.getFechaProgramada().toDate());
                Utils.limpiarHora(cal);
                if (Utils.mismoDia(cal, fechaSeleccionada)) return true;
            }
        }

        if (horario != null) {//Revisar horario
            List<Timestamp> horas = horario.getFechaHorasDia(fechaSeleccionada,
                    (fechaInicio != null) ? Utils.timestampToCalendar(fechaInicio): null);
            return !horas.isEmpty();
        }

        return false;
    }


    /**
     * Devuelve ingestas del medicamento de un día seleccionado
     * @param dia
     * @return
     */
    public List<Ingesta> getIngestasPorDia(Calendar dia) {
        List<Ingesta> resultado = new ArrayList<>();
        if (lIngestas == null || dia == null) return resultado;

        for (Ingesta ing : lIngestas) {
            //Se tiene en cuenta las fechas programadas y las no programadas
            Timestamp fechaBase = (ing.getFechaProgramada() != null) ? ing.getFechaProgramada() : ing.getFechaIngesta();
            if (fechaBase != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(fechaBase.toDate());
                if (Utils.mismoDia(cal, dia)) resultado.add(ing);
            }
        }
        return resultado;
    }

    /**
     * Calcula las ingestas con estado pendiente en un día
     * @param dia
     * @param horasProgramadas
     * @return
     */
    public List<Ingesta> getIngestasPendientesDia(Calendar dia, List<Timestamp> horasProgramadas) {
        List<Ingesta> ingestasExistentes = getIngestasPorDia(dia);

        List<Ingesta> pendientes = new ArrayList<>();

        if (horasProgramadas == null) return pendientes;
        if (ingestasExistentes == null) ingestasExistentes = new ArrayList<>();

        for (Timestamp horaProgramada : horasProgramadas) { //para cada hora programada del medicamento hoy
            Ingesta ingEncontrada = null;

            for (Ingesta ing : ingestasExistentes) {// Buscar si ya existe ingesta para esa hora
                if (ing.getFechaProgramada() == null) continue;
                if (Utils.mismaFechaHoraMinuto(ing.getFechaProgramada(), horaProgramada)) {
                    ingEncontrada = ing;
                    break;
                }
            }

            if (ingEncontrada == null) { //si no existe la ingesta se crea una pendiente
                Ingesta nuevaPendiente = new Ingesta(horaProgramada, new Timestamp(new java.util.Date()),
                        EstadoIngesta.PENDIENTE.toString(), this, null);
                pendientes.add(nuevaPendiente);
                continue;
            }

            // Si existe pero está pendiente
            if (ingEncontrada.getEstadoIngesta() == EstadoIngesta.PENDIENTE) {
                pendientes.add(ingEncontrada);
            }
        }

        return pendientes;
    }

    /**
     * Devuelve ingestas de un día, creando más si es necesario
     * @param fecha
     * @param tipoDiaObj
     * @return
     */

    public List<Ingesta> generarIngestasFecha(Calendar fecha, Object tipoDiaObj){
        List<Ingesta> existentes = getIngestasPorDia(fecha);
        List<Ingesta> resultado = new ArrayList<>();
        if(existentes != null) resultado.addAll(existentes);

        if(horario == null) return resultado;
        List<Timestamp> horasProgramadas = horario.getFechaHorasDia(fecha, Utils.timestampToCalendar(fechaInicio));
        if(horasProgramadas == null || horasProgramadas.isEmpty()) return resultado;

        for(Timestamp horaProg : horasProgramadas){
            Ingesta existente = buscarIngesta(existentes, horaProg);

            if(existente != null){
                resultado.add(existente);
                continue;
            }

            // Determinar estado según tipo día
            String tipoDia = tipoDiaObj.toString();
            String estado;

            if(tipoDia.contains("PASADO")){
                estado = EstadoIngesta.OLVIDO.toString();
            }
            else{
                estado = EstadoIngesta.PENDIENTE.toString();
            }

            Ingesta nueva = new Ingesta(horaProg, null, estado, this, null);
            lIngestas.add(nueva);
            resultado.add(nueva);
        }

        return resultado;
    }

    /**
     * Busca ingesta que coincida con la hora
     * @param lista
     * @param hora
     * @return
     */
    private Ingesta buscarIngesta(List<Ingesta> lista, Timestamp hora){
        if(lista == null) return null;
        for(Ingesta ing : lista){
            if(ing.getFechaProgramada() == null) continue;
            if(Utils.mismaFechaHoraMinuto(ing.getFechaProgramada(), hora)) return ing;
        }
        return null;
    }

    public List<Timestamp> getFechaHorasDia(Calendar fechaSeleccionada){
        if (horario == null) return new ArrayList<>();
        return horario.getFechaHorasDia(fechaSeleccionada,
                (fechaInicio != null) ? Utils.timestampToCalendar(fechaInicio): null);
    }

    public void actualizarSigIngesta(Ingesta ingTomada) {
        if (horario == null) return;

        // Obtener todas las horas del día de la ingesta tomada
        Calendar fechaIngesta = Calendar.getInstance();
        fechaIngesta.setTime(ingTomada.getFechaProgramada().toDate());
        List<Timestamp> horasHoy = horario.getFechaHorasDia(fechaIngesta, (fechaInicio != null) ? Utils.timestampToCalendar(fechaInicio) : null);

        // Buscar la próxima ingesta en la lista de horas de hoy
        Timestamp proxima = null;
        for (Timestamp t : horasHoy) {
            if (t.toDate().after(ingTomada.getFechaProgramada().toDate())) {
                proxima = t;
                break;
            }
        }

        // Si no queda ninguna hoy, avanzar al siguiente día según el intervalo
        if (proxima == null) {
            Calendar sig = (fechaIngesta != null) ? (Calendar) fechaIngesta.clone() : Calendar.getInstance();

            // Avanzar hasta la próxima fecha según el horario
            Calendar limite = (Calendar) sig.clone();
            limite.add(Calendar.YEAR, 2); // prevenir loop infinito
            do {
                horario.avanzarIntervalo(sig);
                List<Timestamp> horasSig = horario.getFechaHorasDia(sig,
                        (fechaInicio != null) ? Utils.timestampToCalendar(fechaInicio) : null);
                if (!horasSig.isEmpty()) {
                    proxima = horasSig.get(0);
                    break;
                }
            } while (sig.before(limite));
        }

        if (proxima != null) horario.setSigIngesta(proxima);
    }


}
