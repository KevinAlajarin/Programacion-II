import services.SocialNetwork;
import utils.JsonLoader;
import models.Cliente;
import exceptions.SocialNetworkException; // Importar la excepción padre
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final SocialNetwork red = new SocialNetwork();

    public static void main(String[] args) {
        System.out.println("=== TPO UADE - ITERACIÓN 1 ===");
        System.out.println("   (Estructuras: Mapas, Listas, Pilas, Colas)");

        // Carga automática inicial
        // Nota: JsonLoader ya maneja sus propias excepciones internamente
        JsonLoader.cargar("datos.json", red);

        int opcion;
        do {
            mostrarMenu();
            opcion = obtenerOpcion();
            ejecutarOpcion(opcion);
        } while (opcion != 0);
    }

    private static void mostrarMenu() {
        System.out.println("\n--- MENÚ PRINCIPAL ---");
        System.out.println("1. Agregar Cliente Manual");
        System.out.println("2. Buscar Cliente por Nombre (O(1))");
        System.out.println("3. Buscar Cliente por Scoring (Índice Map)");
        System.out.println("4. Enviar Solicitud de Seguimiento");
        System.out.println("5. Procesar Cola de Solicitudes (FIFO)");
        System.out.println("6. Deshacer Última Acción (Stack)");
        System.out.println("7. Mostrar Todos los Clientes");
        System.out.println("8. Eliminar Cliente del Sistema");
        System.out.println("0. Salir");
        System.out.print(">> Seleccione: ");
    }

    private static int obtenerOpcion() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void ejecutarOpcion(int op) {
        // Bloque Try-Catch Global para el manejo de excepciones de negocio y sistema
        try {
            switch (op) {
                case 1:
                    System.out.print("Nombre: ");
                    String nombre = scanner.nextLine();
                    System.out.print("Scoring (0-100): ");
                    int score = Integer.parseInt(scanner.nextLine());

                    // Esto puede lanzar ClienteYaExisteException o IllegalArgumentException
                    red.agregarCliente(nombre, score);
                    break;

                case 2:
                    System.out.print("Nombre a buscar: ");
                    String bNombre = scanner.nextLine();
                    Cliente c1 = red.buscarPorNombre(bNombre);

                    if (c1 != null) {
                        // Formato limpio solicitado
                        System.out.println("✅ Encontrado: " + c1.getNombre() + " Scoring: " + c1.getScoring());
                    } else {
                        System.out.println("❌ Cliente no encontrado.");
                    }
                    break;

                case 3:
                    System.out.print("Scoring a buscar: ");
                    int s = Integer.parseInt(scanner.nextLine());

                    // Recibimos una LISTA (Lógica corregida para Map<Integer, List>)
                    java.util.List<Cliente> encontrados = red.buscarPorScoring(s);

                    if (!encontrados.isEmpty()) {
                        System.out.println("✅ Clientes con scoring " + s + ":");
                        for (Cliente c : encontrados) {
                            System.out.println("   -> " + c);
                        }
                    } else {
                        System.out.println("❌ Nadie tiene ese scoring exacto.");
                    }
                    break;

                case 4:
                    System.out.print("Solicitante: ");
                    String u1 = scanner.nextLine();
                    System.out.print("A quien seguir: ");
                    String u2 = scanner.nextLine();

                    // Esto puede lanzar ClienteNoEncontradoException u OperacionInvalidaException
                    red.enviarSolicitud(u1, u2);
                    break;

                case 5:
                    red.procesarSolicitudes();
                    break;

                case 6:
                    red.deshacerUltimaAccion();
                    break;

                case 7:
                    // NUEVO: Reporte general
                    red.mostrarEstadoGeneral();
                    break;

                case 8:
                    // NUEVO: Eliminación total
                    System.out.print("Nombre del cliente a eliminar: ");
                    String nombreBorrar = scanner.nextLine();

                    // Confirmación de seguridad
                    System.out.print("¿Estás seguro? Se borrará todo rastro (S/N): ");
                    String confirma = scanner.nextLine();

                    if (confirma.equalsIgnoreCase("S")) {
                        red.eliminarClienteTotalmente(nombreBorrar);
                    } else {
                        System.out.println("Operación cancelada.");
                    }
                    break;

                case 0:
                    System.out.println("Cerrando sistema...");
                    break;

                default:
                    System.out.println("Opción no válida.");
            }

            // --- MANEJO DE ERRORES ---
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Error: Debe ingresar un número entero válido.");

        } catch (SocialNetworkException e) {
            // Captura todas nuestras excepciones de negocio (ClienteYaExiste, OperacionInvalida, etc.)
            System.out.println("⛔ Error de Negocio: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            // Captura validaciones de argumentos (ej: scoring negativo)
            System.out.println("🚫 Datos Inválidos: " + e.getMessage());

        } catch (Exception e) {
            // Captura cualquier otro error inesperado (NullPointer, etc.)
            System.out.println("☠️ Error Inesperado del Sistema: " + e.getMessage());
            e.printStackTrace(); // Útil para depurar si algo explota
        }
    }
}