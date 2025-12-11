import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class MazeInput {

    // ===================== PARSE MAP =====================
    public static List<String[]> parseMapFile(String fileName) throws IOException {

        List<String> allLines = Files.readAllLines(Paths.get(fileName));
        List<String[]> grid = new ArrayList<>();

        Pattern cellPattern = Pattern.compile("\"([^\"]*)\"");  // จับ "เนื้อหา"

        for (String line : allLines) {

            line = line.trim();
            if (line.length() < 2)
                continue;

            // ลบขอบ #
            String content = line.substring(1, line.length() - 1);

            List<String> row = new ArrayList<>();

            // เริ่มต้นด้วย S
            if (content.startsWith("S")) {
                row.add("S");
                content = content.substring(1);
            }

            Matcher m = cellPattern.matcher(content);
            int lastEnd = 0;

            while (m.find()) {

                String between = content.substring(lastEnd, m.start());

                for (char c : between.toCharArray()) {
                    if (c == '#') row.add("X");
                }

                String inside = m.group(1).trim();

                if (inside.equals("#") || inside.isEmpty())
                    row.add("X");
                else
                    row.add(inside);

                lastEnd = m.end();
            }

            // tail
            String tail = content.substring(lastEnd);
            for (char c : tail.toCharArray()) {
                if (c == '#') row.add("X");
                if (c == 'G') row.add("G");
            }

            grid.add(row.toArray(new String[0]));
        }

        return grid;
    }

    // ===================== APPLY CELL SIZE =====================
    private static void applyCellSize(JTable table, int cols, int cellSize) {
        table.setRowHeight(cellSize);
        for (int i = 0; i < cols; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(cellSize);
        }
    }

    // ===================== DISPLAY GRID =====================
    public static void displayGrid(List<String[]> grid) {

        int rows = grid.size();
        int cols = grid.get(0).length;

        String[][] tableData = new String[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                tableData[i][j] = grid.get(i)[j];

        String[] columnNames = new String[cols];
        for (int i = 0; i < cols; i++) columnNames[i] = "";

        JTable table = new JTable(tableData, columnNames);
        table.setEnabled(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // ขนาดช่องเริ่มต้น
        final int[] cellSize = { 12 };
        applyCellSize(table, cols, cellSize[0]);

        // ========== CUSTOM RENDERER ==========
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {

                Component comp = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);

                comp.setBackground(Color.WHITE);
                comp.setForeground(Color.BLACK);

                String v = value == null ? "" : value.toString();

                if (v.equals("X")) {
                    comp.setBackground(Color.BLACK);
                    comp.setForeground(Color.WHITE);
                } 
                else if (v.equals("S")) {
                    comp.setForeground(Color.GREEN);
                } 
                else if (v.equals("G")) {
                    comp.setForeground(Color.RED);
                } 
                else {
                    comp.setForeground(Color.BLUE);
                }

                // =======================================
                // 🔥 ฟอนต์ปรับเป็น 10px เสมอ (ไม่ลดลง)
                // จุดประสงค์: เลขสองหลักยังคงแสดงแม้ช่องมีขนาดเล็ก
                // =======================================
                comp.setFont(new Font("Monospaced", Font.BOLD, 10));

                setHorizontalAlignment(SwingConstants.CENTER);
                return comp;
            }
        };

        // apply renderer
        for (int c = 0; c < cols; c++)
            table.getColumnModel().getColumn(c).setCellRenderer(renderer);

        JScrollPane scroll = new JScrollPane(table);

        // ========== ZOOM BUTTONS ==========
        JButton zoomIn = new JButton("+");
        JButton zoomOut = new JButton("-");

        zoomIn.addActionListener(e -> {
            cellSize[0] = Math.min(50, cellSize[0] + 2);
            applyCellSize(table, cols, cellSize[0]);
            table.repaint();
        });

        zoomOut.addActionListener(e -> {
            cellSize[0] = Math.max(4, cellSize[0] - 2); // ช่องเล็กกว่า 10px ก็ยังโชว์เลข
            applyCellSize(table, cols, cellSize[0]);
            table.repaint();
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Zoom: "));
        top.add(zoomIn);
        top.add(zoomOut);

        JFrame frame = new JFrame("Maze Viewer");
        frame.setLayout(new BorderLayout());
        frame.add(top, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ===================== MAIN =====================
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter file name: ");
        String fileName = sc.nextLine();

        try {
            if (Files.exists(Paths.get(fileName))) {
                List<String[]> grid = parseMapFile(fileName);
                displayGrid(grid);
            } else {
                System.out.println("File not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sc.close();
        }
    }
}
