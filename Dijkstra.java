import java.awt.Point;
import java.util.List;

public class Dijkstra {
    
    public static List<Point> findPath(String[][] mapData) {
        DijkstraSolver solver = new DijkstraSolver(mapData);
        solver.solveShortestPath();
        return solver.reconstructPath();
    }
    
    public static int pathCost(String[][] mapData, List<Point> path) {
        if (path == null || path.isEmpty()) return 0;
        
        DijkstraSolver solver = new DijkstraSolver(mapData);
        int finalCost = solver.solveShortestPath();
        
        return finalCost != DijkstraSolver.INFINITY ? finalCost : 0;
    }
}