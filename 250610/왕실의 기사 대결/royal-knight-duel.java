import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
    
    static final int BLANK = 0, TRAP = 1, WALL = 2;
    static final int[] dx = {-1, 0, 1, 0};
    static final int[] dy = {0, 1, 0, -1};
    
    static int L, N, Q;
    static int[][] map;
    static List<Soldier> soldiers;
    static List<Command> commands;
    
    public static void main(String[] args) throws IOException {
        init();
        simulation();
        printResult();
    }
    
    private static void printResult() throws IOException {
        int totalDamage = 0;
        for (Soldier s : soldiers) {
            totalDamage += (s.k - s.currentHealth);
        }
        bw.write(String.valueOf(totalDamage));
        bw.newLine();
        bw.flush();
        bw.close();
        br.close();
    }
    
    static void simulation() {
        for (Command c : commands) {
            Soldier soldier = getSoldier(c.i);
            if (soldier == null) continue;
            int dir = c.d;
            
            if (!canMove(soldier, dir)) continue;
            
            List<Soldier> movedSoldiers = new ArrayList<>();
            move(soldier, dir, movedSoldiers, new HashSet<>());
            
            // 이동 처리
            for (Soldier s : movedSoldiers) {
                s.r += dx[dir];
                s.c += dy[dir];
            }
            
            // 피해 처리
            for (Soldier s : movedSoldiers) {
                if (s.id == soldier.id) continue;
                int damage = 0;
                for (Node n : getRange(s)) {
                    if (map[n.x][n.y] == TRAP) damage++;
                }
                s.currentHealth -= damage;
            }
            
            // 사망자 제거
            soldiers.removeIf(s -> s.currentHealth <= 0);
        }
    }
    
    private static void move(Soldier soldier, int dir, List<Soldier> movedSoldiers, Set<Integer> visited) {
        if (visited.contains(soldier.id)) return;
        visited.add(soldier.id);
        
        int nx = soldier.r + dx[dir];
        int ny = soldier.c + dy[dir];
        Soldier temp = new Soldier(soldier.id, nx, ny, soldier.h, soldier.w, soldier.k);
        List<Node> newRange = getRange(temp);
        // 벽 및 범위 확인
        for (Node n : newRange) {
            if (!inRange(n.x, n.y) || map[n.x][n.y] == WALL) return;
        }
        // 겹침 확인 및 연쇄 밀기
        for (Soldier s : soldiers) {
            if (s.id == soldier.id) continue;
            for (Node n : newRange) {
                if (belongs(n, getRange(s))) {
                    move(s, dir, movedSoldiers, visited);
                    break;
                }
            }
        }
        movedSoldiers.add(soldier);
    }
    
    private static boolean canMove(Soldier soldier, int dir) {
        if (soldier == null) return false;
        int nx = soldier.r + dx[dir];
        int ny = soldier.c + dy[dir];
        Soldier temp = new Soldier(soldier.id, nx, ny, soldier.h, soldier.w, soldier.k);
        List<Node> newRange = getRange(temp);
        for (Node n : newRange) {
            if (!inRange(n.x, n.y) || map[n.x][n.y] == WALL) return false;
        }
        for (Soldier s : soldiers) {
            if (s.id == soldier.id) continue;
            for (Node n : newRange) {
                if (belongs(n, getRange(s))) {
                    if (!canMove(s, dir)) return false;
                    break;
                }
            }
        }
        return true;
    }
    
    private static boolean belongs(Node n, List<Node> range) {
        for (Node m : range) {
            if (m.x == n.x && m.y == n.y) return true;
        }
        return false;
    }
    
    static void init() throws IOException {
        String[] tokens = br.readLine().split(" ");
        L = Integer.parseInt(tokens[0]);
        N = Integer.parseInt(tokens[1]);
        Q = Integer.parseInt(tokens[2]);
        map = new int[L][L];
        for (int i = 0; i < L; i++) {
            tokens = br.readLine().split(" ");
            for (int j = 0; j < L; j++) {
                map[i][j] = Integer.parseInt(tokens[j]);
            }
        }
        soldiers = new ArrayList<>();
        commands = new ArrayList<>();
        for (int i = 1; i <= N; i++) {
            tokens = br.readLine().split(" ");
            int r = Integer.parseInt(tokens[0]) - 1;
            int c = Integer.parseInt(tokens[1]) - 1;
            int h = Integer.parseInt(tokens[2]);
            int w = Integer.parseInt(tokens[3]);
            int k = Integer.parseInt(tokens[4]);
            soldiers.add(new Soldier(i, r, c, h, w, k));
        }
        for (int x = 0; x < Q; x++) {
            tokens = br.readLine().split(" ");
            int i = Integer.parseInt(tokens[0]);
            int d = Integer.parseInt(tokens[1]);
            commands.add(new Command(i, d));
        }
    }
    
    static boolean inRange(int x, int y) {
        return x >= 0 && x < L && y >= 0 && y < L;
    }
    
    private static Soldier getSoldier(int id) {
        for (Soldier s : soldiers) {
            if (s.id == id) return s;
        }
        return null;
    }
    
    static List<Node> getRange(Soldier s) {
        List<Node> range = new ArrayList<>();
        for (int i = s.r; i < s.r + s.h; i++) {
            for (int j = s.c; j < s.c + s.w; j++) {
                range.add(new Node(i, j));
            }
        }
        return range;
    }
    
    static class Soldier {
        int id, r, c, h, w, k, currentHealth;
        Soldier(int id, int r, int c, int h, int w, int k) {
            this.id = id; this.r = r; this.c = c; this.h = h; this.w = w; this.k = k;
            this.currentHealth = k;
        }
    }
    
    static class Command {
        int i, d;
        Command(int i, int d) { this.i = i; this.d = d; }
    }
    
    static class Node {
        int x, y;
        Node(int x, int y) { this.x = x; this.y = y; }
    }
}
