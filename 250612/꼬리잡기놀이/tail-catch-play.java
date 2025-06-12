import java.util.*;
import java.io.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

	/**
	 * N*N 크기의 격자에서 꼬리잡기놀이 진행 3명 이상이 한 팀으로 구성
	 *
	 * 맨 앞에 있는 사람을 머리사람 맨 뒤에 있는 사람을 꼬리사람
	 *
	 * 각 팀은 주어진 이동 선을 따라서만 이동 각 팀의 이동선은 끝이 이어져있음
	 *
	 * 각 라운드 별 다음과 같이 진행 1. 머리사람을 따라서 한 칸 이동 2. 각 라운드마다 공이 정해진 선을 따라 던져짐 3. 공이 던져지는
	 * 경우에 해당 선에 사람이 있으면 최초에 만나게 되는 사람만이 공을 얻어 점수를 얻음 점수는 머리사람을 시작으로 팀 내에서 K번째 사람이라면
	 * K의 제곱만큼 점수를 얻음 아무도 공을 받지 못하는 경우 점수를 획득하지 못함
	 *
	 * 입력 첫번째줄 격자의 크기(N), 팀의 개수(M), 라운드 수(K)
	 *
	 * N개의 줄에 걸쳐 초기 상태의 정보 (빈칸(0), 머리사람(1), 머리사람과 꼬리사람이 아닌 나머지(2), 꼬리사람(3), 이동선(4)
	 */

	static final int BLANK = 0, HEAD = 1, REMAIN = 2, TAIL = 3, MOVABLE = 4;

	static int N, M, K;
	static int[][] map;
	static List<Team> teamList;

	public static void main(String[] args) throws IOException {
		init();
		simulation();
	}

	static void simulation() throws IOException {
		int totalScore = 0;
		for (int turn = 1; turn <= K; turn++) {
			moveMembersPerTeam(turn);
			int score = throwBall(turn);
			totalScore += score;
		}

		bw.write(totalScore + "\n");
		bw.flush();
		bw.close();
		br.close();
	}

	/**
	 * N: 격자의 한 변 크기 turn: 1 이상인 자연수(몇 번째 턴인지)
	 */
	private static int throwBall(int turn) {
		int changedTurn = (turn - 1) % (4 * N) + 1;
		int dir = (changedTurn - 1) / N;
		int step = (changedTurn - 1) % N;

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

		while (true) {
			if (!inRange(startX, startY))
				break;

			Team catchedTeam = null;
			Member catchedMember = isMemberAtPoint(startX, startY);

			if (catchedMember != null) {
				for (Team team : teamList) {
					if (isContain(team, catchedMember)) {
						catchedTeam = team;
						break;
					}
				}
				
				if(catchedTeam != null) {
					ArrayList<Member> newMembers = new ArrayList<>();
					for (Member member : catchedTeam.members) {
						int changedNum = catchedTeam.members.size() - member.num + 1;
						if (member.role == HEAD) {
							newMembers.add(new Member(member.x, member.y, changedNum, TAIL));
							map[member.x][member.y] = TAIL;
						} else if (member.role == TAIL) {
							newMembers.add(new Member(member.x, member.y, changedNum, HEAD));
							map[member.x][member.y] = HEAD;
						} else {
							newMembers.add(new Member(member.x, member.y, changedNum, REMAIN));
						}
					}
					Collections.sort(newMembers, new Comparator<Member>() {
						public int compare(Member o1, Member o2) {
							return o1.role - o2.role;
						}
					});
					catchedTeam.members = newMembers;
				}
				return getScore(catchedMember);
			}
			startX += dx;
			startY += dy;
		}
		return 0;
	}

	private static boolean isContain(Team team, Member catchedMember) {
		for (Member member : team.members) {
			if (member.x == catchedMember.x && member.y == catchedMember.y && member.role == catchedMember.role
					&& member.role == catchedMember.role)
				return true;
		}
		return false;
	}

	static int getScore(Member member) {
		return (int) Math.pow(member.num, 2);
	}

	private static void moveMembersPerTeam(int turn) {
		for (Team team : teamList) {
			moveMembers(team);
		}
	}

	private static void moveMembers(Team team) {
		int[] dx = { 0, 0, -1, 1 };
		int[] dy = { -1, 1, 0, 0 };

		HashMap<Integer, Member> memberHashMap = new HashMap<>(); // 기존의 좌표 정보 저장
		for (Member member : team.members) {
			memberHashMap.put(member.num - 1, new Member(member.x, member.y, member.num, member.role));
			map[member.x][member.y] = MOVABLE;
		}

		Member head = team.members.get(0);
		boolean headMoved = false;
		ArrayList<Member> newMember = new ArrayList<>();
		for (int d = 0; d < 4; d++) {
			int nx = head.x + dx[d];
			int ny = head.y + dy[d];

			if (!inRange(nx, ny))
				continue;
			if (map[nx][ny] != MOVABLE || isMemberAtPoint(nx, ny) != null)
				continue;

			newMember.add(new Member(nx, ny, head.num, head.role));
			map[nx][ny] = head.role;
			headMoved = true;
			break;
		}

		if (!headMoved) {
			Member tail = findTail(team);
			newMember.add(new Member(tail.x, tail.y, head.num, head.role));
		}

		Collections.sort(team.members, new Comparator<Member>() {
			public int compare(Member o1, Member o2) {
				return o1.num - o2.num;
			}
		});

		for (int i = 1; i < team.members.size(); i++) {
			Member member = team.members.get(i);
			Member prevPos = memberHashMap.get(i - 1);

			newMember.add(new Member(prevPos.x, prevPos.y, member.num, member.role));
			map[prevPos.x][prevPos.y] = member.role;
		}

		team.members = newMember;
	}

	private static Member findTail(Team team) {
		for (Member member : team.members) {
			if (member.role == TAIL)
				return member;
		}
		return null;
	}

	static Member isMemberAtPoint(int x, int y) {
		for (Team team : teamList) {
			for (Member member : team.members) {
				if (member.x == x && member.y == y)
					return member;
			}
		}

		return null;
	}

	static void printTeamInfo() {
		for (Team team : teamList) {
			System.out.println(team);
		}
	}

	static void init() throws IOException {
		String[] tokens = br.readLine().split(" ");
		N = Integer.parseInt(tokens[0]);
		M = Integer.parseInt(tokens[1]);
		K = Integer.parseInt(tokens[2]);

		map = new int[N][N];

		for (int i = 0; i < N; i++) {
			tokens = br.readLine().split(" ");
			for (int j = 0; j < N; j++) {
				map[i][j] = Integer.parseInt(tokens[j]);
			}
		}

		findTeams();
	}

	private static void findTeams() {
		teamList = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (map[i][j] == HEAD) {
					Team team = bfsToFindTeam(i, j);
					teamList.add(team);
				}
			}
		}
	}

	private static Team bfsToFindTeam(int i, int j) {
		int[] dx = { 0, 0, -1, 1 };
		int[] dy = { -1, 1, 0, 0 };

		ArrayList<Member> members = new ArrayList<>();
		Queue<Point> queue = new ArrayDeque<>();
		boolean[][] visited = new boolean[N][N];

		queue.add(new Point(i, j));
		visited[i][j] = true;

		int num = 1;
		while (!queue.isEmpty()) {
			Point curNode = queue.poll();

			int nodeValue = map[curNode.x][curNode.y];
			if (nodeValue == HEAD || nodeValue == REMAIN || nodeValue == TAIL)
				members.add(new Member(curNode.x, curNode.y, num++, nodeValue));

			for (int d = 0; d < 4; d++) {
				int nx = curNode.x + dx[d];
				int ny = curNode.y + dy[d];

				if (!inRange(nx, ny) || visited[nx][ny])
					continue;

				int newNodeValue = map[nx][ny];

				if (nodeValue == HEAD) { // 1일때 :2또는 3으로 이어짐
					if (newNodeValue == REMAIN || newNodeValue == TAIL) {
						queue.add(new Point(nx, ny));
						visited[nx][ny] = true;
					}
				} else if (nodeValue == REMAIN) { // 2일때 : 2또는 3으로 이어짐
					if (newNodeValue == REMAIN || newNodeValue == TAIL) {
						queue.add(new Point(nx, ny));
						visited[nx][ny] = true;
					}
				} else if (nodeValue == TAIL) {
					if (newNodeValue == MOVABLE || newNodeValue == HEAD) {
						queue.add(new Point(nx, ny));
						visited[nx][ny] = true;
					}
				}
			}
		}
		return new Team(members);
	}

	static boolean inRange(int x, int y) {
		return x >= 0 && x < N && y >= 0 && y < N;
	}

	static class Point {
		int x, y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + "]";
		}
	}

	static class Team {
		ArrayList<Member> members;
		HashSet<Member> memberSet;

		public Team(ArrayList<Member> members) {
			super();
			this.members = members;
			this.memberSet = new HashSet<>(members);
		}

		@Override
		public String toString() {
			return "Team [members=" + members + "]";
		}
	}

	static class Member {
		int x, y;
		int num;
		int role;

		@Override
		public String toString() {
			return "Member [x=" + x + ", y=" + y + ", num=" + num + ", role=" + role + "]";
		}

		public Member(int x, int y, int num, int role) {
			super();
			this.x = x;
			this.y = y;
			this.num = num;
			this.role = role;
		}

	}
}
