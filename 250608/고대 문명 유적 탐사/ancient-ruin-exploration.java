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
            // 최적 회전 맵으로 교체
            map = best.map;
            // 연쇄 제거 및 채우기
            int answer = count();
            if(answer == 0) break;
            bw.write(answer + " ");
        }
        bw.flush();
        bw.close();
        br.close();
    }

    /** 연쇄 제거 & 채우기 */
    private static int count() {
        int totalCount = 0;
        while (true) {
            List<Node> relics = checkRelicsRemain();
            if (relics.isEmpty()) return totalCount;
            // 삭제 표시
            for (Node node : relics) {
                map[node.x][node.y] = -1;
                totalCount++;
            }
            // 벽면 큐에서 채우기
            //우선순위에따라 정렬
            Collections.sort(relics, new Comparator<Node>() {
            	public int compare(Node o1, Node o2) {
            		if(o1.y == o2.y) {
            			return o2.x - o1.x;
            		}else {
            			return o1.y - o2.y;
            		}
            	}
            });
            
            for (Node node : relics) {
                if (!additional.isEmpty()) {
                    map[node.x][node.y] = additional.poll();
                }
            }
        }
    }

    private static List<Node> checkRelicsRemain() {
        List<Node> relics = new ArrayList<>();
        boolean[][] visited = new boolean[MAP_SIZE][MAP_SIZE];

        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                if (!visited[i][j]) {
                    bfs(i, j, visited, relics, map);
                }
            }
        }
        return relics;
    }

    /** 모든 회전 시뮬레이션 후 우선순위에 따라 첫 번째를 반환 */
    private static Simulation discovery() {
        List<Simulation> simulationResults = new ArrayList<>();

        for (int i = 1; i < MAP_SIZE - 1; i++) {
            for (int j = 1; j < MAP_SIZE - 1; j++) {
                for (int degree = 1; degree <= 3; degree++) {
                    int[][] tempMap = copyMap(map);
                    int[][] targetMap = getTargetMap(i, j, map);
                    int[][] rotated = rotateSubgrid(targetMap, degree);
                    tempMap = copySmallMap(i, j, tempMap, rotated);

                    // → 수정된 부분: r=i, c=j 그대로 넘깁니다.
                    Simulation sim = simulation(tempMap, i, j, degree);
                    simulationResults.add(sim);
                }
            }
        }

        // cost 내림차순, degree(=rotationCount) 오름차순, r 오름, c 오름
        simulationResults.sort((s1, s2) -> {
            if (s1.cost != s2.cost) return s2.cost - s1.cost;
            if (s1.rotationCount != s2.rotationCount) return s1.rotationCount - s2.rotationCount;
            if (s1.r != s2.r) return s1.r - s2.r;
            return s1.c - s2.c;
        });

        return simulationResults.get(0);
    }

    /** 첫 회전 후 1차 획득 개수만 계산 (removeNodes.size()) */
    private static Simulation simulation(int[][] tempMap, int i, int j, int rotateCount) {
        List<Node> removeNodes = new ArrayList<>();
        boolean[][] visited = new boolean[MAP_SIZE][MAP_SIZE];

        for (int x = 0; x < MAP_SIZE; x++) {
            for (int y = 0; y < MAP_SIZE; y++) {
                if (!visited[x][y]) {
                    bfs(x, y, visited, removeNodes, tempMap);
                }
            }
        }

        // → 수정된 부분: r=i, c=j, rotationCount=degree
        return new Simulation(removeNodes.size(), removeNodes, rotateCount, i, j, tempMap);
    }

    private static void bfs(int i, int j, boolean[][] visited, List<Node> removeNodes, int[][] map) {
        List<Node> tempList = new ArrayList<>();
        Queue<Node> queue = new ArrayDeque<>();
        int target = map[i][j];

        visited[i][j] = true;
        queue.add(new Node(i, j));

        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            if (map[cur.x][cur.y] == target) {
                tempList.add(cur);
            }
            for (int d = 0; d < 4; d++) {
                int nx = cur.x + dx[d], ny = cur.y + dy[d];
                if (!inRange(nx, ny) || visited[nx][ny] || map[nx][ny] != target) continue;
                visited[nx][ny] = true;
                queue.add(new Node(nx, ny));
            }
        }
        if (tempList.size() >= 3) {
            removeNodes.addAll(tempList);
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

    private static int[][] copySmallMap(int i, int j, int[][] tempMap, int[][] sourceMap) {
        for (int r = 0; r < MINI_MAP_SIZE; r++) {
            for (int c = 0; c < MINI_MAP_SIZE; c++) {
                tempMap[i - 1 + r][j - 1 + c] = sourceMap[r][c];
            }
        }
        return tempMap;
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
        int cost;            // 1차 획득 개수
        List<Node> costList;
        int rotationCount;   // degree
        int r, c;            // 회전 중심
        int[][] map;         // 회전 후 맵

        public Simulation(int cost, List<Node> costList, int rotationCount, int r, int c, int[][] map) {
            this.cost = cost;
            this.costList = costList;
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