import java.io.*;
import java.util.*;

public class Main {
	static final int BLANK = 0, HEAD = 1, PERSON = 2, TAIL = 3, ROAD = 4;

	static int N, M, K;
	static int[][] map;
	static int totalScore;
	static List<Team> teamList;
	static int[] dx = { 0, 0, -1, 1 }; // 좌, 우, 상, 하
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
					teamList.add(findTeamInOrder(i, j, visited));
				}
			}
		}
	}

	static Team findTeamInOrder(int sx, int sy, boolean[][] visited) {
		List<People> members = new ArrayList<>();
		int currentX = sx;
		int currentY = sy;

		while (map[currentX][currentY] != BLANK && !visited[currentX][currentY]) {
			visited[currentX][currentY] = true;
			members.add(new People(currentX, currentY));

			if (map[currentX][currentY] == TAIL)
				break;

			for (int d = 0; d < 4; d++) {
				int nx = currentX + dx[d];
				int ny = currentY + dy[d];

				if (!inRange(nx, ny) || map[nx][ny] == BLANK || map[nx][ny] == HEAD || visited[nx][ny])
					continue;

				if (members.size() == 1 && map[nx][ny] != PERSON)
					continue;

				currentX = nx;
				currentY = ny;
				break;
			}
		}
		return new Team(members);
	}

	static void simulate() {
		for (int turn = 1; turn <= K; turn++) {
			moveTeams();
			totalScore += throwBall(turn);
		}
	}

	static void moveTeams() {
		List<List<Node>> allTeamsNewPositions = new ArrayList<>();
		for (Team team : teamList) {
			Node newHeadPos = findMovable(team);

			if (newHeadPos == null) {
				List<Node> currentPositions = new ArrayList<>();
				for (People p : team.peoples) {
					currentPositions.add(p.point);
				}
				allTeamsNewPositions.add(currentPositions);
			} else {
				List<Node> newPositions = new ArrayList<>();
				newPositions.add(newHeadPos);
				for (int i = 0; i < team.peoples.size() - 1; i++) {
					newPositions.add(team.peoples.get(i).point);
				}
				allTeamsNewPositions.add(newPositions);
			}
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
				team.peoples.get(j).point = newPositions.get(j);
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
			int nx = head.point.x + dx[d];
			int ny = head.point.y + dy[d];
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
			if (map[x][y] >= HEAD && map[x][y] <= TAIL) {
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
			if (t.peoples.stream().anyMatch(p -> p.point.x == x && p.point.y == y)) {
				hitTeam = t;
				break;
			}
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

		Collections.reverse(hitTeam.peoples);
		return position * position;
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