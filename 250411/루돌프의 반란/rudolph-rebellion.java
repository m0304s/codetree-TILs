import java.io.*;
import java.util.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
	
	/**
	 * 1번~P번 P명의 산타클로스 존재
	 * N*N 크기의 격자
	 * 좌상단(1,1) -> 격자 N+1 * N+1 크기
	 * M개의 턴으로 게임이 진행
	 * 매 턴마다 루돌프와 산타들이 한 번씩 움직임
	 * 
	 * 루돌프가 한 번 움직인 뒤, 1번~P번 산타까지 순서대로 움직임
	 * 기절 OR 격자 밖인 산타는 움직일 수 없음
	 * 
	 * 거리 측정 방식 : (r1 - r2)^2 + (c1 - c2)^2
	 * 
	 * 루돌프의 움직임
	 * 게임에서 탈락하지 않은 산타 중 가장 가까운 산타를 향해 1칸 돌진
	 * 가장 가까운 산타
	 * : 거리가 가장 작음 -> r이 클 수록 -> c가 클수록
	 * 8방향으로 돌진 가능
	 * 인접한 대각선 방향으로 전진하는 것도 1칸 전진하는 것으로 생각
	 * 
	 * 산타의 움직임
	 * 1번부터 P번까지 순서대로 움직임
	 * 기절하거나 탈락한 산타는 움직일 수 없음
	 * 루돌프에게 가장 가까워지는 방향으로 1칸 이동
	 * 다른 산타가 있는 칸이나, 게임판 밖으로는 움직일 수 없음
	 * 움직일 수 있는 칸이 없다면, 움직이지 않음
	 * 움직일 수 있는 칸이 있더라도, 루돌프와 가까워지는 방법이 아니면 움직이지 않음
	 * 상하좌우 4방향 중 한칸으로 이동 -> 상우하좌 우선순위
	 * 
	 * 
	 * 충돌
	 * 산타와 루돌프가 같은 칸에 있는 경우
	 * 루돌프가 움직여서 충돌이 일어난 경우, 산타는 C만큼의 점수를 얻음, 산타는 루돌프가 이동해온 방향으로 C칸 밀려남
	 * 산타가 움직여서 충돌이 일어난 경우, 산타는 D만큼의 점수를 얻음, 산타는 자신이 이동해온 반대 방향으로 D칸 밀려남
	 * 포물선 모양을 그리며 밀려나기 때문에, 이동하는 도중에는 충돌이 일어나지 않음
	 * 밀려난 위치가 게임판 밖이라면 산타는 게임에서 탈락
	 * 밀려난 칸에 다른 산타가 있는 경우, 상호작용 발생
	 * 
	 * 상호작용
	 * 충돌 후 착지하는 칸에 다른 산타가 있으면, 그 산타는 1칸 해당 방향으로 밀려남
	 * 연쇄적으로 1칸씩 밀려나는 것도 가능
	 * 게임판 밖으로 밀려난 산타는 게임 탈락
	 * 
	 * 기절
	 * 루돌프와 충돌 후 기절, 현재가 K턴이면, (K+1)번째 턴까지 기절
	 * (K+2)번째 턴부터 정상상태로 변함
	 * 기절상태인 산타는 움직일 수 없음, 밀려날 수는 있음
	 * 
	 * 게임종료
	 * M번의 턴 모두 진행
	 * P명의 산타가 모두 게임에서 탈락하게 되는 경우, 그 즉시 종료
	 * 
	 * 출력
	 * 각 산타가 얻은 최종 점수
	 * 출력
	 */
	
	static int N,M,P,C,D;
	static Point rudolph;
	static ArrayList<Santa> santaList;
	
	static int outCnt = 0;
	public static void main(String[] args) throws IOException{
		init();
		Collections.sort(santaList, (o1,o2) -> (o1.num - o2.num));
		solution();
		bw.flush();
		bw.close();
		br.close();
	}
	
	static void solution() throws IOException{
		for(int turn=0;turn<M;turn++) {
			moveRudolph(turn);	//루돌프의 움직임
			moveSanta(turn);	//산타의 움직임
			getPointToLiveSanta();	//살아남은 산타 1포인트 증가
		}
		
		for(int i=0;i<santaList.size();i++) {
			Santa santa = santaList.get(i);
			bw.write(santa.point + " ");
		}bw.write("\n");
	}
	
