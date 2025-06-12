import java.util.*;
import java.io.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

    /**
     * N*N 크기의 격자에서 꼬리잡기놀이 진행
     * 3명 이상이 한 팀으로 구성
     *
     * 맨 앞에 있는 사람을 머리사람
     * 맨 뒤에 있는 사람을 꼬리사람
     *
     * 각 팀은 주어진 이동 선을 따라서만 이동
     * 각 팀의 이동선은 끝이 이어져있음
     *
     * 각 라운드 별 다음과 같이 진행
     * 1. 머리사람을 따라서 한 칸 이동
     * 2. 각 라운드마다 공이 정해진 선을 따라 던져짐
     * 3. 공이 던져지는 경우에 해당 선에 사람이 있으면 최초에 만나게 되는 사람만이 공을 얻어 점수를 얻음
     * 점수는 머리사람을 시작으로 팀 내에서 K번째 사람이라면 K의 제곱만큼 점수를 얻음
     * 아무도 공을 받지 못하는 경우 점수를 획득하지 못함
     *
     * 입력
     * 첫번째줄 격자의 크기(N), 팀의 개수(M), 라운드 수(K)
     *
     * N개의 줄에 걸쳐 초기 상태의 정보 (빈칸(0), 머리사람(1), 머리사람과 꼬리사람이 아닌 나머지(2), 꼬리사람(3), 이동선(4)
     */

    static final int BLANK = 0, HEAD = 1, REMAIN = 2, TAIL = 3, MOVABLE = 4;

    static int N,M,K;
    static int [][] map;
    static ArrayList<Team> teamList;

    public static void main(String[] args) throws IOException {
        init();
        simulation();
    }

    static void simulation() throws IOException{
        int totalScore = 0;
        for(int turn=1;turn<=K;turn++){
            moveMembersPerTeam(turn);
            int score = throwBall(turn);
            totalScore += score;
        }
        bw.write(totalScore+"\n");
        bw.flush();
        bw.close();
        br.close();
    }

    /**
     * N: 격자의 한 변 크기
     * turn: 1 이상인 자연수(몇 번째 턴인지)r
     */
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
        while(true){
            if(!inRange(startX,startY)) break;  //벽밖을 만나면 종료

            Team catchedTeam = null;
            Member catchedMember = isMemberIsAtPoint(startX,startY);
            if(isMemberIsAtPoint(startX,startY) != null){   //사람을 만났을 경우
                for(Team team : teamList){
                    if(isContain(team,catchedMember)){
                        catchedTeam = team;
                        break;
                    }
                }

                if(catchedTeam != null){
                    //그 팀을 반대로 전환, (머리 -> 꼬리, 꼬리 -> 머리, 각 팀원 num : team.members.size() - member.num
                    // 1->3, 2->2, 3->1
                    ArrayList<Member> newMembers = new ArrayList<>();
                    for(Member member : catchedTeam.members) {
                        int changedNum = catchedTeam.members.size() - member.num + 1;
                        if(member.role == HEAD) {
                            newMembers.add(new Member(changedNum,member.x,member.y, TAIL));
                            map[member.x][member.y] = TAIL;
                        }else if(member.role == TAIL){
                            newMembers.add(new Member(changedNum,member.x,member.y, HEAD));
                            map[member.x][member.y] = HEAD;
                        }else {
                            newMembers.add(new Member(changedNum,member.x,member.y, REMAIN));
                        }
                    }
                    Collections.sort(newMembers, new Comparator<Member>() {
                        @Override
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
        for(Member member : team.members){
            if(member.x == catchedMember.x && member.y == catchedMember.y && member.role == catchedMember.role && member.num == catchedMember.num) return true;
        }

        return false;
    }

    /**
     * 각 팀별로 팀원들의 위치를 이동시킴
     */
    private static void moveMembersPerTeam(int turn) {
        for(Team team : teamList){
            moveMembers(team, turn);
        }
    }

    static void printMap(){
        for(int i=0;i<N;i++){
            for(int j=0;j<N;j++){
                System.out.print(map[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("=======================");
    }


    static void moveMembers(Team team, int turn) {
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};

        HashMap<Integer, Member> memberHashMap = new HashMap<>();
        for (Member member : team.members) {
            memberHashMap.put(member.num-1, new Member(member.num,member.x,member.y,member.role));
            map[member.x][member.y] = MOVABLE;
        }

        Member head = team.members.get(0);
        boolean headMoved = false;
        ArrayList<Member> newMember = new ArrayList<>();
        for (int d = 0; d < 4; d++) {
            int nx = head.x + dx[d];
            int ny = head.y + dy[d];
            if (!inRange(nx, ny)) continue;
            if (map[nx][ny] != MOVABLE || isMemberIsAtPoint(nx, ny) != null) continue;

            newMember.add(new Member(head.num,nx,ny,head.role));
            map[nx][ny] = head.role;
            headMoved = true;
            break;
        }

        if (!headMoved) {
            Member tail = findTail(team);

            newMember.add(new Member(head.num, tail.x, tail.y, head.role));
            map[tail.x][tail.y] = head.role;
        }

        Collections.sort(team.members, new Comparator<Member>() {
            @Override
            public int compare(Member o1, Member o2) {
                return o1.num - o2.num;
            }
        });
        for (int i = 1; i < team.members.size(); i++) {
            Member member = team.members.get(i);
            Member prevPos = memberHashMap.get(i - 1);

            newMember.add(new Member(member.num, prevPos.x, prevPos.y, member.role));

            map[prevPos.x][prevPos.y] = member.role;
        }
        team.members = newMember;
    }

    private static Member findTail(Team team) {
        for(Member member : team.members){
            if(member.role == TAIL) return member;
        }
        return null;
    }

    /**
     * 목표로 하는 좌표에 사람이 존재하는지 체크
     * @param x 좌표
     * @param y 좌표
     * @return 사람이 존재하면 그 사람 반환, 없으면 null
     */
    static Member isMemberIsAtPoint(int x,int y){
        for(Team team : teamList){
            for(Member member : team.members){
                if(x == member.x && y == member.y){
                    return member;
                }
            }
        }
        return null;
    }

    /**
     * 머리사람 시작으로 팀내 K번째 사람
     * return (K^2)
     */
    static int getScore(Member member){
        return (int)Math.pow(member.num,2);
    }

    static void init() throws IOException{
        String [] tokens = br.readLine().split(" ");
        N = Integer.parseInt(tokens[0]);
        M = Integer.parseInt(tokens[1]);
        K = Integer.parseInt(tokens[2]);

        map = new int[N][N];
        for(int i=0;i<N;i++){
            tokens = br.readLine().split(" ");
            for(int j=0;j<N;j++){
                map[i][j] = Integer.parseInt(tokens[j]);
            }
        }

        findTeams();    //팀 검색
    }

    static void findTeams(){
        teamList = new ArrayList<>();
        for(int i=0;i<N;i++){
            for(int j=0;j<N;j++){
                if(map[i][j] == HEAD){
                    Team team = bfsToFindTeam(i,j);
                    teamList.add(team);
                }
            }
        }
    }

    private static Team bfsToFindTeam(int x, int y) {
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};

        ArrayList<Member> members = new ArrayList<>();
        Queue<Point> queue = new ArrayDeque<>();
        boolean[][] visited = new boolean[N][N];
        queue.add(new Point(x, y));
        visited[x][y] = true;

        int num = 1;
        while (!queue.isEmpty()) {
            Point curNode = queue.poll();
            int nodeValue = map[curNode.x][curNode.y];
            // 팀 구성원(HEAD, REMAIN, TAIL)만 members에 추가
            if (nodeValue == HEAD || nodeValue == REMAIN || nodeValue == TAIL) {
                members.add(new Member(num++, curNode.x, curNode.y, nodeValue));
            }

            for (int d = 0; d < 4; d++) {
                int nx = curNode.x + dx[d];
                int ny = curNode.y + dy[d];

                if (!inRange(nx, ny) || visited[nx][ny]) continue;
                int newNodeValue = map[nx][ny];

                if (nodeValue == HEAD) { // 1일 때: 반드시 2로 이어짐
                    if (newNodeValue == REMAIN) {
                        queue.add(new Point(nx, ny));
                        visited[nx][ny] = true;
                    }
                } else if (nodeValue == REMAIN) { // 2일 때: 2 또는 3으로 이어짐
                    if (newNodeValue == REMAIN || newNodeValue == TAIL) {
                        queue.add(new Point(nx, ny));
                        visited[nx][ny] = true;
                    }
                } else if (nodeValue == TAIL) { // 3일 때: 4 또는 1로 이어짐
                    if (newNodeValue == MOVABLE || newNodeValue == HEAD) {
                        queue.add(new Point(nx, ny));
                        visited[nx][ny] = true;
                    }
                }
            }
        }

        return new Team(members);
    }

    static boolean inRange(int x,int y){
        return x >= 0 && x < N && y >= 0 && y < N;
    }

    static class Team{
        ArrayList<Member> members;
        HashSet<Member> memberSet;

        public Team(ArrayList<Member> members) {
            this.members = members;
            this.memberSet = new HashSet<>(members);
        }

        @Override
        public String toString() {
            return "Team{" +
                    "members=" + members +
                    '}';
        }
    }

    static class Point{
        int x,y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    static class Member{
        int x,y;
        int role;
        int num;


        public Member(int num,int x, int y, int role) {
            this.x = x;
            this.y = y;
            this.num = num;
            this.role = role;
        }

        @Override
        public String toString() {
            return "Member{" +
                    "x=" + x +
                    ", y=" + y +
                    ", role=" + role +
                    ", num=" + num +
                    '}';
        }
    }
}
