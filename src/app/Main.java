package app;

import algorithm.DijkstraListener;
import algorithm.DijkstraSolver;
import model.Edge;
import model.Vertex;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Vertex a = new Vertex("A");
        Vertex b = new Vertex("B");
        Vertex c = new Vertex("C");
        Vertex d = new Vertex("D");
        Vertex e = new Vertex("E");

        a.addEdge(b, 6);
        a.addEdge(d, 1);
        d.addEdge(b, 2);
        d.addEdge(e, 1);
        b.addEdge(c, 5);
        b.addEdge(e, 2);
        e.addEdge(c, 5);

        DijkstraSolver solver = new DijkstraSolver();

        // --- AQUI ACONTECE A MÁGICA ---
        // Estamos ensinando o Main a "pintar" o que o algoritmo diz
        solver.setListener(new DijkstraListener() {
            @Override
            public void onVertexVisiting(Vertex v) {
                System.out.println("🟡 VISITANDO: " + v.getName() + " (Ficou Amarelo)");
            }

            @Override
            public void onVertexFinalized(Vertex v) {
                System.out.println("☑️ FINALIZADO: " + v.getName() + " (Ficou Cinza/Ok)");
            }

            @Override
            public void onEdgeRelaxed(Edge e, double newDistance) {
                System.out.println("   💚 ARESTA VERDE: Até " + e.getTarget().getName() + " custo caiu para " + newDistance);
            }

            @Override
            public void onEdgeRejected(Edge e, double distanceSent) {
                System.out.println("   🔴 ARESTA VERMELHA: Ir para " + e.getTarget().getName() + " custaria " + distanceSent + " (Muito caro/igual)");
            }
        });

        System.out.println("--- INÍCIO DA ANIMAÇÃO ---");
        List<Vertex> path = solver.findShortestPath(a, c);
        System.out.println("--- FIM ---");

        System.out.println("\nCaminho Final: " + path);
    }
}