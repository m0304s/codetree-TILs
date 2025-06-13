import java.io.*;
import java.util.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

	static int N, M, K;
	static int[][] maze;
	static List<Person> participants;
	static Node exit;

	public static void main(String[] args) throws IOException {
		init();
		simulation();
	}

	static void simulation() throws IOException {
		int totalMoveLength = 0;
		for (int time = 1; time <= K; time++) {
			move();
			rotateMaze();
		}
		for (Person p : participants)
			totalMoveLength += p.moveLength;

		bw.write(totalMoveLength + "\n");
		bw.write((exit.x + 1) + " " + (exit.y + 1) + "\n");
		bw.flush();
		bw.close();
		br.close();
	}

	static void rotateMaze() {
		int sx = -1, sy = -1, size = 0;
		Loop: for (int s = 2; s <= N; s++) {
			for (int r = 0; r < N - s + 1; r++) {
				for (int c = 0; c < N - s + 1; c++) {
					// 현재 정사각형: (r, c) 부터 크기 s
					if (!(exit.x >= r && exit.x < r + s && exit.y >= c && exit.y < c + s)) {
						continue;
					}

					boolean hasParticipant = false;
					for (Person p : participants) {
						if (p.status != Status.ESCAPE && (p.x >= r && p.x < r + s && p.y >= c && p.y < c + s)) {
							hasParticipant = true;
							break;
						}
					}

					if (hasParticipant) {
						sx = r;
						sy = c;
						size = s;
						break Loop;
					}
				}
			}
		}
		
		if(sx == -1) return;
		
        int[][] tempBoard = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int wall = maze[sx + i][sy + j];
                if (wall > 0) wall--; // 내구도 감소
                tempBoard[i][j] = wall;
            }
        }

        // 시계방향 90도 회전
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
            	maze[sx + i][sy + j] = tempBoard[size - 1 - j][i];
            }
        }

        // 3. 참가자와 출구 위치 회전
        for (Person p : participants) {
            if (p.status == Status.ESCAPE) continue;
            if (p.x >= sx && p.x < sx + size && p.y >= sy && p.y < sy + size) {
                int ox = p.x - sx, oy = p.y - sy;
                int rx = oy, ry = size - 1 - ox;
                p.x = sx + rx;
                p.y = sy + ry;
            }
        }

        // 출구 위치 회전
        if (exit.x >= sx && exit.x < sx + size && exit.y >= sy && exit.y < sy + size) {
            int ox = exit.x - sx, oy = exit.y - sy;
            int rx = oy, ry = size - 1 - ox;
            exit.x = sx + rx;
            exit.y = sy + ry;
        }
	}

	private static void move() {
		List<Move> movable = getMovablePerson();
		for (Move move : movable) {
			int[] dx = { -1, 1, 0, 0 };
			int[] dy = { 0, 0, -1, 1 };
			Person p = move.person;
			int dir = move.dir;

			p.x += dx[dir];
			p.y += dy[dir];
			p.moveLength++;

			if (p.x == exit.x && p.y == exit.y) {
				M--;
				p.status = Status.ESCAPE;
			}
		}
	}

	private static List<Move> getMovablePerson() {
		List<Move> movable = new ArrayList<>();
		int[] dx = { -1, 1, 0, 0 };
		int[] dy = { 0, 0, -1, 1 };

		for (Person p : participants) {
			if (p.status == Status.ESCAPE)
				continue;

			int originalDistance = calcDistance(new Node(p.x, p.y), exit); // 기존의 거리
			int moveDir = -1;

			for (int d = 0; d < 2; d++) {
				int nx = p.x + dx[d];
				int ny = p.y + dy[d];

				if (!inRange(nx, ny) || maze[nx][ny] != 0)
					continue;

				int newDistance = calcDistance(new Node(nx, ny), exit);

				if (newDistance < originalDistance) {
					moveDir = d;
					break;
				}
			}

			if (moveDir == -1) {
				for (int d = 2; d < 4; d++) {
					int nx = p.x + dx[d];
					int ny = p.y + dy[d];

					if (!inRange(nx, ny) || maze[nx][ny] != 0)
						continue;

					int newDistance = calcDistance(new Node(nx, ny), exit);

					if (newDistance < originalDistance) {
						moveDir = d;
						break;
					}
				}
			}

			if (moveDir != -1) {
				movable.add(new Move(p, moveDir));
			}
		}
		return movable;
	}

	static boolean inRange(int x, int y) {
		return x >= 0 && x < N && y >= 0 && y < N;
	}

	static int calcDistance(Node o1, Node o2) {
		return Math.abs(o1.x - o2.x) + Math.abs(o1.y - o2.y);
	}

	static void init() throws IOException {
		String[] tokens = br.readLine().split(" ");
		N = Integer.parseInt(tokens[0]);
		M = Integer.parseInt(tokens[1]);
		K = Integer.parseInt(tokens[2]);

		maze = new int[N][N];
		for (int i = 0; i < N; i++) {
			tokens = br.readLine().split(" ");
			for (int j = 0; j < N; j++) {
				maze[i][j] = Integer.parseInt(tokens[j]);
			}
		}

		participants = new ArrayList<>();
		// 참가자 정보 입력
		for (int i = 0; i < M; i++) {
			tokens = br.readLine().split(" ");
			int r = Integer.parseInt(tokens[0]) - 1;
			int c = Integer.parseInt(tokens[1]) - 1;

			participants.add(new Person(i + 1, 0, r, c, Status.ALIVE));
		}

		// 출구 정보 입력
		tokens = br.readLine().split(" ");
		int r = Integer.parseInt(tokens[0]) - 1;
		int c = Integer.parseInt(tokens[1]) - 1;

		exit = new Node(r, c);
	}

	static class Person {
		int num, moveLength, x, y;
		Status status;

		public Person(int num, int moveLength, int x, int y, Status status) {
			super();
			this.num = num;
			this.moveLength = moveLength;
			this.x = x;
			this.y = y;
			this.status = status;
		}

		@Override
		public String toString() {
			return "Person [num=" + num + ", moveLength=" + moveLength + ", x=" + x + ", y=" + y + ", status=" + status
					+ "]";
		}
	}

	static enum Status {
		ESCAPE, ALIVE
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

	static class Move {
		Person person;
		int dir;

		public Move(Person person, int dir) {
			super();
			this.person = person;
			this.dir = dir;
		}

		@Override
		public String toString() {
			return "Move [person=" + person + ", dir=" + dir + "]";
		}

	}
}
