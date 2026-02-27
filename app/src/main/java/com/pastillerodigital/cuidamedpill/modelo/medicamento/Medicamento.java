package com.pastillerodigital.cuidamedpill.modelo.medicamento;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.pastillerodigital.cuidamedpill.modelo.Persistible;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoMed;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.horario.Horario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

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

    public static Map<String, Object> toMap(Medicamento med) {
        Map<String, Object> map = new HashMap<>();

        map.put(Constantes.MED_NOMBREMED, med.getNombreMed());
        map.put(Constantes.MED_COLORSIMB, med.getColorSimb());
        map.put(Constantes.MED_TIPOMEDSTR, med.getTipoMedStr());
        map.put(Constantes.MED_FECHACAD, med.getFechaCad());
        map.put(Constantes.MED_FECHAFIN, med.getFechaFin());
        map.put(Constantes.MED_NMEDRESTANTES, med.getnMedRestantes());
        map.put(Constantes.MED_NOTASMED, med.getNotasMed());

        if(med.getHorario() != null) {
            map.put(Constantes.MED_HORARIO, med.getHorario().toMap());
        }

        return map;
    }

    public boolean hayIngestaDia(Calendar fechaSeleccionada) {
        if (fechaSeleccionada == null) return false;

        Calendar fecha = (Calendar) fechaSeleccionada.clone();
        limpiarHora(fecha);

        // 1Si hay horario, comprobarlo
        if (horario != null && horario.getSigIngesta() != null) {
            if (hayIngestaPorHorario(fecha)) {
                return true;
            }
        }

        // Comprobar ingestas registradas manualmente
        if (lIngestas != null) {
            Calendar calIng = Calendar.getInstance();

            for (Ingesta ing : lIngestas) {
                if (ing.getFechaProgramada() == null) continue;

                calIng.setTime(ing.getFechaProgramada().toDate());
                limpiarHora(calIng);

                if (mismoDia(calIng, fecha)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hayIngestaPorHorario(Calendar fechaSeleccionada) {

        Calendar sigIngestaCal = Calendar.getInstance();
        sigIngestaCal.setTime(horario.getSigIngesta().toDate());
        limpiarHora(sigIngestaCal);

        // Evitar loops infinitos
        Calendar limite = (Calendar) fechaSeleccionada.clone();
        limite.add(Calendar.YEAR, 2);

        while (!sigIngestaCal.after(fechaSeleccionada) && sigIngestaCal.before(limite)) {
            if (mismoDia(sigIngestaCal, fechaSeleccionada)) return true;
            avanzarIntervaloHorario(sigIngestaCal);
        }

        return mismoDia(sigIngestaCal, fechaSeleccionada);
    }

    private void avanzarIntervaloHorario(Calendar cal) {
        switch (horario.getTipoIntervalo()) {
            case DIARIO:
                cal.add(Calendar.DAY_OF_YEAR, horario.getIntervalo());
                break;
            case SEMANAL:
                cal.add(Calendar.WEEK_OF_YEAR, horario.getIntervalo());
                break;
            case QUINCENAL:
                cal.add(Calendar.WEEK_OF_YEAR, 2 * horario.getIntervalo());
                break;
            case MENSUAL:
                cal.add(Calendar.MONTH, horario.getIntervalo());
                break;
            case TRIMESTRAL:
                cal.add(Calendar.MONTH, 3 * horario.getIntervalo());
                break;
            case ANUAL:
                cal.add(Calendar.YEAR, horario.getIntervalo());
                break;
        }
    }

    private boolean mismoDia(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private void limpiarHora(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    public List<Ingesta> getIngestasPorDia(Calendar diaObjetivo) {
        List<Ingesta> resultado = new ArrayList<>();
        if (lIngestas == null || diaObjetivo == null) return resultado;

        Calendar calIng = Calendar.getInstance();
        for (Ingesta ing : lIngestas) {
            if (ing.getFechaProgramada() == null) continue;
            calIng.setTime(ing.getFechaProgramada().toDate());

            boolean mismoDia = calIng.get(Calendar.YEAR) == diaObjetivo.get(Calendar.YEAR) &&
                            calIng.get(Calendar.DAY_OF_YEAR) == diaObjetivo.get(Calendar.DAY_OF_YEAR);

            if (mismoDia)resultado.add(ing);
        }

        return resultado;
    }

    public List<Ingesta> getIngestasPendientesDia(Calendar dia, List<Timestamp> horasProgramadas) {
        List<Ingesta> ingestasExistentes = getIngestasPorDia(dia);

        List<Ingesta> pendientes = new ArrayList<>();

        if (horasProgramadas == null) return pendientes;
        if (ingestasExistentes == null) ingestasExistentes = new ArrayList<>();

        for (Timestamp horaProgramada : horasProgramadas) { //para cada hora programada del medicamento hoy
            Ingesta ingEncontrada = null;

            for (Ingesta ing : ingestasExistentes) {// Buscar si ya existe ingesta para esa hora
                if (ing.getFechaProgramada() == null) continue;
                if (mismaFechaHoraMinuto(ing.getFechaProgramada(), horaProgramada)) {
                    ingEncontrada = ing;
                    break;
                }
            }


            if (ingEncontrada == null) { //si no existe la ingesta se crea una pendiente
                Ingesta nuevaPendiente = new Ingesta(horaProgramada, new Timestamp(new java.util.Date()),
                        EstadoIngesta.PENDIENTE.toString(), this);
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

    private boolean mismaFechaHoraMinuto(Timestamp t1, Timestamp t2) {

        Calendar c1 = Calendar.getInstance();
        c1.setTime(t1.toDate());
        Calendar c2 = Calendar.getInstance();
        c2.setTime(t2.toDate());

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) &&
                c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY) &&
                c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE);
    }


    public List<Ingesta> generarIngestasFecha(Calendar fecha, Object tipoDiaObj){
        List<Ingesta> existentes = getIngestasPorDia(fecha);
        List<Ingesta> resultado = new ArrayList<>();
        if(existentes != null) resultado.addAll(existentes);

        if(horario == null) return resultado;
        List<Timestamp> horasProgramadas = horario.getFechaHorasDia(fecha);
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

            Ingesta nueva = new Ingesta(horaProg, null, estado, this);
            lIngestas.add(nueva);
            resultado.add(nueva);
        }

        return resultado;
    }

    private Ingesta buscarIngesta(List<Ingesta> lista, Timestamp hora){
        if(lista == null) return null;

        for(Ingesta ing : lista){
            if(ing.getFechaProgramada() == null) continue;
            if(mismaFechaHoraMinuto(ing.getFechaProgramada(), hora)) return ing;
        }

        return null;
    }

}
