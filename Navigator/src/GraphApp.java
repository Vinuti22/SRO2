import java.util.*;

public class GraphApp {
    // Список смежности: Город -> Список дорог из него
    private Map<String, List<Edge>> map = new HashMap<>();

    // Добавление дороги
    public void addRoad(String city1, String city2, int dist) {
        map.computeIfAbsent(city1, k -> new ArrayList<>()).add(new Edge(city2, dist));
        map.computeIfAbsent(city2, k -> new ArrayList<>()).add(new Edge(city1, dist));
    }

    public void findPath(String start, String end) {
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.weight));
        Map<String, Integer> minDistance = new HashMap<>();
        Map<String, String> parentNodes = new HashMap<>();

        queue.add(new Node(start, 0));
        minDistance.put(start, 0);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.weight > minDistance.getOrDefault(current.name, Integer.MAX_VALUE)) continue;
            if (current.name.equals(end)) break;

            for (Edge road : map.getOrDefault(current.name, new ArrayList<>())) {
                int newPath = minDistance.get(current.name) + road.distance;
                if (newPath < minDistance.getOrDefault(road.target, Integer.MAX_VALUE)) {
                    minDistance.put(road.target, newPath);
                    parentNodes.put(road.target, current.name);
                    queue.add(new Node(road.target, newPath));
                }
            }
        }
        printRoute(start, end, minDistance, parentNodes);
    }

    // Вспомогательный класс для алгоритма
    private static class Node {
        String name;
        int weight;
        Node(String n, int w) { name = n; weight = w; }
    }

    private void printRoute(String start, String end, Map<String, Integer> dists, Map<String, String> parents) {
        if (!dists.containsKey(end)) {
            System.out.println("Маршрут не найден.");
            return;
        }
        List<String> path = new ArrayList<>();
        for (String at = end; at != null; at = parents.get(at)) path.add(at);
        Collections.reverse(path);
        System.out.println("Кратчайший путь: " + String.join(" -> ", path));
        System.out.println("Общее расстояние: " + dists.get(end) + " км");
    }

    public static void main(String[] args) {
        GraphApp myCity = new GraphApp();
        // Задаем связи (Вершины и ребра)
        myCity.addRoad("Центр", "Аэропорт", 15);
        myCity.addRoad("Центр", "Вокзал", 5);
        myCity.addRoad("Вокзал", "Аэропорт", 8);
        myCity.addRoad("Вокзал", "Университет", 10);
        myCity.addRoad("Университет", "Аэропорт", 20);

        myCity.findPath("Центр", "Аэропорт");
    }
}