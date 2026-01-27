package structures;

import models.Cliente;
import java.util.*;

public class SocialGraph {
    // Lista de adyacencia: O(1) para buscar vecinos
    private Map<Cliente, Set<Cliente>> adjList;

    public SocialGraph() {
        this.adjList = new HashMap<>();
    }

    public void addVertex(Cliente c) {
        adjList.putIfAbsent(c, new HashSet<>());
    }

    public void addEdge(Cliente c1, Cliente c2) {
        addVertex(c1);
        addVertex(c2);
        adjList.get(c1).add(c2);
        adjList.get(c2).add(c1); // Amistad bidireccional
    }

    public void removeEdge(Cliente c1, Cliente c2) {
        if (adjList.containsKey(c1)) adjList.get(c1).remove(c2);
        if (adjList.containsKey(c2)) adjList.get(c2).remove(c1);
    }

    public Set<Cliente> getNeighbors(Cliente c) {
        return adjList.getOrDefault(c, Collections.emptySet());
    }

    // Requerimiento Iteración 3: BFS para distancia mínima
    public int getDistance(Cliente start, Cliente end) {
        if (start.equals(end)) return 0;
        if (!adjList.containsKey(start) || !adjList.containsKey(end)) return -1;

        Queue<Cliente> queue = new LinkedList<>();
        Map<Cliente, Integer> distances = new HashMap<>();

        queue.add(start);
        distances.put(start, 0);

        while (!queue.isEmpty()) {
            Cliente current = queue.poll();
            int currentDist = distances.get(current);

            if (current.equals(end)) {
                return currentDist;
            }

            for (Cliente neighbor : getNeighbors(current)) {
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, currentDist + 1);
                    queue.add(neighbor);
                }
            }
        }
        return -1;
    }
}