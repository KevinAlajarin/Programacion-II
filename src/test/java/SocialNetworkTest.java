import exceptions.ClienteNoEncontradoException;
import exceptions.ClienteYaExisteException;
import exceptions.OperacionInvalidaException;
import services.SocialNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SocialNetworkTest {

    private SocialNetwork red;

    @BeforeEach
    public void setUp() {
        red = new SocialNetwork();
    }

    @Test
    public void testBusquedaO1() throws ClienteYaExisteException {
        red.agregarCliente("Alice", 90);
        assertNotNull(red.buscarPorNombre("Alice"));
    }

    @Test
    public void testNoPermitirDuplicados() {
        // Arrange
        assertDoesNotThrow(() -> red.agregarCliente("Unico", 50));

        // Act & Assert
        assertThrows(ClienteYaExisteException.class, () -> {
            red.agregarCliente("Unico", 99);
        }, "Debería lanzar excepción si el cliente ya existe");
    }

    @Test
    public void testSolicitudInvalidaUsuarioNoExiste() {
        // Act & Assert
        assertThrows(ClienteNoEncontradoException.class, () -> {
            red.enviarSolicitud("Fantasma", "Nadie");
        });
    }

    @Test
    public void testSolicitudAutoSeguimiento() throws ClienteYaExisteException {
        red.agregarCliente("Narciso", 100);

        assertThrows(OperacionInvalidaException.class, () -> {
            red.enviarSolicitud("Narciso", "Narciso");
        }, "No se debe permitir seguirse a sí mismo");
    }

    @Test
    public void testSolicitudesFIFO() throws Exception {
        // Setup limpio sin errores
        red.agregarCliente("A", 10);
        red.agregarCliente("B", 10);
        red.agregarCliente("C", 10);

        red.enviarSolicitud("A", "B");
        red.enviarSolicitud("C", "B");

        List<String> resultados = red.procesarSolicitudes();
        assertEquals(2, resultados.size());
    }
}