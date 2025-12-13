import java.awt.Point;
import java.util.*;
//import java.util.stream.Collectors;

public class GeneticSolver {

    public static List<Point> findPath(char[][] grid) {
        int n = grid.length;
        int m = grid[0].length;
        
        int sx = -1, sy = -1, gx = -1, gy = -1;
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (grid[i][j] == 'S') { sx = i; sy = j; }
                if (grid[i][j] == 'G' || grid[i][j] == 'E') { gx = i; gy = j; }
            }
        }

        if (sx == -1 || gx == -1) {
            return Collections.emptyList();
        }

        // ====== GA PARAMETERS ======
        int POP = 500;              
        int GEN = 3000;              
        int ELITE = 10;             
        int LEN = Math.max(30, n * m); 
        double MUT_RATE = 0.08;     
        int TOURNAMENT_K = 5;       
        long SEED = System.nanoTime();

        Random rand = new Random(SEED);

        // Genes: 0=U,1=D,2=L,3=R
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        int[][] pop = new int[POP][LEN];
        for (int i = 0; i < POP; i++) {
            for (int j = 0; j < LEN; j++) pop[i][j] = rand.nextInt(4);
        }

        int[][] newPop = new int[POP][LEN];
        int[] fit = new int[POP];

        int bestFitEver = Integer.MIN_VALUE;
        int[] bestChromEver = new int[LEN];
        
        // --- GA Loop ---
        for (int g = 0; g < GEN; g++) {
            // ----- Evaluate Fitness -----
            for (int i = 0; i < POP; i++) {
                // *** แก้ไข: ประกาศ x, y ภายในลูปเพื่อให้ขอบเขตชัดเจน ***
                int x = sx, y = sy;
                int steps = 0;
                int hits = 0;
                int revisits = 0;

                boolean[][] visited = new boolean[n][m];
                visited[x][y] = true;

                boolean reached = false;
                int reachAt = -1;

                for (int t = 0; t < LEN; t++) {
                    int move = pop[i][t];
                    int nx = x + dx[move];
                    int ny = y + dy[move];

                    if (nx < 0 || ny < 0 || nx >= n || ny >= m || grid[nx][ny] == '#') {
                        hits++;
                        continue;
                    }

                    x = nx; y = ny;
                    steps++;

                    if (visited[x][y]) revisits++;
                    visited[x][y] = true;

                    if (x == gx && y == gy) {
                        reached = true;
                        reachAt = t + 1;
                        break;
                    }
                }

                int manhattan = Math.abs(x - gx) + Math.abs(y - gy);

                int score;
                if (reached) {
                    score = 1_000_000 - (reachAt * 1000) - (hits * 50);
                } else {
                    score = - (manhattan * 500) - (hits * 80) - (revisits * 10) - (steps * 1);
                }
                fit[i] = score;

                if (score > bestFitEver) {
                    bestFitEver = score;
                    System.arraycopy(pop[i], 0, bestChromEver, 0, LEN);
                }
            }

            if (bestFitEver > 500_000) break;

            // ----- Selection, Crossover, Mutation -----
            Integer[] idx = new Integer[POP];
            for (int i = 0; i < POP; i++) idx[i] = i;
            Arrays.sort(idx, (a, b) -> Integer.compare(fit[b], fit[a]));

            // copy elites
            for (int e = 0; e < ELITE; e++) {
                int src = idx[e];
                System.arraycopy(pop[src], 0, newPop[e], 0, LEN);
            }

            // selection (tournament) + crossover + mutation
            for (int i = ELITE; i < POP; i++) {
                
                // Tournament selection for parent 1
                int best_idx_in_tournament_p1 = rand.nextInt(POP);
                for (int k = 1; k < TOURNAMENT_K; k++) {
                    int cand_idx_in_tournament = rand.nextInt(POP);
                    if (fit[idx[cand_idx_in_tournament]] > fit[idx[best_idx_in_tournament_p1]]) {
                        best_idx_in_tournament_p1 = cand_idx_in_tournament;
                    }
                }
                int p1 = idx[best_idx_in_tournament_p1]; // ดึง Index จริงของ Pop

                // Tournament selection for parent 2
                int best_idx_in_tournament_p2 = rand.nextInt(POP);
                for (int k = 1; k < TOURNAMENT_K; k++) {
                    int cand_idx_in_tournament = rand.nextInt(POP);
                    if (fit[idx[cand_idx_in_tournament]] > fit[idx[best_idx_in_tournament_p2]]) {
                        best_idx_in_tournament_p2 = cand_idx_in_tournament;
                    }
                }
                int p2 = idx[best_idx_in_tournament_p2]; // ดึง Index จริงของ Pop

                // one-point crossover
                int cut = 1 + rand.nextInt(LEN - 1);
                for (int j = 0; j < LEN; j++) {
                    newPop[i][j] = (j < cut) ? pop[p1][j] : pop[p2][j];
                }

                // mutation (per gene)
                for (int j = 0; j < LEN; j++) {
                    if (rand.nextDouble() < MUT_RATE) {
                        newPop[i][j] = rand.nextInt(4);
                    }
                }
            }

            // swap populations
            int[][] tmp = pop; pop = newPop; newPop = tmp;
        }
        
        // --- Reconstruct Path จาก Best Chromosome ---
        int x = sx, y = sy; // x, y ถูกประกาศที่นี่เพื่อใช้ในการสร้างเส้นทางสุดท้าย
        List<Point> finalPath = new LinkedList<>();
        finalPath.add(new Point(sx, sy));

        for (int t = 0; t < LEN; t++) {
            int mv = bestChromEver[t];
            int nx = x + dx[mv], ny = y + dy[mv];
            
            if (nx < 0 || ny < 0 || nx >= n || ny >= m || grid[nx][ny] == '#') {
                continue;
            }
            
            x = nx; y = ny;
            finalPath.add(new Point(x, y));

            if (x == gx && y == gy) { break; }
        }

        return finalPath;
    }
    
    public static int pathCost(char[][] mapData, List<Point> path) {
        if (path == null || path.isEmpty()) return 0;
        return path.size();
    }
}