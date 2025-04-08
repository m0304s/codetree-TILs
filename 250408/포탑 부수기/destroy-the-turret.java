import java.io.*;
import java.util.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
	
	/**
	 * NxM 크기의 포탑 격자
	 * K번 턴 만큼, 4가지 액션(공격자 선정, 공격자 공격, 포탑 부서짐, 포탑 정비) 진행
	 * 부서지지 않은 포탑이 1개가 된다면 그 즉시 중지
	 * 
	 * 1. 공격자 선정
	 * 	부서지지 않은 포탑 중 가장 약한 포탑이 공격자로 선정됨
	 * 	선정된 포탑은 N+M만큼의 공격력 증가
	 * 	가장 약산 포탑 선정 기준
	 * 		1. 공격력이 가장 낮은 포탑
	 * 		2. 가장 최근에 공격한 포탑
	 * 		3. 행과 열의 합이 가장 큰 포탑
	 * 		4. 열 값이 가장 큰 포탑
	 * 
	 * 2. 공격자 공격
	 * 	자신을 제외한 가장 강한 포탑 공격
	 * 	가장 강한 포탑 선정 기준
	 * 		1. 공겨력이 가장 높은 포탑
	 * 		2. 공격한지 가장 오래된 포탑
	 * 		3. 행과 열의 합이 가장 작은 포탑
	 * 		4. 열 값이 가장 작은 포탑
	 * 
	 * 	공격의 종류
	 * 	1. 레이저 공격
	 * 		상하좌우의 4개의 방향으로 움직일 수 있음
	 * 		부서진 포탑이 있는 위치는 지날 수 없음
	 * 		가장자리에서 막힌 방향으로 진행한다면, 반대편으로 진행 Ex) (2,4) -> (2,1)
	 * 		레이저 공격은 공격자의 위치에서 공격 대상 포탑까지 최단 경로로 공격 -> BFS
	 * 		경로 길이가 똑같은 최단 경로가 2개 이상일 경우, 우/하/좌/상 우선순위로 결정
	 * 		피해를 입은 포탑은 공격자의 공격력만큼 공격력이 줄어듦
	 * 		공격 대상을 제외한 레이저 경로에 존재하는 포탑은 공격자 공격력의 절반 만큼의 공격력 감소
	 * 
	 * 	2. 포탄 공격
	 * 		레이저 공격이 실패하면 포탄 공격을 진행
	 * 		공격 대상은 공격자의 공격력 만큼의 피해를 받음
	 * 		주위 8개의 방향에 있는 포탑도 피해 입음(공격자의 공격력의 절반 만큼, 자신을 제외)
	 * 	
	 * 	3. 포탑 부서짐
	 * 		공격력이 0 이하가 된 포탑은 부서짐
	 * 
	 * 	4. 포탑 정비
	 * 		공격하거나, 공격을 받지 않은 포탑은 공격력이 1 증가
	 * 
	 * 포탑
	 * 	1. 공격력
	 * 	2. 최근에 공격한 시점
	 * 	3. 관여 여부
	 */
	
	static int N,M,K;
	static Tower[][] map;
	static ArrayList<Tower> towerList;
	
	public static void main(String[] args) throws IOException{
		init();
		simulation();
	}

	static void simulation() {
		for(int k=1;k<=K;k++) {
			if(towerList.size() == 1) break;
			Tower attackTower = findAttackTower();	//가장 약한 포탑
			Tower defenseTower = findDefenseTower();	//가장 강한 포탑

			attackTower.time = k;
			attackTower.related = true;
			attackTower.power += N + M;	//최종 공력력
			ArrayList<Node> route = bfs(attackTower, defenseTower);
			if(route == null) {
				//폭탄 공격
				bombAttack(attackTower,defenseTower,attackTower.power);
			}else {
				//레이저 공격
				laserAttack(route,attackTower.power);
			}
			
			destroyTower();
			addPower();	//공격과 무관한 포탑의 공격력 +1
		}
		
		printResult();	//남아있는 포탑 중 가장 강한 포탑의 공격력 출력
	}
	
	static void printResult() {
		Collections.sort(towerList);
		
		System.out.println(towerList.get(towerList.size()-1).power);
	}
	
	private static void addPower() {
		for(int i=0;i<N;i++) {
			for(int j=0;j<M;j++) {
				if(map[i][j] == null) continue;
				
				if(map[i][j].related) {
					map[i][j].related = false;
				}else {
					map[i][j].power++;
				}
			}
		}
	}

	private static void destroyTower() {
		for(int i=0;i<N;i++) {
			for(int j=0;j<M;j++) {
				if(map[i][j] == null) continue;
				
				Tower tower = map[i][j];
				
				if(tower.power <= 0) {
					map[i][j] = null;
					towerList.remove(tower);
				}
			}
		}
	}

	private static void bombAttack(Tower attackTower, Tower defenseTower, int attackPower) {
		int [] dx8 = {-1,-1,0,1,1,1,0,-1};
		int [] dy8 = {0,1,1,1,0,-1,-1,-1};
		
		//간접 피해를 받는 타워
		for(int d=0;d<8;d++) {
			int nx = defenseTower.r + dx8[d];
			int ny = defenseTower.c + dy8[d];
			
			if(nx < 0) nx += N;
			else if(nx >= N) nx -= N;
			
			if(ny < 0) ny += M;
			else if(ny >= M) ny -= M;
			
			
			if(nx == attackTower.r && ny == attackTower.c) continue;
			if(map[nx][ny] == null) continue;
			
			map[nx][ny].power -= attackPower/2;
			map[nx][ny].related = true;
		}
		
		//직접 피해를 입는 타워
		map[defenseTower.r][defenseTower.c].power -= attackPower;
		map[defenseTower.r][defenseTower.c].related = true;
	}

	private static void laserAttack(ArrayList<Node> route, int attackPower) {
		//간접 피해를 받는 타워
		for(int i=route.size() -1; i>=1;i--) {
			Node point = route.get(i);
			Tower tower = map[point.x][point.y];
			tower.power -= attackPower/2;
			tower.related = true;
		}
		
		//직접 피해를 받는 타워
		Node point = route.get(0);
		Tower tower = map[point.x][point.y];
		tower.power -= attackPower;
		tower.related = true;
	}

	/**
	 *	1. 레이저 공격
	 * 		상하좌우의 4개의 방향으로 움직일 수 있음
	 * 		부서진 포탑이 있는 위치는 지날 수 없음
	 * 		가장자리에서 막힌 방향으로 진행한다면, 반대편으로 진행 Ex) (2,4) -> (2,1)
	 * 		레이저 공격은 공격자의 위치에서 공격 대상 포탑까지 최단 경로로 공격 -> BFS
	 * 		경로 길이가 똑같은 최단 경로가 2개 이상일 경우, 우/하/좌/상 우선순위로 결정
	 * 		피해를 입은 포탑은 공격자의 공격력만큼 공격력이 줄어듦
	 * 		공격 대상을 제외한 레이저 경로에 존재하는 포탑은 공격자 공격력의 절반 만큼의 공격력 감소
	 */
	//defenseTower까지 갈 수 있는 최단 경로 계산
	private static ArrayList<Node> bfs(Tower attack, Tower defense) {
		int [] dx = {0,1,0,-1};
		int [] dy = {1,0,-1,0};
		ArrayList<Node> route = new ArrayList<>();
		Queue<BFSNode> queue = new ArrayDeque<>();
		boolean [][] visited= new boolean[N][M];
		visited[attack.r][attack.c] = true;
		queue.add(new BFSNode(attack.r, attack.c, null));
		
		while(!queue.isEmpty()) {
			BFSNode curNode = queue.poll();
			
			if(curNode.x == defense.r && curNode.y == defense.c) {
				//경로 역추적
				while(curNode.parent != null) {
					route.add(new Node(curNode.x,curNode.y));
					curNode = curNode.parent;
				}
				return route;
			}
			
			for(int d=0;d<4;d++) {
				int nx = curNode.x + dx[d];
				int ny = curNode.y + dy[d];
				
				if(nx < 0) {
					nx += N;
				}else if(nx >= N) {
					nx -= N;
				}
				
				if(ny < 0) {
					ny += M;
				}else if(ny >= M) {
					ny -= M;
				}
				
				if(visited[nx][ny]) continue;
				if(map[nx][ny] == null) continue;	//부서진 경우
				
				queue.add(new BFSNode(nx, ny, curNode));
				visited[nx][ny] = true;
			}
		}
		return null;
	}
	
	static class BFSNode{
		BFSNode parent;
		int x,y;
		public BFSNode(int x,int y,BFSNode parent) {
			this.x = x;
			this.y = y;
			this.parent = parent;
		}
	}

	private static Tower findDefenseTower() {
		return towerList.get(towerList.size()-1);
	}

	private static Tower findAttackTower() {
		Collections.sort(towerList);
		return towerList.get(0);
	}

	static void init() throws IOException{
		String [] tokens = br.readLine().split(" ");
		N = Integer.parseInt(tokens[0]);
		M = Integer.parseInt(tokens[1]);
		K = Integer.parseInt(tokens[2]);
		
		map = new Tower[N][M];
		towerList = new ArrayList<>();
		//타워 정보 입력
		for(int i=0;i<N;i++) {
			tokens = br.readLine().split(" ");
			for(int j=0;j<M;j++) {
				int power = Integer.parseInt(tokens[j]);
				if(power == 0) continue;
				Tower tower = new Tower(i,j,power,0,false);
				map[i][j] = tower;
				towerList.add(tower);
			}
		}
	}


	/**
	 *	포탑
	 * 	1. 공격력
	 * 	2. 최근에 공격한 시점
	 * 	3. 관여 여부
	 */
	static class Tower implements Comparable<Tower>{
		@Override
		public int compareTo(Tower o) {
			if(this.power == o.power) {
				if(this.time == o.time) {
					if((this.r + this.c) == (o.r + o.c)) {
						return o.c - this.c;
					}else {
						return (o.r + o.c) - (this.r + this.c); 
					}
				}else {
					return o.time - this.time;
				}
			}else {
				return this.power - o.power;
			}
		}
		int r,c;
		int power;
		int time;
		boolean related;
		public Tower(int r, int c, int power, int time, boolean related) {
			super();
			this.r = r;
			this.c = c;
			this.power = power;
			this.time = time;
			this.related = related;
		}
		@Override
		public String toString() {
			return "Tower [r=" + r + ", c=" + c + ", power=" + power + ", time=" + time + ", related=" + related + "]";
		}
	}
	
	static class Node{
		@Override
		public String toString() {
			return "Node [x=" + x + ", y=" + y + "]";
		}
		int x,y;
		public Node(int x,int y) {
			this.x = x;
			this.y = y;
		}
	}
}
