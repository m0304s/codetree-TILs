import java.util.*;
import java.io.*;

public class Main {
    public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    public static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

    static int R;
    static int C;
    static int K;

    static int [] dx = {0,0,-1,1};
    static int [] dy = {-1,1,0,0};
    static int [][] map;
    public static void main(String[] args) throws IOException{
        String [] tokens = br.readLine().split(" ");
        R = Integer.parseInt(tokens[0]);
        C = Integer.parseInt(tokens[1]);
        K = Integer.parseInt(tokens[2]);

        map = new int[R][C];

        int answer = 0;
        for(int no = 1; no<=K; no++){
            tokens = br.readLine().split(" ");
            int c = Integer.parseInt(tokens[0])-1;
            int d = Integer.parseInt(tokens[1]);

            int [] res = move(c,d,no);  //골렘 이동
            int x = res[1];
            int y = res[2];
            boolean inBoard = res[0] == 1;

            if(inBoard){
                answer += bfs(x,y,no);
            }else{
                map = new int[R][C];
            }
        }
        bw.write(answer+"\n");
        bw.flush();
    }
    public static int bfs(int sx,int sy, int num){
        boolean [][] visited = new boolean[R][C];
        List<Integer> canAnswer = new ArrayList<>();
        Queue<int[]> queue = new LinkedList<>();
        visited[sx][sy] = true;
        queue.add(new int[]{sx,sy});

        while(!queue.isEmpty()){
            int [] current = queue.poll();
            int x = current[0];
            int y = current[1];

            for(int i=0;i<4;i++){
                int nx = x + dx[i];
                int ny = y + dy[i];

                if(!inBoard(nx,ny) || visited[nx][ny] || map[nx][ny] == 0) continue;

                if(Math.abs(map[x][y]) == Math.abs(map[nx][ny]) || (map[x][y] < 0 && Math.abs(map[nx][ny]) != Math.abs(map[x][y]))){
                    visited[nx][ny] = true;
                    canAnswer.add(nx);
                    queue.add(new int[]{nx,ny});
                }
            }
        }
        Collections.sort(canAnswer, Collections.reverseOrder());
        return canAnswer.get(0) + 1;
    }

    public static int [] move(int c,int d, int num){
        int x = -2;
        int y = c;

        while(true){
            if(check(x+2,y) && check(x+1,y-1) && check(x+1,y+1)){    //아래로 이동
                x+=1;
            }else if(check(x-1,y-1) && check(x,y-2) && check(x+1,y-1) && check(x+1,y-2) && check(x+2,y-1)){     //왼쪽 이동
                x+=1;
                y-=1;
                d = (d-1+4)%4;
            }else if(check(x-1,y+1) && check(x,y+2) && check(x+1,y+1) && check(x+2,y+1) && check(x+1,y+2)){     //오른쪽 이동
                x+=1;
                y+=1;
                d = (d+1)%4;
            }else{          //골렘의 일부가 밖으로 나갔을 경우
                break;
            }
        }

        if(!inBoard(x,y) || !inBoard(x-1,y) || !inBoard(x,y-1) || !inBoard(x,y+1) || !inBoard(x+1,y)){
            return new int[]{0,-1,-1};
        }else{
            map[x][y] = map[x-1][y] = map[x][y-1] = map[x+1][y] = map[x][y+1] = num;
            int [] exit = getExit(x,y,d);
            int ex = exit[0];
            int ey = exit[1];
            map[ex][ey] = -num;
            return new int[]{1,x,y};
        }
    }

    public static boolean check(int x,int y){   //골렘이 이동 가능한지 확인
        if(!inBoard(x,y)){
            if(x<R && y>=0 && y<C){
                return true;
            }
        }else{  //숲 내부
            if(map[x][y] == 0){
                return true;
            }
        }
        return false;
    }
    
    public static boolean inBoard(int x,int y){
        if(x>=0 && x<R && y>=0 && y<C){
            return true;
        }
        return false;
    }

    public static int [] getExit(int x,int y,int d){
        switch(d){
            case 0: //북
                return new int[]{x-1,y};
            case 1: //동
                return new int[]{x,y+1};
            case 2: //남
                return new int[]{x+1,y};
            case 3: //서
                return new int[]{x,y-1};
            default:
                return null;
        }
    }
}