//	private static boolean isAllOut() {
//		for(int i=0;i<santaList.size();i++) {
//			if(santaList.get(i).status == Status.ALIVE || santaList.get(i).status == Status.STUNNED) return false;
//		}
//		return true;
//	}

	private static void getPointToLiveSanta() {
		for(int i=0;i<santaList.size();i++) {
			Santa santa = santaList.get(i);
			if(santa.status == Status.OUT) continue;
			
			santa.point++;
		}
	}

	/**
	 * 산타의 움직임
	 * 1번부터 P번까지 순서대로 움직임
	 * 기절하거나 탈락한 산타는 움직일 수 없음
	 * 루돌프에게 가장 가까워지는 방향으로 1칸 이동
	 * 다른 산타가 있는 칸이나, 게임판 밖으로는 움직일 수 없음
	 * 움직일 수 있는 칸이 없다면, 움직이지 않음
	 * 움직일 수 있는 칸이 있더라도, 루돌프와 가까워지는 방법이 아니면 움직이지 않음
	 * 상하좌우 4방향 중 한칸으로 이동 -> 상우하좌 우선순위
	 */
	private static void moveSanta(int turn) {
		int [] dx = {-1,0,1,0};
		int [] dy = {0,1,0,-1};
		
		for(int i=0;i<P;i++) {
			Santa santa = santaList.get(i);
			if(santa.status == Status.OUT) continue;
			if(santa.status == Status.STUNNED) {
				//기절 상태인 경우, 기절된 Turn의 +2 인 경우부터 이동 가능
				if(turn < santa.stunnedTurn + 2) continue;
				
				santa.status = Status.ALIVE;
			}
			
			int originalDistance = calcManhattenDistance(new Point(santa.x,santa.y), rudolph);
			ArrayList<int []> candidates = new ArrayList<>();
			
			for(int d=0;d<4;d++) {
				int nx = santa.x + dx[d];
				int ny = santa.y + dy[d];
				
				if(!inRange(nx, ny)) continue;
				int newDistance = calcManhattenDistance(new Point(nx,ny), rudolph);
				if(originalDistance <= newDistance) continue;
				Santa anotherSanta = isSanta(nx, ny);
				if(anotherSanta != null) continue;
				candidates.add(new int[] {d,newDistance});
			}
			
			if(candidates.isEmpty()) continue;	//이동 방향이 없는 경우
			
			//가장 가까워지는 순으로, 방향은 상우하좌 순으로
			Collections.sort(candidates, new Comparator<int []>() {
				public int compare(int [] o1, int [] o2) {
					if(o1[1] == o2[1]) {
						return o1[0] - o2[0];
					}else {
						return o1[1] - o2[1];
					}
				}
			});
			int bestDir = candidates.get(0)[0];
			
			santa.x += dx[bestDir];
			santa.y += dy[bestDir];
			
			//산타와 루돌프 충돌
			if(santa.x == rudolph.x && santa.y == rudolph.y) {
				collideSanta(rudolph, santa, turn, D, bestDir);
			}
		}
	}

	/**
	 * 루돌프의 움직임
	 * 게임에서 탈락하지 않은 산타 중 가장 가까운 산타를 향해 1칸 돌진
	 * 가장 가까운 산타
	 * : 거리가 가장 작음 -> r이 클 수록 -> c가 클수록
	 * 8방향으로 돌진 가능
	 * 인접한 대각선 방향으로 전진하는 것도 1칸 전진하는 것으로 생각
	 */
	static void moveRudolph(int turn) {
		int [] dx = {-1,-1,0,1,1,1,0,-1};
		int [] dy = {0,1,1,1,0,-1,-1,-1};
		
		NearestSantaSimulation best = findNearestSanta();
		Santa targetSanta = best.santa;	//목표로 하는 산타
		int dir = best.dir;	//목표로 하는 산타로 이동하기 위한 방향값
		
		rudolph.x += dx[dir];
		rudolph.y += dy[dir];
		
		//루돌프가 이동한 위치에 산타가 있을 경우, 충돌 및 연쇄반응 처리
		Santa santa = isSanta(rudolph.x, rudolph.y);
		if(santa != null) {
			collideRudolph(rudolph, santa,turn, C, dir);
		}
	}
	
	/**
	 * 루돌프가 산타와 충돌
	 * @param rudolph
	 * @param santa
	 * @param turn
	 * @param score
	 * @param dir
	 */
	private static void collideRudolph(Point rudolph, Santa santa, int turn, int score, int dir) {
		int [] dx = {-1,-1,0,1,1,1,0,-1};
		int [] dy = {0,1,1,1,0,-1,-1,-1};
		
		int nx = santa.x + score * dx[dir];
		int ny = santa.y + score * dy[dir];
		
		santa.point += score;
		santa.stunnedTurn = turn;
		santa.status = Status.STUNNED;
		
		if(!inRange(nx, ny)) {
			santa.status = Status.OUT;
			return;
		}
		
		Santa isAnotherSanta = isSanta(nx, ny);
		if(isAnotherSanta != null) {
			//위치에 다른 산타가 있을 경우
			slide(santa,isAnotherSanta,dir);
		}else {			
			santa.x = nx;
			santa.y = ny;
		}
	}
	
	/**
	 * 산타가 루돌프와 충돌
	 * @param rudolph
	 * @param santa
	 * @param turn
	 * @param d2
	 * @param bestDir
	 */
	private static void collideSanta(Point rudolph, Santa santa, int turn, int score, int dir) {
		int [] dx = {-1,0,1,0};
		int [] dy = {0,1,0,-1};
		
		//산타는 자신이 이동해온 반대방향으로 이동
		int reverseDir = (dir + 2) % 4;
		int nx = santa.x + score * dx[reverseDir];
		int ny = santa.y + score * dy[reverseDir];
		
		santa.point += score;
		santa.status = Status.STUNNED;
		santa.stunnedTurn = turn;
		
		if(!inRange(nx,ny)) {
			santa.status = Status.OUT;
			return;
		}
	
		Santa anotherSanta = isSanta(nx, ny);
		if(anotherSanta != null) {
			slideSanta(santa,anotherSanta,reverseDir);
		}else {
			santa.x = nx;
			santa.y = ny;			
		}
		
	}
	
	private static void slideSanta(Santa santa, Santa anotherSanta, int dir) {
		// TODO Auto-generated method stub
		int [] dx = {-1,0,1,0};
		int [] dy = {0,1,0,-1};
		santa.x = anotherSanta.x;
		santa.y = anotherSanta.y;
		
		int nx = anotherSanta.x + dx[dir];
		int ny = anotherSanta.y + dy[dir];
		
		if(!inRange(nx, ny)) {
			anotherSanta.status = Status.OUT;
			return;
		}
		Santa anothorSanta = isSanta(nx, ny);
		if(anothorSanta == null) {
			anotherSanta.x = nx;
			anotherSanta.y = ny;
		}else {
			slide(anotherSanta, anothorSanta,dir);
		}
	}

	/**
	 * @param santa : 미는 산타
	 * @param targetSanta : 밀려나는 산타
	 * @param dir : 미는 방향
	 */
	static void slide(Santa santa,Santa targetSanta, int dir) {
		int [] dx = {-1,-1,0,1,1,1,0,-1};
		int [] dy = {0,1,1,1,0,-1,-1,-1};
		
		santa.x = targetSanta.x;
		santa.y = targetSanta.y;
		
		int nx = targetSanta.x + dx[dir];
		int ny = targetSanta.y + dy[dir];
		
		if(!inRange(nx, ny)) {
			targetSanta.status = Status.OUT;
			return;
		}
		Santa anothorSanta = isSanta(nx, ny);
		if(anothorSanta == null) {
			targetSanta.x = nx;
			targetSanta.y = ny;
		}else {
			slide(targetSanta, anothorSanta,dir);
		}
	}

	private static Santa isSanta(int x, int y) {
		for(int i=0;i<santaList.size();i++) {
			Santa santa = santaList.get(i);
			if(santa.x == x && santa.y == y) {
				return santa;
			}
		}
		return null;
	}

	/**
	 * 산타와 루돌프의 거리를 계산
	 * @return 루돌프가 1칸 돌진했을때, 가장 가까워지는 산타 반환
	 */
	private static NearestSantaSimulation findNearestSanta() {
	    int[] dx = {-1, -1, 0, 1, 1, 1, 0, -1};
	    int[] dy = {0, 1, 1, 1, 0, -1, -1, -1};

	    ArrayList<NearestSantaSimulation> candidates = new ArrayList<>();
	    for (Santa santa : santaList) {
	        if (santa.status == Status.OUT) continue; // 탈락한 산타는 제외
	        int distance = calcManhattenDistance(rudolph, new Point(santa.x, santa.y));
	        // 임시로 dir은 0으로 지정 (나중에 계산할 예정)
	        candidates.add(new NearestSantaSimulation(santa, distance, 0));
	    }

	    // 후보가 하나도 없으면 null 반환
	    if (candidates.isEmpty()) return null;

	    // 거리 기준 오름차순, 거리가 같다면 행(r)이 큰 순, 그 다음 열(c)이 큰 순으로 정렬
	    Collections.sort(candidates, new Comparator<NearestSantaSimulation>() {
	        public int compare(NearestSantaSimulation o1, NearestSantaSimulation o2) {
	            if (o1.distance == o2.distance) {
	                if(o1.santa.x == o2.santa.x) {
	                    return o2.santa.y - o1.santa.y;
	                } else {
	                    return o2.santa.x - o1.santa.x;
	                }
	            } else {
	                return o1.distance - o2.distance;
	            }
	        }
	    });

	    NearestSantaSimulation best = candidates.get(0);
	    Santa targetSanta = best.santa;
	    
	    int moveX = 0, moveY = 0;
	    if (targetSanta.x > rudolph.x) moveX = 1;
	    else if (targetSanta.x < rudolph.x) moveX = -1;
	    else moveX = 0;

	    if (targetSanta.y > rudolph.y) moveY = 1;
	    else if (targetSanta.y < rudolph.y) moveY = -1;
	    else moveY = 0;

	    int dir = -1;
	    for (int d = 0; d < 8; d++) {
	        if (dx[d] == moveX && dy[d] == moveY) {
	            dir = d;
	            break;
	        }
	    }
	    best.dir = dir;
	    return best;
	}

	
	
	private static int calcManhattenDistance(Point point1, Point point2) {
		return (int)Math.pow((point1.x - point2.x),2) + (int)Math.pow((point1.y - point2.y), 2);
	}

	static boolean inRange(int x,int y) {
		return x >= 0 && x < N && y >= 0 && y < N;
	}

	static void init() throws IOException{
		String [] tokens = br.readLine().split(" ");
		N = Integer.parseInt(tokens[0]);
		M = Integer.parseInt(tokens[1]);
		P = Integer.parseInt(tokens[2]);
		C = Integer.parseInt(tokens[3]);
		D = Integer.parseInt(tokens[4]);
		
		tokens = br.readLine().split(" ");
		int x = Integer.parseInt(tokens[0])-1;
		int y = Integer.parseInt(tokens[1])-1;
		
		rudolph = new Point(x, y);
		
		santaList = new ArrayList<>();
		for(int i=1;i<=P;i++) {
			tokens = br.readLine().split(" ");
			int num = Integer.parseInt(tokens[0]);
			int r = Integer.parseInt(tokens[1])-1;
			int c = Integer.parseInt(tokens[2])-1;
			
			Santa santa = new Santa(num, r, c, 0, -1, Status.ALIVE);
			santaList.add(santa);
		}
	}
	
	static class Point{
		int x,y;
		public Point(int x,int y) {
			this.x = x;
			this.y = y;
		}
		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + "]";
		}
	}
	
	static class Santa{
		int num;
		int x,y;
		int point;
		int stunnedTurn;
		Status status;
		public Santa(int num,int x, int y, int point, int stunnedTurn,Status status) {
			super();
			this.num = num;
			this.x = x;
			this.y = y;
			this.point = point;
			this.stunnedTurn = stunnedTurn;
			this.status = status;
		}
		@Override
		public String toString() {
			return "Santa [num=" + num + ", x=" + x + ", y=" + y + ", point=" + point + ", stunnedTurn=" + stunnedTurn
					+ ", status=" + status + "]";
		}
	}
	
	static class NearestSantaSimulation{
		Santa santa;
		int distance;
		int dir;
		public NearestSantaSimulation(Santa santa, int distance, int dir) {
			super();
			this.santa = santa;
			this.dir = dir;
			this.distance = distance;
		}
		
	}
	
	static enum Status{
		ALIVE, STUNNED, OUT;
	}

}
