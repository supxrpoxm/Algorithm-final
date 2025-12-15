import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.awt.Point;

public class MazeMain {

    private static JTable table;
    private static PathRenderer pathRenderer;
    private static JLabel info;
    private static String[][] tableData;

    // กำหนดขนาด cell
    private static void applyCellSize(JTable table, int cols, int size) {
        table.setRowHeight(size);
        for (int i = 0; i < cols; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(size);
        table.setFont(new Font("Segoe UI Emoji", Font.BOLD, (int)(size * 0.6)));
    }

    // reset view
    private static void resetView() {
        pathRenderer.setPath(null);
        table.repaint();
        info.setText("Ready");
    }

    // โหลด maze จากไฟล์
    private static void loadMaze(File file) throws IOException {
        List<String[]> grid = MazeInput.parseMapFile(file.getPath());

        int rows = grid.size();
        int cols = 0;
        for (String[] r : grid) cols = Math.max(cols, r.length);

        tableData = new String[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                tableData[i][j] = (j < grid.get(i).length) ? grid.get(i)[j] : "X";

        table.setModel(new javax.swing.table.DefaultTableModel(
                tableData, new String[cols]
        ));
        pathRenderer.setMap(tableData);

        for (int c = 0; c < cols; c++)
            table.getColumnModel().getColumn(c).setCellRenderer(pathRenderer);

        applyCellSize(table, cols, table.getRowHeight());
        resetView();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

                // ===== Initial Load =====
                JFileChooser chooser = new JFileChooser(".");
                chooser.setDialogTitle("Select maze file");
                if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

                tableData = null;
                table = new JTable();
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                table.setEnabled(false);

                pathRenderer = new PathRenderer(null);
                info = new JLabel("Ready");

                loadMaze(chooser.getSelectedFile());

                JScrollPane scroll = new JScrollPane(table);

                // ===== Top Panel =====
                JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

                String[] algs = {"A*", "Dijkstra", "Genetic"};
                JComboBox<String> algSelect = new JComboBox<>(algs);

                JButton btnRun = new JButton("▶ Run");
                JButton btnLoad = new JButton("📂 Load File");
                JButton zoomIn = new JButton("➕");
                JButton zoomOut = new JButton("➖");

                top.add(new JLabel("Algorithm:"));
                top.add(algSelect);
                top.add(btnRun);
                top.add(btnLoad);
                top.add(Box.createHorizontalStrut(10));
                top.add(new JLabel("Zoom:"));
                top.add(zoomIn);
                top.add(zoomOut);
                top.add(Box.createHorizontalStrut(10));
                top.add(info);

                // ===== Listeners =====
                algSelect.addActionListener(e -> resetView());

                btnLoad.addActionListener(e -> {
                    JFileChooser fc = new JFileChooser(".");
                    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        try {
                            loadMaze(fc.getSelectedFile());
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage());
                        }
                    }
                });

                zoomIn.addActionListener(e -> applyCellSize(
                        table, table.getColumnCount(), table.getRowHeight() + 3));

                zoomOut.addActionListener(e -> applyCellSize(
                        table, table.getColumnCount(), Math.max(9, table.getRowHeight() - 3)));

                btnRun.addActionListener(e -> {
                    resetView();
                    String alg = (String) algSelect.getSelectedItem();
                    info.setText("Running " + alg + "...");

                    new Thread(() -> {
                        try {
                            List<Point> path = null;
                            int cost = 0;
                            long t0 = System.nanoTime();

                            // เรียก algorithm ตามเลือก
                            if ("A*".equals(alg)) {
                                path = AStar.findPath(tableData);
                                cost = AStar.pathCost(tableData, path);
                            } else if ("Dijkstra".equals(alg)) {
                                path = Dijkstra.findPath(tableData);
                                cost = Dijkstra.pathCost(tableData, path);
                            } else {
                                path = GeneticSolver.findPath(tableData);
                                cost = GeneticSolver.pathCost(tableData, path);
                            }

                            double time = (System.nanoTime() - t0) / 1e9;
                            List<Point> finalPath = path;
                            int finalCost = cost;

                            SwingUtilities.invokeLater(() -> {
                                if (finalPath == null || finalPath.isEmpty()) {
                                    info.setText("No path found");
                                } else {
                                    pathRenderer.setPath(finalPath);
                                    table.repaint();

                                    // นับ step จริง: ยกเว้นกำแพง
                                    int steps = 0;
                                    for (int i = 0; i < finalPath.size() - 1; i++) {
                                        Point p = finalPath.get(i);
                                        int r = p.x;
                                        int c = p.y;
                                        String cell = tableData[r][c];
                                        if (!"X".equals(cell) && !"#".equals(cell)) {
                                            steps++;
                                        }
                                    }

                                    info.setText(String.format(
                                            "%s: Steps=%d Cost=%d Time=%.3f s",
                                            alg, steps, finalCost, time));
                                }
                            });

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                });

                // ===== Frame =====
                JFrame frame = new JFrame("Maze Viewer");
                frame.setLayout(new BorderLayout());
                frame.add(top, BorderLayout.NORTH);
                frame.add(scroll, BorderLayout.CENTER);
                frame.setSize(1000, 800);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
