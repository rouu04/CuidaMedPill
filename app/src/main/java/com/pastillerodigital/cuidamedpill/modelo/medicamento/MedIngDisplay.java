package com.pastillerodigital.cuidamedpill.modelo.medicamento;

public class MedIngDisplay {
    private Medicamento medicamento;
    private Ingesta ingesta; // Puede ser null si aún no se ha registrado

    public MedIngDisplay(Medicamento medicamento, Ingesta ingesta) {
        this.medicamento = medicamento;
        this.ingesta = ingesta;
    }

    // Getters
    public Medicamento getMedicamento() { return medicamento; }
    public Ingesta getIngesta() { return ingesta; }

    public boolean estaCompletado() {
        return ingesta != null && "COMPLETADO".equals(ingesta.getEstadoIngestaStr());
    }
}
