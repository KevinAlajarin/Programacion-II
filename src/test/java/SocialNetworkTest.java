import exceptions.ClienteNoEncontradoException;
import exceptions.ClienteYaExisteException;
import exceptions.OperacionInvalidaException;
import exceptions.SocialNetworkException;
import models.Cliente;
import services.SocialNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SocialNetworkTest {

    private SocialNetwork red;

    @BeforeEach
    public void setUp() {
        red = new SocialNetwork();
    }

    // ==========================================
    // --- ITERACION 1: TESTS ORIGINALES (MANTENIDOS) ---
    // ==========================================

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

    @Test
    public void testBuscarPorScoring_AVLTreeLogic() throws ClienteYaExisteException {
        // Arrange
        red.agregarCliente("Alto", 80);
        red.agregarCliente("OtroAlto", 80); // Prueba de colision en nodo del árbol
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
    public void testSolicitudesFIFO_Buzones() throws Exception {
        // En Iteración 2, probamos que lleguen al buzón de destino
        red.agregarCliente("A", 10);
        red.agregarCliente("B", 10);
        red.agregarCliente("C", 10);

        // A quiere seguir a B (Buzón de B)
        red.enviarSolicitud("A", "B");
        // C quiere seguir a B (Buzón de B)
        red.enviarSolicitud("C", "B");

        List<String> resultados = red.procesarSolicitudes();

        assertEquals(2, resultados.size());
        // Verificamos que se procesaron
        assertTrue(resultados.get(0).contains("A") || resultados.get(1).contains("A"));
    }

    @Test
    public void testDeshacer_EliminaClienteDelMapaYArbol() throws Exception {
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
        assertTrue(red.buscarPorScoring(10).isEmpty(), "Debe eliminarse del Árbol AVL (Lista vacia)");

        assertDoesNotThrow(() -> red.deshacerUltimaAccion());
    }

    // ==========================================
    // --- ITERACION 2: NUEVOS REQUERIMIENTOS ---
    // ==========================================

    @Test
    public void testRestriccionMaxDosAmigos_Unitario() {
        // Prueba unitaria directa sobre la clase Cliente para validar la regla de negocio
        Cliente c = new Cliente("Kevin", 100);
        Cliente a1 = new Cliente("A1", 50);
        Cliente a2 = new Cliente("A2", 50);
        Cliente a3 = new Cliente("A3", 50);

        c.agregarSeguido(a1); // 1 amigo
        c.agregarSeguido(a2); // 2 amigos (Max)

        // El tercer intento debe lanzar excepción IllegalStateException
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            c.agregarSeguido(a3);
        });

        assertTrue(exception.getMessage().contains("ya sigue al máximo de 2 personas"));
    }

    @Test
    public void testNivelCuatro_BFS() throws SocialNetworkException {
        // Setup de la Cadena: Alice -> Bob -> Charlie -> David -> Eve
        // Eve es el nivel 4 (Tataranieto) de Alice.

        red.agregarCliente("Alice", 100);
        red.agregarCliente("Bob", 100);
        red.agregarCliente("Charlie", 100);
        red.agregarCliente("David", 100);
        red.agregarCliente("Eve", 100);

        // Creamos las relaciones
        red.enviarSolicitud("Alice", "Bob");
        red.enviarSolicitud("Bob", "Charlie");
        red.enviarSolicitud("Charlie", "David");
        red.enviarSolicitud("David", "Eve");

        // Procesamos todas las colas para consolidar amistades
        red.procesarSolicitudes();

        // --- CAPTURA DE CONSOLA ---
        // Iniciamos la captura DESPUÉS de procesar solicitudes para no capturar los logs de "Aceptada..."
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Ejecutar BFS desde Alice
        if (red instanceof SocialNetwork) {
            ((SocialNetwork) red).analizarNivelCuatro("Alice");
        }

        // Recuperar lo que se imprimió
        String salida = outContent.toString();

        // Restaurar salida normal para seguir viendo cosas en consola
        System.setOut(System.out);

        // --- VALIDACIONES ---

        // 1. Debe encontrar a Eve (Nivel 4)
        assertTrue(salida.contains("Eve"), "El reporte debería incluir a 'Eve' (Nivel 4)");

        // 2. NO debe encontrar a Charlie (Nivel 2)
        // Ahora usamos "Charlie" que no se confunde con la palabra "Clientes"
        assertFalse(salida.contains("Charlie"), "El reporte NO debería incluir a 'Charlie' (Nivel 2)");

        // 3. Verificación extra
        assertFalse(salida.contains("No hay nadie"), "Debería encontrar resultados");
    }
}