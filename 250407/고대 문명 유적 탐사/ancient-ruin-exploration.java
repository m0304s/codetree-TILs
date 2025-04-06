import java.io.*;
import java.util.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

	static final int MAP_SIZE = 5;
	static final int [] dx = {0,0,-1,1};
	static final int [] dy = {-1,1,0,0};
	static int [] add;	//추가되는 유물 정보 저장
	static int [][] map;	//유물의 상태 저장
	static int K,M;
	static int addIdx;

	public static void main(String[] args) throws IOException{
		String [] tokens = br.readLine().split(" ");
		K = Integer.parseInt(tokens[0]);	//탐사 횟수
		M = Integer.parseInt(tokens[1]);	//벽면에 적힌 유물의 개수

		createMap();
		createAddArr();
		simulation();

		bw.flush();
		bw.close();
		br.close();
	}

	static void simulation() throws  IOException{
		addIdx = 0;
		for(int k=0;k<K;k++) {
			int totalCost = 0;

			ArrayList<Possible> availables = new ArrayList<Possible>();
			for(int i=1;i<MAP_SIZE-1;i++){
				for(int j=1;j<MAP_SIZE-1;j++){
					//(i,j) : 탐사 구역의 중심 좌표
					for(int rotate = 1;rotate < 4; rotate++){
						int [][] simulationMap = createSimulationMap(i,j,rotate);	//회전된 좌표 배열
						Result result =  findRemoveNodes(simulationMap);	//유적지 좌표
						if(result == null) continue;

						List<Node> removedPoints = result.removeNodes;	//삭제된 유적지 좌표 목록
						Possible possible = new Possible(result.cost, rotate, i,j, removedPoints, simulationMap);
						availables.add(possible);
					}
				}
			}

			if(availables.isEmpty()) break;
			/*
			 * 회전 목표
			 * 1. 유물 1차 획득 가치를 최대화
			 * 2. 가치가 동일할 경우 회전 각도가 가장 작은 방법
			 * 3. 각도가 동일한 방법이 여러 가지일 경우 회전 중심 좌표 열이 가장 작은 구간
			 * 4. 중심 좌표 열이 동일한 경우, 행이 가장 작은 구간
			 */
			Collections.sort(availables, new Comparator<Possible>() {
				@Override
				public int compare(Possible o1, Possible o2) {
					if(o1.cost == o2.cost){
						if(o1.rotate == o2.rotate){
							if(o1.c == o2.c){
								return o1.r - o2.r;
							} else {
								return o1.c - o2.c;
							}
						}else{
							return o1.rotate - o2.rotate;
						}
					}else{
						return o2.cost - o1.cost;
					}
				}
			});

			Possible best = availables.get(0);	//최선의 방식 선택
			totalCost += best.cost;
			//유적 채우기
			Collections.sort(best.removedNodes, new Comparator<Node>() {
				@Override
				public int compare(Node o1, Node o2) {
					if(o1.y == o2.y){
						return o2.x - o1.x;
					}else{
						return o1.y - o2.y;
					}
				}
			});

			map = best.roratedMap;

			while(true){
				//유적지에 좌표 추가
				for(int i=0;i<best.removedNodes.size();i++){
					Node addPoint = best.removedNodes.get(i);	//추가해야 하는 유적 좌표
					map[addPoint.x][addPoint.y] = add[addIdx++];
				}

				//추가한 좌표 기준으로 유물을 생성할 수 있으면 유물 연쇄획득
				Result result = findRemoveNodes(map);
				if(result == null) break;

				totalCost += result.removeNodes.size();

				best.removedNodes = result.removeNodes;
				Collections.sort(best.removedNodes, new Comparator<Node>() {
					@Override
					public int compare(Node o1, Node o2) {
						if(o1.y == o2.y){
							return o2.x - o1.x;
						}else{
							return o1.y - o2.y;
						}
					}
				});
			}

			bw.write(totalCost + " ");
		}
	}

	private static Result findRemoveNodes(int[][] simulationMap) {
		List<Node> removeNodeList = new ArrayList<>();
		int totalCost = 0;
		boolean [][] visited = new boolean[MAP_SIZE][MAP_SIZE];
		for(int i=0;i<MAP_SIZE;i++){
			for(int j=0;j<MAP_SIZE;j++){
				if(!visited[i][j] && simulationMap[i][j] != 0){	//방문하지 않았고, 유적이 존재하는 경우에만 bfs 탐색
					int cost = simulationMap[i][j];
					ArrayList<Node> nodes = bfs(i,j,visited,simulationMap,simulationMap[i][j]);	//동일한 값을 가지는 유적의 개수
					if(nodes == null){
						continue;
					}
					//조각의 개수가 3개 이상인 경우에만 유물로 변경
					if(nodes.size() >= 3){
						totalCost += nodes.size();
						removeNodeList.addAll(nodes);
					}
				}
			}
		}
		if(removeNodeList.isEmpty()) return null;

		return new Result(removeNodeList,totalCost);
	}

	private static ArrayList<Node> bfs(int i, int j, boolean [][] visited,int [][] map, int value) {
		ArrayList<Node> nodeList = new ArrayList<>();	//동일한 value를 가지는 조각 모음
		Queue<Node> queue = new ArrayDeque<>();
		visited[i][j] = true;
		queue.add(new Node(i,j));
		nodeList.add(new Node(i,j));

		while (!queue.isEmpty()){
			Node curNode = queue.poll();

			for(int d=0;d<4;d++){
				int nx = curNode.x + dx[d];
				int ny = curNode.y + dy[d];

				if(!inRange(nx,ny) || visited[nx][ny] || map[nx][ny] != value) continue;

				Node nextNode = new Node(nx,ny);
				visited[nx][ny] = true;
				nodeList.add(nextNode);
				queue.add(nextNode);
			}
		}

		if(nodeList.size() >= 3){
			for(Node node : nodeList){
				map[node.x][node.y] = 0;	//유물 삭제
			}

			return nodeList;
		}

		return null;
	}

	//bfs를 진행할 5x5짜리 2차원 배열 반환
	private static int[][] createSimulationMap(int i, int j, int rotate) {
		int [][] targetMap = cloneMap(map);
		for(int r=0;r<rotate;r++){
			targetMap = rotateMap(i,j,targetMap);
		}
		return targetMap;
	}

	/**
	 * 중심 좌표를 기준으로 3x3짜리 배열을 만들어 90도 회전
	 * @param i
	 * @param j
	 * @param targetMap
	 * @return
	 */
	private static int[][] rotateMap(int i, int j, int[][] targetMap) {
		int [][] newMap = cloneMap(targetMap);
		newMap[i][j] = targetMap[i][j];
		newMap[i-1][j+1] = targetMap[i-1][j-1];	//(i-1,j-1) -> (i-1,j+1)
		newMap[i][j+1] = targetMap[i-1][j];	//(i-1,j) -> (i,j+1)
		newMap[i+1][j+1] = targetMap[i-1][j+1];	//(i-1,j+1) -> (i+1,j+1)
		newMap[i+1][j] = targetMap[i][j+1];	//(i,j+1) -> (i+1,j)
		newMap[i+1][j-1] = targetMap[i+1][j+1];	//(i+1,j+1) -> (i+1,j-1)
		newMap[i][j-1] = targetMap[i+1][j];	//(i+1,j) -> (i,j-1)
		newMap[i-1][j-1] = targetMap[i+1][j-1];	//(i+1,j-1) ->  (i-1,j-1)
		newMap[i-1][j] = targetMap[i][j-1];		//(i,j-1) -> (i-1,j)
		return newMap;
	}

	static int [][] cloneMap(int [][] srcMap){
		int [][] arr = new int[srcMap.length][srcMap[0].length];
		for(int i=0;i<arr.length;i++){
			for(int j=0;j<arr[0].length;j++){
				arr[i][j] = srcMap[i][j];
			}
		}
		return arr;
	}

	static boolean inRange(int x,int y){
		return x >= 0 && x < MAP_SIZE && y >= 0 && y < MAP_SIZE;
	}

	private static void createAddArr() throws IOException{
		add = new int[M];
		String [] tokens = br.readLine().split(" ");
		for(int i=0;i<M;i++){
			add[i] = Integer.parseInt(tokens[i]);
		}
	}

	static void createMap() throws IOException{
		map = new int[MAP_SIZE][MAP_SIZE];
		for(int i=0;i<MAP_SIZE;i++){
			String [] tokens = br.readLine().split(" ");
			for(int j=0;j<MAP_SIZE;j++){
				map[i][j] = Integer.parseInt(tokens[j]);
			}
		}
	}

	/*
     * 회전 목표
	 * 1. 유물 1차 획득 가치를 최대화
	 * 2. 가치가 동일할 경우 회전 각도가 가장 작은 방법
	 * 3. 각도가 동일한 방법이 여러 가지일 경우 회전 중심 좌표 열이 가장 작은 구간
	 * 4. 중심 좌표 열이 동일한 경우, 행이 가장 작은 구간
	 */
	static class Possible{
		int cost;
		int rotate;
		int r,c;
		List<Node> removedNodes;
		int [][] roratedMap;

		Possible(int cost, int rotate, int r,int c, List<Node> removedNodes, int [][] roratedMap){
			this.cost = cost;
			this.rotate = rotate;
			this.r = r;
			this.c = c;
			this.removedNodes = removedNodes;
			this.roratedMap = roratedMap;
		}
	}

	/**
	 * 유적 좌표
	 */
	static class Node{
		int x,y;
		public Node(int x,int y){
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "Node{" +
					"x=" + x +
					", y=" + y +
					'}';
		}
	}

	static class Result{
		List<Node> removeNodes;
		int cost;

		public Result(List<Node> removeNodes, int cost){
			this.removeNodes = removeNodes;
			this.cost = cost;
		}
	}
}