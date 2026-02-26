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
        red.agregarCliente("Alto", 80);
        red.agregarCliente("OtroAlto", 80);
        red.agregarCliente("Bajo", 20);

        List<Cliente> resultados80 = red.buscarPorScoring(80);
        assertNotNull(resultados80);
        assertEquals(2, resultados80.size(), "Deberia haber 2 clientes con scoring 80");

        boolean estaAlto = resultados80.stream().anyMatch(c -> c.getNombre().equals("Alto"));
        boolean estaOtroAlto = resultados80.stream().anyMatch(c -> c.getNombre().equals("OtroAlto"));

        assertTrue(estaAlto, "La lista debe contener a Alto");
        assertTrue(estaOtroAlto, "La lista debe contener a OtroAlto");

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
        red.agregarCliente("A", 10);
        red.agregarCliente("B", 10);
        red.agregarCliente("C", 10);

        red.enviarSolicitud("A", "B");
        red.enviarSolicitud("C", "B");

        List<String> resultados = red.procesarSolicitudes();

        assertEquals(2, resultados.size());
        assertTrue(resultados.get(0).contains("A") || resultados.get(1).contains("A"));
    }

    @Test
    public void testDeshacer_EliminaClienteDelMapaYArbol() throws Exception {
        String nombre = "Error";
        red.agregarCliente(nombre, 10);

        assertNotNull(red.buscarPorNombre(nombre));
        assertFalse(red.buscarPorScoring(10).isEmpty());

        red.deshacerUltimaAccion();

        assertNull(red.buscarPorNombre(nombre), "Debe eliminarse del Map de Nombres");
        assertTrue(red.buscarPorScoring(10).isEmpty(), "Debe eliminarse del Árbol AVL (Lista vacia)");
        assertDoesNotThrow(() -> red.deshacerUltimaAccion());
    }

    // ==========================================
    // --- ITERACION 2: NUEVOS REQUERIMIENTOS ---
    // ==========================================

    @Test
    public void testRestriccionMaxDosAmigos_Unitario() {
        Cliente c = new Cliente("Kevin", 100);
        Cliente a1 = new Cliente("A1", 50);
        Cliente a2 = new Cliente("A2", 50);
        Cliente a3 = new Cliente("A3", 50);

        c.agregarSeguido(a1);
        c.agregarSeguido(a2);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            c.agregarSeguido(a3);
        });

        assertTrue(exception.getMessage().contains("ya sigue al máximo de 2 personas"));
    }

    @Test
    public void testNivel_BFS_Dinamico() throws SocialNetworkException {
        red.agregarCliente("Alice", 100);
        red.agregarCliente("Bob", 100);
        red.agregarCliente("Charlie", 100);
        red.agregarCliente("David", 100);
        red.agregarCliente("Eve", 100);

        red.enviarSolicitud("Alice", "Bob");
        red.enviarSolicitud("Bob", "Charlie");
        red.enviarSolicitud("Charlie", "David");
        red.enviarSolicitud("David", "Eve");
        red.procesarSolicitudes();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // ACTUALIZADO: Llamamos al método dinámico, pidiendo el nivel 4
        if (red instanceof SocialNetwork) {
            ((SocialNetwork) red).analizarNivel("Alice", 4);
        }

        String salida = outContent.toString();
        System.setOut(System.out);

        assertTrue(salida.contains("Eve"), "El reporte debería incluir a 'Eve' (Nivel 4)");
        assertFalse(salida.contains("Charlie"), "El reporte NO debería incluir a 'Charlie' (Nivel 2)");
        assertFalse(salida.contains("No hay nadie"), "Debería encontrar resultados");
    }

    // ==========================================
    // --- ITERACION 3: GRAFOS Y AMISTADES    ---
    // ==========================================

    @Test
    public void testCrearAmistadBidireccional() throws Exception {
        red.agregarCliente("A", 50);
        red.agregarCliente("B", 50);

        red.crearAmistad("A", "B");

        Cliente clienteA = red.buscarPorNombre("A");
        Cliente clienteB = red.buscarPorNombre("B");

        // Verificamos que se agregaron mutuamente
        assertTrue(clienteA.getAmigos().contains(clienteB), "A debe tener a B como amigo");
        assertTrue(clienteB.getAmigos().contains(clienteA), "B debe tener a A como amigo");

        // Verificamos que la estructura HashSet impidió duplicados si intentamos de nuevo
        red.crearAmistad("B", "A");
        assertEquals(1, clienteA.getAmigos().size(), "El tamaño debe seguir siendo 1 (sin duplicados)");
    }

    @Test
    public void testCrearAmistadAutoBucle() throws Exception {
        red.agregarCliente("Solo", 50);
        assertThrows(IllegalArgumentException.class, () -> {
            red.crearAmistad("Solo", "Solo");
        }, "No debería permitir ser amigo de uno mismo");
    }

    @Test
    public void testCalcularDistancia_BFS_Caminos() throws Exception {
        // Setup Grafo: A - B - C - D
        //              |
        //              E
        red.agregarCliente("A", 10);
        red.agregarCliente("B", 10);
        red.agregarCliente("C", 10);
        red.agregarCliente("D", 10);
        red.agregarCliente("E", 10);

        red.crearAmistad("A", "B");
        red.crearAmistad("B", "C");
        red.crearAmistad("C", "D");
        red.crearAmistad("A", "E");

        // 1. Mismo nodo = 0
        assertEquals(0, red.calcularDistancia("A", "A"));

        // 2. Distancia Directa = 1
        assertEquals(1, red.calcularDistancia("A", "B"));
        assertEquals(1, red.calcularDistancia("A", "E"));

        // 3. Distancia Larga = 3
        assertEquals(3, red.calcularDistancia("A", "D"), "Debe tomar 3 saltos llegar a D");

        // 4. Camino inverso = 2 (Como es no dirigido, C hacia A debe funcionar igual)
        assertEquals(2, red.calcularDistancia("C", "A"));
    }

    @Test
    public void testCalcularDistancia_SinConexion() throws Exception {
        // Grafo dividido en dos islas: (A - B) y (C - D)
        red.agregarCliente("A", 10);
        red.agregarCliente("B", 10);
        red.agregarCliente("C", 10);
        red.agregarCliente("D", 10);

        red.crearAmistad("A", "B");
        red.crearAmistad("C", "D");

        int saltos = red.calcularDistancia("A", "D");
        assertEquals(-1, saltos, "Si no hay conexión, debe retornar -1");
    }
}