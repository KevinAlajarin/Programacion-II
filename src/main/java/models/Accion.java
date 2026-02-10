package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Accion {
    public enum TipoAccion {
        AGREGAR_CLIENTE,
        SEGUIR_USUARIO,
        ELIMINAR_CLIENTE
    }

    private TipoAccion tipo;
    private String sujeto;
    private String objeto;
    private Object respaldo;

    // --- NUEVO: Requisito de Fecha y Hora ---
    private LocalDateTime fecha;

    public Accion(TipoAccion tipo, String sujeto, String objeto) {
        this.tipo = tipo;
        this.sujeto = sujeto;
        this.objeto = objeto;
        this.fecha = LocalDateTime.now(); // Se guarda el momento exacto
    }

    public Accion(TipoAccion tipo, String sujeto, Object respaldo) {
        this.tipo = tipo;
        this.sujeto = sujeto;
        this.respaldo = respaldo;
        this.fecha = LocalDateTime.now(); // Se guarda el momento exacto
    }

    public TipoAccion getTipo() { return tipo; }
    public String getSujeto() { return sujeto; }
    public String getObjeto() { return objeto; }
    public Object getRespaldo() { return respaldo; }

    @Override
    public String toString() {
        // Formato bonito: [18:30:05] AGREGAR_CLIENTE...
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        String tiempo = "[" + fecha.format(fmt) + "] ";

        if (objeto != null) return tiempo + tipo + ": " + sujeto + " -> " + objeto;
        return tiempo + tipo + ": " + sujeto;
    }
}