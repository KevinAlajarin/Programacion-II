package models;

public class Accion {
    public enum TipoAccion {
        AGREGAR_CLIENTE,
        SEGUIR_USUARIO
    }

    private TipoAccion tipo;
    private String sujeto;
    private String objeto;

    public Accion(TipoAccion tipo, String sujeto, String objeto) {
        this.tipo = tipo;
        this.sujeto = sujeto;
        this.objeto = objeto;
    }

    public TipoAccion getTipo() { return tipo; }
    public String getSujeto() { return sujeto; }
    public String getObjeto() { return objeto; }

    @Override
    public String toString() {
        return "[" + tipo + "] " + sujeto + (objeto != null ? " -> " + objeto : "");
    }
}