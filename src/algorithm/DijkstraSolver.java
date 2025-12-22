package algorithm;

import model.Edge;
import model.Vertex;

import java.util.*;

public class DijkstraSolver {

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

            if (current.equals(end)) break;

            if (currentWrapper.totalDistance() > distances.getOrDefault(current, Double.POSITIVE_INFINITY)) {
                continue;
            }

            for (Edge edge : current.getEdges()) {
                Vertex neighbor = edge.getTarget();
                double newDist = distances.get(current) + edge.getWeight();

                // Relaxamento: Achamos um caminho melhor?
                if (newDist < distances.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current); // Anota de onde viemos
                    queue.add(new NodeWrapper(neighbor, newDist));
                }
            }
        }

        return buildPath(previous, end);
    }

    private List<Vertex> buildPath(Map<Vertex, Vertex> previous, Vertex end) {
        List<Vertex> path = new ArrayList<>();

        if (!previous.containsKey(end) && previous.isEmpty()) return path; // Ajuste fino pode ser necessário dependendo do caso

        Vertex step = end;
        while (step != null) {
            path.add(step);
            step = previous.get(step);
        }

        Collections.reverse(path); 
        return path;
    }
}