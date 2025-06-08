import java.io.*;
import java.util.*;

public class Main {
    private static final int MAP_SIZE = 5;
    private static final int MINI_MAP_SIZE = 3;

    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    private static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

    static int K, M;
    static int[][] map;
    static Queue<Integer> additional;
    static int[] dx = {0, 0, -1, 1};
    static int[] dy = {-1, 1, 0, 0};

    public static void main(String[] args) throws IOException {
        init();
        for (int turn = 1; turn <= K; turn++) {
            Simulation best = discovery();
            if (best.cost == 0) {
                break;
            }
            
            map = best.map;
            int scoreForTurn = count();
            
            bw.write(scoreForTurn + " ");
        }
        bw.flush();
        bw.close();
        br.close();
    }

    /** 연쇄 제거 & 채우기 */
    private static int count() {
        int totalScore = 0;
        
        while (true) {
            List<Node> relicsToRemove = checkRelicsRemain();

            if (relicsToRemove.isEmpty()) {
                break;
            }

            // 3. 찾은 유물을 제거하고 점수를 획득
            totalScore += relicsToRemove.size();
            for (Node node : relicsToRemove) {
                map[node.x][node.y] = 0; // 빈 공간으로 표시 (0 또는 -1 사용)
            }

            // 채우는 순서: 열 번호 오름차순, 행 번호 내림차순
            Collections.sort(relicsToRemove, (o1, o2) -> {
                if (o1.y == o2.y) {
                    return o2.x - o1.x; // 열이 같으면 행이 큰 순서 (아래쪽 먼저)
                }
                return o1.y - o2.y;     // 열이 작은 순서 (왼쪽 먼저)
            });

            for (Node node : relicsToRemove) {
                if (!additional.isEmpty()) {
                    map[node.x][node.y] = additional.poll();
                }
            }
        }
        return totalScore;
    }

