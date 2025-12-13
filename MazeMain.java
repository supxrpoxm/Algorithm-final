import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class MazeMain {

    private static void applyCellSize(JTable table, int cols, int size) {
        table.setRowHeight(size);
        for (int i = 0; i < cols; i++) table.getColumnModel().getColumn(i).setPreferredWidth(size);
        table.setFont(new Font("Segoe UI Emoji", Font.BOLD, (int)(size * 0.6))); 
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> {
            try {
                String fileName = JOptionPane.showInputDialog(null, "Enter maze file name:", "Maze File", JOptionPane.PLAIN_MESSAGE);
                if (fileName == null || fileName.isEmpty()) {
                     fileName = "default_maze.txt";
                }

                List<String[]> grid = MazeInput.parseMapFile(fileName);

                int rows = grid.size();
                int tempCols = 0; 
                for (String[] r : grid) tempCols = Math.max(tempCols, r.length);
                final int cols = tempCols; 

                String[][] tableData = new String[rows][cols];
                char[][] charGrid = new char[rows][cols]; // สำหรับ Genetic Solver

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        String cell = (j < grid.get(i).length ? grid.get(i)[j] : "X");
                        tableData[i][j] = cell;

                        // แปลง String[][] เป็น char[][] สำหรับ Genetic (S, G, #, .)
                        if (cell.equals("X") || cell.equals("#")) {
                            charGrid[i][j] = '#'; // ผนัง
                        } else if (cell.equals("S") || cell.equals("G") || cell.equals("E")) {
                            charGrid[i][j] = cell.charAt(0); // S, G, E
                        } else {
                            charGrid[i][j] = '.'; // ทางเดิน (Cost ตัวเลขทั้งหมดถือเป็น .)
                        }
                    }
                }

                String[] columnNames = new String[cols];
                for (int i = 0; i < cols; i++) columnNames[i] = "";

                JTable table = new JTable(tableData, columnNames);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                table.setEnabled(false);

                PathRenderer pathRenderer = new PathRenderer(tableData);
                for (int c = 0; c < cols; c++) table.getColumnModel().getColumn(c).setCellRenderer(pathRenderer);

                final int[] cellSize = {18};
                applyCellSize(table, cols, cellSize[0]);

                JScrollPane scroll = new JScrollPane(table);

                // top UI
                JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel info = new JLabel("Ready");
                
                String[] algs = {"A*", "Dijkstra", "Genetic"}; // เพิ่ม Genetic
                JComboBox<String> algSelect = new JComboBox<>(algs);
                
                JButton btnRun = new JButton("▶️ Run");
                JButton zoomIn = new JButton("➕");
                JButton zoomOut = new JButton("➖");

                top.add(new JLabel("Algorithm:"));
                top.add(algSelect);
                top.add(btnRun);
                top.add(Box.createHorizontalStrut(12));
                top.add(new JLabel("Zoom:"));
                top.add(zoomIn);
                top.add(zoomOut);
                top.add(Box.createHorizontalStrut(12));
                top.add(info);

                zoomIn.addActionListener(e -> {
                    cellSize[0] = Math.min(60, cellSize[0] + 3); 
                    applyCellSize(table, cols, cellSize[0]);
                });
                zoomOut.addActionListener(e -> {
                    cellSize[0] = Math.max(9, cellSize[0] - 3); 
                    applyCellSize(table, cols, cellSize[0]);
                });

                btnRun.addActionListener(e -> {
                    String alg = (String) algSelect.getSelectedItem();
                    
                    pathRenderer.setPath(null); 
                    table.repaint();
                    info.setText("Running " + alg + "...");
                    
                    new Thread(() -> {
                        try {
                            List<Point> path = null;
                            int cost = 0;
                            long t0 = System.nanoTime();

                            if ("A*".equals(alg)) {
                                path = AStar.findPath(tableData);
                                if (path != null) cost = AStar.pathCost(tableData, path);
                            } else if ("Dijkstra".equals(alg)) {
                                path = Dijkstra.findPath(tableData);
                                if (path != null) cost = Dijkstra.pathCost(tableData, path);
                            } else if ("Genetic".equals(alg)) { 
                                path = GeneticSolver.findPath(charGrid); // ใช้ charGrid
                                if (path != null) cost = GeneticSolver.pathCost(charGrid, path);
                            } 

                            long t1 = System.nanoTime();
                            double ms = (t1 - t0) / 1_000_000.0;

                            final List<Point> finalPath = path;
                            final int finalCost = cost;
                            
                            if (finalPath == null || finalPath.isEmpty() || finalCost == 0) {
                                SwingUtilities.invokeLater(() -> info.setText(String.format("No path found using %s. Time: %.3f ms", alg, ms)));
                            } else {
                                SwingUtilities.invokeLater(() -> {
                                    pathRenderer.setPath(finalPath);
                                    table.repaint();
                                    info.setText(String.format("%s: Steps=%d cost=%d time=%.3f ms", alg, finalPath.size(), finalCost, ms));
                                });
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Error in " + alg + ": " + ex.getMessage()));
                        }
                    }).start();
                });

                JFrame frame = new JFrame("Maze Viewer - Pathfinding");
                frame.setLayout(new BorderLayout());
                frame.add(top, BorderLayout.NORTH);
                frame.add(scroll, BorderLayout.CENTER);
                frame.setSize(1000, 800);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);

            } catch (IOException ioe) {
                ioe.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error reading file: " + ioe.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "An unexpected error occurred: " + e.getMessage());
            }
        });
    }
}