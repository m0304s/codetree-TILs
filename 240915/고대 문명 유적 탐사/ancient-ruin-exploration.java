import java.io.*;
import java.util.*;

public class Main {
	public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	public static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
	
	public static int K;
	public static int M;
	public static int [] dx = {-1,1,0,0};
	public static int [] dy = {0,0,-1,1};
	public static int [] degrees = new int [] {90,180,270};
	
	public static int [][] map = new int[5][5];
	public static int [][] newMap = new int[5][5];
	public static Queue<Integer> pieces;
	public static PriorityQueue<Pair> remove;
	
	public static class Pair implements Comparable<Pair>{
		int x;
		int y;
		public Pair(int x,int y) {
			this.x =x;
			this.y =y;
		}
		public int compareTo(Pair o1) {
			if(o1.y == this.y) {
				return o1.x - this.x;
			}else {
				return this.y - o1.y;
			}
		}
	}
	
	public static class Node implements Comparable<Node>{
		int centerX;
		int centerY;
		int totalScore;
		int degree;
		
		public Node(int centerX,int centerY,int totalScore, int degree) {
			this.centerX = centerX;
			this.centerY = centerY;
			this.totalScore = totalScore;
			this.degree = degree;
		}
		
		public int compareTo(Node o) {
			if(this.totalScore == o.totalScore) {
				if(this.degree == o.degree) {
					if(this.centerY == o.centerY) {
						return this.centerX - o.centerX;	//4. 행을 기준으로 오름차순 
					}else {
						return this.centerY - o.centerY;	//3. 열을 기준으로 오름차순 
					}
				}else{
					return this.degree - o.degree;		//2. 각도 기준으로 오름차순 
				}
			}else {
				return o.totalScore - this.totalScore;	//1. 점수 기준으로 내림차순 
			}
		}
		
	}
	
	
	public static void main(String [] args) throws IOException{
		String [] tokens = br.readLine().split(" ");
		K = Integer.parseInt(tokens[0]);
		M = Integer.parseInt(tokens[1]);
		
		for(int i=0;i<5;i++) {
			tokens = br.readLine().split(" ");
			for(int j=0;j<5;j++) {
				map[i][j] = Integer.parseInt(tokens[j]);
			}
		}
		pieces = new LinkedList<>();
		tokens = br.readLine().split(" ");
		for(int i=0;i<M;i++) {
			pieces.add(Integer.parseInt(tokens[i]));
		}
		int [] answer = new int[K];
		for(int t=0;t<K;t++) {
			ArrayList<Node> candidates = new ArrayList<>();
			// 중심 좌표를 기준으로 90, 180, 270도 회전
        	for (int cnt = 0; cnt < 3; cnt++) {
        		for (int i = 1; i <= 3; i++) {
        			for (int j = 1; j <= 3; j++) {
        				rotateMap(i - 1, j - 1, degrees[cnt]);
        				int score = explore(newMap);
        				
        				// 유물을 획득하는 경우에만 후보 리스트에 추가
        				if (score > 0) {
        					candidates.add(new Node(i, j, score, degrees[cnt]));
        				}
        			}
        		}
        	}
			
			if(candidates.isEmpty()) {
				break;
			}
			
			Collections.sort(candidates);
			
			Node best = candidates.get(0);
			rotateMap(best.centerX-1,best.centerY-1,best.degree);
			map = newMap;
			int score = explore(map);
			int sum = 0;
			while(score>0) {
				fillMap();
				sum+=score;
				score = explore(map);
			}
			answer[t] = sum;
		}
		
		for(int i : answer) {
			if(i == 0) {
				break;
			}
			System.out.print(i + " ");
		}
	}
	
	public static void fillMap() {
		while(!remove.isEmpty()) {
			Pair cur = remove.poll();
			map[cur.x][cur.y] = pieces.poll();
		}
	}
	
	public static int explore(int [][] map) {
		boolean[][] visited = new boolean[5][5];
		Queue<Pair> queue = new LinkedList<>();
		
		//사라지는 유물 저장
		remove = new PriorityQueue<>();
		
		for(int x=0;x<5;x++) {
			for(int y=0;y<5;y++) {
				if(!visited[x][y]) {
					queue.add(new Pair(x,y));
					visited[x][y] = true;
                    ArrayList<Pair> tempRemoveList = new ArrayList<>();
                    tempRemoveList.add(new Pair(x,y));
					int blockSize = 1;
					int blockNum = map[x][y];
					while(!queue.isEmpty()) {
						Pair now = queue.poll();
						
						for(int i=0;i<4;i++) {
							int newX = now.x + dx[i];
							int newY = now.y + dy[i];
							
							if(inRange(newX,newY) && !visited[newX][newY]) {
								if(map[newX][newY] == blockNum) {
									queue.add(new Pair(newX,newY));
									visited[newX][newY] = true;
									tempRemoveList.add(new Pair(newX,newY));
									blockSize++;
								}
							}
						}
					}
					
					if(blockSize>=3) {
						remove.addAll(tempRemoveList);
					}
				}
			}
		}
		return remove.size();
	}
    
	public static boolean inRange(int x,int y) {
		if(x>=0 && x<5 && y>=0 && y<5) {
			return true;
		}return false;
	}
	private static void rotateMap(int sx, int sy, int rotate) {
		newMap = new int[5][5];
		
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				newMap[i][j] = map[i][j];
			}
		}
		
		for (int i = sx; i < sx + 3; i++) {
			for (int j = sy; j < sy + 3; j++) {
				int ox = i - sx;
				int oy = j - sy;
				
				int rx = oy;
				int ry = 3 - ox - 1;
				
				if (rotate == 90) { // 90도 회전
					rx = oy;
					ry = 3 - ox - 1;
				} else if (rotate == 180) { // 180도 회전
					rx = 3 - ox - 1;
					ry = 3 - oy - 1;
				} else { // 270도 회전
					rx = 3 - oy - 1;
					ry = ox;
				}
				
				newMap[rx + sx][ry + sy] = map[i][j];
			}
		}
	}
	public static int [][] cloneMap(int [][] targetMap){
		int [][] newMap = new int[targetMap.length][targetMap[0].length];
		for(int i=0;i<newMap.length;i++) {
			for(int j=0;j<newMap[0].length;j++) {
				newMap[i][j] = targetMap[i][j];
			}
		}
		return newMap;
	}
}