    private static List<Node> checkRelicsRemain() {
        List<Node> relics = new ArrayList<>();
        boolean[][] visited = new boolean[MAP_SIZE][MAP_SIZE];

        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                if (!visited[i][j] && map[i][j] > 0) { // 0이 아닌 유물만 탐색
                    bfs(i, j, visited, relics, map);
                }
            }
        }
        return relics;
    }

    /** 모든 회전 시뮬레이션 후 우선순위에 따라 최적의 결과를 반환 */
    private static Simulation discovery() {
        List<Simulation> simulationResults = new ArrayList<>();

        for (int j = 1; j < MAP_SIZE - 1; j++) { // 중심 열
            for (int i = 1; i < MAP_SIZE - 1; i++) { // 중심 행
                for (int degree = 1; degree <= 3; degree++) {
                    // 회전 시뮬레이션
                    int[][] tempMap = copyMap(map);
                    int[][] subgrid = getTargetMap(i, j, tempMap);
                    int[][] rotated = rotateSubgrid(subgrid, degree);
                    copySmallMap(i, j, tempMap, rotated);
                    
                    // 회전 후 1차 획득량 계산
                    Simulation sim = simulateAcquisition(tempMap, i, j, degree);
                    simulationResults.add(sim);
                }
            }
        }

        // 우선순위에 따라 정렬
        simulationResults.sort((s1, s2) -> {
            if (s1.cost != s2.cost) return s2.cost - s1.cost;           // 1. 획득 가치 내림차순
            if (s1.rotationCount != s2.rotationCount) return s1.rotationCount - s2.rotationCount; // 2. 회전 각도 오름차순
            if (s1.c != s2.c) return s1.c - s2.c;                         // 3. 중심 열 오름차순
            return s1.r - s2.r;                                           // 4. 중심 행 오름차순
        });

        return simulationResults.get(0);
    }

    private static Simulation simulateAcquisition(int[][] tempMap, int r, int c, int rotateCount) {
        List<Node> removeNodes = new ArrayList<>();
        boolean[][] visited = new boolean[MAP_SIZE][MAP_SIZE];

        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                if (!visited[i][j] && tempMap[i][j] > 0) {
                    bfs(i, j, visited, removeNodes, tempMap);
                }
            }
        }

        return new Simulation(removeNodes.size(), rotateCount, r, c, tempMap);
    }

    private static void bfs(int startX, int startY, boolean[][] visited, List<Node> removeNodes, int[][] currentMap) {
        List<Node> component = new ArrayList<>();
        Queue<Node> queue = new ArrayDeque<>();
        int targetValue = currentMap[startX][startY];

        visited[startX][startY] = true;
        queue.add(new Node(startX, startY));
        component.add(new Node(startX, startY));

        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            
            for (int d = 0; d < 4; d++) {
                int nx = cur.x + dx[d];
                int ny = cur.y + dy[d];

                if (!inRange(nx, ny) || visited[nx][ny] || currentMap[nx][ny] != targetValue) {
                    continue;
                }
                
                visited[nx][ny] = true;
                queue.add(new Node(nx, ny));
                component.add(new Node(nx, ny));
            }
        }

        if (component.size() >= 3) {
            removeNodes.addAll(component);
        }
    }
    
    private static boolean inRange(int x, int y) {
        return x >= 0 && x < MAP_SIZE && y >= 0 && y < MAP_SIZE;
    }

    private static int[][] rotateSubgrid(int[][] subgrid, int degree) {
        int[][] result = subgrid;
        for (int d = 0; d < degree; d++) {
            int[][] tmp = new int[MINI_MAP_SIZE][MINI_MAP_SIZE];
            for (int r = 0; r < MINI_MAP_SIZE; r++) {
                for (int c = 0; c < MINI_MAP_SIZE; c++) {
                    tmp[r][c] = result[MINI_MAP_SIZE - 1 - c][r];
                }
            }
            result = tmp;
        }
        return result;
    }

    private static int[][] getTargetMap(int i, int j, int[][] currentMap) {
        int[][] target = new int[MINI_MAP_SIZE][MINI_MAP_SIZE];
        for (int r = 0; r < MINI_MAP_SIZE; r++) {
            for (int c = 0; c < MINI_MAP_SIZE; c++) {
                target[r][c] = currentMap[i - 1 + r][j - 1 + c];
            }
        }
        return target;
    }

    private static void copySmallMap(int i, int j, int[][] tempMap, int[][] sourceMap) {
        for (int r = 0; r < MINI_MAP_SIZE; r++) {
            for (int c = 0; c < MINI_MAP_SIZE; c++) {
                tempMap[i - 1 + r][j - 1 + c] = sourceMap[r][c];
            }
        }
    }

    private static int[][] copyMap(int[][] original) {
        int[][] copy = new int[MAP_SIZE][MAP_SIZE];
        for (int r = 0; r < MAP_SIZE; r++) {
            System.arraycopy(original[r], 0, copy[r], 0, MAP_SIZE);
        }
        return copy;
    }

    private static void init() throws IOException {
        String[] tokens = br.readLine().split(" ");
        K = Integer.parseInt(tokens[0]);
        M = Integer.parseInt(tokens[1]);
        map = new int[MAP_SIZE][MAP_SIZE];
        for (int i = 0; i < MAP_SIZE; i++) {
            tokens = br.readLine().split(" ");
            for (int j = 0; j < MAP_SIZE; j++) {
                map[i][j] = Integer.parseInt(tokens[j]);
            }
        }
        tokens = br.readLine().split(" ");
        additional = new ArrayDeque<>();
        for (String t : tokens) {
            additional.add(Integer.parseInt(t));
        }
    }

    private static class Simulation {
        int cost;
        int rotationCount;
        int r, c;
        int[][] map;

        public Simulation(int cost, int rotationCount, int r, int c, int[][] map) {
            this.cost = cost;
            this.rotationCount = rotationCount;
            this.r = r;
            this.c = c;
            this.map = map;
        }
    }

    private static class Node {
        int x, y;
        public Node(int x, int y) { this.x = x; this.y = y; }
    }
}