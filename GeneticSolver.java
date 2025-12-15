import java.awt.Point;
import java.util.*;

public class GeneticSolver {

    // ===================== CELL COST =====================
    private static int weight(String v) {
        if (v == null) return -1;
        if (v.equals("#") || v.equals("X")) return -1;
        if (v.equals("S") || v.equals("G") || v.equals("E")) return 0;
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 1;
        }
    }

    // ===================== PUBLIC API =====================
    public static List<Point> findPath(String[][] grid) {

        int n = grid.length;
        int m = grid[0].length;

        int sx = -1, sy = -1, gx = -1, gy = -1;

        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++) {
                if (grid[i][j].equals("S")) { sx = i; sy = j; }
                if (grid[i][j].equals("G") || grid[i][j].equals("E")) {
                    gx = i; gy = j;
                }
            }

        if (sx == -1 || gx == -1) return Collections.emptyList();

        // ============ PURE GA PARAMETERS ============
        final int POP = 400;
        final int GEN = 3000;
        final int LEN = n * m;
        final int ELITE = 10;
        final double MUT = 0.07;
        final double CROSS = 0.9;
        final int TOURN = 5;

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        Random rand = new Random();

        int[][] pop = initPopulation(POP, LEN, rand);
        int[][] next = new int[POP][LEN];
        double[] fitness = new double[POP];

        int[] bestChrom = null;
        List<Point> bestPath = null;
        int bestCost = Integer.MAX_VALUE;

        // ===================== GA LOOP =====================
        for (int gen = 0; gen < GEN; gen++) {

            for (int i = 0; i < POP; i++) {
                FitnessResult r = evaluate(
                        pop[i], sx, sy, gx, gy, n, m, grid, dx, dy
                );

                // ใช้ fitness ปกติสำหรับ selection
                fitness[i] = fitness(r);

                if (r.reached) {
                    // สร้าง path และ clean loop
                    List<Point> path = buildPath(pop[i], sx, sy, gx, gy, n, m, grid, dx, dy);
                    List<Point> cleanedPath = simplifyPath(path, gx, gy);

                    int c = calculatePathCost(cleanedPath, grid);

                    // เก็บ chromosome + path ที่ cost ต่ำสุด
                    if (c < bestCost) {
                        bestCost = c;
                        bestChrom = pop[i].clone();
                        bestPath = cleanedPath;
                    }
                }
            }

            // Selection + Elitism
            Integer[] idx = new Integer[POP];
            for (int i = 0; i < POP; i++) idx[i] = i;
            Arrays.sort(idx, (a, b) -> Double.compare(fitness[b], fitness[a]));

            // Elitism
            for (int i = 0; i < ELITE; i++)
                System.arraycopy(pop[idx[i]], 0, next[i], 0, LEN);

            // Reproduction
            for (int i = ELITE; i < POP; i++) {
                int p1 = idx[tournament(idx, fitness, rand, TOURN)];
                int p2 = idx[tournament(idx, fitness, rand, TOURN)];

                if (rand.nextDouble() < CROSS)
                    crossover(pop[p1], pop[p2], next[i], LEN, rand);
                else
                    System.arraycopy(pop[p1], 0, next[i], 0, LEN);

                mutate(next[i], MUT, rand);
            }

            int[][] tmp = pop; pop = next; next = tmp;
        }

        // ถ้าไม่มี path ถึง goal
        if (bestPath == null) return Collections.emptyList();

        return bestPath;
    }

    // ===================== FITNESS =====================
    private static class FitnessResult {
        boolean reached;
        int cost, steps, revisits, manhattan;
    }

    private static FitnessResult evaluate(
            int[] c, int sx, int sy, int gx, int gy,
            int n, int m, String[][] g,
            int[] dx, int[] dy) {

        FitnessResult r = new FitnessResult();
        boolean[][] visited = new boolean[n][m];

        int x = sx, y = sy;
        visited[x][y] = true;

        for (int mv : c) {
            int nx = x + dx[mv];
            int ny = y + dy[mv];

            if (nx < 0 || ny < 0 || nx >= n || ny >= m) continue;
            int w = weight(g[nx][ny]);
            if (w < 0) continue;

            x = nx; y = ny;
            r.steps++;
            r.cost += w;

            if (visited[x][y]) r.revisits++;
            visited[x][y] = true;

            if (x == gx && y == gy) {
                r.reached = true;
                break;
            }
        }

        r.manhattan = Math.abs(x - gx) + Math.abs(y - gy);
        return r;
    }

    private static double fitness(FitnessResult r) {
        if (!r.reached)
            return -1e6 - r.manhattan * 1000 - r.steps * 10;
        return 1e8 - r.cost * 1e4 - r.steps * 100 - r.revisits * 500;
    }

    // ===================== GA OPERATORS =====================
    private static int[][] initPopulation(int p, int len, Random r) {
        int[][] pop = new int[p][len];
        for (int i = 0; i < p; i++)
            for (int j = 0; j < len; j++)
                pop[i][j] = r.nextInt(4);
        return pop;
    }

    private static int tournament(Integer[] idx, double[] f, Random r, int k) {
        int best = r.nextInt(idx.length);
        for (int i = 1; i < k; i++) {
            int c = r.nextInt(idx.length);
            if (f[idx[c]] > f[idx[best]]) best = c;
        }
        return best;
    }

    private static void crossover(int[] a, int[] b, int[] c, int len, Random r) {
        int p1 = r.nextInt(len);
        int p2 = r.nextInt(len);
        if (p1 > p2) { int t = p1; p1 = p2; p2 = t; }
        for (int i = 0; i < len; i++)
            c[i] = (i >= p1 && i < p2) ? b[i] : a[i];
    }

    private static void mutate(int[] c, double rate, Random r) {
        for (int i = 0; i < c.length; i++)
            if (r.nextDouble() < rate)
                c[i] = r.nextInt(4);
    }

    // ===================== PATH BUILD =====================
    private static List<Point> buildPath(
            int[] c, int sx, int sy, int gx, int gy,
            int n, int m, String[][] g,
            int[] dx, int[] dy) {

        List<Point> path = new ArrayList<>();
        int x = sx, y = sy;
        path.add(new Point(x, y));

        for (int mv : c) {
            int nx = x + dx[mv];
            int ny = y + dy[mv];

            if (nx < 0 || ny < 0 || nx >= n || ny >= m) continue;
            if (weight(g[nx][ny]) < 0) continue;

            x = nx; y = ny;
            path.add(new Point(x, y));

            if (x == gx && y == gy) break;
        }
        return path;
    }

    // ===================== SIMPLIFY PATH =====================
    private static List<Point> simplifyPath(List<Point> rawPath, int gx, int gy) {
        if (rawPath.size() <= 1) return rawPath;

        List<Point> cleaned = new ArrayList<>();
        Map<String, Integer> lastSeen = new HashMap<>();

        cleaned.add(rawPath.get(0));
        lastSeen.put(rawPath.get(0).x + "," + rawPath.get(0).y, 0);

        for (int i = 1; i < rawPath.size(); i++) {
            Point cur = rawPath.get(i);
            String key = cur.x + "," + cur.y;

            if (cur.x == gx && cur.y == gy) {
                if (!cleaned.get(cleaned.size() - 1).equals(cur))
                    cleaned.add(cur);
                break;
            }

            if (lastSeen.containsKey(key)) {
                int loopStart = lastSeen.get(key);
                cleaned = new ArrayList<>(cleaned.subList(0, loopStart + 1));
                lastSeen.clear();
                for (int j = 0; j < cleaned.size(); j++) {
                    Point p = cleaned.get(j);
                    lastSeen.put(p.x + "," + p.y, j);
                }
                cleaned.add(cur);
                lastSeen.put(key, cleaned.size() - 1);
                continue;
            }

            cleaned.add(cur);
            lastSeen.put(key, cleaned.size() - 1);
        }

        return cleaned;
    }

    // ===================== COST =====================
    public static int calculatePathCost(List<Point> path, String[][] grid) {
        int cost = 0;
        for (int i = 1; i < path.size(); i++) {
            int w = weight(grid[path.get(i).x][path.get(i).y]);
            if (w > 0) cost += w;
        }
        return cost;
    }

    public static int pathCost(List<Point> path, String[][] grid) {
        return calculatePathCost(path, grid);
    }

    public static int pathCost(String[][] grid, List<Point> path) {
        return calculatePathCost(path, grid);
    }
}
