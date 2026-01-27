package exceptions;

public class ClienteNoEncontradoException extends SocialNetworkException {
    public ClienteNoEncontradoException(String nombre) {
        super("El cliente '" + nombre + "' no fue encontrado.");
    }
}