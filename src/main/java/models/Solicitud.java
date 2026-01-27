package models;

public class Solicitud {
    private String solicitante;
    private String solicitado;
    private long timestamp;

    public Solicitud(String solicitante, String solicitado) {
        this.solicitante = solicitante;
        this.solicitado = solicitado;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSolicitante() { return solicitante; }
    public String getSolicitado() { return solicitado; }

    @Override
    public String toString() {
        return "Solicitud: " + solicitante + " quiere seguir a " + solicitado;
    }
}