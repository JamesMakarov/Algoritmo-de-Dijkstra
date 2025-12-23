package algorithm;

import model.Edge;
import model.Vertex;

import java.util.*;

public class DijkstraSolver {

    private DijkstraListener listener;

    public void setListener(DijkstraListener listener) {
        this.listener = listener;
    }

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

            notifyVisiting(current);

            if (current.equals(end)) {
                notifyFinalized(current);
                break;
            }

            if (currentWrapper.totalDistance() > distances.getOrDefault(current, Double.POSITIVE_INFINITY)) {
                notifyFinalized(current);
                continue;
            }

            for (Edge edge : current.getEdges()) {
                Vertex neighbor = edge.getTarget();
                double newDist = distances.get(current) + edge.getWeight();
                double currentNeighborDist = distances.getOrDefault(neighbor, Double.POSITIVE_INFINITY);

                if (newDist < currentNeighborDist) {
                    notifyRelaxed(edge, newDist);
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    queue.add(new NodeWrapper(neighbor, newDist));
                } else {
                    notifyRejected(edge, newDist);
                }
            }
            notifyFinalized(current);
        }

        // --- CORREÇÃO AQUI ---
        // Passamos 'start' para validar se o caminho está completo
        return buildPath(previous, end, start);
    }

    private List<Vertex> buildPath(Map<Vertex, Vertex> previous, Vertex end, Vertex start) {
        List<Vertex> path = new ArrayList<>();

        // 1. Caso Trivial: Início e Fim são iguais
        if (start.equals(end)) {
            path.add(start);
            return path;
        }

        // 2. CORREÇÃO DE BUG: Se o 'end' não tem "pai" no mapa previous, ele nunca foi visitado/alcançado.
        if (!previous.containsKey(end)) {
            return path; // Retorna LISTA VAZIA indicando falha
        }

        Vertex step = end;
        while (step != null) {
            path.add(step);
            step = previous.get(step);
        }

        Collections.reverse(path);
        return path;
    }
}