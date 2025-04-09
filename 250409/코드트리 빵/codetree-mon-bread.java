import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
    
    /**
     * 빵을 구하려 하는 사람 M명
     * i번 사람은 i분에 각자의 베이스캠프에서 출발해 편의점으로 이동
     * N*N 크기의 격자 위에서 진행
     * 
     * 1분 동안 다음 나열된 행동을 진행
     * 1. 격자에 있는 모든 사람이 본인이 가고 싶은 편의점 방향을 향해 1칸 이동
     *    이때, 최단거리로 탐색하며, (상 좌 우 하)의 우선순위로 편의점을 향해 1칸 이동
     * 2. 편의점에 도착하면 멈춤, 이후부턴 다른 사람들은 해당 편의점을 지나갈 수 없음
     * 3. 현재 시간이 t분이고, t<=m이면, t번 사람은 가고싶은 편의점과 가장 가까이 있는 베이스 캠프에 들어감
     *    이후 다른 사람들은 해당 베이스캠프가 있는 칸을 지날 수 없음
     *    가장 가까이 있는 베이스캠프:
     *      1. 행이 작을 수록
     *      2. 열이 작을 수록
     */
    
    static final int BASECAMP = 1, BLANK = 0, USED = 3;
    static int N, M;
    static int [][] map;
    static ArrayList<ConvenientStore> convenientStores;
    static ArrayList<Node> personList;
    
    static ArrayList<Node> nodesToChangeUsing;
    
    public static void main(String[] args) throws IOException {
        init();
        solution();
    }
    
    static void solution() throws IOException{
        int time = 0;
        while (!isAllConvenientStoreUsed()) {
            time++;
            nodesToChangeUsing = new ArrayList<>();
            moveToConvenientStore();  // 1번 행동
            //2번 로직 구현 -> moveToConvenientStore 안에 구현되어 있음
            moveToBaseCamp(time);
            
            for(Node node : nodesToChangeUsing) {
            	map[node.x][node.y] = USED;
            }
        }
        
        bw.write(time+"\n");
        bw.flush();
        bw.close();
    }
    
    private static void moveToBaseCamp(int time) {
    	/**
    	 * 가고싶어하는 편의점에 가장 가까이 있는 베이스캠프 탐색
    	 * 행이 가장 작을수록, 열이 가장 작을수록
    	 */
    	
    	//갈수 있는 모든 사람이 이동하고 난 뒤에 이동할 수 없음 처리
    	for(int i=0;i<M;i++) {
    		Node person = personList.get(i);	//i번째 사람
    		ConvenientStore store = convenientStores.get(i);	//i번째 사람이 가고 싶어하는 편의점
    		
    		//person이 베이스캠프가 아닌 경우 continue;
    		if(i + 1 <= time && !inRange(person.x,person.y) && !store.isUsed) {
    			//이동할 수 있고, 가고자 하는 사람이 편의점에 도달하지 않은 경우    			
    			Node nearestBaseCamp = findNearestBaseCamp(store);
    			nodesToChangeUsing.add(nearestBaseCamp);
    			person.x = nearestBaseCamp.x;
    			person.y = nearestBaseCamp.y;
    		}
    	}
    	
	}

    private static Node findNearestBaseCamp(ConvenientStore store) {
        int[] dx = {-1, 0, 0, 1};
        int[] dy = {0, -1, 1, 0};
        Queue<Node> queue = new ArrayDeque<>();
        boolean [][] visited = new boolean[N][N];
        
        queue.add(new Node(store.x, store.y));
        visited[store.x][store.y] = true;
        
        while(!queue.isEmpty()) {
            int levelSize = queue.size();
            ArrayList<Node> candidates = new ArrayList<>();
            
            for (int i = 0; i < levelSize; i++) {
                Node curNode = queue.poll();
                
                // BASECAMP인 경우 후보로 추가
                if (map[curNode.x][curNode.y] == BASECAMP) {
                    candidates.add(curNode);
                }
                
                // 인접 셀 탐색
                for (int d = 0; d < 4; d++) {
                    int nx = curNode.x + dx[d];
                    int ny = curNode.y + dy[d];
                    
                    if (!inRange(nx, ny) || visited[nx][ny] || map[nx][ny] == USED) continue;
                    
                    visited[nx][ny] = true;
                    queue.add(new Node(nx, ny));
                }
            }
            
            // 해당 레벨에서 BASECAMP 후보가 하나라도 있다면 우선순위에 따라 선택
            if (!candidates.isEmpty()) {
                Collections.sort(candidates, new Comparator<Node>() {
                    public int compare(Node o1, Node o2) {
                        if (o1.x == o2.x) {
                            return o1.y - o2.y;
                        } else {
                            return o1.x - o2.x;
                        }
                    }
                });
           
                Node chosen = candidates.get(0);
                return chosen;
            }
        }
        return null;
    }


	private static void moveToConvenientStore() {
        for (int i = 0; i < M; i++) {
            Node person = personList.get(i);  // i번째 사람
            // 사람이 아직 격자 내에 있지 않으면 이동하지 않음.
            if (!inRange(person.x, person.y)) continue;
            
            ConvenientStore targetStore = convenientStores.get(i); // i번째 사람이 가고 싶은 편의점
            
            // 이미 편의점에 도착한 경우 추가 이동 없이 넘어감.
            if (person.x == targetStore.x && person.y == targetStore.y) continue;
            
            // i번째 사람이 움직여야 하는 방향
            Node moveDir = findFastRouteToConvenientStore(person.x, person.y, targetStore.x, targetStore.y);
            
            // 편의점에 도착한 경우
            if (targetStore.x == moveDir.x && targetStore.y == moveDir.y) {
                targetStore.isUsed = true;
                nodesToChangeUsing.add(new Node(targetStore.x,targetStore.y));
            }
            person.x = moveDir.x;
            person.y = moveDir.y;
        }
    }
    
    // 편의점까지 최단 경로로 가기 위해 어떤 방향으로 이동해야 하는지 반환
    static Node findFastRouteToConvenientStore(int startX, int startY, int endX, int endY) {
        // (상, 좌, 우, 하) 우선순위
        int[] dx = {-1, 0, 0, 1};
        int[] dy = {0, -1, 1, 0};
        
        Queue<NodeToBFS> queue = new ArrayDeque<>();
        boolean[][] visited = new boolean[N][N];
        
        visited[startX][startY] = true;
        queue.add(new NodeToBFS(startX, startY, null));
        
        while (!queue.isEmpty()) {
            NodeToBFS curNode = queue.poll();
            if (curNode.x == endX && curNode.y == endY) {
                // 목표에 도달한 경우, 경로를 역추적하여 리스트 생성
                ArrayList<Node> route = new ArrayList<>();
                NodeToBFS cur = curNode;
                while (cur != null) {
                    route.add(new Node(cur.x, cur.y));
                    cur = cur.parent;
                }
                Collections.reverse(route);
                // 이미 출발지에 있다면 그대로 반환, 아니면 한 칸 전진한 위치를 반환
                return (route.size() > 1) ? route.get(1) : route.get(0);
            }
            
            for (int d = 0; d < 4; d++) {
                int nx = curNode.x + dx[d];
                int ny = curNode.y + dy[d];
                
                // 범위 밖이거나, 방문했거나, 사용중인 칸(편의점/베이스캠프)인 경우 넘어감
                if (!inRange(nx, ny) || visited[nx][ny] || map[nx][ny] == USED) continue;
                
                visited[nx][ny] = true;
                queue.add(new NodeToBFS(nx, ny, curNode));
            }
        }
        return null;
    }
    
    static class NodeToBFS {
        int x, y;
        NodeToBFS parent;
        public NodeToBFS(int x, int y, NodeToBFS parent) {
            this.x = x;
            this.y = y;
            this.parent = parent;
        }
		@Override
		public String toString() {
			return "NodeToBFS [x=" + x + ", y=" + y + ", parent=" + parent + "]";
		}
    }
    
    static boolean inRange(int x, int y) {
        return x >= 0 && x < N && y >= 0 && y < N;
    }
    
    private static boolean isAllConvenientStoreUsed() {
        for (ConvenientStore store : convenientStores) {
            if (!store.isUsed) return false;
        }
        return true;
    }
    
    static void init() throws IOException {
        String[] tokens = br.readLine().split(" ");
        N = Integer.parseInt(tokens[0]);  // 격자 크기
        M = Integer.parseInt(tokens[1]);  // 사람의 수
        
        // 격자 입력
        map = new int[N][N];
        for (int i = 0; i < N; i++) {
            tokens = br.readLine().split(" ");
            for (int j = 0; j < N; j++) {
                map[i][j] = Integer.parseInt(tokens[j]);
            }
        }
        
        // 가고 싶어 하는 편의점 입력
        convenientStores = new ArrayList<>();
        for (int i = 0; i < M; i++) {
            tokens = br.readLine().split(" ");
            int x = Integer.parseInt(tokens[0])-1;
            int y = Integer.parseInt(tokens[1])-1;
            convenientStores.add(new ConvenientStore(x, y, false));
        }
        
        // personList 초기화 (초기 위치: 격자 바깥)
        personList = new ArrayList<>();
        for (int i = 0; i < M; i++) {
            personList.add(new Node(-1, -1));
        }
    }
    
    static class ConvenientStore {
        int x, y;
        boolean isUsed;
        public ConvenientStore(int x, int y, boolean isUsed) {
            this.x = x;
            this.y = y;
            this.isUsed = isUsed;
        }
		@Override
		public String toString() {
			return "ConvenientStore [x=" + x + ", y=" + y + ", isUsed=" + isUsed + "]";
		}
    }
    
    static class Node {
        int x, y;
        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
		@Override
		public String toString() {
			return "Node [x=" + x + ", y=" + y + "]";
		}
    }
}
