import java.io.*;
import java.util.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
	static int [] dx = {0,0,-1,1};
	static int [] dy = {-1,1,0,0};
	
	/**
	 * N * N 크기의 배양 용기
	 * 좌측 하단 (0,0)
	 * 우측 상단 (N,N)
	 * 
	 * Q번의 실험 진행
	 * 
	 * 1. 미생물 투입
	 * 	좌측 하단 좌표 (r1,c1), 우측 상단 좌표 (r2,c2)인 직사각형 영역에 미생물 투입
	 * 	영역 내 다른 미생물이 존재하면 새로 투입된 미생물이 영역 내의 미생물 잡아먹음
	 * 	즉, 영역 내에는 새로 투입된 미생물만 존재
	 * 
	 * 	기존에 있던 미생물 무리 A가 새로 투입된 미생물 무리 B에게 잡아먹혀 A무리가 2개로 나뉘어질 경우, A 무리 미생물은 폐기
	 * 
	 * 2. 배양 용기 이동
	 * 	기존 배양 용기에 있는 무리 중 가장 차지한 영역이 넓은 무리 선택
	 * 	크기가 같을 경우, 가장 먼저 투입된 미생물 선택
	 *  선택된 무리를 새 배양용기에 옮김
	 *  조건 : 
	 *  	기존 용기에서의 형태 유지
	 *  	미생물 무리가 차지한 영역이 배양 용기의 범위를 벗어나면 안됨
	 *  	다른 미생물 무리와 겹치면 안됨
	 *  최대한 x 좌표가 작은 위치로 미생물을 옮겨야 함
	 *  그런 위치가 둘 이상인 경우 최대한 y좌표가 작은 위치로 오도록 미생물을 옮김
	 *  
	 *	옮기는 과정에서 위치할 장소가 없는 미생물 무리는 폐기 처리
	 *
	 * 3. 실험 결과 기록
	 * 미생물 무리 중 상하좌우로 맞닿는 면이 있는 무리끼리는 인접한 무리라고 표현
	 * 모든 인접한 무리 쌍 확인
	 * (A,B) -> A 영역의 넓이 * B 영역의 넓이
	 * 
	 * 모든 쌍의 성과를 더한 값이 실험의 결과
	 */
	
	static int N;
	static int Q;
	static int [][] map;
	static List<Vacteria> vacterias;
	
	public static void main(String[] args) throws IOException{
		String [] tokens = br.readLine().split(" ");
		N = Integer.parseInt(tokens[0]);
		Q = Integer.parseInt(tokens[1]);
		map = new int[N][N];
		vacterias = new ArrayList<>();
		
		for(int q=1;q<=Q;q++) {
			addVacterias(q);
			moveVacterias();
			calculateScore();
		}
		bw.flush();
		bw.close();
		br.close();
	}
	
	/**
	 * 인접한 구역 계산
	 */
	private static void calculateScore() throws IOException {
	    int totalScore = 0;
	    for (int i = 0; i < vacterias.size(); i++) {
	        Vacteria A = vacterias.get(i);
	        for (int j = i + 1; j < vacterias.size(); j++) {
	            Vacteria B = vacterias.get(j);
	            // A와 B가 인접하면 A.size * B.size 더하기
	            if (areAdjacent(A.points, B.points)) {
	                totalScore += A.points.size() * B.points.size();
	            }
	        }
	    }
	    bw.write(totalScore + "\n");
	}

	/**
	 * A 군집의 어떤 셀이 B 군집의 셀과 상하좌우로 붙어있는지 확인
	 */
	private static boolean areAdjacent(List<Node> aPts, List<Node> bPts) {
	    // B 군집 셀을 빠르게 lookup 하기 위해 boolean 그리드로 표시
	    boolean[][] isB = new boolean[N][N];
	    for (Node p : bPts) {
	        isB[p.x][p.y] = true;
	    }

	    for (Node p : aPts) {
	        for (int d = 0; d < 4; d++) {
	            int nx = p.x + dx[d];
	            int ny = p.y + dy[d];
	            if (inRange(nx, ny) && isB[nx][ny]) {
	                return true;
	            }
	        }
	    }
	    return false;
	}



	/**
	 * 배양 용기 이동
	 */
	private static void moveVacterias() {
		int [][] newMap = new int[N][N];	//새 배양 용기
		Collections.sort(vacterias, new Comparator<Vacteria>() {
			public int compare(Vacteria o1, Vacteria o2) {
				if(o1.points.size() == o2.points.size()) {	//미생물 무리 크기가 동일할 경우, 먼저 투입된 미생물 우선
					return o1.inputTime - o2.inputTime;
				}else {
					return o2.points.size() - o1.points.size();
				}
			}
		});
		
		List<Vacteria> removeVacteria = new ArrayList<>();
		for(int index = 0; index<vacterias.size();index++) {
			Vacteria moveVacteria = vacterias.get(index);	//움직일 박테리아
			Node minPointValue = getMinPointValue(moveVacteria);
			
			List<Simulation> simulationResults = new ArrayList<>();
			
			for(int i=0;i<N;i++) {
				for(int j=0;j<N;j++) {
					//미생물이 들어갈 수 있는 좌표 목록 반환
					Simulation simulation = checkCanPosition(moveVacteria, minPointValue, i,j, newMap);
					
					//배치될 수 있는 장소가 없을 경우
					if(simulation == null) continue;
					
					simulationResults.add(simulation);
				}
			}
			
			if(simulationResults.isEmpty()) {
				removeVacteria.add(moveVacteria);	//배치하지 못하는 미생물 무리
				continue;
			}
			
			//배치될 수 있는 장소들 중 x좌표가 가장 작은 곳, 그런 장소가 여러 곳이라면 y좌표가 가장 작은 곳
			Collections.sort(simulationResults, new Comparator<Simulation>() {
				public int compare(Simulation s1, Simulation s2) {
					if(s1.minX == s2.minX) return s1.minY - s2.minY;
					else return s1.minX - s2.minX;
				}
			});
			
			Simulation selectedSimulation = simulationResults.get(0);
			moveVacteria.points = selectedSimulation.newPoints;
			for(Node node : moveVacteria.points) {
				newMap[node.x][node.y] = moveVacteria.inputTime; 
			}
		}
		
		vacterias.removeAll(removeVacteria);
		map = newMap;
	}

	private static Simulation checkCanPosition(Vacteria moveVacteria, Node minPointValue, int startX, int startY, int [][] newMap) {
		//새로운 좌표 목록 (상대좌표)
		List<Node> pointList = new ArrayList<>();
		
		int minX = minPointValue.x;	//새로 배치될 박테리아의 상대좌표 기준점
		int minY = minPointValue.y; //새로 배치될 박테리아의 상대좌표 기준점
		for(Node node : moveVacteria.points) {
			pointList.add(new Node(node.x - minX, node.y - minY));
		}
		
		List<Node> finalPointList = new ArrayList<>();
		int finalMinX = Integer.MAX_VALUE;
		int finalMinY = Integer.MAX_VALUE;
		
		for(int i=0;i<pointList.size();i++) {
			int realPointX = pointList.get(i).x + startX;
			int realPointY = pointList.get(i).y + startY;
			
			if(!inRange(realPointX,realPointY) || newMap[realPointX][realPointY] != 0) return null;
			
			finalPointList.add(new Node(realPointX,realPointY));
			finalMinX = Math.min(finalMinX, realPointX);
			finalMinY = Math.min(finalMinY, realPointY);
		}
		return new Simulation(finalPointList, finalMinX, finalMinY);
	}

	/**
	 * 미생물 투입
	 */
	private static void addVacterias(int inputTime) throws IOException{
		String [] tokens = br.readLine().split(" ");
		int r1 = Integer.parseInt(tokens[0]);
		int c1 = Integer.parseInt(tokens[1]);
		int r2 = Integer.parseInt(tokens[2]);
		int c2 = Integer.parseInt(tokens[3]);
		
		List<Node> points = new ArrayList<>();
		
		for(int i=r1;i<r2;i++) {
			for(int j=c1;j<c2;j++) {
				if(map[i][j] != 0) {	//기존에 이미 위치한 미생물이 있을 경우
					Vacteria originalVacteria = findVacteria(map[i][j]);
					removePoint(originalVacteria, new Node(i,j));
				}
				map[i][j] = inputTime;
				points.add(new Node(i,j));
			}
		}
		
		Vacteria vacteria = new Vacteria(points, inputTime);
		vacterias.add(vacteria);
		
		ArrayList<Vacteria> removedVacteria = new ArrayList<>();
		//미생물 무리가 2개로 나뉘어져 있는게 아닌지 체크
		for(int index=0;index<vacterias.size();index++) {
			Vacteria targetVacteria = vacterias.get(index);
			int targetTime = targetVacteria.inputTime;
			boolean isOnePiece = isOnePiece(targetTime, targetVacteria);
			
			//2개로 나뉘어진 경우 폐기 처리
			if(!isOnePiece) {
				for(int i=0;i<targetVacteria.points.size();i++) {
					Node point = targetVacteria.points.get(i);
					if(map[point.x][point.y] == targetVacteria.inputTime) {
						map[point.x][point.y] = 0;
					}
				}
				
				removedVacteria.add(targetVacteria);
			}
		}
		
		vacterias.removeAll(removedVacteria);
	}
	
	private static void removePoint(Vacteria originalVacteria, Node node) {
		Node removePoint = null;
		for(int i=0;i<originalVacteria.points.size();i++) {
			Node point = originalVacteria.points.get(i);
			if(point.x == node.x && point.y == node.y) {
				removePoint = point;
				break;
			}
		}
		
		originalVacteria.points.remove(removePoint);
	}

	private static Vacteria findVacteria(int inputTime) {
		for(int index = 0; index<vacterias.size();index++){
			Vacteria vacteria = vacterias.get(index);
			if(vacteria.inputTime == inputTime) return vacteria;
		}
		return null;
	}

	/**
	 * 미생물 무리가 2개로 나뉘어져 있는지 아닌지 여부 체크(BFS 사용)
	 * @param targetVacteria
	 * @return
	 */
	private static boolean isOnePiece(int targetVacteria, Vacteria vacteria) {
		Queue<Node> queue = new ArrayDeque<>();
		
		boolean [][] visited = new boolean[N][N];
		List<Node> points = vacteria.points;
		
		Node startPoint = points.get(0);
		visited[startPoint.x][startPoint.y] = true;
		queue.add(new Node(startPoint.x,startPoint.y));
		
		int size = 0;
		
		while(!queue.isEmpty()) {
			Node curNode = queue.poll();
			size++;
			for(int d=0;d<4;d++) {
				int nx = curNode.x + dx[d];
				int ny = curNode.y + dy[d];
				
				if(!inRange(nx,ny)) continue;
				if(visited[nx][ny]) continue;
				if(map[nx][ny] != targetVacteria) continue;
				
				queue.add(new Node(nx,ny));
				visited[nx][ny] = true;
			}
		}
		return (size == points.size());
	}
	
	private static boolean inRange(int x,int y) {
		return x >= 0 && x < N && y >= 0 && y < N;
	}

	private static Node getMinPointValue(Vacteria moveVacteria) {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		
		for(Node point : moveVacteria.points) {
			minX = Math.min(point.x, minX);
			minY = Math.min(point.y, minY);
		}
		
		return new Node(minX,minY);
	}
	
	/**
	 * 미생물 무리
	 * points : 미생물 좌표
	 * inputTime : 미생물 무리가 추가된 시간
	 */
	static class Vacteria{
		List<Node> points;
		int inputTime;
		
		public Vacteria(List<Node> points, int inputTime) {
			this.points = points;
			this.inputTime = inputTime;
		}
	}
	
	static class Node{
		int x,y;
		
		public Node(int x,int y) {
			this.x = x;
			this.y = y;
		}
	}

	static class Simulation{
		List<Node> newPoints;
		int minX;
		int minY;
		public Simulation(List<Node> newPoints, int minX, int minY) {
			super();
			this.newPoints = newPoints;
			this.minX = minX;
			this.minY = minY;
		}
	}
}
