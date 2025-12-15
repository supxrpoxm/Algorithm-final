import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class PathRenderer extends DefaultTableCellRenderer {
    private List<Point> path;
    private String[][] mapData;
    private final Map<Point, Point> pathMap = new HashMap<>();

    public PathRenderer(String[][] mapData) {
        this.mapData = mapData;
        setOpaque(true);
        setHorizontalAlignment(CENTER);
    }

    public void setMap(String[][] mapData) {
        this.mapData = mapData;
    }

    public void setPath(List<Point> path) {
        this.path = path;
        pathMap.clear();
        if (path != null && path.size() > 1) {
            for (int i = 0; i < path.size() - 1; i++) {
                pathMap.put(path.get(i), path.get(i + 1));
            }
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setFont(table.getFont());
        String cellValue = (value != null) ? value.toString() : ".";

        Color wallColor = new Color(50, 50, 50);
        Color pathColor = new Color(144, 238, 144);
        Color arrowColor = new Color(0, 100, 0);

        if ("X".equals(cellValue) || "#".equals(cellValue)) {
            label.setBackground(wallColor);
            label.setText("");
            label.setForeground(wallColor);
        } else if ("S".equals(cellValue)) {
            label.setBackground(new Color(193, 255, 193));
            label.setText("S");
            label.setForeground(Color.BLACK);
        } else if ("E".equals(cellValue) || "G".equals(cellValue)) {
            label.setBackground(new Color(255, 179, 179));
            label.setText("G");
            label.setForeground(Color.BLACK);
        } else {
            label.setBackground(Color.WHITE);
            if (".".equals(cellValue)) label.setText(".");
            else if (cellValue.length() >= 1 && Character.isDigit(cellValue.charAt(0))) label.setText(cellValue);
            else label.setText(".");
            label.setForeground(Color.BLACK);
        }

        if (path != null) {
            Point current = new Point(row, column);
            if (pathMap.containsKey(current)) {
                label.setBackground(pathColor);
                Point next = pathMap.get(current);
                label.setText(getDirectionArrow(current, next));
                label.setForeground(arrowColor);
            } else if (path.contains(current) && !"S".equals(cellValue) && !"E".equals(cellValue) && !"G".equals(cellValue)) {
                label.setBackground(pathColor);
            }
        }

        return label;
    }

    private String getDirectionArrow(Point current, Point next) {
        if (next.y > current.y) return "\u2192";
        if (next.y < current.y) return "\u2190";
        if (next.x < current.x) return "\u2191";
        if (next.x > current.x) return "\u2193";
        return "•";
    }
}
