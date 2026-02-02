import exceptions.ClienteNoEncontradoException;
import exceptions.ClienteYaExisteException;
import exceptions.OperacionInvalidaException;
import models.Cliente;
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

    // --- REQ 1: Busqueda y Gestion de Clientes ---

    @Test
    public void testBusquedaO1_PorNombre() throws ClienteYaExisteException {
        red.agregarCliente("Alice", 90);
        assertNotNull(red.buscarPorNombre("Alice"), "El cliente deberia encontrarse en el HashMap");
        assertEquals(90, red.buscarPorNombre("Alice").getScoring());
    }

    @Test
    public void testNoPermitirDuplicados() {
        assertDoesNotThrow(() -> red.agregarCliente("Unico", 50));
        assertThrows(ClienteYaExisteException.class, () -> {
            red.agregarCliente("Unico", 99);
        }, "Deberia lanzar excepcion si el cliente ya existe");
    }

    // --- TEST CORREGIDO: Busqueda por Scoring (Retorna Lista) ---
    @Test
    public void testBuscarPorScoring_MapLogic() throws ClienteYaExisteException {
        // Arrange
        red.agregarCliente("Alto", 80);
        red.agregarCliente("OtroAlto", 80); // Prueba de colision
        red.agregarCliente("Bajo", 20);

        // Act & Assert

        // 1. Busqueda exitosa (Scoring 80) - Debe retornar lista con 2 elementos
        List<Cliente> resultados80 = red.buscarPorScoring(80);
        assertNotNull(resultados80);
        assertEquals(2, resultados80.size(), "Deberia haber 2 clientes con scoring 80");

        // Verificamos que los clientes correctos estén en la lista
        boolean estaAlto = resultados80.stream().anyMatch(c -> c.getNombre().equals("Alto"));
        boolean estaOtroAlto = resultados80.stream().anyMatch(c -> c.getNombre().equals("OtroAlto"));

        assertTrue(estaAlto, "La lista debe contener a Alto");
        assertTrue(estaOtroAlto, "La lista debe contener a OtroAlto");

        // 2. Busqueda fallida (Scoring no existe)
        List<Cliente> resultados99 = red.buscarPorScoring(99);
        assertNotNull(resultados99);
        assertTrue(resultados99.isEmpty(), "Si el scoring no existe, debe retornar lista vacia");
    }

    // --- REQ 2: Solicitudes ---

    @Test
    public void testSolicitudInvalidaUsuarioNoExiste() {
        assertThrows(ClienteNoEncontradoException.class, () -> {
            red.enviarSolicitud("Fantasma", "Nadie");
        });
    }

    @Test
    public void testSolicitudAutoSeguimiento() throws ClienteYaExisteException {
        red.agregarCliente("Narciso", 100);
        assertThrows(OperacionInvalidaException.class, () -> {
            red.enviarSolicitud("Narciso", "Narciso");
        });
    }

    @Test
    public void testSolicitudesFIFO() throws Exception {
        red.agregarCliente("A", 10);
        red.agregarCliente("B", 10);
        red.agregarCliente("C", 10);

        red.enviarSolicitud("A", "B");
        red.enviarSolicitud("C", "B");

        List<String> resultados = red.procesarSolicitudes();

        assertEquals(2, resultados.size());
        assertTrue(resultados.get(0).contains("A"));
        assertTrue(resultados.get(1).contains("C"));
    }

    // --- TEST CORREGIDO: Deshacer ---
    @Test
    public void testDeshacer_EliminaClienteDelMapa() throws Exception {
        // Arrange
        String nombre = "Error";
        red.agregarCliente(nombre, 10);

        // Verificar antes
        assertNotNull(red.buscarPorNombre(nombre));
        assertFalse(red.buscarPorScoring(10).isEmpty());

        // Act
        red.deshacerUltimaAccion();

        // Assert
        assertNull(red.buscarPorNombre(nombre), "Debe eliminarse del Map de Nombres");
        assertTrue(red.buscarPorScoring(10).isEmpty(), "Debe eliminarse del Map de Scoring (Lista vacia)");

        assertDoesNotThrow(() -> red.deshacerUltimaAccion());
    }

    @Test
    public void testEstadoInicialVacio() {
        assertNull(red.buscarPorNombre("Nadie"));
        assertTrue(red.procesarSolicitudes().isEmpty());
    }
}