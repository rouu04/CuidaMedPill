package com.pastillerodigital.cuidamedpill.modelo.medicamento;

public class MedHorasDisplay {
    private Medicamento medicamento;
    private String hora;
    private Ingesta ingesta; // Puede ser null si aún no se ha registrado

    public MedHorasDisplay(Medicamento medicamento, String hora, Ingesta ingesta) {
        this.medicamento = medicamento;
        this.hora = hora;
        this.ingesta = ingesta;
    }

    // Getters
    public Medicamento getMedicamento() { return medicamento; }
    public String getHora() { return hora; }
    public Ingesta getIngesta() { return ingesta; }

    public boolean estaCompletado() {
        return ingesta != null && "COMPLETADO".equals(ingesta.getEstadoIngestaStr());
    }
}
