import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class MazeMain {

    // helper to set row height and column widths
    private static void applyCellSize(JTable table, int cols, int size) {
        table.setRowHeight(size);
        for (int i = 0; i < cols; i++) table.getColumnModel().getColumn(i).setPreferredWidth(size);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                String fileName = JOptionPane.showInputDialog(null, "Enter maze file name:", "Maze File", JOptionPane.PLAIN_MESSAGE);
                if (fileName == null || fileName.isEmpty()) {
                    // ใช้ไฟล์จำลองหากผู้ใช้ไม่ป้อนชื่อ
                    fileName = "default_maze.txt"; 
                }

                List<String[]> grid = MazeInput.parseMapFile(fileName);

                int rows = grid.size();
                
                // *** การแก้ไขสำหรับข้อผิดพลาด 'effectively final' ***
                int tempCols = 0; 
                for (String[] r : grid) tempCols = Math.max(tempCols, r.length);
                final int cols = tempCols; // ทำให้ cols เป็น final

                String[][] tableData = new String[rows][cols];
                for (int i = 0; i < rows; i++)
                    for (int j = 0; j < cols; j++)
                        tableData[i][j] = (j < grid.get(i).length ? grid.get(i)[j] : "X");

                String[] columnNames = new String[cols];
                for (int i = 0; i < cols; i++) columnNames[i] = "";

                JTable table = new JTable(tableData, columnNames);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                table.setEnabled(false);

                // create renderer and attach (PathRenderer will draw arrows when path set)
                PathRenderer pathRenderer = new PathRenderer(tableData);
                // ต้อง Cast pathRenderer เป็น TableCellRenderer ก่อน
                for (int c = 0; c < cols; c++) table.getColumnModel().getColumn(c).setCellRenderer(pathRenderer);

                final int[] cellSize = {12};
                applyCellSize(table, cols, cellSize[0]);

                JScrollPane scroll = new JScrollPane(table);

                // top UI
                JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel info = new JLabel("Ready");
                String[] algs = {"A*"};
                JComboBox<String> algSelect = new JComboBox<>(algs);
                JButton btnRun = new JButton("Run");
                JButton zoomIn = new JButton("+");
                JButton zoomOut = new JButton("-");

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
                    cellSize[0] = Math.min(60, cellSize[0] + 2);
                    applyCellSize(table, cols, cellSize[0]);
                });
                zoomOut.addActionListener(e -> {
                    cellSize[0] = Math.max(4, cellSize[0] - 2);
                    applyCellSize(table, cols, cellSize[0]);
                });

                btnRun.addActionListener(e -> {
                    // Reset PathRenderer ก่อนเรียกใช้งานใหม่
                    pathRenderer.setPath(null);
                    table.repaint();
                    info.setText("Running A*...");
                    
                    String alg = (String) algSelect.getSelectedItem();
                    if ("A*".equals(alg)) {
                        new Thread(() -> {
                            try {
                                long t0 = System.nanoTime();
                                // เรียกใช้ AStar
                                java.util.List<Point> path = AStar.findPath(tableData);
                                long t1 = System.nanoTime();
                                double ms = (t1 - t0) / 1_000_000.0;

                                if (path == null) {
                                    SwingUtilities.invokeLater(() -> info.setText(String.format("No path found. Time: %.3f ms", ms)));
                                } else {
                                    int cost = AStar.pathCost(tableData, path);
                                    SwingUtilities.invokeLater(() -> {
                                        pathRenderer.setPath(path);      // set path -> renderer uses arrows
                                        table.repaint();
                                        info.setText(String.format("Steps=%d cost=%d time=%.3f ms", path.size(), cost, ms));
                                    });
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage()));
                            }
                        }).start();
                    }
                });

                JFrame frame = new JFrame("Maze - A* Viewer");
                frame.setLayout(new BorderLayout());
                frame.add(top, BorderLayout.NORTH);
                frame.add(scroll, BorderLayout.CENTER);
                frame.setSize(1000, 800);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);

            } catch (IOException ioe) {
                ioe.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error reading file: " + ioe.getMessage() + ". Using default empty grid.");
                // ออกจากโปรแกรมหากอ่านไฟล์ไม่ได้
            }
        });
    }
}