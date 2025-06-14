import java.io.*;
import java.util.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

	/**
	 * N * M 격자
	 * 각 포탑에는 공격력이 존재, 상황에 따라 공격력이 줄어들거나 늘어날 수 있음
	 * 공격력이 0 이하가 되면, 해당 포탑은 부서지며 더 이상의 공격을 할 수 없음
	 * 
	 * 하나의 턴은 4가지 액션을 순서대로 진행하며, K번 반복
	 * 
	 * 1. 공격자 선정
	 *	부서지지 않은 포탑 중 가장 약한 포탑이 공격자로 선정
	 *	공격자로 선정된 포탑은 핸디캡이 적용되어 N+M만큼의 공격력이 증가
	 *	가장 약한 포탑 선정 기준
	 *		1. 공격력이 가장 낮은 포탑
	 *		2. 가장 최근에 공격한 포탑
	 *		3. 행과 열의 합이 가장 큰 포탑
	 *		4. 열 값이 가장 큰 포탑
	 *
	 * 2. 공격자의 공격
	 * 	공격자는 자신을 제외한 가장 강한 포탑을 공격
	 * 	가장 강한 포탑 선정 기준
	 * 		1. 공격력이 가장 높은 포탑
	 * 		2. 공격한지 가장 오래된 포탑
	 * 		3. 행과 열의 합이 가장 작은 포탑
	 * 		4. 열 값이 가장 작은 포탑
	 * 	공격할 때는 레이저 공격을 먼저 시도, 그게 안되면 포탄 공격을 시도
	 * 	1. 레이저 공격
	 * 		상하좌우 4개의 방향으로 움직일 수 있음
	 * 		부서진 포탑이 있는 위치는 지날 수 없음
	 * 		가장자리에서 막힌 방향으로 진행하고자 한다면, 반대편으로 나옴
	 * 		레이저 공격은 공격자의 위치에서 공격 대상 포탑까지의 최단 경로로 공격, 만약 그런 경로가 존재하지 않는다면 포탑 공격을 시도
	 * 		최단경로가 정해지면, 공격 대상에는 공격자의 공격력 만큼의 피해, 공격 대상을제외한 레이저 경로에 있는 포탑은 공격자 공격력의 절반 만큼의 공격을 받음
	 * 
	 * 	2. 포탑 공격
	 * 		공격 대상에 포탄을 던짐
	 * 		공격 대상은 공격자 공격력 만큼의 피해
	 * 		주위 8개의 방향에 있는 포탑은 공격자 공격력의 절반만큼의 피해
	 * 		공격자는 피해를 입지 않음
	 * 		만약 가장자리에 포탄이 떨어진다면, 포탄의 추가피해가 반대편 격자까지 영향
	 * 	
	 * 3. 포탑의 부서짐
	 * 		공격력이 0 이하기 된 포탑은 부서짐
	 * 
	 * 4. 포탑 정비
	 * 	부서지지 않은 포탑 중 공격과 무관했던 포탑은 공격력이 1씩 올라감
	 * 		공격과 무관함 -> 공격자도 아니고, 공격에 피해를 입은 포탑도 아니라는 뜻
	 * 
	 * 출력 : K번의 턴이 종료된 후 남아있는 포탑 중 가장 강한 포탑의 공격력을 출력
	 */
	
	static int N,M,K;
	static Tower[][] map;
	static List<Tower> towerList;
	
	public static void main(String [] args) throws IOException{
		init();
		solution();
	}
	
	static void solution() throws IOException{
		for(int k=1;k<=K;k++) {
			if(towerList.size() <= 1) break;
			
			Tower attacker = findAttacker();
			Tower target = findTarget();
			
			attacker.power += (N+M);
			attacker.lastAttacked = k;
			attacker.related = true;
			
			List<Node> route = findLazerRoute(attacker,target);
			
			if(route != null) {
				//레이저 공격
				lazerAttack(route, attacker.power);
			}else {
				bombAttack(attacker, target, attacker.power);
			}
			destroy();
			repair();
		}
		
		printResult();
	}
	
	

	static void printResult() throws IOException{
		Collections.sort(towerList);
		
		bw.write(towerList.get(towerList.size()-1).power+"\n");
		bw.flush();
		bw.close();
		br.close();
	}

	private static void bombAttack(Tower attacker, Tower target, int power) {
		int [] dx8 = {-1,-1,0,1,1,1,0,-1};
		int [] dy8 = {0,1,1,1,0,-1,-1,-1};
		
		for(int d=0;d<8;d++) {
			int nx = target.x + dx8[d];
			int ny = target.y + dy8[d];
			
			if(nx < 0) nx += N;
			else if(nx >= N) nx -= N;
			
			if(ny < 0) ny += M;
			else if(ny >= M) ny -= M;
			
			if(nx == attacker.x && ny == attacker.y) continue;
			if(map[nx][ny] == null) continue;
			
			map[nx][ny].power -= power/2;
			map[nx][ny].related = true;
		}
		
		map[target.x][target.y].power -= power;
		map[target.x][target.y].related = true;
	}

	private static void lazerAttack(List<Node> route, int power) {
		for(int i=1;i<route.size();i++) {
			Node point = route.get(i);
			Tower tower = map[point.x][point.y];
			
			tower.related = true;
			tower.power -= (power) / 2;
		}
		
		Node point = route.get(0);
		Tower tower = map[point.x][point.y];
		
		tower.related = true;
		tower.power -= power;
	}

	private static void repair() {
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

	private static void destroy() {
		for(int i=0;i<N;i++) {
			for(int j=0;j<M;j++) {
				if(map[i][j] == null) continue;
				
				if(map[i][j].power <= 0) {
					Tower tower = map[i][j];
					
					map[i][j] = null;
					towerList.remove(tower);
				}
			}
		}
	}

	private static List<Node> findLazerRoute(Tower attacker, Tower target) {
		Queue<BFSNode> queue = new ArrayDeque<>();
		boolean [][] visited = new boolean[N][M];
		List<Node> route = new ArrayList<>();
		
		// 우 하 좌 상
		int [] dx = {0,1,0,-1};
		int [] dy = {1,0,-1,0};
		
		queue.add(new BFSNode(attacker.x, attacker.y,null));
		visited[attacker.x][attacker.y] = true;
		
		while(!queue.isEmpty()) {
			BFSNode curNode = queue.poll();
			
			if(curNode.x == target.x && curNode.y == target.y) {
				while(curNode.parent != null) {
					route.add(new Node(curNode.x, curNode.y));
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
				if(map[nx][ny] == null) continue;
				
				queue.add(new BFSNode(nx,ny,curNode));
				visited[nx][ny] = true;
			}
		}
		return null;
	}

	private static Tower findTarget() {
		Collections.sort(towerList);
		return towerList.get(towerList.size()-1);
	}

	private static Tower findAttacker() {
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
		for(int i=0;i<N;i++) {
			tokens = br.readLine().split(" ");
			for(int j=0;j<M;j++) {
				int power = Integer.parseInt(tokens[j]);
				
				if(power > 0) {
					Tower tower = new Tower(i,j,power,0,false);
					
					map[i][j] = tower;
					towerList.add(tower);
				}
			}
		}
	}
	
	static class Node{
		int x,y;
		public Node(int x,int y) {
			this.x = x;
			this.y = y;
		}
		@Override
		public String toString() {
			return "Node [x=" + x + ", y=" + y + "]";
		}
	}
	
	static class BFSNode{
		int x,y;
		BFSNode parent;
		@Override
		public String toString() {
			return "BFSNode [x=" + x + ", y=" + y + ", parent=" + parent + "]";
		}
		public BFSNode(int x, int y, BFSNode parent) {
			super();
			this.x = x;
			this.y = y;
			this.parent = parent;
		}		
	}
	static class Tower implements Comparable<Tower>{
		int x,y;
		int power;
		int lastAttacked;
		boolean related;
		public Tower(int x, int y, int power, int lastAttacked, boolean related) {
			super();
			this.x = x;
			this.y = y;
			this.power = power;
			this.lastAttacked = lastAttacked;
			this.related = related;
		}
		
		
		@Override
		public String toString() {
			return "Tower [x=" + x + ", y=" + y + ", power=" + power + ", lastAttacked=" + lastAttacked + ", related="
					+ related + "]";
		}


		public int compareTo(Tower o) {
			if(this.power == o.power) {
				if(this.lastAttacked == o.lastAttacked) {
					if((this.x + this.y) == (o.x + o.y)) {
						return o.y - this.y;
					}else return (o.x + o.y) - (this.x + this.y); 
				}else return o.lastAttacked - this.lastAttacked;
			}else return this.power - o.power;
		}
	}
}
