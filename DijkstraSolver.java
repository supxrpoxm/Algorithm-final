import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.awt.Point; // ใช้ java.awt.Point

public class DijkstraSolver {

    // Inner Class สำหรับ PriorityQueue
    private class PointData {
        int x, y;
        int cost;
        public PointData(int x, int y, int cost) {
            this.x = x;
            this.y = y;
            this.cost = cost;
        }
    }

    public static final int INFINITY = 999999999; 

    private String[][] maze; 
    private int[][] dist;     
    private Point[][] predecessor; // ใช้ java.awt.Point
    private int rows;
    private int cols;
    
    public int startX, startY; 
    public int goalX, goalY;

    public DijkstraSolver(String[][] mazeArray) {
        this.maze = mazeArray;
        this.rows = mazeArray.length;
        this.cols = mazeArray[0].length; 
        
        this.dist = new int[rows][cols];
        this.predecessor = new Point[rows][cols]; 

        for (int[] row : dist) {
            Arrays.fill(row, INFINITY);
        }

        findStartAndGoal();
    }

    private void findStartAndGoal() {
        startX = -1; goalX = -1; 
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                if (maze[x][y].equals("S")) {
                    startX = x;
                    startY = y;
                } else if (maze[x][y].equals("G") || maze[x][y].equals("E")) { // รองรับ G และ E
                    goalX = x;
                    goalY = y;
                }
            }
        }
    }

    private int getWeight(int x, int y) {
        String weight = maze[x][y];
        
        if (weight.equals("S") || weight.equals("G") || weight.equals("E")) {
            return 0; 
        }
        
        try {
            return Integer.parseInt(weight);
        } catch (NumberFormatException e) {
            // ถ้าไม่ใช่ตัวเลข (เช่น '.' หรือ 'X' หรือ '#') ให้ถือว่า Cost = 1 (ยกเว้นผนัง)
            if (weight.equals("X") || weight.equals("#")) return INFINITY;
            return 1; 
        }
    }

    public int solveShortestPath() {
        if (goalX == -1 || startX == -1) {
            return INFINITY;
        }
        
        PriorityQueue<PointData> pq = new PriorityQueue<>(Comparator.comparingInt(cell -> cell.cost)); 

        dist[startX][startY] = 0;
        pq.add(new PointData(startX, startY, 0));

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        while (!pq.isEmpty()) {
            PointData current = pq.poll();
            int x = current.x;
            int y = current.y;
            int currentCost = current.cost;
            
            if (currentCost > dist[x][y]) {
                continue;
            }
            
            if (x == goalX && y == goalY) {
                break;
            }

            for (int[] dir : directions) {
                int nextX = x + dir[0]; 
                int nextY = y + dir[1]; 
                
                // ตรวจสอบขอบเขตและผนัง ('X' หรือ '#')
                if (nextX >= 0 && nextX < rows && nextY >= 0 && nextY < cols && 
                    !maze[nextX][nextY].equals("X") && !maze[nextX][nextY].equals("#")) { 
                    
                    int nextWeight = getWeight(nextX, nextY);
                    int newCost = currentCost + nextWeight;
                    
                    if (newCost < dist[nextX][nextY]) {
                        dist[nextX][nextY] = newCost;
                        predecessor[nextX][nextY] = new Point(x, y); // ใช้ java.awt.Point
                        pq.add(new PointData(nextX, nextY, newCost));
                    }
                }
            }
        }
        
        return dist[goalX][goalY];
    }
    
    // คืนค่าเป็น List<Point> (java.awt.Point)
    public List<Point> reconstructPath() {
        LinkedList<Point> path = new LinkedList<>(); 
        
        if (goalX == -1 || goalY == -1 || dist[goalX][goalY] == INFINITY) {
            return path;
        }

        Point current = new Point(goalX, goalY); 
        
        int maxSteps = rows * cols * 2; 
        int steps = 0;
        
        // เดินย้อนกลับจาก Goal ไป Start
        while (current != null && (current.x != startX || current.y != startY) && steps < maxSteps) {
            path.addFirst(current);
            current = predecessor[current.x][current.y]; 
            steps++;
        }
        
        if (current != null && current.x == startX && current.y == startY) {
             path.addFirst(current);
        }
        
        return path; 
    }
    
    // (เมธอด print ต่างๆ ถูกละไว้ แต่คุณสามารถนำโค้ดเดิมไปใส่ได้ โดยใช้ java.awt.Point)
}