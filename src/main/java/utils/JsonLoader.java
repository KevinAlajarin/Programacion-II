package utils;

import services.SocialNetwork;
import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class JsonLoader {
    // DTOs internos para mapear el JSON exacto
    private class ClienteJson {
        String nombre;
        int scoring;
        List<String> siguiendo;
    }

    private class DataWrapper {
        List<ClienteJson> clientes;
    }

    public static void cargar(String ruta, SocialNetwork red) {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(ruta)) {
            DataWrapper data = gson.fromJson(reader, DataWrapper.class);

            if (data == null || data.clientes == null) {
                System.out.println("⚠️ El archivo JSON está vacío o tiene formato incorrecto.");
                return;
            }

            System.out.println("--- Iniciando Carga Masiva ---");

            // FASE 1: Cargar Nodos (Clientes)
            int cargados = 0;
            for (ClienteJson c : data.clientes) {
                try {
                    // Intentamos agregar cada cliente individualmente
                    red.agregarCliente(c.nombre, c.scoring);
                    cargados++;
                } catch (Exception e) {
                    // Si falla (ej: duplicado), lo reportamos pero seguimos con el siguiente
                    System.err.println("   [Error Carga Cliente] " + c.nombre + ": " + e.getMessage());
                }
            }
            System.out.println("Nodes cargados: " + cargados + "/" + data.clientes.size());

            // FASE 2: Cargar Relaciones (Solicitudes/Conexiones)
            int relaciones = 0;
            for (ClienteJson c : data.clientes) {
                if (c.siguiendo != null) {
                    for (String seguido : c.siguiendo) {
                        try {
                            red.enviarSolicitud(c.nombre, seguido);
                            relaciones++;
                        } catch (Exception e) {
                            // Ignoramos errores de solicitud (ej: usuario destino no existe) para no ensuciar tanto
                            // Opcional: System.err.println("   [Error Relación] " + c.nombre + "->" + seguido + ": " + e.getMessage());
                        }
                    }
                }
            }
            System.out.println("Solicitudes encoladas: " + relaciones);
            System.out.println("✅ Carga JSON completada.");

        } catch (IOException e) {
            System.err.println("❌ Error crítico leyendo archivo: " + e.getMessage());
            System.err.println("Asegúrate de que 'datos.json' esté en la raíz del proyecto (junto al pom.xml).");
        }
    }
}