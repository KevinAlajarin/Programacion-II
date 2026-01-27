package services;

import models.Accion;
import java.util.Stack;

public class ActionHistory {
    private Stack<Accion> historial;

    public ActionHistory() {
        this.historial = new Stack<>();
    }

    public void registrarAccion(Accion accion) {
        historial.push(accion);
    }

    public Accion deshacer() {
        if (historial.isEmpty()) return null;
        return historial.pop();
    }

    public boolean isEmpty() {
        return historial.isEmpty();
    }
}