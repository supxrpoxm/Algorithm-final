import java.awt.Point;
import java.util.*;

public class AStar {

    // find path from S to G using 4-direction moves, minimal total enter-cost
    // grid: String[][] with "X" for wall, "S", "G", or numbers as strings
    public static List<Point> findPath(String[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;

        Point start = null, goal = null;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String v = grid[r][c];
                if ("S".equals(v)) start = new Point(r, c);
                if ("G".equals(v)) goal = new Point(r, c);
            }
        }
        if (start == null || goal == null) return null;

        // find minimum numeric cost (>=1) to use as heuristic factor (avoid overestimating)
        int minCost = Integer.MAX_VALUE;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                try {
                    int val = Integer.parseInt(grid[r][c]);
                    if (val > 0) minCost = Math.min(minCost, val);
                } catch (Exception ignored) {}
            }
        if (minCost == Integer.MAX_VALUE) minCost = 1;

        // A* state
        class Node {
            int r, c;
            int g; // cost so far
            int f;
            Node parent;
            Node(int r, int c, int g, int f, Node p){ this.r=r;this.c=c;this.g=g;this.f=f;this.parent=p;}
        }

        Comparator<Node> cmp = Comparator.comparingInt(n -> n.f);
        PriorityQueue<Node> open = new PriorityQueue<>(cmp);
        boolean[][] closed = new boolean[rows][cols];
        int[][] bestG = new int[rows][cols];
        for (int[] row : bestG) Arrays.fill(row, Integer.MAX_VALUE);

        int h0 = heuristic(start.x, start.y, goal.x, goal.y, minCost);
        open.add(new Node(start.x, start.y, 0, h0, null));
        bestG[start.x][start.y] = 0;

        int[][] dirs = { {-1,0},{1,0},{0,-1},{0,1} };

        while(!open.isEmpty()) {
            Node cur = open.poll();
            if (closed[cur.r][cur.c]) continue;
            if (cur.r==goal.x && cur.c==goal.y) {
                // reconstruct path from start -> goal
                LinkedList<Point> path = new LinkedList<>();
                Node p = cur;
                while (p != null) { path.addFirst(new Point(p.r, p.c)); p = p.parent; }
                return path;
            }
            closed[cur.r][cur.c] = true;

            for (int[] d : dirs) {
                int nr = cur.r + d[0], nc = cur.c + d[1];
                if (nr<0||nr>=rows||nc<0||nc>=cols) continue;
                String val = grid[nr][nc];
                if ("X".equals(val)) continue; // wall

                int moveCost = cellCost(val);
                if (moveCost < 0) continue; // skip invalid

                int ng = cur.g + moveCost;
                if (ng < bestG[nr][nc]) {
                    bestG[nr][nc] = ng;
                    int h = heuristic(nr, nc, goal.x, goal.y, minCost);
                    open.add(new Node(nr,nc,ng, ng + h, cur));
                }
            }
        }

        return null; // no path
    }

    // compute heuristic: Manhattan * minCost
    private static int heuristic(int r, int c, int gr, int gc, int minCost) {
        return (Math.abs(r - gr) + Math.abs(c - gc)) * minCost;
    }

    // parse cell string to movement cost (entering that cell)
    // S and G -> treat as 0 cost to enter; numbers parsed; X -> impassable -> return -1
    private static int cellCost(String s) {
        if (s == null) return -1;
        s = s.trim();
        if (s.equals("X")) return -1;
        if (s.equals("S") || s.equals("G")) return 0;
        try {
            return Math.max(0, Integer.parseInt(s));
        } catch (Exception e) {
            return -1;
        }
    }

    // utility to compute total cost of path (sum of entering costs excluding start cell)
    public static int pathCost(String[][] grid, List<Point> path) {
        if (path == null || path.isEmpty()) return 0;
        int total = 0;
        // skip first point (start), sum cost of each subsequent cell
        for (int i = 1; i < path.size(); i++) {
            Point p = path.get(i);
            total += cellCost(grid[p.x][p.y]);
        }
        return total;
    }
}
