import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
    
    static int N, M, P, C, D;
    static Point rudolph;
    static ArrayList<Santa> santaList;
    
    public static void main(String[] args) throws IOException{
        init();
        Collections.sort(santaList, (o1, o2) -> (o1.num - o2.num));
        solution();
        bw.flush();
        bw.close();
        br.close();
    }
    
    static void solution() throws IOException{
        for (int turn = 0; turn < M; turn++) {
            if (isAllOut()) {
                break;
            }
            moveRudolph(turn);  // 루돌프의 움직임
            moveSanta(turn);    // 산타의 움직임
            getPointToLiveSanta();  // 살아있는 산타에게 1포인트 증가
        }
        
        for (Santa santa : santaList) {
            bw.write(santa.point + " ");
        }
        bw.write("\n");
    }
    
    private static boolean isAllOut() {
        for (Santa s : santaList) {
            if (s.status == Status.ALIVE || s.status == Status.STUNNED) {
                return false;
            }
        }
        return true;
    }
    
    private static void getPointToLiveSanta() {
        for (Santa santa : santaList) {
            if (santa.status == Status.OUT) {
                continue;
            }
            santa.point++;
        }
    }
    
    /**
     * 산타의 움직임
     * - 1번부터 P번까지 순서대로 진행
     * - 기절(STUNNED)이나 탈락(OUT)이면 움직일 수 없음
     * - 상, 우, 하, 좌(4방향) 중 루돌프와의 거리가 "줄어드는" 경우에만 이동 (즉, 새 거리가 원래보다 작아야 함)
     * - 이미 다른 산타가 있는 칸이나 게임판 밖이면 이동하지 않음
     */
    private static void moveSanta(int turn) {
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        
        for (int i = 0; i < P; i++) {
            Santa santa = santaList.get(i);
            if (santa.status == Status.OUT) {
                continue;
            }
            if (santa.status == Status.STUNNED) {
                if (turn < santa.stunnedTurn + 2) {
                    continue;
                }
                santa.status = Status.ALIVE;
            }
            
            int originalDistance = calcManhattenDistance(new Point(santa.x, santa.y), rudolph);
            ArrayList<int[]> candidates = new ArrayList<>();
            
            for (int d = 0; d < 4; d++) {
                int nx = santa.x + dx[d];
                int ny = santa.y + dy[d];
                if (!inRange(nx, ny)) {
                    continue;
                }
                int newDistance = calcManhattenDistance(new Point(nx, ny), rudolph);
                // 산타는 루돌프에게 더 가까워져야 하므로, 새 거리가 원래보다 작아야 함.
                if (newDistance >= originalDistance) {
                    continue;
                }
                if (isSanta(nx, ny) != null) {
                    continue;  // 다른 산타가 있는 칸이면 이동 불가
                }
                candidates.add(new int[] {d, newDistance});
            }

            if (candidates.isEmpty()) {
                continue;
            }
            
            // 후보 칸 중, 거리가 작을수록 우선, 거리가 같으면 상(0), 우(1), 하(2), 좌(3) 순
            Collections.sort(candidates, new Comparator<int[]>() {
                public int compare(int[] o1, int[] o2) {
                    if (o1[1] == o2[1]) {
                        return o1[0] - o2[0];
                    } else {
                        return o1[1] - o2[1];
                    }
                }
            });
            int bestDir = candidates.get(0)[0];
            santa.x += dx[bestDir];
            santa.y += dy[bestDir];
            
            // 만약 이동한 후 루돌프와 같은 칸이면, 산타에 의한 충돌 처리
            if (santa.x == rudolph.x && santa.y == rudolph.y) {
                collideSanta(rudolph, santa, turn, D, bestDir);
            }
        }
    }
    
    /**
     * 루돌프의 움직임
     * - 탈락하지 않은 산타 중 가장 가까운 산타를 향해 1칸 돌진
     * - 8방향(대각 포함)으로 이동하며, 산타와 충돌하는 경우 충돌 처리
     */
    static void moveRudolph(int turn) {
        int[] dx = {-1, -1, 0, 1, 1, 1, 0, -1};
        int[] dy = {0, 1, 1, 1, 0, -1, -1, -1};
        
        NearestSantaSimulation best = findNearestSanta();
        Santa targetSanta = best.santa; // 가장 가까운 산타
        int dir = best.dir;             // 해당 산타로 이동하기 위한 8방향 인덱스
        
        rudolph.x += dx[dir];
        rudolph.y += dy[dir];
        
        // 루돌프가 이동한 위치에 산타가 있다면 충돌 처리(루돌프에 의한 충돌)
        Santa santa = isSanta(rudolph.x, rudolph.y);
        if (santa != null) {
            collideRudolph(rudolph, santa, turn, C, dir);
        }
    }
    
    /**
     * 루돌프에 의한 충돌 처리
     * - 산타는 C만큼 점수를 얻고, 루돌프가 이동한 방향으로 C칸 밀려남 (8방향)
     */
    private static void collideRudolph(Point rudolph, Santa santa, int turn, int score, int dir) {
        int[] dx = {-1, -1, 0, 1, 1, 1, 0, -1};
        int[] dy = {0, 1, 1, 1, 0, -1, -1, -1};
        
        int nx = santa.x + score * dx[dir];
        int ny = santa.y + score * dy[dir];
        
        santa.point += score;
        santa.stunnedTurn = turn;
        santa.status = Status.STUNNED;
        
        if (!inRange(nx, ny)) {
            santa.status = Status.OUT;
            return;
        }
        
        Santa isAnotherSanta = isSanta(nx, ny);
        if (isAnotherSanta != null) {
            slide(santa, isAnotherSanta, dir);
        } else {
            santa.x = nx;
            santa.y = ny;
        }
    }
    
    /**
     * 산타에 의한 충돌 처리
     * - 산타는 D만큼 점수를 얻고, 자신이 이동해온 반대 방향(상, 우, 하, 좌)으로 D칸 밀려남
     */
    private static void collideSanta(Point rudolph, Santa santa, int turn, int score, int dir) {
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        
        int reverseDir = (dir + 2) % 4;
        int nx = santa.x + score * dx[reverseDir];
        int ny = santa.y + score * dy[reverseDir];
        
        santa.point += score;
        santa.status = Status.STUNNED;
        santa.stunnedTurn = turn;
        
        if (!inRange(nx, ny)) {
            santa.status = Status.OUT;
            return;
        }
        
        Santa anotherSanta = isSanta(nx, ny);
        if (anotherSanta != null) {
            slideSanta(santa, anotherSanta, reverseDir);
        } else {
            santa.x = nx;
            santa.y = ny;
        }
    }
    
    private static void slideSanta(Santa santa, Santa anotherSanta, int dir) {
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        santa.x = anotherSanta.x;
        santa.y = anotherSanta.y;
        
        int nx = anotherSanta.x + dx[dir];
        int ny = anotherSanta.y + dy[dir];
        
        if (!inRange(nx, ny)) {
            anotherSanta.status = Status.OUT;
            return;
        }
        Santa temp = isSanta(nx, ny);
        if (temp == null) {
            anotherSanta.x = nx;
            anotherSanta.y = ny;
        } else {
            slideSanta(anotherSanta, temp, dir);
        }
    }
    
    /**
     * 8방향(대각 포함) 연쇄 밀림 처리
     */
    static void slide(Santa santa, Santa targetSanta, int dir) {
        int[] dx = {-1, -1, 0, 1, 1, 1, 0, -1};
        int[] dy = {0, 1, 1, 1, 0, -1, -1, -1};
        
        santa.x = targetSanta.x;
        santa.y = targetSanta.y;
        
        int nx = targetSanta.x + dx[dir];
        int ny = targetSanta.y + dy[dir];
        
        if (!inRange(nx, ny)) {
            targetSanta.status = Status.OUT;
            return;
        }
        Santa temp = isSanta(nx, ny);
        if (temp == null) {
            targetSanta.x = nx;
            targetSanta.y = ny;
        } else {
            slide(targetSanta, temp, dir);
        }
    }
    
    private static Santa isSanta(int x, int y) {
        for (Santa santa : santaList) {
            if (santa.x == x && santa.y == y && santa.status != Status.OUT) {
                return santa;
            }
        }
        return null;
    }
    
    /**
     * 루돌프와 산타 간의 제곱 거리 계산.
     * (r1 - r2)^2 + (c1 - c2)^2
     */
    private static int calcManhattenDistance(Point point1, Point point2) {
        return (int)Math.pow((point1.x - point2.x), 2) + (int)Math.pow((point1.y - point2.y), 2);
    }
    
    static boolean inRange(int x, int y) {
        return x >= 0 && x < N && y >= 0 && y < N;
    }
    
    /**
     * 루돌프가 1칸 돌진할 때, 탈락하지 않은 산타 중 가장 가까운 산타와
     * 그 산타로 이동하기 위한 8방향 인덱스를 구한다.
     * 거리 동일시, 행(r)이 큰, 그 다음 열(c)이 큰 산타를 우선한다.
     */
    private static NearestSantaSimulation findNearestSanta() {
        int[] dx = {-1, -1, 0, 1, 1, 1, 0, -1};
        int[] dy = {0, 1, 1, 1, 0, -1, -1, -1};

        ArrayList<NearestSantaSimulation> candidates = new ArrayList<>();
        for (Santa santa : santaList) {
            if (santa.status == Status.OUT) {
                continue;
            }
            int distance = calcManhattenDistance(rudolph, new Point(santa.x, santa.y));
            candidates.add(new NearestSantaSimulation(santa, distance, 0));
        }
        if (candidates.isEmpty()) {
            return null;
        }
        
        Collections.sort(candidates, new Comparator<NearestSantaSimulation>() {
            public int compare(NearestSantaSimulation o1, NearestSantaSimulation o2) {
                if (o1.distance == o2.distance) {
                    if (o1.santa.x == o2.santa.x) {
                        return o2.santa.y - o1.santa.y;
                    } else {
                        return o2.santa.x - o1.santa.x;
                    }
                } else {
                    return o1.distance - o2.distance;
                }
            }
        });
        
        NearestSantaSimulation best = candidates.get(0);
        Santa targetSanta = best.santa;
        
        int moveX = 0, moveY = 0;
        if (targetSanta.x > rudolph.x) {
            moveX = 1;
        } else if (targetSanta.x < rudolph.x) {
            moveX = -1;
        } else {
            moveX = 0;
        }

        if (targetSanta.y > rudolph.y) {
            moveY = 1;
        } else if (targetSanta.y < rudolph.y) {
            moveY = -1;
        } else {
            moveY = 0;
        }
        
        int dir = -1;
        // 만약 루돌프와 대상 산타가 같은 칸이면 기본 0번 방향을 사용
        if (moveX == 0 && moveY == 0) {
            dir = 0;
        } else {
            for (int d = 0; d < 8; d++) {
                if (dx[d] == moveX && dy[d] == moveY) {
                    dir = d;
                    break;
                }
            }
        }
        best.dir = dir;
        return best;
    }
    
    static void init() throws IOException{
        String[] tokens = br.readLine().split(" ");
        N = Integer.parseInt(tokens[0]);
        M = Integer.parseInt(tokens[1]);
        P = Integer.parseInt(tokens[2]);
        C = Integer.parseInt(tokens[3]);
        D = Integer.parseInt(tokens[4]);
        
        tokens = br.readLine().split(" ");
        int x = Integer.parseInt(tokens[0]) - 1;
        int y = Integer.parseInt(tokens[1]) - 1;
        
        rudolph = new Point(x, y);
        
        santaList = new ArrayList<>();
        for (int i = 1; i <= P; i++) {
            tokens = br.readLine().split(" ");
            int num = Integer.parseInt(tokens[0]);
            int r = Integer.parseInt(tokens[1]) - 1;
            int c = Integer.parseInt(tokens[2]) - 1;
            Santa santa = new Santa(num, r, c, 0, -1, Status.ALIVE);
            santaList.add(santa);
        }
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
    
    static class Santa {
        int num;
        int x, y;
        int point;
        int stunnedTurn;
        Status status;
        public Santa(int num, int x, int y, int point, int stunnedTurn, Status status) {
            this.num = num;
            this.x = x;
            this.y = y;
            this.point = point;
            this.stunnedTurn = stunnedTurn;
            this.status = status;
        }
        @Override
        public String toString() {
            return "Santa [num=" + num + ", x=" + x + ", y=" + y + 
                   ", point=" + point + ", stunnedTurn=" + stunnedTurn + ", status=" + status + "]";
        }
    }
    
    static class NearestSantaSimulation {
        Santa santa;
        int distance;
        int dir;
        public NearestSantaSimulation(Santa santa, int distance, int dir) {
            this.santa = santa;
            this.distance = distance;
            this.dir = dir;
        }
    }
    
    static enum Status {
        ALIVE, STUNNED, OUT;
    }
}
