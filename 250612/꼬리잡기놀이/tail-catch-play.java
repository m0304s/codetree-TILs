import java.io.*;
import java.util.*;

public class Main {
	static final int BLANK = 0, HEAD = 1, PERSON = 2, TAIL = 3, ROAD = 4;

	static int N, M, K;
	static int[][] map;
	static int totalScore;
	static List<Team> teamList;
	static int[] dx = { 0, 0, -1, 1 };
	static int[] dy = { -1, 1, 0, 0 };

	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

	public static void main(String[] args) throws IOException {
		init();
		simulate();
		bw.write(totalScore + "\n");
		bw.flush();
		bw.close();
		br.close();
	}

	static void init() throws IOException {
		String[] tok = br.readLine().split(" ");
		N = Integer.parseInt(tok[0]);
		M = Integer.parseInt(tok[1]);
		K = Integer.parseInt(tok[2]);
		map = new int[N][N];
		for (int i = 0; i < N; i++) {
			tok = br.readLine().split(" ");
			for (int j = 0; j < N; j++) {
				map[i][j] = Integer.parseInt(tok[j]);
			}
		}
		teamList = new ArrayList<>();
		boolean[][] visited = new boolean[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (map[i][j] == HEAD) {
					teamList.add(findTeam(i, j, visited));
				}
			}
		}
	}

	static Team findTeam(int sx, int sy, boolean[][] visited) {
		List<People> members = new ArrayList<>();
		Queue<int[]> q = new LinkedList<>();

		visited[sx][sy] = true;
		q.add(new int[] { sx, sy });
		members.add(new People(sx, sy));

		while (!q.isEmpty()) {
			int[] curr = q.poll();

			// 꼬리면 종료
			if (map[curr[0]][curr[1]] == TAIL && members.size() > 1)
				break;

			for (int d = 0; d < 4; d++) {
				int nx = curr[0] + dx[d];
				int ny = curr[1] + dy[d];

				if (!inRange(nx, ny) || visited[nx][ny])
					continue;

				// 머리사람(1) 바로 옆은 몸통(2)이어야 함
				if (members.size() == 1 && map[nx][ny] != PERSON)
					continue;

				// 몸통, 꼬리를 찾으면 멤버에 추가
				if (map[nx][ny] == PERSON || map[nx][ny] == TAIL) {
					visited[nx][ny] = true;
					q.add(new int[] { nx, ny });
					members.add(new People(nx, ny));
				}
			}
		}
		return new Team(members);
	}

	static void simulate() {
		totalScore = 0;
		for (int turn = 1; turn <= K; turn++) {
			moveTeams();
			totalScore += throwBall(turn);
		}
	}

	static void moveTeams() {
		List<List<Node>> allTeamsNewPositions = new ArrayList<>();
		for (Team team : teamList) {
			List<Node> newPositions = new ArrayList<>();
			Node newHeadPos = findMovable(team);

			// 팀의 새로운 좌표 리스트 생성
			newPositions.add(newHeadPos);
			for (int i = 0; i < team.peoples.size() - 1; i++) {
				newPositions.add(team.peoples.get(i).point);
			}
			allTeamsNewPositions.add(newPositions);
		}

		for (Team team : teamList) {
			for (People p : team.peoples) {
				map[p.point.x][p.point.y] = ROAD;
			}
		}

		for (int i = 0; i < teamList.size(); i++) {
			Team team = teamList.get(i);
			List<Node> newPositions = allTeamsNewPositions.get(i);

			for (int j = 0; j < team.peoples.size(); j++) {
				// 팀 내부 좌표 정보 업데이트
				team.peoples.get(j).point = newPositions.get(j);

				// 맵에 새로운 위치 그리기
				Node pt = team.peoples.get(j).point;
				int marker = (j == 0) ? HEAD : (j == team.peoples.size() - 1) ? TAIL : PERSON;
				map[pt.x][pt.y] = marker;
			}
		}
	}

	static Node findMovable(Team team) {
		People head = team.peoples.get(0);
		Node secondPersonPos = team.peoples.get(1).point;
		for (int d = 0; d < 4; d++) {
			int nx = head.point.x + dx[d], ny = head.point.y + dy[d];
			if (!inRange(nx, ny))
				continue;
			if (nx == secondPersonPos.x && ny == secondPersonPos.y)
				continue;
			if (map[nx][ny] == ROAD || map[nx][ny] == TAIL) {
				return new Node(nx, ny);
			}
		}
		return null;
	}

	static int throwBall(int turn) {
		int cycle = 4 * N;
		int idx = (turn - 1) % cycle;
		int dir = idx / N;
		int step = idx % N;
		int sx, sy, mx, my;
		switch (dir) {
		case 0:
			sx = step;
			sy = 0;
			mx = 0;
			my = 1;
			break;
		case 1:
			sx = N - 1;
			sy = step;
			mx = -1;
			my = 0;
			break;
		case 2:
			sx = N - 1 - step;
			sy = N - 1;
			mx = 0;
			my = -1;
			break;
		default:
			sx = 0;
			sy = N - 1 - step;
			mx = 1;
			my = 0;
			break;
		}
		int x = sx, y = sy;
		while (inRange(x, y)) {
			int v = map[x][y];
			if (v >= HEAD && v <= TAIL) {
				return scoreHit(x, y);
			}
			x += mx;
			y += my;
		}
		return 0;
	}

	static int scoreHit(int x, int y) {
		Team hitTeam = null;
		for (Team t : teamList) {
			for (People p : t.peoples) {
				if (p.point.x == x && p.point.y == y) {
					hitTeam = t;
					break;
				}
			}
			if (hitTeam != null)
				break;
		}
		if (hitTeam == null)
			return 0;
		int position = 0;
		for (int i = 0; i < hitTeam.peoples.size(); i++) {
			if (hitTeam.peoples.get(i).point.x == x && hitTeam.peoples.get(i).point.y == y) {
				position = i + 1;
				break;
			}
		}
		int score = position * position;
		Collections.reverse(hitTeam.peoples);

		// 맵 업데이트
		for (int i = 0; i < hitTeam.peoples.size(); i++) {
			Node pt = hitTeam.peoples.get(i).point;
			int marker = (i == 0) ? HEAD : (i == hitTeam.peoples.size() - 1) ? TAIL : PERSON;
			map[pt.x][pt.y] = marker;
		}
		return score;
	}

	static boolean inRange(int x, int y) {
		return x >= 0 && x < N && y >= 0 && y < N;
	}

	static class People {
		Node point;

		People(int x, int y) {
			this.point = new Node(x, y);
		}
	}

	static class Node {
		int x, y;

		Node(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	static class Team {
		List<People> peoples;

		Team(List<People> p) {
			this.peoples = p;
		}
	}
}