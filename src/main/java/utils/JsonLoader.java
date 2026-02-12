package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import models.Cliente;
import services.SocialNetwork;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

public class JsonLoader {

    public static void cargar(String rutaArchivo, SocialNetwork red) {
        Gson gson = new Gson();

        try (Reader reader = new FileReader(rutaArchivo)) {
            // 1. Definimos el tipo de dato que vamos a leer (Lista de DTOs)
            Type listaTipo = new TypeToken<List<ClienteDTO>>() {}.getType();
            List<ClienteDTO> dtos = gson.fromJson(reader, listaTipo);

            if (dtos == null) {
                System.out.println("El archivo JSON está vacío o tiene un formato incorrecto.");
                return;
            }

            System.out.println("--- INICIO CARGA DE DATOS ---");

            // --- FASE 1: CARGAR NODOS (Crear los clientes) ---
            for (ClienteDTO dto : dtos) {
                try {
                    // Delegamos en la red para que lo meta en el HashMap y en el Árbol AVL
                    red.agregarCliente(dto.nombre, dto.scoring);
                } catch (Exception e) {
                    System.out.println("⚠️ Saltando cliente duplicado o inválido: " + dto.nombre);
                }
            }
            System.out.println("✅ Fase 1 completada: Clientes cargados.");

            // --- FASE 2: CARGAR ARISTAS (Crear las relaciones) ---
            for (ClienteDTO dto : dtos) {
                // Si el JSON dice que este cliente sigue a alguien...
                if (dto.siguiendo != null && !dto.siguiendo.isEmpty()) {

                    // Recuperamos al cliente real (el Objeto) de la memoria
                    Cliente origen = red.buscarPorNombre(dto.nombre);

                    if (origen != null) {
                        for (String nombreDestino : dto.siguiendo) {
                            // Recuperamos al amigo real
                            Cliente destino = red.buscarPorNombre(nombreDestino);

                            if (destino != null) {
                                try {
                                    // Establecemos la relación directa
                                    // Nota: Usamos agregarSeguido directo para evitar la cola de solicitudes al iniciar
                                    origen.agregarSeguido(destino);
                                } catch (IllegalStateException e) {
                                    System.out.println("⚠️ " + e.getMessage() + " (en carga de " + dto.nombre + ")");
                                }
                            } else {
                                System.out.println("⚠️ Error: " + dto.nombre + " intenta seguir a " + nombreDestino + " (No existe)");
                            }
                        }
                    }
                }
            }
            System.out.println("✅ Fase 2 completada: Relaciones establecidas.");
            System.out.println("-----------------------------");

        } catch (IOException e) {
            System.out.println("❌ Error al leer el archivo JSON: " + e.getMessage());
        }
    }

    // --- CLASE INTERNA (DTO) ---
    // Data Transfer Object: Sirve solo para mapear la estructura del JSON
    // sin ensuciar la lógica de negocio del modelo real.
    private static class ClienteDTO {
        String nombre;
        int scoring;
        List<String> siguiendo; // Leemos esto como lista de Strings simple
    }
}