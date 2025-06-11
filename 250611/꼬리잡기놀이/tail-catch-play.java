import java.io.*;
import java.util.*;

public class Main {
    static final int BLANK  = 0;
    static final int HEAD   = 1;
    static final int PERSON = 2;
    static final int TAIL   = 3;
    static final int ROAD   = 4;

    static int N, M, K;
    static int[][] map;
    static long totalScore;
    static List<Team> teamList;
    static int[] dx = { 0,  0, -1, 1 };
    static int[] dy = {-1,  1,  0, 0 };

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

    /** 입력 읽고, 팀 정보 구성 */
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

        // HEAD 위치마다 순서대로 팀 구성
        teamList = new ArrayList<>();
        boolean[][] visited = new boolean[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (map[i][j] == HEAD && !visited[i][j]) {
                    List<People> members = findMembers(i, j, visited);
                    teamList.add(new Team(members));
                }
            }
        }

        // 검증 (선택)
        // if (teamList.size() != M) throw new IllegalStateException("팀 개수 불일치");
    }

    /** HEAD부터 PERSON → … → TAIL 순서대로 궤적을 따라가며 People 리스트 생성 */
    static List<People> findMembers(int sx, int sy, boolean[][] visited) {
        List<People> members = new ArrayList<>();
        int x = sx, y = sy;
        members.add(new People(x, y));
        visited[x][y] = true;

        while (map[x][y] != TAIL) {
            boolean moved = false;
            for (int d = 0; d < 4; d++) {
                int nx = x + dx[d], ny = y + dy[d];
                if (!inRange(nx, ny) || visited[nx][ny]) continue;
                int v = map[nx][ny];
                if (v == PERSON || v == TAIL) {
                    x = nx; y = ny;
                    members.add(new People(x, y));
                    visited[x][y] = true;
                    moved = true;
                    break;
                }
            }
            if (!moved) break;  // 혹시 경로가 끊기면 멈춤
        }
        return members;
    }

    /** K 라운드 동안 팀 이동 → 공 던지기 */
    static void simulate() {
        for (int round = 1; round <= K; round++) {
            moveAllTeams();
            totalScore += throwBall(round);
        }
    }

    /** 모든 팀을 머리 기준으로 한 칸씩 이동 */
    static void moveAllTeams() {
        for (Team team : teamList) {
            // 1) 새 머리 위치 찾기
            People head = team.peoples.get(0);
            Node newHead = findMovablePoint(head);
            if (newHead == null) continue;

            // 2) 기존 위치 모두 ROAD 처리
            for (People p : team.peoples) {
                map[p.point.x][p.point.y] = ROAD;
            }

            // 3) 이전 좌표 복사
            Node[] prev = new Node[team.peoples.size()];
            for (int i = 0; i < team.peoples.size(); i++) {
                Node cur = team.peoples.get(i).point;
                prev[i] = new Node(cur.x, cur.y);
            }

            // 4) 꼬리→몸통 순으로 따라가기
            for (int i = team.peoples.size() - 1; i >= 1; i--) {
                team.peoples.get(i).point = prev[i - 1];
            }
            // 5) 머리 이동
            head.point = newHead;

            // 6) HEAD, PERSON, TAIL 재마킹
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

    /** 머리가 이동할 수 있는 ROAD 또는 TAIL 칸을 상하좌우 탐색 */
    static Node findMovablePoint(People head) {
        for (int d = 0; d < 4; d++) {
            int nx = head.point.x + dx[d];
            int ny = head.point.y + dy[d];
            if (!inRange(nx, ny)) continue;
            if (map[nx][ny] == ROAD || map[nx][ny] == TAIL) {
                return new Node(nx, ny);
            }
        }
        return null;
    }

    /** 공 던지기: 한 변 N칸씩, 4방향 순환 */ 
    static int throwBall(int turn) {
        int cycleLen = 4 * N;
        int changed = (turn - 1) % cycleLen;
        int dir   = changed / N;      // 0: →, 1: ↑, 2: ←, 3: ↓
        int step  = changed % N;

        int sx, sy, mx, my;
        switch (dir) {
            case 0: sx = step;     sy = 0;      mx = 0;  my = 1;  break;  // 아래→오른쪽
            case 1: sx = N - 1;    sy = step;   mx = -1; my = 0;  break;  // 오른쪽→위
            case 2: sx = N - 1 - step; sy = N - 1; mx = 0;  my = -1; break; // 위→왼쪽
            default: sx = 0;       sy = N - 1 - step; mx = 1;  my = 0;  break; // 왼쪽→아래
        }

        int x = sx, y = sy;
        while (inRange(x, y)) {
            int v = map[x][y];
            if (v == HEAD || v == PERSON || v == TAIL) {
                return checkScore(x, y);
            }
            x += mx;
            y += my;
        }
        return 0;
    }

    /** 맞힌 지점의 팀원을 찾아 점수 계산하고, 팀의 방향을 뒤집음 */
    static int checkScore(int x, int y) {
        // 해당 좌표가 속한 팀 찾기
        Team hit = null;
        for (Team team : teamList) {
            for (People p : team.peoples) {
                if (p.point.x == x && p.point.y == y) {
                    hit = team;
                    break;
                }
            }
            if (hit != null) break;
        }
        if (hit == null) return 0;

        // 몇 번째 사람인지 계산 (1-based)
        int idx = 0;
        for (int i = 0; i < hit.peoples.size(); i++) {
            People p = hit.peoples.get(i);
            if (p.point.x == x && p.point.y == y) {
                idx = i + 1;
                break;
            }
        }
        int score = idx * idx;

        // 팀 방향 뒤집기
        Collections.reverse(hit.peoples);
        // 맵에 새 HEAD/TAIL/몸통 마킹
        for (int i = 0; i < hit.peoples.size(); i++) {
            Node pt = hit.peoples.get(i).point;
            if (i == 0) {
                map[pt.x][pt.y] = HEAD;
            } else if (i == hit.peoples.size() - 1) {
                map[pt.x][pt.y] = TAIL;
            } else {
                map[pt.x][pt.y] = PERSON;
            }
        }
        return score;
    }

    static boolean inRange(int x, int y) {
        return x >= 0 && x < N && y >= 0 && y < N;
    }

    static class People {
        Node point;
        People(int x, int y) { this.point = new Node(x, y); }
        @Override public String toString() {
            return "("+ point.x +","+ point.y +")";
        }
    }

    static class Node {
        int x, y;
        Node(int x, int y) { this.x = x; this.y = y; }
    }

    static class Team {
        List<People> peoples;
        Team(List<People> list) { this.peoples = list; }
    }
}