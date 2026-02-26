package services;

import models.Cliente;
import java.util.List;
import exceptions.ClienteYaExisteException;
import exceptions.ClienteNoEncontradoException;
import exceptions.SocialNetworkException;

public interface ISocialNetwork {
    void agregarCliente(String nombre, int scoring) throws ClienteYaExisteException;
    Cliente buscarPorNombre(String nombre);
    List<Cliente> buscarPorScoring(int scoring);
    void enviarSolicitud(String solicitante, String solicitado) throws SocialNetworkException;
    List<String> procesarSolicitudes();
    void deshacerUltimaAccion();
    void mostrarEstadoGeneral();
    void eliminarClienteTotalmente(String nombre) throws ClienteNoEncontradoException;
    void verHistorial();
    void mostrarConexionesDe(String nombre) throws ClienteNoEncontradoException;
    void analizarNivel(String nombreOrigen, int nivelObjetivo);

    // ==========================================
    // --- NUEVO ITERACIÓN 3: GRAFOS GENERALES ---
    // ==========================================

    /**
     * Crea una amistad bidireccional entre dos clientes.
     */
    void crearAmistad(String nombre1, String nombre2) throws ClienteNoEncontradoException;

    /**
     * Calcula la distancia mínima (saltos) entre dos clientes usando BFS.
     * @return Número de saltos, o -1 si no hay conexión.
     */
    int calcularDistancia(String origen, String destino) throws ClienteNoEncontradoException;
}