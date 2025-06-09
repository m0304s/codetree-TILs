import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
   
    //상 우 하 좌
    static int [] dx4 = {-1,0,1,0};
    static int [] dy4 = {0,1,0,-1};
    
    static int [] dx8 = {-1,-1,0,1,1,1,0,-1};
    static int [] dy8 = {0,1,1,1,0,-1,-1,-1};
    
    static int N,M,P,C,D;
    static Node rudolph;
    static List<Santa> santaList;
    
    public static void main(String [] args) throws IOException{
    	init();
    	Collections.sort(santaList, (o1,o2) -> (o1.number - o2.number));
    	for(int turn=1;turn<=M;turn++) {
    		if(checkAllOut()) break;
    		moveRudolph(turn);
    		moveSanta(turn);
    		getAliveSantaOneScore();
    	}
    	
    	for(Santa santa : santaList) {
    		bw.write(santa.score + " ");
    	}
    	bw.flush();
    	bw.close();
    	br.close();
    }
    
    private static void getAliveSantaOneScore() {
		for(Santa santa : santaList) {
			if(santa.status != Status.OUT) {
				santa.score++;
			}
		}
	}
    
    private static void moveSanta(int turn) {
    	for(int i=0;i<P;i++) {
    		Santa moveSanta = santaList.get(i);
    		
    		if(moveSanta.status == Status.OUT) continue;
    		if(moveSanta.status == Status.STUNNED) {
    			if(turn < moveSanta.stunnedTurn +2) continue;
    			
    			moveSanta.status = Status.ALIVE;
    		}
    		
    		int originalDistance = calcDistance(new Node(moveSanta.x, moveSanta.y), rudolph);	//기존의 산타와 루돌프 사이 거리
    		
    		List<int []> candidates = new ArrayList<>();
    		for(int d=0;d<4;d++) {
    			int nx = moveSanta.x + dx4[d];
    			int ny = moveSanta.y + dy4[d];
    			
    			int newDistance = calcDistance(new Node(nx,ny), rudolph);
    			if(!inRange(nx,ny) || newDistance >= originalDistance) {	//격자 밖을 벗어나거나, 거리가 줄어들지 않는 경우
    				continue;
    			}
    			
    			if(isSanta(nx,ny) != null) {	//움직인 칸에 산타가 존재한다면..
    				continue;
    			}
    			
    			candidates.add(new int[] {d,newDistance});
    		}
    		
    		if(candidates.isEmpty()) {	//움직일 수 있는 칸이 없다면..
    			continue;
    		}
    		Collections.sort(candidates, new Comparator<int[]>(){
    			public int compare(int [] o1, int [] o2) {
    				if(o1[1] == o2[1]) {
    					return o1[0] - o2[0];
    				}else {
    					return o1[1] - o2[1];
    				}
    			}
    		});
    		
    		int bestDir = candidates.get(0)[0];	//산타가 이동할 방향
    		
    		moveSanta.x += dx4[bestDir];
    		moveSanta.y += dy4[bestDir];
    		
    		if(moveSanta.x == rudolph.x && moveSanta.y == rudolph.y) {
    			collideSanta(moveSanta,turn,bestDir);
    		}
    	}
	}

	private static void collideSanta(Santa moveSanta, int turn, int bestDir) {
		int reverseDir = (bestDir + 2) % 4;
		
		int nx = moveSanta.x + dx4[reverseDir] * D;
		int ny = moveSanta.y + dy4[reverseDir] * D;
		
		moveSanta.score += D;
		moveSanta.status = Status.STUNNED;
		moveSanta.stunnedTurn = turn;
		
		if(!inRange(nx,ny)) {
			moveSanta.status = Status.OUT;
			return;
		}
		
		Santa anotherSanta = isSanta(nx,ny);
		
		if(anotherSanta != null) {
			slideSanta(moveSanta,anotherSanta,reverseDir);
		}else {
			moveSanta.x = nx;
			moveSanta.y = ny;
		}
	}

	private static void slideSanta(Santa moveSanta, Santa anotherSanta, int reverseDir) {
		moveSanta.x = anotherSanta.x;
		moveSanta.y = anotherSanta.y;
		
		int nx = anotherSanta.x + dx4[reverseDir];
		int ny = anotherSanta.y + dy4[reverseDir];
		
		if(!inRange(nx,ny)) {
			anotherSanta.status = Status.OUT;
			return;
		}
		
		Santa temp = isSanta(nx,ny);
		if(temp == null) {
			anotherSanta.x = nx;
			anotherSanta.y = ny;
		}else {
			slideSanta(anotherSanta,temp,reverseDir);
		}
	}

	private static void moveRudolph(int turn) {
    	RudolphToSanta best = findNearestSanta();
    	Santa targetSanta = best.santa;	//가장 가까운 산타
    	int dir = best.direction;	//가장 가까운 산타로 가기 위해 움직이는 방향
    	
    	rudolph.x += dx8[dir];
    	rudolph.y += dy8[dir];
    	
    	Santa santa = isSanta(rudolph.x,rudolph.y);
    	
    	if(santa != null) {
    		//루돌프가 움직인 위치에 산타가 존재하는 경우
    		collideRudolph(santa,turn,dir);
    	}
	}
    
	private static void collideRudolph(Santa santa, int turn, int dir) {
		int nx = santa.x + dx8[dir] * C;
		int ny = santa.y + dy8[dir] * C;
		
		santa.score += C;
		santa.status = Status.STUNNED;
		santa.stunnedTurn = turn;
		
		if(!inRange(nx,ny)) {
			santa.status = Status.OUT;
			return;
		}
		
		Santa isAnotherSanta = isSanta(nx,ny);
		if(isAnotherSanta != null) {
			slide(santa,isAnotherSanta,dir);
		}else {
			santa.x = nx;
			santa.y = ny;
		}
	}

	private static void slide(Santa santa, Santa targetSanta, int dir) {
		santa.x = targetSanta.x;
		santa.y = targetSanta.y;
		
		int nx = targetSanta.x + dx8[dir];
		int ny = targetSanta.y + dy8[dir];
		
		if(!inRange(nx,ny)) {
			targetSanta.status = Status.OUT;
			return;
		}
		
		Santa temp = isSanta(nx,ny);
		if(temp == null) {
			targetSanta.x = nx;
			targetSanta.y = ny;
		}else {
			slide(targetSanta,temp,dir);
		}
	}

	private static Santa isSanta(int x, int y) {
		for(Santa santa : santaList) {
			if(santa.status == Status.OUT) continue;
			if(santa.x == x && santa.y == y) return santa;
		}
		return null;
	}

	private static RudolphToSanta findNearestSanta() {
		List<RudolphToSanta> candidates = new ArrayList<>();
		
		for(Santa santa : santaList) {
			if(santa.status == Status.OUT) continue;
			int distance = calcDistance(new Node(santa.x,santa.y), rudolph);
			RudolphToSanta simulation = new RudolphToSanta(distance, 0, santa);
			
			candidates.add(simulation);
		}
		
		Collections.sort(candidates, new Comparator<RudolphToSanta>() {
			public int compare(RudolphToSanta o1, RudolphToSanta o2) {
				if(o1.distance == o2.distance) {
					if(o1.santa.x == o2.santa.x) {
						return o2.santa.y - o1.santa.y;
					}else return o2.santa.x - o1.santa.x;
				}else return o1.distance - o2.distance;
			}
		});
		
		if(candidates.isEmpty()) return null;
		
		RudolphToSanta best = candidates.get(0);
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
                if (dx8[d] == moveX && dy8[d] == moveY) {
                    dir = d;
                    break;
                }
            }
        }
		
        best.direction = dir;
		return best;
	}

	static boolean checkAllOut() {
    	for(Santa santa : santaList) {
    		if(santa.status != Status.OUT) return false;
    	}
    	
    	return true;
    }
    
    

	private static int calcDistance(Node a, Node b) {
	    int dx = a.x - b.x, dy = a.y - b.y;
	    return dx*dx + dy*dy;
	}
    
    static void init() throws IOException{
    	String [] tokens = br.readLine().split(" ");
    	N = Integer.parseInt(tokens[0]);
    	M = Integer.parseInt(tokens[1]);
    	P = Integer.parseInt(tokens[2]);
    	C = Integer.parseInt(tokens[3]);
    	D = Integer.parseInt(tokens[4]);
    	
    	tokens = br.readLine().split(" ");
    	int rudolphX = Integer.parseInt(tokens[0])-1;
    	int rudolphY = Integer.parseInt(tokens[1])-1;
    	
    	rudolph = new Node(rudolphX, rudolphY);
    	
    	santaList = new ArrayList<>();
    	for(int i=0;i<P;i++) {
    		tokens = br.readLine().split(" ");
    		int number = Integer.parseInt(tokens[0]);
    		int r = Integer.parseInt(tokens[1])-1;
    		int c = Integer.parseInt(tokens[2])-1;
    		
    		Santa santa = new Santa(number, -1, r, c, 0, Status.ALIVE);
    		santaList.add(santa);
    	}
    }
    
    static boolean inRange(int x,int y) {
    	return x >= 0 && x < N && y >= 0 && y < N;
    }
    
    static class Santa{
    	int number;
    	int stunnedTurn;
    	int x,y;
    	int score;
    	Status status;
		@Override
		public String toString() {
			return "Santa [number=" + number + ", stunnedTurn=" + stunnedTurn + ", x=" + x + ", y=" + y + ", score="
					+ score + ", status=" + status + "]";
		}
		public Santa(int number, int stunnedTurn, int x, int y, int score, Status status) {
			super();
			this.number = number;
			this.stunnedTurn = stunnedTurn;
			this.x = x;
			this.y = y;
			this.score = score;
			this.status = status;
		}    	
    }
    
    static enum Status{
    	STUNNED, ALIVE, OUT
    }
    
    static class Node{
    	int x,y;
    	public Node(int x, int y) {
    		this.x = x;
    		this.y = y;
    	}
		@Override
		public String toString() {
			return "Node [x=" + x + ", y=" + y + "]";
		}
    	
    }
    
    static class RudolphToSanta{
    	int distance;
    	int direction;
    	Santa santa;
		public RudolphToSanta(int distance,int direction, Santa santa) {
			super();
			this.direction = direction;
			this.distance = distance;
			this.santa = santa;
		}
		@Override
		public String toString() {
			return "RudolphToSanta [distance=" + distance + ", direction=" + direction + ", santa=" + santa + "]";
		}
    }
}