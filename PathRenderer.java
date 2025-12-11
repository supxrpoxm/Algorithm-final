import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.Point;
import java.util.List;

/**
 * PathRenderer extends DefaultTableCellRenderer to:
 * - show arrows for path cells (green arrows)
 * - otherwise show numbers / S / G / walls with colors
 *
 * Usage:
 *   PathRenderer r = new PathRenderer(tableData);
 *   table.getColumnModel().getColumn(i).setCellRenderer(r);
 *   r.setPath(pathList);
 */
public class PathRenderer extends DefaultTableCellRenderer {

    private final String[][] grid;
    private Set<Point> pathSet = new HashSet<>();
    private Map<Point, Character> arrowMap = new HashMap<>(); // map cell -> arrow char

    public PathRenderer(String[][] gridData) {
        this.grid = gridData;
    }

    // set path as list of Points (row,col) from start -> goal
    public void setPath(List<Point> path) {
        pathSet.clear();
        arrowMap.clear();
        if (path == null) return;
        for (Point p : path) pathSet.add(new Point(p.x, p.y));
        // build arrows for path steps (for each cell except goal: arrow towards next)
        for (int i = 0; i + 1 < path.size(); i++) {
            Point cur = path.get(i);
            Point nx = path.get(i + 1);
            char arrow = getArrowForDelta(nx.x - cur.x, nx.y - cur.y);
            arrowMap.put(new Point(cur.x, cur.y), arrow);
        }
    }

    private char getArrowForDelta(int dr, int dc) {
        if (dr == -1 && dc == 0) return '\u2191'; // up
        if (dr == 1 && dc == 0) return '\u2193';  // down
        if (dr == 0 && dc == -1) return '\u2190'; // left
        if (dr == 0 && dc == 1) return '\u2192';  // right
        return '\u25CF'; // fallback dot
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        String v = value == null ? "" : value.toString();

        // background / foreground defaults
        comp.setBackground(Color.WHITE);
        comp.setForeground(Color.BLACK);

        if ("X".equals(v)) {
            comp.setBackground(Color.BLACK);
            comp.setForeground(Color.WHITE);
            setText("");
            return comp;
        }

        // if this cell is on path, show arrow (green)
        Point p = new Point(row, column);
        if (arrowMap.containsKey(p)) {
            char arrow = arrowMap.get(p);
            setText(String.valueOf(arrow));
            setForeground(new Color(0, 128, 0)); // green
            setHorizontalAlignment(SwingConstants.CENTER);
            comp.setBackground(Color.WHITE);
            return comp;
        }

        // goal cell: show G
        if ("G".equals(v)) {
            setText("G");
            setForeground(Color.RED);
            setHorizontalAlignment(SwingConstants.CENTER);
            return comp;
        }

        // start cell: S
        if ("S".equals(v)) {
            setText("S");
            setForeground(Color.GREEN);
            setHorizontalAlignment(SwingConstants.CENTER);
            return comp;
        }

        // otherwise show numeric value (blue)
        setText(v);
        setForeground(Color.BLUE);
        setHorizontalAlignment(SwingConstants.CENTER);
        return comp;
    }
}
