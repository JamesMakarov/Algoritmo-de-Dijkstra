package model;

public class Edge {

    private Vertex target; // this represents the vertex that the edge is incident on
    private double weight; // this represents how much it will cost to pass through the vertex

    public Edge (Vertex target, double weight) {
        this.target = target;
        this.weight = weight;
    }

    public Vertex getTarget() {
        return target;
    }

    public double getWeight() {
        return weight;
    }

    public void setTarget (Vertex target) {
        this.target = target;
    }

    public void setWeight (double weight) {
        this.weight = weight;
    }

}
