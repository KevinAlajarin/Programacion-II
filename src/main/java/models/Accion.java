package models;

public class Accion {
    public enum TipoAccion {
        AGREGAR_CLIENTE,
        SEGUIR_USUARIO,
        ELIMINAR_CLIENTE  // <--- NUEVO TIPO
    }

    private TipoAccion tipo;
    private String sujeto; // Quien realiza la acción o el afectado principal
    private String objeto; // El destinatario (ej. a quien sigue)

    // NUEVO: Para guardar el estado anterior (Snapshot/Memento)
    private Object respaldo;

    public Accion(TipoAccion tipo, String sujeto, String objeto) {
        this.tipo = tipo;
        this.sujeto = sujeto;
        this.objeto = objeto;
    }

    // Constructor sobrecargado para cuando tenemos respaldo
    public Accion(TipoAccion tipo, String sujeto, Object respaldo) {
        this.tipo = tipo;
        this.sujeto = sujeto;
        this.respaldo = respaldo;
    }

    public TipoAccion getTipo() { return tipo; }
    public String getSujeto() { return sujeto; }
    public String getObjeto() { return objeto; }
    public Object getRespaldo() { return respaldo; }

    @Override
    public String toString() {
        if (objeto != null) return "[" + tipo + "] " + sujeto + " -> " + objeto;
        return "[" + tipo + "] " + sujeto;
    }
}