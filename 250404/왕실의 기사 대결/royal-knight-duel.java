import java.io.*;
import java.util.*;

public class 왕실의기사대결 {
	//상 우 하 좌
	static final int [] dx = {-1,0,1,0};
	static final int [] dy = {0,1,0,-1};
	static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
	static final int BLANK = 0, TRAP = 1, WALL = 2;

	static int L,N,Q;
	static int [][] map;
	static int [][] trap;
	static HashMap<Integer, Soldier> soldierMap;
	
	public static void main(String[] args) throws IOException{
		String [] tokens = br.readLine().split(" ");
		L = Integer.parseInt(tokens[0]);	//체스판의 크기
		N = Integer.parseInt(tokens[1]);	//기사의 수
		Q = Integer.parseInt(tokens[2]);	//명령의 수
		
		createMap();
		createSoldier();
		
		simulation();
		bw.write(calcTotalDamage()+"\n");
		bw.flush();
		bw.close();
		br.close();
	}
	
	static int calcTotalDamage() {
		int totalDamage = 0;
		for(Integer key : soldierMap.keySet()) {
			Soldier soldier = soldierMap.get(key);
			totalDamage+=(soldier.initialHealth - soldier.health);
		}
		
		return totalDamage;
	}
	
	static void simulation() throws IOException{
		for(int q = 1;q<=Q;q++) {
			// 명령 입력
			String [] tokens = br.readLine().split(" ");
			int soldierNum = Integer.parseInt(tokens[0]);
			int dir = Integer.parseInt(tokens[1]);
			
			if(!soldierMap.containsKey(soldierNum)) continue;
			if(checkCanMove(soldierNum,dir)) {
				//병사 이동
				move(soldierNum,dir,false);
			}else {
				//이동 불가능한 경우
				continue;
			}
		
		}
	}
	
	
	private static void move(int soldierNum, int dir, boolean isPushed) {
		Soldier soldier = soldierMap.get(soldierNum);
		List<Node> points = soldier.points;	//병사가 차지하고 있는 좌표 목록
		
		for(Node point : points) {
			int nx = point.x + dx[dir];
			int ny = point.y + dy[dir];
			
			//기존에 다른 병사가 위치하고 있는 경우
			if(map[nx][ny] != soldierNum && map[nx][ny] != 0) 
				move(map[nx][ny],dir,true);
			
		}
		//기존 위치를 맵에서 삭제
		for(Node node : soldier.points) {
			map[node.x][node.y] = BLANK; 
		}

		List<Node> newPoints = new ArrayList<>();
		for(Node point : soldier.points) {
			int nx = point.x + dx[dir];
			int ny = point.y + dy[dir];
			
			newPoints.add(new Node(nx,ny));
		}
		soldier.points = newPoints;
		//지도 업데이트
		for(Node node : soldier.points) {
			map[node.x][node.y] = soldierNum; 
		}
		
		/**
		 * 데미지 계산
		 * 밀친 병사는 피해를 입지 않음, 밀어내진 병사만 피해를 입음
		 */
		if(isPushed) {
			int damage = 0;
			for(Node point : newPoints) {
				if(trap[point.x][point.y] == TRAP) damage++;
			}
			
			//체력이 사라졌을 경우
			if(damage >= soldier.health) {
				soldierMap.remove(soldierNum);
				cleanMap(soldierNum);
			}else {
				soldier.health -= damage;
			}
		}
	}

	private static void cleanMap(int soldierNum) {
		for(int i=1;i<=L;i++) {
			for(int j=1;j<=L;j++) {
				if(map[i][j] == soldierNum) map[i][j] = BLANK;
			}
		}
	}

	private static boolean checkCanMove(int soldierNum, int dir) {
		//현재 병사의 좌표들
		Soldier soldier = soldierMap.get(soldierNum);
		
		List<Node> soldierPoints = soldier.points;
		for(Node point : soldierPoints) {
			int nx = point.x + dx[dir];
			int ny = point.y + dy[dir];
			
			if(!inRange(nx,ny)) return false;
			if(trap[nx][ny] == WALL) return false;
			
			int occupant = map[nx][ny];
			if(occupant == BLANK || occupant == soldierNum) continue;
			if(!checkCanMove(occupant, dir)) return false;
		}
		return true;
	}
	
	static boolean inRange(int x,int y) {
		return x >= 1 && x <= L && y >= 1 && y <= L;
	}

	private static void createSoldier() throws IOException{
		soldierMap = new HashMap<>();
		for(int i=1;i<=N;i++) {
			String [] tokens = br.readLine().split(" ");
			int r = Integer.parseInt(tokens[0]);
			int c = Integer.parseInt(tokens[1]);
			int h = Integer.parseInt(tokens[2]);
			int w = Integer.parseInt(tokens[3]);
			int k = Integer.parseInt(tokens[4]);
			
			Soldier soldier = new Soldier(r, c, h, w, k);
			for(int x=0;x<h;x++) {
				for(int y=0;y<w;y++) {
					map[r+x][c+y] = i;
				}
			}
			soldierMap.put(i, soldier);
		}
	}

	private static void createMap() throws IOException{
		map = new int[L+1][L+1];
		trap = new int [L+1][L+1];
		for(int i=1;i<=L;i++) {
			String [] tokens = br.readLine().split(" ");
			for(int j=1;j<=L;j++) {
				int num = Integer.parseInt(tokens[j-1]);
				if(num == TRAP) trap[i][j] = TRAP;
				else if(num == WALL) trap[i][j] = WALL;
			}
		}
	}
	
	static void debug() {
		for(int i=1;i<=L;i++) {
			for(int j=1;j<=L;j++) {
				System.out.print(map[i][j] + " ");
			}System.out.println();
		}
		
		System.out.println(soldierMap);
	}

	/**
	 * LxL 크기의 체스판 ,좌표는 (1,1)부터 시작 , 빈칸(0) 함정(1) 벽(2)
	 * 기사
	 * 초기위치 : (r,c) 방패를 들고 있음 
	 * 방패는 (r,c) 기준 h*w 크기의 직사각형 형태
	 * 체력 : k
	 */
	static class Soldier{
		List<Node> points;
		int initialHealth;
		int r,c;
		int health;
		

		
		@Override
		public String toString() {
			return "Soldier [points=" + points + ", initialHealth=" + initialHealth + ", r=" + r + ", c=" + c
					+ ", health=" + health + "]";
		}
		public Soldier(int r,int c,int h,int w, int health) {
			this.points = new ArrayList<>();
			createSoldierPoints(r,c,h,w);
			this.health = health;
			this.r = r;
			this.c = c;
			this.initialHealth = health;
		}
		private void createSoldierPoints(int r, int c, int h, int w) {
			for(int i=0;i<h;i++) {
				for(int j=0;j<w;j++) {
					points.add(new Node(r+i,c+j));
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
	
	
}
