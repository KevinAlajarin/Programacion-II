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

    // NUEVO: Consulta específica de un solo usuario
    void mostrarConexionesDe(String nombre) throws ClienteNoEncontradoException;
}