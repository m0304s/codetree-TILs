import java.io.*;
import java.util.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
	static final int BLANK = 0, HEAD = 1, PERSON = 2, TAIL = 3, ROAD = 4;

	static int N, M, K;
	static int[][] map;
	static int totalScore;
	static List<Team> teamList;
	static int[] dx = { 0, 0, -1, 1 };
	static int[] dy = { -1, 1, 0, 0 };

	public static void main(String[] args) throws IOException {
		init();
		simulation();
		
		bw.write(totalScore+"\n");
		bw.flush();
		bw.close();
		br.close();
	}

	private static void simulation() {
		for(int k=1;k<=K;k++) {
			moveTeam();
			totalScore += throwBall(k);
		}
	}

	private static int throwBall(int turn) {
        int changedTurn = (turn - 1) % (4 * N) + 1;

        int dir  = (changedTurn - 1) / N;   // 0: 첫 구간, 1: 두 번째 구간, 2: 세 번째, 3: 네 번째
        int step = (changedTurn - 1) % N;   // 해당 구간 안에서의 '몇 번째 턴인지' 0~(N-1)

        // (3) 시작 좌표와 이동 방향 설정
        int startX = 0, startY = 0;
        int dx = 0, dy = 0;

        switch (dir) {
            case 0:
                startX = step;
                startY = 0;
                dx = 0;
                dy = 1;
                break;

            case 1:
                startX = N - 1;
                startY = step;
                dx = -1;
                dy = 0;
                break;

            case 2:
                startX = (N - 1) - step;
                startY = N - 1;
                dx = 0;
                dy = -1;
                break;

            case 3:
                startX = 0;
                startY = (N - 1) - step;
                dx = 1;
                dy = 0;
                break;
        }
        //시작좌표 : (startX,startY) 변화량 : (dx,dy);
        int x = startX;
        int y = startY;
        int score = 0;
        while(true) {
        	if(!inRange(x,y)) break;	//격자밖을 벗어나면..
        	if(map[x][y] == HEAD || map[x][y] == PERSON || map[x][y] == TAIL) {	//사람과 부딪히면...
        		score = checkScore(x,y);
        		break;
        	}
        	
        	x += dx;
        	y += dy;
        }
        return score;
	}

	//(x,y) 좌표에 있는 사람이 팀에서 몇 번째 사람인지 체크 
	private static int checkScore(int x, int y) {
		Team team = findTeam(x,y);
		int returnValue = 0;
		for(int i=0;i<team.peoples.size();i++) {
			People people = team.peoples.get(i);
			if(people.point.x == x && people.point.y == y) {
				returnValue =  (i+1) * (i+1);
				break;
			}
		}
		
		reverseTeam(team);
		return returnValue;
	}

	private static void reverseTeam(Team team) {
		Collections.reverse(team.peoples);
		for(int i=0;i<team.peoples.size();i++) {
			Node point = team.peoples.get(i).point;
			if(i == 0) {
				map[point.x][point.y] = TAIL; 
			}else if(i == team.peoples.size() -1) {
				map[point.x][point.y] = HEAD; 
			}else {
				map[point.x][point.y] = PERSON; 
			}
		}
	}

	private static Team findTeam(int x, int y) {
		for(Team team : teamList) {
			for(People p : team.peoples) {
				if(p.point.x == x && p.point.y == y) {
					return team;
				}
			}
		}
		return null;
	}

	private static void debug() {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				System.out.print(map[i][j] + " ");
			}
			System.out.println();
		}

		for (Team team : teamList) {
			System.out.println(team);
		}
		
		System.out.println("====================");
	}

	/**
	 * 각 팀은 머리사람을 따라서 한 칸 이동
	 */
	private static void moveTeam() {
		for (Team team : teamList) {
			// 1) 이동할 머리 좌표 찾기
			People head = team.peoples.get(0);
			Node newHead = findMovablePoint(head);
			if (newHead == null)
				continue;

			// 2) 이전 모든 좌표는 ROAD로 표시
			for (People p : team.peoples) {
				map[p.point.x][p.point.y] = ROAD;
			}

			// 3) 이전 좌표들 복사
			Node[] prevPos = new Node[team.peoples.size()];
			for (int i = 0; i < team.peoples.size(); i++) {
				People p = team.peoples.get(i);
				prevPos[i] = new Node(p.point.x, p.point.y);
			}

			// 4) 꼬리부터 몸통까지 한 칸씩 뒤따라 이동
			for (int i = team.peoples.size() - 1; i >= 1; i--) {
				team.peoples.get(i).point = prevPos[i - 1];
			}
			// 5) 머리 이동
			head.point = newHead;

			// 6) HEAD / PERSON / TAIL로 다시 맵 마킹
			for (int i = 0; i < team.peoples.size(); i++) {
				People p = team.peoples.get(i);
				if (i == 0) {
					map[p.point.x][p.point.y] = HEAD;
				} else if (i == team.peoples.size() - 1) {
					map[p.point.x][p.point.y] = TAIL;
				} else {
					map[p.point.x][p.point.y] = PERSON;
				}
			}
		}
	}

	// 머리사람이 이동할 수 있는 좌표 찾기
	// 머리사람이 이동할 수 있는 좌표 -> 상하좌우 중 도로이면서, 이미 점거중인 사람이 없는 좌표
	private static Node findMovablePoint(People head) {
		for (int d = 0; d < 4; d++) {
			int nx = head.point.x + dx[d];
			int ny = head.point.y + dy[d];

			if (!inRange(nx, ny))
				continue;

			if (map[nx][ny] == ROAD || map[nx][ny] == TAIL) {
				return new Node(nx, ny);
			}
		}
		return null;
	}

	static void init() throws IOException {
		totalScore = 0;
		String[] tokens = br.readLine().split(" ");
		N = Integer.parseInt(tokens[0]);
		M = Integer.parseInt(tokens[1]);
		K = Integer.parseInt(tokens[2]);

		map = new int[N][N];
		teamList = new ArrayList<>();

		for (int i = 0; i < N; i++) {
			tokens = br.readLine().split(" ");
			for (int j = 0; j < N; j++) {
				map[i][j] = Integer.parseInt(tokens[j]);
			}
		}

		// team 정보 입력 (bfs 활용)
		findTeam();
	}

	private static void findTeam() {
		boolean[][] visited = new boolean[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (!visited[i][j] && map[i][j] == HEAD) {
					findMembers(i, j, visited);
				}
			}
		}
	}

	private static void findMembers(int i, int j, boolean[][] visited) {
		List<People> team = new ArrayList<>();
		Queue<Node> queue = new ArrayDeque<>();
		visited[i][j] = true;
		queue.add(new Node(i, j));

		while (!queue.isEmpty()) {
			Node curNode = queue.poll();

			int curValue = map[curNode.x][curNode.y];

			if (curValue == HEAD || curValue == PERSON || curValue == TAIL) {
				team.add(new People(curNode.x, curNode.y));
			}

			for (int d = 0; d < 4; d++) {
				int nx = curNode.x + dx[d];
				int ny = curNode.y + dy[d];

				if (!inRange(nx, ny) || visited[nx][ny])
					continue;

				int newValue = map[nx][ny];

				if (newValue == PERSON || newValue == TAIL) {
					visited[nx][ny] = true;
					queue.add(new Node(nx, ny));
				}
			}
		}
		teamList.add(new Team(team));
	}

	static boolean inRange(int x, int y) {
		return x >= 0 && x < N && y >= 0 && y < N;
	}

	static class People {
		Node point;

		public People(int i, int j) {
			super();
			this.point = new Node(i, j);
		}

		@Override
		public String toString() {
			return "People [point=" + point +"]";
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

	static class Team {
		List<People> peoples;

		public Team(List<People> peoples) {
			super();
			this.peoples = peoples;
		}

		@Override
		public String toString() {
			return "Team [peoples=" + peoples + "]";
		}
	}
}
