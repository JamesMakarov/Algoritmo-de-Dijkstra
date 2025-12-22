package algorithm;

import model.Edge;
import model.Vertex;

import java.util.*;

public class DijkstraSolver {

    // Adicionamos o ouvinte (pode ser null se ninguém quiser ouvir)
    private DijkstraListener listener;

    public void setListener(DijkstraListener listener) {
        this.listener = listener;
    }

    // Helper para notificar sem dar erro de NullPointerException
    private void notifyVisiting(Vertex v) { if(listener != null) listener.onVertexVisiting(v); }
    private void notifyFinalized(Vertex v) { if(listener != null) listener.onVertexFinalized(v); }
    private void notifyRelaxed(Edge e, double d) { if(listener != null) listener.onEdgeRelaxed(e, d); }
    private void notifyRejected(Edge e, double d) { if(listener != null) listener.onEdgeRejected(e, d); }

    private record NodeWrapper(Vertex node, double totalDistance) implements Comparable<NodeWrapper> {
        @Override
        public int compareTo(NodeWrapper other) {
            return Double.compare(this.totalDistance, other.totalDistance);
        }
    }

    public List<Vertex> findShortestPath(Vertex start, Vertex end) {
        Map<Vertex, Double> distances = new HashMap<>();
        Map<Vertex, Vertex> previous = new HashMap<>();
        PriorityQueue<NodeWrapper> queue = new PriorityQueue<>();

        distances.put(start, 0.0);
        queue.add(new NodeWrapper(start, 0.0));

        while (!queue.isEmpty()) {
            NodeWrapper currentWrapper = queue.poll();
            Vertex current = currentWrapper.node();

            // EVENTO: Estou visitando este nó agora!
            notifyVisiting(current);

            if (current.equals(end)) {
                notifyFinalized(current); // Achei o fim
                break;
            }

            if (currentWrapper.totalDistance() > distances.getOrDefault(current, Double.POSITIVE_INFINITY)) {
                // Se esse caminho é velho, já finaliza sem olhar vizinhos
                notifyFinalized(current);
                continue;
            }

            for (Edge edge : current.getEdges()) {
                Vertex neighbor = edge.getTarget();
                double newDist = distances.get(current) + edge.getWeight();
                double currentNeighborDist = distances.getOrDefault(neighbor, Double.POSITIVE_INFINITY);

                if (newDist < currentNeighborDist) {
                    // EVENTO: Caminho melhor encontrado! (Aresta VERDE)
                    notifyRelaxed(edge, newDist);

                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    queue.add(new NodeWrapper(neighbor, newDist));
                } else {
                    // EVENTO: Caminho ruim/pior (Aresta VERMELHA)
                    notifyRejected(edge, newDist);
                }
            }

            // EVENTO: Terminei de olhar todos os vizinhos desse nó
            notifyFinalized(current);
        }

        return buildPath(previous, end);
    }

    private List<Vertex> buildPath(Map<Vertex, Vertex> previous, Vertex end) {
        List<Vertex> path = new ArrayList<>();
        if (!previous.containsKey(end) && previous.isEmpty()) return path;

        Vertex step = end;
        while (step != null) {
            path.add(step);
            step = previous.get(step);
        }
        Collections.reverse(path);
        return path;
    }
}