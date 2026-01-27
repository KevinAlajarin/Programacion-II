package exceptions;

public class ClienteYaExisteException extends SocialNetworkException {
    public ClienteYaExisteException(String nombre) {
        super("El cliente '" + nombre + "' ya existe en el sistema.");
    }
}