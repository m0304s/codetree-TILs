import java.util.*;
import java.io.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

	/**
	 * M명의 참가자 미로의 구성 N * N 크기의 격자, 좌상단은 (1,1) 미로의 각칸은 다음 3가지 중 하나의 상태 
	 * 1. 빈 칸 
	 * 참가자가 이동 가능한 칸 
	 * 2. 벽 
	 * 참가자가 이동할 수 없는 칸 
	 * 1이상 9이하의 내구도를 갖고 있음 
	 * 회전할 때, 내구도가 1씩 깎임
	 * 내구도가 0이 되면, 빈 칸으로 변경 3. 출구 참가자가 해당 칸에 도달하면, 즉시 탈출
	 * 
	 * 1초마다 모든 참가자는 한 칸씩 움직임 
	 * 움직이는 조건 
	 * 두 위치의 최단 거리는 |x1- x2| + |y1 - y2| 
	 * 모든 참가자는 동시에 움직임 
	 * 상하좌우로 움직일 수 있으며, 벽이 없는 곳으로 이동할 수 있음 
	 * 움직인 칸은 현재 머물러 있던 칸보다 출구까지의 최단 거리가 가까워야 함 
	 * 움직일 수 있는 칸이 2개 이상이라면, 상하로 움직이는 것을 우선시함 
	 * 참가자가 움직일 수 없는 상황이라면, 움직이지 않음 
	 * 한 칸에 2명 이상의 참가자가 있을 수 있음
	 * 
	 * 모든 참가자가 이동을 끝내면, 다음 조건에 의해 미로가 회전 
	 * 미로 회전 조건 
	 * 한 명 이상의 참가자와 출구를 포함한 가장 작은 정사각형을 선택 
	 * 가장 작은 크기를 갖는 정사각형이 2개 이상이라면, 좌상단 r좌표가 작은 것이 우선되고, 그래도 같으면 c좌표가 작은 것이 우선순위
	 * 선택된 정사각형은 시계방향으로 90도 회전, 회전된 벽은 내구도가 1씩 깎임
	 * 
	 * K초 동안 위의 과정이 계속 반복, 만약 K초 전에 모든 참가자가 탈출에 성공한다면, 게임이 종료 게임이 끝났을 때, 모든 참가자들의 이동
	 * 거리 합과 출구 좌표를 출력
	 */

	// 상 하 좌 우
	static int[] dx = { -1, 1, 0, 0 };
	static int[] dy = { 0, 0, -1, 1 };
	static int N, M, K;
	static int totalLength;
	static Position[][] maze;
	static Position exit;

	public static void main(String[] args) throws IOException {
		init();
		simulation();
	}
	
	static void makeWallToBlank() {
		for(int i=0;i<N;i++) {
			for(int j=0;j<N;j++) {
				if(maze[i][j].status == Status.WALL && maze[i][j].wall.health <= 0) {
					maze[i][j].status = Status.BLANK;
					maze[i][j].wall = null;
				}
			}
		}
	}

	static void simulation() throws IOException{
		for (int time = 1; time <= K; time++) {
			moveParticipants(); // 참가자들 이동
	        if (M == 0) {
	            break;
	        }
	        rotateMaze();
		}
		//남아있는 사람들 이동거리 합산
		List<Position> remain = getPositionsWhichHaveParticipants();
		for(Position p : remain) {
			for(Person person : p.applicant) {
				totalLength += person.moveLength;
			}
		}
		bw.write(totalLength + "\n");
	    bw.write((exit.point.x + 1) + " " + (exit.point.y + 1) + "\n");
	    bw.flush();
	    bw.close();
	}

	/**
	 * 미로 회전
	 */
	private static void rotateMaze() {
	    List<Position> participants = getPositionsWhichHaveParticipants();
	    if (participants.isEmpty()) {
	        return; // 회전할 참가자가 없으면 종료
	    }

	    int bestSize = Integer.MAX_VALUE;
	    int bestR = -1, bestC = -1;

	    for (Position p : participants) {
	        int px = p.point.x;
	        int py = p.point.y;
	        int ex = exit.point.x;
	        int ey = exit.point.y;

	        // 현재 참가자와 출구를 포함하는 정사각형의 한 변의 길이
	        int size = Math.max(Math.abs(px - ex), Math.abs(py - ey)) + 1;

	        // 정사각형의 좌상단 좌표(r, c) 계산
	        // 정사각형은 (r,c)부터 (r+size-1, c+size-1)까지의 영역
	        // 이 영역 안에 참가자와 출구가 모두 포함되어야 함
	        int r = Math.max(px, ex) - size + 1;
	        int c = Math.max(py, ey) - size + 1;
	        
	        r = Math.max(0, r);
	        c = Math.max(0, c);

	        // 가장 우선순위 높은 정사각형 정보 갱신 (크기 -> r좌표 -> c좌표 순)
	        if (size < bestSize) {
	            bestSize = size;
	            bestR = r;
	            bestC = c;
	        } else if (size == bestSize) {
	            if (r < bestR) {
	                bestR = r;
	                bestC = c;
	            } else if (r == bestR) {
	                if (c < bestC) {
	                    bestC = c;
	                }
	            }
	        }
	    }

	    if (bestR == -1) return;

	    Position[][] temp = new Position[bestSize][bestSize];

	    for (int i = 0; i < bestSize; i++) {
	        for (int j = 0; j < bestSize; j++) {
	            temp[i][j] = maze[bestR + i][bestC + j];
	        }
	    }

	    for (int i = 0; i < bestSize; i++) {
	        for (int j = 0; j < bestSize; j++) {
	            Position rotatedPos = temp[bestSize - 1 - j][i];

	            // 3. 회전된 벽 내구도 감소 및 데이터 업데이트
	            // -----------------------------------------------------------------
	            if (rotatedPos.status == Status.WALL) {
	                rotatedPos.wall.health--;
	                if (rotatedPos.wall.health == 0) {
	                    rotatedPos.status = Status.BLANK;
	                    rotatedPos.wall = null;
	                }
	            }
	            
	            // 객체의 좌표 정보도 실제 위치에 맞게 업데이트
	            rotatedPos.point.x = bestR + i;
	            rotatedPos.point.y = bestC + j;
	            
	            // 만약 회전시킨 위치가 출구라면, 전역 exit 변수도 갱신
	            if (rotatedPos.status == Status.EXIT) {
	                exit = rotatedPos;
	            }

	            maze[bestR + i][bestC + j] = rotatedPos;
	        }
	    }
	}
	

	private static void moveParticipants() {
		List<Movable> movableList = getMovableParticipants(); // 움직이는 지원자들 리스트

		for (Movable move : movableList) {
			Position position = move.position;
			int dir = move.dir;

			int nx = position.point.x + dx[dir]; // 새롭게 이동할 좌표
			int ny = position.point.y + dy[dir];
			for (Person p : maze[position.point.x][position.point.y].applicant)
				p.moveLength++;

			//만약 새로운 좌표가 탈출구라면..? totalLength에 이동거리 추가 후 삭제처리
			//그렇지 않으면..? 이동처리
			if(isExit(nx,ny)) {
				List<Person> exitParticipants = maze[position.point.x][position.point.y].applicant;
				for(Person p : exitParticipants) {
					totalLength += p.moveLength;
				}
				M -= exitParticipants.size(); // 1. 총 참가자 수에서 탈출한 인원만큼 빼줍니다.
			    maze[position.point.x][position.point.y].applicant.clear();
				continue;
			}
			
			maze[nx][ny].applicant.addAll(maze[position.point.x][position.point.y].applicant);
			maze[position.point.x][position.point.y].applicant.clear();
		}
	}

	private static boolean isExit(int x, int y) {
		return x == exit.point.x && y == exit.point.y;
	}

	/**
	 * 움직일 수 있는 지원자 & 어느 방향으로 이동해야 하는지 반환
	 * 
	 * @return
	 */
	private static List<Movable> getMovableParticipants() {
		List<Movable> movableList = new ArrayList<>();
		List<Position> positionWhichHaveParticipants = getPositionsWhichHaveParticipants();
		for (Position position : positionWhichHaveParticipants) {
		    int originalDistance = calcDistance(position.point, exit.point);
		    for (int d = 0; d < 4; d++) { // 상(0), 하(1), 좌(2), 우(3) 순서로 탐색
		        int nx = position.point.x + dx[d];
		        int ny = position.point.y + dy[d];

		        if (!inRange(nx, ny) || maze[nx][ny].status == Status.WALL)
		            continue;

		        int newDistance = calcDistance(new Node(nx, ny), exit.point);

		        if (originalDistance > newDistance) {
		            movableList.add(new Movable(position, d));
		            break; // 최우선 경로를 찾았으므로 더 이상 탐색할 필요 없음
		        }
		    }
		}

		return movableList;
	}

	private static List<Position> getPositionsWhichHaveParticipants() {
		List<Position> list = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (maze[i][j].applicant != null && !maze[i][j].applicant.isEmpty()) {
					list.add(maze[i][j]);
				}
			}
		}
		return list;
	}

	static boolean inRange(int x, int y) {
		return x >= 0 && x < N && y >= 0 && y < N;
	}

	/**
	 * 최단거리 계산
	 */
	static int calcDistance(Node o1, Node o2) {
		int a = Math.abs(o1.x - o2.x);
		int b = Math.abs(o1.y - o2.y);
		return a + b;
	}

	static void init() throws IOException {
		totalLength = 0;
		String[] tokens = br.readLine().split(" ");
		N = Integer.parseInt(tokens[0]);
		M = Integer.parseInt(tokens[1]);
		K = Integer.parseInt(tokens[2]);

		maze = new Position[N][N];

		for (int i = 0; i < N; i++) {
			tokens = br.readLine().split(" ");
			for (int j = 0; j < N; j++) {
				int health = Integer.parseInt(tokens[j]);
				if (health == 0) { // 빈칸인 경우
					maze[i][j] = new Position(null, new ArrayList<>(), Status.BLANK, new Node(i, j));
				} else { // 벽인 경우
					maze[i][j] = new Position(new Wall(health), new ArrayList<>(), Status.WALL, new Node(i, j));
				}
			}
		}

		// 참가자 좌표 입력
		for (int i = 0; i < M; i++) {
			tokens = br.readLine().split(" ");
			int x = Integer.parseInt(tokens[0]) - 1;
			int y = Integer.parseInt(tokens[1]) - 1;
			Person person = new Person(i + 1, 0);
			maze[x][y].applicant.add(person);
		}

		// 탈출구 입력
		tokens = br.readLine().split(" ");
		int x = Integer.parseInt(tokens[0]) - 1;
		int y = Integer.parseInt(tokens[1]) - 1;

		maze[x][y].wall = null;
		maze[x][y].applicant = null;
		maze[x][y].status = Status.EXIT;
		exit = maze[x][y];
	}

	static void printMaze() {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				System.out.println(" Status : " + maze[i][j]);
			}
		}
		System.out.println("==========================");
	}

	static class Position {
		// 벽일수도..빈칸일수도..참가자가 여러 명 있을 수도
		Node point;
		Wall wall;
		ArrayList<Person> applicant;
		Status status;

		public Position(Wall wall, ArrayList<Person> applicant, Status status, Node point) {
			super();
			this.wall = wall;
			this.applicant = applicant;
			this.status = status;
			this.point = point;
		}

		@Override
		public String toString() {
			return "Position [point=" + point + ", wall=" + wall + ", applicant=" + applicant + ", status=" + status
					+ "]";
		}

	}

	static enum Status {
		WALL, BLANK, EXIT
	}

	static class Person {
		int num;
		int moveLength;

		public Person(int num, int moveLength) {
			super();
			this.num = num;
			this.moveLength = moveLength;
		}

		@Override
		public String toString() {
			return "Person [num=" + num + ", moveLength=" + moveLength + "]";
		}
	}

	static class Wall {
		int health;

		public Wall(int health) {
			this.health = health;
		}

		@Override
		public String toString() {
			return "Wall [health=" + health + "]";
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

	static class Movable {
		Position position;
		int dir;

		public Movable(Position person, int dir) {
			super();
			this.position = person;
			this.dir = dir;
		}

		@Override
		public String toString() {
			return "Movable [position=" + position + ", dir=" + dir + "]";
		}

	}
}