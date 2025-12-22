package model;

import java.util.ArrayList;
import java.util.List;

public class Vertex {
    private String name;
    private List<Edge> edges;

    public Vertex(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
    }

    public void addEdge(Vertex target, double weight) {
        this.edges.add(new Edge(target, weight));
    }

    public String getName() { return name; }
    public List<Edge> getEdges() { return edges; }

    public void setName (String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Nome inválido");
        this.name = name;
    }

    @Override
    public String toString() { return name; }
}