import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class VisualMapApp extends JFrame {
    private Image backgroundImage;
    private final List<PointNode> nodes = new ArrayList<>();
    private final List<Edge> roads = new ArrayList<>();

    private Integer startNode = null;
    private Integer endNode = null;
    private List<Integer> path = new ArrayList<>();

    public VisualMapApp() {
        setTitle("Навигатор: ЛКМ - Точка, ПКМ - Путь, Колесико - Удалить");
        setSize(1200, 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        backgroundImage = new ImageIcon("map.png").getImage();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ЛЕВАЯ КНОПКА - Создать точку
                if (SwingUtilities.isLeftMouseButton(e)) {
                    nodes.add(new PointNode(e.getX(), e.getY()));
                    if (nodes.size() > 1) {
                        int from = nodes.size() - 2;
                        int to = nodes.size() - 1;
                        int dist = (int) Math.hypot(nodes.get(from).x - nodes.get(to).x, nodes.get(from).y - nodes.get(to).y);
                        roads.add(new Edge(from, to, dist));
                    }
                }
                // ПРАВАЯ КНОПКА - Выбор Старт/Финиш
                else if (SwingUtilities.isRightMouseButton(e)) {
                    if (nodes.isEmpty()) return;
                    int clickedIndex = findNearestNode(e.getX(), e.getY());
                    if (startNode == null) {
                        startNode = clickedIndex;
                        path.clear();
                    } else {
                        endNode = clickedIndex;
                        calculatePath();
                        startNode = null;
                        endNode = null;
                    }
                }
                // НАЖАТИЕ НА КОЛЕСИКО - Удаление
                else if (e.getButton() == MouseEvent.BUTTON2) {
                    if (nodes.isEmpty()) return;
                    int target = findNearestNode(e.getX(), e.getY());
                    removeNode(target);
                }
                repaint();
            }
        });
    }

    private void removeNode(int index) {
        // Удаляем все дороги, связанные с этой точкой
        roads.removeIf(edge -> edge.fromIndex == index || edge.toIndex == index);

        // Сдвигаем индексы в оставшихся дорогах
        for (Edge edge : roads) {
            if (edge.fromIndex > index) edge.fromIndex--;
            if (edge.toIndex > index) edge.toIndex--;
        }

        nodes.remove(index);
        path.clear(); // Сбрасываем путь, так как карта изменилась
        startNode = null;
        endNode = null;
    }

    private void calculatePath() {
        if (startNode == null || endNode == null) return;
        PriorityQueue<NodeDist> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.dist));
        Map<Integer, Integer> distances = new HashMap<>();
        Map<Integer, Integer> parents = new HashMap<>();

        pq.add(new NodeDist(startNode, 0));
        distances.put(startNode, 0);

        while (!pq.isEmpty()) {
            NodeDist current = pq.poll();
            if (current.dist > distances.getOrDefault(current.id, Integer.MAX_VALUE)) continue;

            for (Edge edge : roads) {
                int neighbor = -1;
                if (edge.fromIndex == current.id) neighbor = edge.toIndex;
                else if (edge.toIndex == current.id) neighbor = edge.fromIndex;

                if (neighbor != -1) {
                    int newDist = current.dist + edge.weight;
                    if (newDist < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                        distances.put(neighbor, newDist);
                        parents.put(neighbor, current.id);
                        pq.add(new NodeDist(neighbor, newDist));
                    }
                }
            }
        }

        path.clear();
        Integer step = endNode;
        while (step != null) {
            path.add(step);
            step = parents.get(step);
        }
    }

    private int findNearestNode(int x, int y) {
        int closest = 0;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < nodes.size(); i++) {
            double d = Math.hypot(nodes.get(i).x - x, nodes.get(i).y - y);
            if (d < minDist) { minDist = d; closest = i; }
        }
        return closest;
    }

    @Override
    public void paint(Graphics g) {
        Image buffer = createImage(getWidth(), getHeight());
        Graphics2D g2 = (Graphics2D) buffer.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.GRAY);
        for (Edge road : roads) {
            g2.drawLine(nodes.get(road.fromIndex).x, nodes.get(road.fromIndex).y,
                    nodes.get(road.toIndex).x, nodes.get(road.toIndex).y);
        }

        g2.setStroke(new BasicStroke(5));
        g2.setColor(new Color(0, 255, 0, 150)); // Полупрозрачный зеленый
        for (int i = 0; i < path.size() - 1; i++) {
            PointNode n1 = nodes.get(path.get(i));
            PointNode n2 = nodes.get(path.get(i+1));
            g2.drawLine(n1.x, n1.y, n2.x, n2.y);
        }

        for (int i = 0; i < nodes.size(); i++) {
            PointNode n = nodes.get(i);
            g2.setColor(Integer.valueOf(i).equals(startNode) ? Color.BLUE : Color.RED);
            g2.fillOval(n.x - 5, n.y - 5, 10, 10);
        }
        g.drawImage(buffer, 0, 0, this);
    }

    static class PointNode { int x, y; PointNode(int x, int y) { this.x = x; this.y = y; } }
    static class Edge { int fromIndex, toIndex, weight; Edge(int f, int t, int w) { fromIndex = f; toIndex = t; weight = w; } }
    static class NodeDist { int id, dist; NodeDist(int i, int d) { id = i; dist = d; } }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VisualMapApp().setVisible(true));
    }
}