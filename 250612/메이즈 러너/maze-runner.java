import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
    
    
    static int[][] board;
    // 참가자 정보만 리스트로 관리
    static List<Person> participants;
    // 출구 위치
    static Node exit;

    static int N, M, K;
    static int totalMoveSum = 0; // 모든 참가자의 이동 거리 합

    // 상 하 좌 우 (우선순위 순)
    static int[] dx = {-1, 1, 0, 0};
    static int[] dy = {0, 0, -1, 1};


    public static void main(String[] args) throws IOException {
        init();
        simulation();
    }

    static void simulation() throws IOException {
        for (int time = 1; time <= K; time++) {
            moveAllParticipants();

            if (M == 0) { // 모든 참가자가 탈출했다면 종료
                break;
            }

            rotateMaze();
        }
        for(Person p : participants) {
        	totalMoveSum += p.moveLength;
        }
        
        bw.write(totalMoveSum + "\n");
        bw.write((exit.x + 1) + " " + (exit.y + 1) + "\n");
        bw.flush();
        bw.close();
    }

    static void moveAllParticipants() {
        for (Person p : participants) {
            // 이미 탈출한 참가자는 건너뜀
            if (p.escaped) {
                continue;
            }

            // 현재 출구까지의 최단 거리
            int originalDistance = Math.abs(p.x - exit.x) + Math.abs(p.y - exit.y);
            int moveDir = -1;

            // 1. 상하 이동 먼저 탐색
            for (int i = 0; i < 2; i++) {
                int nx = p.x + dx[i];
                int ny = p.y + dy[i];

                if (inRange(nx, ny) && board[nx][ny] == 0) {
                    int newDistance = Math.abs(nx - exit.x) + Math.abs(ny - exit.y);
                    if (newDistance < originalDistance) {
                        moveDir = i;
                        break;
                    }
                }
            }

            // 2. 상하로 못 갔다면 좌우 이동 탐색
            if (moveDir == -1) {
                for (int i = 2; i < 4; i++) {
                    int nx = p.x + dx[i];
                    int ny = p.y + dy[i];
                    if (inRange(nx, ny) && board[nx][ny] == 0) {
                        int newDistance = Math.abs(nx - exit.x) + Math.abs(ny - exit.y);
                        if (newDistance < originalDistance) {
                            moveDir = i;
                            break;
                        }
                    }
                }
            }
            
            // 움직일 방향이 있다면 이동
            if (moveDir != -1) {
                p.x += dx[moveDir];
                p.y += dy[moveDir];
                p.moveLength++;

                // 이동한 곳이 출구라면 탈출 처리
                if (p.x == exit.x && p.y == exit.y) {
                    p.escaped = true;
                    M--; // 남은 참가자 수 감소
                }
            }
        }
    }


    static void rotateMaze() {
        int sx = -1, sy = -1, size = 0;

        Loop:
        for (int s = 2; s <= N; s++) {
            for (int r = 0; r < N - s + 1; r++) {
                for (int c = 0; c < N - s + 1; c++) {
                    // 현재 정사각형: (r, c) 부터 크기 s
                    if (!(exit.x >= r && exit.x < r + s && exit.y >= c && exit.y < c + s)) {
                        continue;
                    }
                    
                    boolean hasParticipant = false;
                    for (Person p : participants) {
                        if (!p.escaped && (p.x >= r && p.x < r + s && p.y >= c && p.y < c + s)) {
                            hasParticipant = true;
                            break;
                        }
                    }

                    if (hasParticipant) {
                        sx = r; sy = c; size = s;
                        break Loop;
                    }
                }
            }
        }

        if (sx == -1) return; // 회전할 대상이 없음

        int[][] tempBoard = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int wall = board[sx + i][sy + j];
                if (wall > 0) wall--; // 내구도 감소
                tempBoard[i][j] = wall;
            }
        }

        // 시계방향 90도 회전
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[sx + i][sy + j] = tempBoard[size - 1 - j][i];
            }
        }

        // 3. 참가자와 출구 위치 회전
        for (Person p : participants) {
            if (p.escaped) continue;
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


    static void init() throws IOException {
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        K = Integer.parseInt(st.nextToken());

        board = new int[N][N];
        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < N; j++) {
                board[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        participants = new ArrayList<>();
        for (int i = 0; i < M; i++) {
            st = new StringTokenizer(br.readLine());
            int x = Integer.parseInt(st.nextToken()) - 1;
            int y = Integer.parseInt(st.nextToken()) - 1;
            participants.add(new Person(i, x, y));
        }

        st = new StringTokenizer(br.readLine());
        int x = Integer.parseInt(st.nextToken()) - 1;
        int y = Integer.parseInt(st.nextToken()) - 1;
        exit = new Node(x, y);
    }

    static boolean inRange(int x, int y) {
        return x >= 0 && x < N && y >= 0 && y < N;
    }

    static class Person {
        int num, moveLength;
        int x, y;
        boolean escaped;

        public Person(int num, int x, int y) {
            this.num = num;
            this.x = x;
            this.y = y;
            this.moveLength = 0;
            this.escaped = false;
        }

		@Override
		public String toString() {
			return "Person [num=" + num + ", moveLength=" + moveLength + ", x=" + x + ", y=" + y + ", escaped="
					+ escaped + "]";
		}
        
    }

    static class Node {
        int x, y;
        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}