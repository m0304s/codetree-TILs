import java.io.*;
import java.util.*;

import javax.jws.soap.SOAPBinding;
import javax.sound.sampled.SourceDataLine;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

    // 상수값 정의: ROAD, NOT_ROAD, WARRIOR, STONE, SIGHT, SCOPE_WALL
    static final int ROAD = 0, NOT_ROAD = 1;
    static final int WARRIOR = 2, STONE = 4, SIGHT = 1, SCOPE_WALL = 3;
    static int N, M;
    static int[][] map;
    static ArrayList<Soldier> soldiers;
    static Point medusa;
    static Point park;
    
    /**
     * NxN 크기의 마을
     * 도로(0), 비도로(1)
     *
     * 메두사 (Sr,Sc)에 위치
     * 공원 (Er,Ec)에 위치
     *
     * 메두사는 도로만을 따라 최단경로로 이동
     *
     * M명의 전사 존재
     * 초기 위치 (R,C)형태로 주어짐, 메두사를 향해 최단경로로 이동
     * 전사는 도로, 비도로 상관하지 않고 최단경로로 이동
     *
     * 1. 메두사의 이동
     * 도로를 따라 공원까지 최단 경로로 이동
     * (상, 하, 좌, 우) 우선순위 따름
     * 메두사의 집으로부터 공원까지 가는 길이 없을 수도 있음
     *
     * 2. 메두사의 시선
     * (상,하,좌,우) 중 하나를 선택해 바라봄 -> 가장 전사를 많이 볼 수 있는 방향으로 바라봄
     * 90도의 시야각
     * 메두사의 시야각 안에 존재하지만, 다른 전사에 가려진 경우 메두사에게 보이지 않음
     *
     * 시야 안에 들어온 병사들은 돌로 변함
     * 3. 전사들의 이동 -> 메두사의 시야가 필요함
     * 돌로 변하지 않은 전사들은 메두사를 향해 최대 2칸까지 이동
     * 격자의 바깥으로 이동 불가, 메두사의 시야에 들어오는 곳으로는 이동할 수 없음
     *
     *  첫번째 이동
     *      상하좌우의 우선순위로 방향 선택
     *  두번째 이동
     *      좌우상하의 우선순위로 방향 선택
     *
     * 4. 전사의 공격
     * 메두사와 같은 칸에 도착한 전사는 메두사를 공격, 사라짐
     *
     * 출력 : 각 턴마다 모든 전사가 이동한 거리의 합, 메두사로 인해 돌이 된 전사의 수, 메두사를 공격한 전사의 수
     * 단 메두사가 공원에 도착하면 0을 출력, 그렇지 않으면 -1을 출력
     */
    
    public static void main(String[] args) throws IOException {
        // 시뮬레이션 혹은 디버그를 위해 초기화 후 실행
        init();
        List<Point> route = findShortestPathFromMedusaToPark();
        if(route == null){  
            bw.write("-1\n");
            bw.close();
            br.close();
        }else {        	
        	simulation(route);
        	bw.flush();
        	bw.close();
        	br.close();
        }
    }
    
    static void simulation(List<Point> route) throws IOException{
        for(int i = route.size() - 2; i >= 0; i--){
            moveMedusa(route, i);    // 메두사 이동
            Simulation best = watchMedusa();  // 메두사의 시선 처리
            int stoneSoldiers = best.stoneSoldiers;
            //병사들 돌로 변경
            int totalMoveDistance = moveSoldiers(best.sights); // 전사들의 이동 처리
            int attackSoldiers = attackMedusa();	// 전사 공격
            changeStatus();			// 돌이었던 병사 ALIVE로 변경
            if(medusa.x == park.x && medusa.y == park.y) bw.write("0\n");
            else
            	bw.write(totalMoveDistance + " " + stoneSoldiers + " " + attackSoldiers+"\n");
        }
    }
    
    private static int attackMedusa() {
    	ArrayList<Soldier> list = new ArrayList<>();
		for(int i=0;i<soldiers.size();i++) {
			Soldier soldier = soldiers.get(i);
			if(soldier.status == Status.STONE) continue;
			
			if(soldier.x == medusa.x && soldier.y == medusa.y) {
				list.add(soldier);
			}
		}
		
		soldiers.removeAll(list);
		return list.size();
	}

	private static void changeStatus() {
		for(int i=0;i<soldiers.size();i++) {
			Soldier soldier = soldiers.get(i);
			if(soldier.status == Status.STONE) soldier.status = Status.ALIVE;
		}
	}

	private static int moveSoldiers(int[][] sights) {
        int totalDistance = 0;
        for (int i = 0; i < soldiers.size(); i++) {
            Soldier soldier = soldiers.get(i);
            if(soldier.status == Status.STONE) continue;
            
            //첫번째 이동 로직
            int[] bestForFirstPhase = moveFirst(soldier,sights);
            if(bestForFirstPhase == null) continue;
            int distance = calcDistance(new Point(soldier.x,soldier.y), new Point(bestForFirstPhase[0],bestForFirstPhase[1]));
            soldier.x = bestForFirstPhase[0];
            soldier.y = bestForFirstPhase[1];
            totalDistance += distance;
            
            if(soldier.x == medusa.x && soldier.y == medusa.y) {
            	continue;
            }
            
            //두번째 이동 로직
            int [] bestForSecondPhase = moveSecond(soldier,sights);
            if(bestForSecondPhase == null) continue;
            
            distance = calcDistance(new Point(soldier.x,soldier.y), new Point(bestForSecondPhase[0],bestForSecondPhase[1]));
            soldier.x = bestForSecondPhase[0];
            soldier.y = bestForSecondPhase[1];
            totalDistance += distance;
        }
        return totalDistance;
    }
    
    private static int[] moveSecond(Soldier soldier, int[][] sights) {
		ArrayList<int []> candidates = new ArrayList<>();
		//좌우상하
		int [] dx = {0, 0, -1, 1};
		int [] dy = {-1, 1, 0, 0};
		
		int originalDistance = calcDistance(new Point(soldier.x,soldier.y), medusa);
		for(int d=0;d<4;d++) {
			int nx = soldier.x + dx[d];
			int ny = soldier.y + dy[d];
			
			if(!inRange(nx, ny)) continue;
			//기존의 거리보다 더 가까워지는 경우
			int newDistance = calcDistance(new Point(nx,ny), medusa);
			if(sights[nx][ny] == SIGHT || sights[nx][ny] == STONE || newDistance >= originalDistance) continue;
			
			candidates.add(new int[] {nx,ny,d, newDistance});
		}
		
		if(candidates.isEmpty()) return null;
		Collections.sort(candidates, new Comparator<int []>(){
			public int compare(int [] o1, int [] o2) {
				if(o1[3] == o2[3]) {					
					return o1[2] - o2[2];
				}else {
					return o1[3] - o2[3];
				}
			}
		});
		
		return candidates.get(0);
	}

	private static int[] moveFirst(Soldier soldier, int[][] sights) {
		ArrayList<int []> candidates = new ArrayList<>();
		//상하좌우
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        
        int originalDistance = calcDistance(new Point(soldier.x,soldier.y), medusa);
		for(int d=0;d<4;d++) {
			int nx = soldier.x + dx[d];
			int ny = soldier.y + dy[d];
			
			if(!inRange(nx, ny)) continue;
			//기존의 거리보다 더 가까워지는 경우
			int newDistance = calcDistance(new Point(nx,ny), medusa);
			if(sights[nx][ny] == SIGHT || sights[nx][ny] == STONE || newDistance >= originalDistance) continue;
			
			candidates.add(new int[] {nx,ny,d, newDistance});
		}
		
		if(candidates.isEmpty()) return null;
		Collections.sort(candidates, new Comparator<int []>(){
			public int compare(int [] o1, int [] o2) {
				if(o1[3] == o2[3])
					return o1[2] - o2[2];
				else return o1[3] - o2[3];
			}
		});
		
		return candidates.get(0);
	}

	private static Simulation watchMedusa() {
        ArrayList<Simulation> simulationResults = new ArrayList<>();
        for (int d = 0; d < 4; d++) {
            Simulation result = countStoneWarriors(d);
            simulationResults.add(result);
        }
        Collections.sort(simulationResults, new Comparator<Simulation>() {
            public int compare(Simulation o1, Simulation o2) {
                if(o1.stoneSoldiers == o2.stoneSoldiers) {
                    return o1.dir - o2.dir;
                } else {
                    return o2.stoneSoldiers - o1.stoneSoldiers;
                }
            }
        });
        
        //메두사를 본 결과 돌로 변한 병사 상태 변환
        Simulation best = simulationResults.get(0);
        for(int i=0;i<N;i++) {
        	for(int j=0;j<N;j++) {
        		if(best.sights[i][j] == STONE) {
        			Soldier soldier = findSoldiers(i,j);
        			soldier.status = Status.STONE;
        		}
        	}
        }
        return simulationResults.get(0);  // 가장 전사를 돌로 만든 결과 반환 (예시)
    }
    
    private static Soldier findSoldiers(int i, int j) {
		for(int k=0;k<soldiers.size();k++) {
			Soldier soldier = soldiers.get(k);
			if(soldier.x == i && soldier.y == j) {
				return soldier;
			}
		}
		return null;
	}

	/**
     * 메두사가 특정 방향(상, 하, 좌, 우)으로 보았을 때, 돌로 변하는 전사의 수와 시야 배열 반환
     */
    private static Simulation countStoneWarriors(int d) {
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        int medusaX = medusa.x;
        int medusaY = medusa.y;
        int totalWarriors = 0;
        
        int[][] sight = new int[N][N];
        // 일직선 방향 시야 처리
        int startX = medusaX + dx[d];
        int startY = medusaY + dy[d];
        totalWarriors += findStraight(new Point(startX, startY), sight, d);
        
        // 대각선 방향 시야 처리
        Sight sights = getSights(d);
        Point leftDiagonal = sights.left;
        Point rightDiagonal = sights.right;
        
        totalWarriors += findDir(medusa, sight, leftDiagonal, d);
        totalWarriors += findDir(medusa, sight, rightDiagonal, d);
        return new Simulation(totalWarriors, d, sight);
    }
    
    /**
     * - medusa로부터 diagonal 방향으로 한 칸씩 이동하며 전사가 존재하면 해당 셀을 STONE 처리
     * - 이후 남은 대각선 경로는 SCOPE_WALL 처리 후 break
     * - 전사가 없으면 SIGHT로 마킹한 후, 메두사의 주 시선 방향(상/하/좌/우)으로 추가 탐색
     * WARRIOR = 2, STONE = 4, SIGHT = 1, SCOPE_WALL = 3
     */
    private static int findDir(Point medusa, int[][] sight, Point diagonal, int dir) {
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        int cnt = 0;
        int step = 1;
        while (true) {
            int nx = medusa.x + step * diagonal.x;
            int ny = medusa.y + step * diagonal.y;
            if (!inRange(nx, ny)) break;
            
            // 전사가 있는 경우: 해당 좌표를 STONE으로 처리 후, 남은 대각선은 SCOPE_WALL 처리
            if (isSoldier(nx, ny)) {
                sight[nx][ny] = STONE;
                cnt += getWarriorsAtPoint(nx, ny);
                step++;
                while (true) {
                    nx = medusa.x + step * diagonal.x;
                    ny = medusa.y + step * diagonal.y;
                    if (!inRange(nx, ny)) break;
                    sight[nx][ny] = SCOPE_WALL;
                    step++;
                }
                break;
            }
            // 전사가 없다면 해당 좌표를 SIGHT로 마킹
            sight[nx][ny] = SIGHT;
            
            // 두 번째 while: 현재 대각선 위치에서 메두사가 주 시선으로 보는 방향으로 직선 탐색
            int sx = nx;
            int sy = ny;
            while (true) {
                sx += dx[dir];
                sy += dy[dir];
                if (!inRange(sx, sy)) break;
                if (sight[sx][sy] == SCOPE_WALL) break;
                if (isSoldier(sx, sy)) {
                    sight[sx][sy] = STONE;
                    cnt += getWarriorsAtPoint(sx, sy);
                    int x = sx;
                    int y= sy;
                    while (true) {
                        sx += dx[dir];
                        sy += dy[dir];
                        if (!inRange(sx, sy)) break;
                        sight[sx][sy] = SCOPE_WALL;
                    }
                    while(true) {
                    	x += diagonal.x;
                    	y += diagonal.y;
                    	if(!inRange(x,y)) break;
                    	sight[x][y] = SCOPE_WALL;
                    }
                    break;
                }
                sight[sx][sy] = SIGHT;
            }
            step++;
        }
        return cnt;
    }
    
    // 일직선 방향 시야 처리 메소드
    private static int findStraight(Point point, int[][] sight, int dir) {
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        int nx = point.x;
        int ny = point.y;
        while (true) {
            if (!inRange(nx, ny)) {
                return 0;
            }
            if (isSoldier(nx, ny)) {
                sight[nx][ny] = STONE;
                return getWarriorsAtPoint(nx, ny);
            }
            sight[nx][ny] = SIGHT;
            nx += dx[dir];
            ny += dy[dir];
        }
    }
    
    private static int getWarriorsAtPoint(int nx, int ny) {
        int cnt = 0;
        for (int i = 0; i < soldiers.size(); i++) {
            Soldier soldier = soldiers.get(i);
            if (nx == soldier.x && ny == soldier.y) cnt++;
        }
        return cnt;
    }
    
    private static boolean isSoldier(int nx, int ny) {
        for (int i = 0; i < soldiers.size(); i++) {
            Soldier soldier = soldiers.get(i);
            if (nx == soldier.x && ny == soldier.y) return true;
        }
        return false;
    }
    
    static void moveMedusa(List<Point> route, int i) {
        Point medusaPoint = route.get(i);
        medusa = medusaPoint;
        int deadSoldierCnt = makeSoldierDie(medusa);
    }
    
    static int makeSoldierDie(Point point) {
        ArrayList<Soldier> deadSoldiers = new ArrayList<>();
        for (int i = 0; i < soldiers.size(); i++) {
            Soldier soldier = soldiers.get(i);
            if (point.x == soldier.x && point.y == soldier.y) {
                deadSoldiers.add(soldier);
            }
        }
        soldiers.removeAll(deadSoldiers);
        return deadSoldiers.size();
    }
    
    // BFS를 이용하여 메두사에서 공원까지의 최단 경로 탐색
    private static List<Point> findShortestPathFromMedusaToPark() {
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        
        ArrayList<Point> history = new ArrayList<>();
        Queue<BFSNode> queue = new ArrayDeque<>();
        boolean[][] visited = new boolean[N][N];
        
        queue.add(new BFSNode(medusa.x, medusa.y, null));
        visited[medusa.x][medusa.y] = true;
        
        while (!queue.isEmpty()){
            BFSNode curNode = queue.poll();
            if (curNode.x == park.x && curNode.y == park.y) {
                BFSNode parent = curNode;
                while (parent != null) {
                    history.add(new Point(parent.x, parent.y));
                    parent = parent.parent;
                }
                return history;
            }
            for (int d = 0; d < 4; d++) {
                int nx = curNode.x + dx[d];
                int ny = curNode.y + dy[d];
                if (!inRange(nx, ny) || visited[nx][ny] || map[nx][ny] != ROAD) continue;
                queue.add(new BFSNode(nx, ny, curNode));
                visited[nx][ny] = true;
            }
        }
        return null;
    }
    
    static boolean inRange(int x, int y) {
        return x >= 0 && x < N && y >= 0 && y < N;
    }
    
    static void init() throws IOException {
        String[] tokens = br.readLine().split(" ");
        N = Integer.parseInt(tokens[0]);
        M = Integer.parseInt(tokens[1]);
        
        tokens = br.readLine().split(" ");
        int x = Integer.parseInt(tokens[0]);
        int y = Integer.parseInt(tokens[1]);
        medusa = new Point(x, y);
        
        x = Integer.parseInt(tokens[2]);
        y = Integer.parseInt(tokens[3]);
        park = new Point(x, y);
        
        tokens = br.readLine().split(" ");
        soldiers = new ArrayList<>();
        for (int i = 0; i < tokens.length; i += 2) {
            x = Integer.parseInt(tokens[i]);
            y = Integer.parseInt(tokens[i + 1]);
            Soldier soldier = new Soldier(x, y, Status.ALIVE);
            soldiers.add(soldier);
        }
        
        map = new int[N][N];
        for (int i = 0; i < N; i++) {
            tokens = br.readLine().split(" ");
            for (int j = 0; j < N; j++) {
                map[i][j] = Integer.parseInt(tokens[j]);
            }
        }
    }
    
    static int calcDistance(Point point1, Point point2) {
        return Math.abs(point1.x - point2.x) + Math.abs(point1.y - point2.y);
    }
    
    // 내부 클래스 정의
    static class Soldier {
        int x, y;
        Status status;
        public Soldier(int x, int y, Status status) {
            this.x = x;
            this.y = y;
            this.status = status;
        }
        @Override
        public String toString() {
            return "Soldier{" +
                    "x=" + x +
                    ", y=" + y +
                    ", status=" + status +
                    '}';
        }
    }
    
    static enum Status {
        ALIVE, STONE
    }
    
    static class Point {
        int x, y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
    
    static class BFSNode {
        int x, y;
        BFSNode parent;
        public BFSNode(int x, int y, BFSNode parent) {
            this.x = x;
            this.y = y;
            this.parent = parent;
        }
    }
    
    static class Simulation {
        int stoneSoldiers;
        int dir;
        int[][] sights;
        public Simulation(int stoneSoldiers, int dir, int[][] sights) {
            this.stoneSoldiers = stoneSoldiers;
            this.dir = dir;
            this.sights = sights;
        }
		@Override
		public String toString() {
			return "Simulation [stoneSoldiers=" + stoneSoldiers + ", dir=" + dir + ", sights=" + Arrays.toString(sights)
					+ "]";
		}
    }
    
    static class Sight {
        Point left;
        Point right;
        public Sight(Point left, Point right) {
            this.left = left;
            this.right = right;
        }
    }
    
    static Sight getSights(int d) {
        Point p1 = new Point(-1, -1); // 좌상단
        Point p2 = new Point(-1, 1);  // 우상단
        Point p3 = new Point(1, -1);  // 좌하단
        Point p4 = new Point(1, 1);   // 우하단
        if (d == 0) {
            return new Sight(p1, p2);
        } else if (d == 1) {
            return new Sight(p3, p4);
        } else if (d == 2) {
            return new Sight(p1, p3);
        } else if (d == 3) {
            return new Sight(p2, p4);
        }
        return null;
    }
}
