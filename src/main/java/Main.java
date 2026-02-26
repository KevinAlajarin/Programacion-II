import services.ISocialNetwork;
import services.SocialNetwork;
import utils.JsonLoader;
import models.Cliente;
import exceptions.SocialNetworkException;
import java.util.Scanner;
import java.util.List;
import exceptions.ClienteNoEncontradoException;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    // --- CAMBIO CLAVE PARA LA DEFENSA ---
    // Declaramos la variable 'red' usando la INTERFAZ (ISocialNetwork).
    // Esto aplica el principio de "Programar contra una interfaz, no una implementación".
    // Permite cambiar la lógica interna (la clase SocialNetwork) sin romper el Main.
    private static final ISocialNetwork red = new SocialNetwork();

    public static void main(String[] args) {
        System.out.println("=== TPO UADE - RED SOCIAL (ITERACIÓN 3) ===");
        System.out.println("   (Estructuras: AVL, Grafos Dirigidos y No Dirigidos)");

        // Carga automática inicial
        // Nota: Hacemos un cast (SocialNetwork) porque el JsonLoader original
        // probablemente espera la clase concreta. Esto es válido.
        JsonLoader.cargar("datos.json", (SocialNetwork) red);

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
        System.out.println("2. Buscar Cliente por Nombre");
        System.out.println("3. Buscar Cliente por Scoring");
        System.out.println("4. Enviar Solicitud de Seguimiento");
        System.out.println("5. Procesar Cola de Solicitudes");
        System.out.println("6. Deshacer Última Acción");
        System.out.println("7. Mostrar Todos los Clientes");
        System.out.println("8. Eliminar Cliente del Sistema");
        System.out.println("9. Ver Historial de Acciones");
        System.out.println("10. Analizar Red");
        System.out.println("11. Ver Conexiones de un Usuario");
        System.out.println("12. Crear Amistad Bidireccional");
        System.out.println("13. Calcular Distancia entre Amigos (BFS)");
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
        // Bloque Try-Catch Global para asegurar la disponibilidad del sistema
        try {
            switch (op) {
                case 1:
                    System.out.print("Nombre: ");
                    String nombre = scanner.nextLine();
                    System.out.print("Scoring (0-100): ");
                    int score = Integer.parseInt(scanner.nextLine());

                    // Delega a la interfaz
                    red.agregarCliente(nombre, score);
                    break;

                case 2:
                    System.out.print("Nombre a buscar: ");
                    String bNombre = scanner.nextLine();
                    Cliente c1 = red.buscarPorNombre(bNombre);

                    if (c1 != null) {
                        System.out.println("✅ Encontrado: " + c1.getNombre() + " Scoring: " + c1.getScoring());
                    } else {
                        System.out.println("❌ Cliente no encontrado.");
                    }
                    break;

                case 3:
                    System.out.print("Scoring a buscar: ");
                    int s = Integer.parseInt(scanner.nextLine());

                    List<Cliente> encontrados = red.buscarPorScoring(s);

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

                    red.enviarSolicitud(u1, u2);
                    break;

                case 5:
                    red.procesarSolicitudes();
                    break;

                case 6:
                    red.deshacerUltimaAccion();
                    break;

                case 7:
                    red.mostrarEstadoGeneral();
                    break;

                case 8:
                    System.out.print("Nombre del cliente a eliminar: ");
                    String nombreBorrar = scanner.nextLine();

                    System.out.print("¿Estás seguro? Se borrará todo rastro (S/N): ");
                    String confirma = scanner.nextLine();

                    if (confirma.equalsIgnoreCase("S")) {
                        red.eliminarClienteTotalmente(nombreBorrar);
                    } else {
                        System.out.println("Operación cancelada.");
                    }
                    break;

                case 9:
                    red.verHistorial();
                    break;

                case 10: {
                    System.out.print("Ingrese cliente origen: ");
                    String origenNivel = scanner.nextLine();
                    System.out.print("Ingrese el nivel de profundidad a analizar (ej. 1, 2, 4): ");
                    int nivel = Integer.parseInt(scanner.nextLine());

                    // Ahora llamamos a la interfaz limpia, sin casteo
                    red.analizarNivel(origenNivel, nivel);
                    break;
                }

                case 11:
                    System.out.print("Ingrese el nombre del usuario a consultar: ");
                    String nombreCons = scanner.nextLine();
                    // Podría lanzar ClienteNoEncontradoException, que ya atrapas en el catch global
                    red.mostrarConexionesDe(nombreCons);
                    break;

                case 12: {
                    System.out.print("Ingrese el nombre del primer cliente: ");
                    String amigo1 = scanner.nextLine();
                    System.out.print("Ingrese el nombre del segundo cliente: ");
                    String amigo2 = scanner.nextLine();

                    try {
                        red.crearAmistad(amigo1, amigo2);
                    } catch (ClienteNoEncontradoException | IllegalArgumentException e) {
                        System.out.println("⚠️ Error: " + e.getMessage());
                    }
                    break;
                }

                case 13: {
                    System.out.print("Ingrese el nombre del cliente origen: ");
                    String origenDist = scanner.nextLine();
                    System.out.print("Ingrese el nombre del cliente destino: ");
                    String destinoDist = scanner.nextLine();

                    try {
                        int saltos = red.calcularDistancia(origenDist, destinoDist);

                        if (saltos == -1) {
                            System.out.println("❌ No hay conexión posible entre " + origenDist + " y " + destinoDist + ".");
                        } else if (saltos == 0) {
                            System.out.println("📍 La distancia es 0 saltos (es la misma persona).");
                        } else {
                            System.out.println("🛤️ La distancia entre " + origenDist + " y " + destinoDist + " es de " + saltos + " salto(s).");
                        }
                    } catch (ClienteNoEncontradoException e) {
                        System.out.println("⚠️ Error: " + e.getMessage());
                    }
                    break;
                }

                case 0:
                    System.out.println("Cerrando sistema...");
                    break;

                default:
                    System.out.println("Opción no válida.");
            }

        } catch (NumberFormatException e) {
            System.out.println("⚠️ Error: Debe ingresar un número entero válido.");

        } catch (SocialNetworkException e) {
            // Polimorfismo de excepciones: Captura cualquier error de negocio
            System.out.println("⛔ Error de Negocio: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println("🚫 Datos Inválidos: " + e.getMessage());

        } catch (Exception e) {
            System.out.println("☠️ Error Inesperado del Sistema: " + e.getMessage());
            // Nota de logging: En producción se usa un Logger, en académico se suele comentar.
            // e.printStackTrace();
        }
    }
}