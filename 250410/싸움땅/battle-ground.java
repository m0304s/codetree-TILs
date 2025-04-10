import java.io.*;
import java.util.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
	
	/**
	 * N*N 크기의 격자
	 * 초기에는 무기가 없는 빈 격자에서 진행
	 * 플레이어는 초기 능력치를 가지고 있음
	 * 
	 * 게임 진행방식
	 * 격자에는 총과 플레이어 위치 가능
	 * 총 : 공격력
	 * 플레이어 : 번호, 초기 능력치
	 * 
	 * 1라운드
	 * 1. 첫번째 플레이어부터 순차적으로 본인이 향하고 있는 방향대로 한 칸 이동
	 * (격자를 벗어나는 경우 반대 방향으로 바꿔서 한 칸 이동)
	 * 
	 * 2-1 이동한 방향에 플레이어가 없는 경우
	 * 	1. 해당 칸에 총이 있는지 확인
	 * 	2-1 총이 있는 경우, 
	 * 		해당 플레이어가 총을 가지고 있지 않는 경우 : 해당 플레이어는 총을 획득
	 * 		해당 플레이어가 이미 총을 가지고 있는 경우 : 공격력이 더 큰 총을 획득, 기존 총은 해당 위치에 둠
	 * 
	 * 2-2 이동한 방향에 플레이어가 있는 경우
	 * 	1. 플레이어가 서로 싸움
	 * 		승자 : 초기 능력치 + 가지고 있는 총의 공격력 합이 더 큰 플레이어
	 *  		   만약 수치가 같은 경우 초기 능력치가 더 높은 플레이어
	 *  2. 승자는 초기 능력치 + 가지고 있는 총의 공격력의 합의 차이 만큼 포인트 획득
	 *     승리한 칸에 떨어져있는 총들과, 원래 들고 있던 총 중 공격력이 가장 높은 총을 획득, 나머지 총들은 해당 격자에 내려놓음
	 *  3. 패자는 가지고 있는 총을 해당 격자에 내려놓음, 원래 가지고 있던 방향대로 한 칸 이동
	 *  	이동하려는 칸에 다른 플레이어가 있거나, 격자 범위 밖인 경우 오른쪽으로 90도씩 회전하여 빈 칸인 순간 이동
	 *  	해당 칸에 총이 있다면, 가장 공격력이 높은 총을 획득하고 나머지 총들은 해당 격자에 내려놓음
	 *  
	 *  출력 : k 라운드 동안 게임을 진행하면서 각 플레이어들이 획득한 포인트를 출력
	 */
	
	static int N,M,K;
	static ArrayList<Player> playerList;
	static ArrayList<Gun>[][] gunMap;
	//상 우 하 좌
	static int [] dx = {-1,0,1,0};
	static int [] dy = {0,1,0,-1};
	
	public static void main(String[] args) throws IOException{
		init();
		simulation();
		printResult();
	}
	
	private static void printResult() throws IOException{
		for(int i=0;i<M;i++) {
			bw.write(playerList.get(i).point+" ");
		}
		bw.write("\n");
		bw.flush();
		bw.close();
		br.close();
	}

	static void simulation() {
		for(int k=1;k<=K;k++) {
			movePlayer();
		}
	}
	
	static void movePlayer() {
	    for (int i = 0; i < M; i++) {
	        Player player = playerList.get(i);  // 이동할 플레이어

	        int nx = player.x + dx[player.dir];
	        int ny = player.y + dy[player.dir];

	        if (!inRange(nx, ny)) {  // 격자 밖을 벗어나는 경우
	            player.dir = (player.dir + 2) % 4;
	            // 새 방향에 따른 nx, ny 재계산
	            nx = player.x + dx[player.dir];
	            ny = player.y + dy[player.dir];
	        }

	        // player를 이동시키기 전에 이동할 좌표에 다른 플레이어가 있는지 확인
	        Player targetPlayer = findPlayerInPlace(nx, ny);

	        // 이동한 좌표로 플레이어 위치 업데이트
	        player.x = nx;
	        player.y = ny;

	        if (targetPlayer != null) {
	            // 해당 칸에 다른 플레이어가 있다면 격투 진행
	            fight(player, targetPlayer);
	        } else {
	            // 해당 칸에 플레이어가 없으면 총 줍기
	            addGunToPlayer(player, player.x, player.y);
	        }
	    }
	}

	
	private static void fight(Player player, Player targetPlayer) {
		int abilityForFirst = player.ability + ((player.gun == null) ? 0 : player.gun.power);
		int abilityForSecond = targetPlayer.ability + ((targetPlayer.gun == null) ? 0 : targetPlayer.gun.power);
		
		Player winner = null;
		Player loser = null;
		if(abilityForFirst > abilityForSecond) {
			//target 플레이어가 승자
			winner = player;
			loser = targetPlayer;
		}else if(abilityForFirst == abilityForSecond) {
			//초기 능력치가 높은 플레이어가 승자
			winner = (player.ability > targetPlayer.ability) ? player : targetPlayer;
			if(winner == player) {
				loser = targetPlayer;
			}else {
				loser = player;
			}
		}else {
			//targetPlayer가 승자
			winner = targetPlayer;
			loser = player;
		}
		
		//승자는 각 플레이어의 초기 능력치와 가지고 있는 총의 공격력의 합 차이만큼 포인트 획득
		int addPoint = Math.abs(abilityForFirst - abilityForSecond);
		winner.point += addPoint;
		
		//패자는 본인이 가진 총을 해당 격자에 내려놓음, 해당 플레이어의 방향대로 한 칸 이동
		int x = loser.x;
		int y = loser.y;
		
		if(loser.gun != null) {			
			Gun loserGun = loser.gun;
			gunMap[x][y].add(loserGun);
			loser.gun = null;
		}
		
	    // 패자가 이동할 좌표 결정
	    // 기존 위치 (x, y)에서, 본인의 방향으로 한 칸 이동하되,
	    // 이동하려는 칸이 격자 범위를 벗어나거나 이미 다른 플레이어가 있는 경우 오른쪽으로 90도씩 회전
	    while (true) {
	        int nx = x + dx[loser.dir];
	        int ny = y + dy[loser.dir];
	        
	        // 격자 범위 밖이거나 해당 칸에 플레이어가 존재하면 방향을 회전
	        if (!inRange(nx, ny) || findPlayerInPlace(nx, ny) != null) {
	            loser.dir = (loser.dir + 1) % 4;
	        } else {
	            // 유효한 칸을 찾았으므로 이동
	            loser.x = nx;
	            loser.y = ny;
	            break;
	        }
	    }
		
		
		//승자는 승리한 칸에서 가장 좋은 총으로 교체
	    ArrayList<Gun> gunList = gunMap[winner.x][winner.y];
	    if (winner.gun != null) {
	        gunList.add(winner.gun);
	    }
	    if (!gunList.isEmpty()) {
	        Collections.sort(gunList);
	        Gun best = gunList.get(0);
	        winner.gun = best;
	        gunList.remove(best);
	    } else {
	        winner.gun = null;
	    } 
	}

	private static void addGunToPlayer(Player player, int nx, int ny) {
		//이동한 칸에 총이 있는지 확인
		ArrayList<Gun> gunList = gunMap[nx][ny];
		if(gunList.isEmpty()) return;
		
		if(player.gun == null) {
			//가장 공격력이 높은 총을 획득
			Collections.sort(gunList);
			Gun best = gunList.get(0);
			player.gun = best;
			gunList.remove(best);
			gunMap[nx][ny] = gunList;
			
		}else {
			//총을 가지고 있는 경우 -> 가장 좋은 총으로 교체
			ArrayList<Gun> available = new ArrayList<>();
			available.addAll(gunList);
			available.add(player.gun);
			
			Collections.sort(available);
			Gun best = available.get(0);
			available.remove(best);
			
			gunMap[nx][ny] = available;
			player.gun = best;
		}
	}

	private static Player findPlayerInPlace(int nx, int ny) {
		for(int i=0;i<M;i++) {
			Player player = playerList.get(i);
			if(player.x == nx && player.y == ny) return player;
		}
		return null;
	}

	static boolean inRange(int x,int y) {
		return x >= 0 && x < N && y >= 0 && y < N;
	}
	
	static void init() throws IOException{
		String [] tokens = br.readLine().split(" ");
		N = Integer.parseInt(tokens[0]);	//격자의 크기
		M = Integer.parseInt(tokens[1]);	//플레이어의 수
		K = Integer.parseInt(tokens[2]);	//라운드의 수
		
		gunMap = new ArrayList[N][N];
		for(int i=0;i<N;i++) {
			for(int j=0;j<N;j++) {
				gunMap[i][j] = new ArrayList<>();
			}
		}
		
		//총 정보 입력
		for(int i=0;i<N;i++) {
			tokens = br.readLine().split(" ");
			for(int j=0;j<N;j++) {
				int power = Integer.parseInt(tokens[j]);
				if(power > 0) {
					//총이 있는 경우
					gunMap[i][j].add(new Gun(power));
				}
			}
		}
		
		playerList = new ArrayList<>();
		//플레이어 정보 입력
		for(int i=0;i<M;i++) {
			tokens = br.readLine().split(" ");
			int x = Integer.parseInt(tokens[0])-1;
			int y = Integer.parseInt(tokens[1])-1;
			int d = Integer.parseInt(tokens[2]);
			int s = Integer.parseInt(tokens[3]);
			
			Player player = new Player(i+1,x,y,s,d,0,null);
//			playerMap[x][y] = player;
			playerList.add(player);
		}
	}
	
	static class Player{
		int id;
		int x,y;
		int ability;
		int dir;
		int point;
		Gun gun;
		
		public Player(int id,int x,int y,int ability, int dir,int point, Gun gun) {
			this.id = id;
			this.x = x;
			this.y = y;
			this.ability = ability;
			this.dir = dir;
			this.gun = gun;
		}

		@Override
		public String toString() {
			return "Player [id=" + id + ", x=" + x + ", y=" + y + ", ability=" + ability + ", dir=" + dir + ", point="
					+ point + ", gun=" + gun + "]";
		}
	}
	
	static class Gun implements Comparable<Gun>{
		int power;	//총의 공격력
		public Gun(int power) {
			this.power = power;
		}
		@Override
		public String toString() {
			return "Gun [power=" + power + "]";
		}
		@Override
		public int compareTo(Gun o) {
			// TODO Auto-generated method stub
			return o.power - this.power;
		}
	}
}
