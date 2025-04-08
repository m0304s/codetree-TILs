import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int VIRUS = 5;

    static int N,M,F;
    static ArrayList<TimeMorphy> timeMorphies;
    static int [][] unknownMap;
    static int [][][] timeWall;

    public static void main(String[] args) throws IOException{
        init();
//        debug();
        int firstTime = phase1();
        int secondTime = phase2(firstTime);

        if (firstTime == -1 || secondTime == -1) {
            System.out.println(-1);
        }else{
            System.out.println(firstTime+secondTime);
        }
    }

    static int phase2(int firstTime) {
        //firstTime까지 각 시간 이상 현상 전파
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        // 시간 1부터 firstTime까지 시뮬레이션
        for (int time = 1; time <= firstTime; time++) {
            for (TimeMorphy tm : timeMorphies) {
                if (!tm.status)
                    continue;           // 이미 사망
                if (time % tm.v != 0)
                    continue;     // 이동 주기가 아님

                int nx = tm.x + dx[tm.d];
                int ny = tm.y + dy[tm.d];

                // 범위 벗어나거나, 벽(1) 혹은 시간의 벽(3) 만나면 사망
                if (!inRangeAtUnknownMap(nx, ny)
                        || unknownMap[nx][ny] != 0) {
                    tm.status = false;
                } else {
                    // 안전한 빈 칸(0)이면 이동
                    tm.x = nx;
                    tm.y = ny;
                    unknownMap[nx][ny] = VIRUS;
                }
            }
        }

        //시간 이상 현상이 각 시간별로 확산되고, 이후 타임머신을 최단 경로로 1칸 이동
        Node startPoint = null;
        for(int i=0;i<N;i++){
            for(int j=0;j<N;j++){
                if(unknownMap[i][j] == 3){
                    startPoint = findStartPointAtPhase2(i,j);
                }
            }
        }

        Node endPoint = findEndPointAtUnknownMap();
        int minTime = simulateAndBFS(startPoint,endPoint, firstTime);
        return minTime+1;
    }

    static Node findStartPointAtPhase2(int x, int y) {
        //동 서 남 북
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        Node startPoint = new Node(x, y);

        boolean[][] visited = new boolean[N][N];
        Queue<Node> queue = new ArrayDeque<>();
        visited[x][y] = true;
        queue.add(startPoint);

        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            for (int d = 0; d < 4; d++) {
                int nx = cur.x + dx[d];
                int ny = cur.y + dy[d];
                if (!inRangeAtUnknownMap(nx, ny) || visited[nx][ny])
                    continue;

                if (unknownMap[nx][ny] == 0) {
                    return new Node(nx,ny);
                }
                if (unknownMap[nx][ny] == 3) {
                    visited[nx][ny] = true;
                    queue.add(new Node(nx, ny));
                }
            }
        }

        return null;
    }

    static int simulateAndBFS(Node startPoint, Node endPoint, int startTime){
        Queue<Node> queue = new ArrayDeque<>();
        if(startPoint == null) return -1;
        queue.add(startPoint);

        // 방문 여부 체크 (N x N)
        boolean[][] visited = new boolean[N][N];
        visited[startPoint.x][startPoint.y] = true;

        //동 서 남 북
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        int t = startTime;

        while (t < 10000) {
            t++;

            for (TimeMorphy tm : timeMorphies) {
                if (!tm.status) continue;               // 이미 사망한 경우 건너뜀
                if (t % tm.v != 0) continue;            // 이동 주기가 아니면 건너뜀

                int nx = tm.x + dx[tm.d];
                int ny = tm.y + dy[tm.d];

                // 이동 가능 여부 확인
                // (범위 내 && unknownMap[nx][ny] == 0)
                if (!inRangeAtUnknownMap(nx, ny) || unknownMap[nx][ny] != 0) {
                    tm.status = false; // 사망 처리
                } else {
                    // 정상적으로 이동
                    tm.x = nx;
                    tm.y = ny;
                    // 이동한 칸을 VIRUS로 표시
                    unknownMap[nx][ny] = VIRUS;
                }
            }

            /* 2) BFS 확장 */
            // 이번 시간에 확장할 노드 수만큼만 처리(동시에 이동)
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                Node cur = queue.poll();

                // 만약 도착 지점이면 현재 시점(t)에 도달한 것
                if (cur.x == endPoint.x && cur.y == endPoint.y) {
                    return t - 1;
                }

                // 상하좌우로 확장
                for (int d = 0; d < 4; d++) {
                    int nx = cur.x + dx[d];
                    int ny = cur.y + dy[d];

                    if (!inRangeAtUnknownMap(nx, ny) || visited[nx][ny])
                        continue;

                    // 이동할 칸이 "시간 이상(=5)"이면 이동 불가
                    // (도착점(4)은 예외적으로 이동 허용)
                    if (unknownMap[nx][ny] == VIRUS) {
                        continue;
                    }

                    // 이동할 칸이 벽(1)이거나 시간벽(3)이면 이동 불가
                    if (unknownMap[nx][ny] == 1 || unknownMap[nx][ny] == 3) {
                        continue;
                    }

                    // 도착점이거나, 빈 칸(0)이면 이동
                    if (unknownMap[nx][ny] == 4 || unknownMap[nx][ny] == 0) {
                        visited[nx][ny] = true;
                        queue.add(new Node(nx, ny));
                    }
                }
            }

            // 현재 시점에 큐가 비었다면 더 이상 진행할 수 없으므로 break
            if (queue.isEmpty()) {
                break;
            }
        }

        // 탈출 불가
        return -1;
    }


    private static Node findEndPointAtUnknownMap() {
        for(int i=0;i<N;i++){
            for(int j=0;j<N;j++){
                if(unknownMap[i][j] == 4){
                    return new Node(i,j);
                }
            }
        }
        return null;
    }

    //시간의 벽에서 탈출구까지의 최단 경로 계산
    static int phase1(){
        //타임머신 출발 위치 체크
        State timeMachine = findStartPoint();
        State endPoint = findEndPoint();
        if(endPoint == null) return -1;
        int minTime = move(timeMachine,endPoint);
        return minTime;
    }

    /**
     * 3차원 공간에서 bfs를 통해 endPoint로 도달하는 최적의 경로 계산
     * 각 면을 넘어가는 기준이 중요함
     */
    static int move(State timeMachine, State endPoint){
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        boolean [][][] visited = new boolean[5][M][M];
        Queue<State> queue = new ArrayDeque<>();

        visited[timeMachine.face][timeMachine.point.x][timeMachine.point.y] = true;
        queue.add(timeMachine);

        while(!queue.isEmpty()){
            State curState = queue.poll();

            if(curState.face == endPoint.face && curState.point.x == endPoint.point.x && curState.point.y == endPoint.point.y){
                return curState.time;
            }

            //동 서 남 북 순으로 탐색
            for(int d=0;d<4;d++){
                int nx = curState.point.x + dx[d];
                int ny = curState.point.y + dy[d];
                int face = curState.face;
                if(!inRangeAtTimeWall(nx,ny)){
                    State changedPoint = changePoint(curState.point.x,curState.point.y,curState.face, d);
                    nx = changedPoint.point.x;
                    ny = changedPoint.point.y;
                    face = changedPoint.face;
                }
                if(visited[face][nx][ny] || timeWall[face][nx][ny] == 1) continue;

                visited[face][nx][ny] = true;
                queue.add(new State(face,curState.time+1,nx,ny));
            }
        }
        return -1;
    }

    /**
     * 시간의 벽을 넘은 좌표에 대해 변환된 좌표 값을 반환
     * @param x     : 초기 x 좌표 (0..M-1)
     * @param y     : 초기 y 좌표 (0..M-1)
     * @param face  : 현재 면 번호 (0:동,1:서,2:남,3:북,4:윗면)
     * @param d     : 이동한 방향 (0:동,1:서,2:남,3:북)
     * @return 변환된 좌표 값 (새 면, 새 x, 새 y)
     */
    private static State changePoint(int x, int y, int face, int d) {
        int newFace = 0;
        int nx = 0;
        int ny = 0;

        switch (face) {
            case 0: // ─ 동쪽 면
                if (d == 0) {
                    newFace = 3; ny = 0; nx = x;
                } else if (d == 1) {
                    newFace = 2; nx = 0; ny = (M-1);
                } else if (d == 2) {

                } else {
                    newFace = 4; nx = (M-1) - x; ny = (M-1);
                }
                break;

            case 1: // ─ 서쪽 면
                if (d == 0) {
                    newFace = 2; nx = x;    ny = 0;
                } else if (d == 1) {
                    newFace = 3; nx = x; ny = M-1;
                } else if (d == 2) {

                } else {
                    newFace = 4; nx = y; ny = x;
                }
                break;

            case 2: // ─ 남쪽 면
                if (d == 0) {
                    newFace = 0; ny = 0; nx = x;
                } else if (d == 1) {
                    newFace = 1; nx = x; ny = M-1;
                } else if (d == 2) {

                } else {
                    newFace = 4; nx = M-1; ny = y;
                }
                break;

            case 3: // ─ 북쪽 면
                if (d == 0) {
                    newFace = 1; nx = x; ny = 0;
                } else if (d == 1) {
                    newFace = 0; nx = x; ny = M-1;
                } else if (d == 2) {

                } else {
                    newFace = 4; nx = 0; ny = (M-1) - y;
                }
                break;

            case 4:
                nx = 0;
                if (d == 0) {
                    newFace = 0; nx = (M-1) - y;
                } else if (d == 1) {
                    newFace = 1; ny = y;
                } else if (d == 2) {
                    newFace = 2; ny = y;
                } else {
                    newFace = 3; ny = (M-1) - y;
                }
                break;

            default:
                throw new IllegalArgumentException("잘못된 face: " + face);
        }

        return new State(newFace, 0, nx, ny);
    }


    private static boolean inRangeAtTimeWall(int x, int y) {
        return x >= 0 && x < M && y >= 0 && y < M;
    }

    static State findEndPoint(){
        //3차원 공간에서의 탈출 지점 계산
        Out : for(int i=0;i<N;i++){
            for(int j=0;j<N;j++){
                if(unknownMap[i][j] == 3){  //시간의 벽을 만난 경우 bfs를 통해 탈출구 계산
                    return bfs(i,j);
                }
            }
        }
        return null;
    }

    static State bfs(int x, int y) {
        //동 서 남 북
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        Node startPoint = new Node(x, y);
        Node prev = null;
        int dir = -1;

        boolean[][] visited = new boolean[N][N];
        Queue<Node> queue = new ArrayDeque<>();
        visited[x][y] = true;
        queue.add(startPoint);

        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            for (int d = 0; d < 4; d++) {
                int nx = cur.x + dx[d];
                int ny = cur.y + dy[d];
                if (!inRangeAtUnknownMap(nx, ny) || visited[nx][ny]) continue;

                if (unknownMap[nx][ny] == 0) {
                    prev = cur;
                    dir = d;
                    queue.clear();
                    break;
                }
                if (unknownMap[nx][ny] == 3) {
                    visited[nx][ny] = true;
                    queue.add(new Node(nx, ny));
                }
            }
        }

        if (prev == null) {
            return null;
        }

        int relX = prev.x - startPoint.x;
        int relY = prev.y - startPoint.y;

        int endX=0, endY=0, face=0;
        switch (dir) {
            case 0: // 동
                face = 0;
                endX = M - 1;
                endY = (M - 1) - relX;
                break;
            case 1: // 서
                face = 1;
                endX = M - 1;
                endY = relX;
                break;
            case 2: // 남
                face = 2;
                endX = M - 1;
                endY = relY;
                break;
            case 3: // 북
                face = 3;
                endX = M-1;
                endY = (M - 1) - relY;
                break;
            default:
                throw new IllegalStateException("알 수 없는 방향: " + dir);
        }

        State result = new State(face,0, endX, endY);
        return result;
    }


    static boolean inRangeAtUnknownMap(int x,int y){
        return x >= 0 && x < N && y >= 0 && y < N;
    }

    static State findStartPoint(){
        //초기 타임머신은 시간의 벽의 윗면 단면도에 위치
        int timeMachineX = 0;
        int timeMachineY = 0;
        Outer: for(int i=0;i<M;i++){
            for(int j=0;j<M;j++){
                if(timeWall[4][i][j] == 2){
                    timeMachineX = i;
                    timeMachineY = j;
                    break Outer;
                }
            }
        }

        return new State(4,0,timeMachineX,timeMachineY);
    }

    static void init() throws IOException{
        String [] tokens = br.readLine().split(" ");
        N = Integer.parseInt(tokens[0]);
        M = Integer.parseInt(tokens[1]);
        F = Integer.parseInt(tokens[2]);
        timeMorphies = new ArrayList<>();

        unknownMap = new int[N][N];
        for(int i=0;i<N;i++){
            tokens = br.readLine().split(" ");
            for(int j=0;j<N;j++){
                unknownMap[i][j] = Integer.parseInt(tokens[j]);
            }
        }

        timeWall = new int[5][M][M];    // 0: 동 1: 서 2: 남 3: 북
        for(int i=0;i<5;i++){
            for(int x=0;x<M;x++){
                tokens = br.readLine().split(" ");
                for(int y=0;y<M;y++){
                    timeWall[i][x][y] = Integer.parseInt(tokens[y]);
                }
            }
        }

        for(int i=0;i<F;i++){
            tokens = br.readLine().split(" ");
            int r = Integer.parseInt(tokens[0]);
            int c = Integer.parseInt(tokens[1]);
            int d = Integer.parseInt(tokens[2]);
            int v = Integer.parseInt(tokens[3]);

            unknownMap[r][c] = VIRUS;
            TimeMorphy morphy = new TimeMorphy(r,c,d,v,true);
            timeMorphies.add(morphy);
        }
    }

    static class TimeMorphy{
        int x,y;
        int d;
        int v;
        boolean status;

        public TimeMorphy(int x, int y, int d, int v, boolean status) {
            this.x = x;
            this.y = y;
            this.d = d;
            this.v = v;
            this.status = status;
        }
    }

    static class State{
        int face,time;
        Node point;
        public State(int face, int time, int x,int y){
            this.face = face;
            this.point = new Node(x,y);
            this.time = time;
        }

        @Override
        public String toString() {
            return "State{" +
                    "face=" + face +
                    ", point=" + point +
                    '}';
        }
    }

    static class Node{
        @Override
        public String toString() {
            return "Node{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }

        int x,y;
        public Node(int x,int y){
            this.x = x;
            this.y = y;
        }
    }
}
