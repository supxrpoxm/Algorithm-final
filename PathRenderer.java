import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathRenderer extends DefaultTableCellRenderer {
    private List<Point> path; 
    private final String[][] mapData;
    private final Map<Point, Point> pathMap = new HashMap<>();

    public PathRenderer(String[][] mapData) {
        this.mapData = mapData;
        setOpaque(true); 
        setHorizontalAlignment(CENTER); 
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
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String cellValue = (String) value;
        JLabel label = (JLabel) c;
        
        label.setFont(table.getFont()); 

        // 1. ตั้งค่าพื้นหลังและสัญลักษณ์ (S, G/E, X/#)
        Color wallColor = new Color(50, 50, 50); 
        
        if ("X".equals(cellValue) || "#".equals(cellValue)) {
            // ผนัง
            c.setBackground(wallColor); 
            label.setText(""); 
            label.setForeground(wallColor); 
        } else if ("S".equals(cellValue)) {
            // จุดเริ่มต้น
            c.setBackground(new Color(193, 255, 193)); 
            label.setText("S"); 
            label.setForeground(Color.BLACK);
        } else if ("E".equals(cellValue) || "G".equals(cellValue)) {
            // จุดสิ้นสุด
            c.setBackground(new Color(255, 179, 179)); 
            label.setText("G"); 
            label.setForeground(Color.BLACK);
        } else {
            // *** ทางเดินทั่วไป (รวม Cost) - แก้ไข Cost 1 ให้แสดงผลเป็น 1 ***
            c.setBackground(Color.WHITE); 
            
            if (".".equals(cellValue)) {
                // ถ้าเป็นจุด '.' (ค่าว่างที่ถูกแทนที่) ให้แสดง '.'
                label.setText("."); 
            } else if (cellValue.length() >= 1 && Character.isDigit(cellValue.charAt(0))) {
                // ถ้าเป็นตัวเลข (รวมถึง "1" ด้วย) ให้แสดงตัวเลขนั้น
                label.setText(cellValue); 
            } else {
                // หากค่าไม่ตรงกับที่คาดไว้ (Empty String หรือสัญลักษณ์ที่ไม่รู้จัก) ให้แสดง "." เป็นค่าเริ่มต้น
                label.setText("."); 
            }
            label.setForeground(Color.BLACK);
        }
        
        // 2. ตกแต่งเส้นทาง (Path) ให้เป็นลูกศรสีเขียว (ทับข้อความ/สีพื้นหลังบางส่วน)
        Point current = new Point(row, column);
        if (path != null) {
            Color pathColor = new Color(144, 238, 144); // เขียวอ่อนสำหรับพื้นหลังเส้นทาง
            Color arrowColor = new Color(0, 100, 0); // เขียวเข้มสำหรับลูกศร

            if (pathMap.containsKey(current)) {
                c.setBackground(pathColor); 
                
                Point next = pathMap.get(current);
                String arrow = getDirectionArrow(current, next);
                label.setText(arrow);
                label.setForeground(arrowColor); 
            } else if (path.contains(current) && !cellValue.equals("S") && !cellValue.equals("E") && !cellValue.equals("G")) {
                c.setBackground(pathColor);
            }
        }
        
        // 3. ทับสัญลักษณ์ 'S' และ 'G' กลับเข้าไปใหม่ (กรณีที่เส้นทางวิ่งผ่าน)
        if ("S".equals(cellValue)) {
            label.setText("S");
            label.setForeground(Color.BLACK);
        } else if ("E".equals(cellValue) || "G".equals(cellValue)) {
            label.setText("G");
            label.setForeground(Color.BLACK);
        }

        return c;
    }
    
    // Helper method สำหรับกำหนดลูกศรทิศทาง
    private String getDirectionArrow(Point current, Point next) {
        if (next.y > current.y) return "\u2192"; // Right arrow
        if (next.y < current.y) return "\u2190"; // Left arrow
        if (next.x < current.x) return "\u2191"; // Up arrow
        if (next.x > current.x) return "\u2193"; // Down arrow
        return "•";
    }
}