import java.io.*;
import java.util.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

	/**
	 * N * N 크기의 책상 배열
	 * 각 책상에는 한 명의 학생이 앉아 있음, (1,1) ~ (N,N)
	 * 각 학생은 민트, 초코, 우유 중 하나의 음식만을 신봉함
	 * 
	 * T(민트), C(초코), M(우유)
	 * 
	 * 초기에 민트, 초코, 우유 중 하나만을 신봉하지만, 다른 사람들에게 영향을 받음에 따라, 초코우유, 민트우유, 민트초코우유를 신봉하는 학생도 생길 수 있음
	 * 각 학생은 초기 신앙심을 가지고 있음
	 * T일 동안, 아침, 점심, 저녁 순서로 다음의 과정이 진행
	 * 
	 * 1. 아침
	 * 	모든 학생은 1만큼 신앙심을 얻음
	 * 
	 * 2. 점심
	 * 	인접한 학생들과 신봉음식이 완전히 같은 경우에만 그룹을 형성함
	 * 	그룹 내에서는 대표자 한 명을 선정함
	 * 	대표자 선정 기준
	 * 		신앙심이 가장 큰 사람
	 * 		R이 가장 작은 사람
	 * 		C가 가장 작은 사람
	 * 
	 *	대표자를 제외한 그룹원은 각자 신앙심을 1씩 대표자에게 넘김
	 *	대표자의 신앙심 = 기존 신앙심 + (그룹원 수 - 1)
	 *	나머지 그룹원의 신앙심 = 1씩 감소
	 *
	 * 3. 저녁
	 * 	모든 그룹의 대표자들이 신앙을 전파
	 * 	전파는 다음 순서대로 진행
	 * 		단일 음식 -> 이중 조합 -> 삼중 조합
	 * 
	 * 	같은 그룹 내에서는 다음 기준으로 전파
	 * 		대표자의 신앙심이 높은 순
	 * 		대표자의 행 번호가 작은 순
	 * 		대표자의 열 번호가 작은 순
	 * 
	 * 	전파자는 자신의 신앙심 중 1만 남기고 나머지를 간절함 X(= B -1)로 바꿔 전파에 사용함
	 * 	전파 방향 : B % 4
	 * 	0(위) 1(아래) 2(왼쪽) 3(오른쪽)
	 * 
	 * 	전파자는 전파할 방향으로 한 칸씩 이동하며 전파를 시도, 격자 밖으로 나가거나 간절함이 0이 되면 전파는 종료
	 * 	만약 전파 대상의 신봉 음식이 
	 * 		전파자와 완전히 동일한 경우 -> 전파를 하지 않고 다음으로 진행
	 * 		전파자와 다른 경우 -> 전파 진행
	 * 	
	 *	전파
	 *		전파 대상의 신앙심을 Y라 할때
	 *			x > y -> 강한 전파
	 *				전파자의 신봉 음식과 동일한 음식을 신봉하게 됨, 전파자의 간절함이 (y+1)만큼 깎임, 전파 대상의 신앙심은 1 증가
	 *				전파자의 간절함(X)이 0이 될 수도 있음..-> 더 이상 전파를 진행하지 않고 종료
	 *			x <= y -> 약한 전파
	 *				전파자가 전파한 음식의 모든 기본 음식에 관심을 가짐
	 *				전파자의 간절함(X)은 0이 되고, 전파 종료 && 대상의 신앙심은 X만큼 증가
	 *
	 *	어떤 학생이 다른 음식의 대표자에게 전파를 당한 경우 -> 해당 학생은 당일에는 전파를 하지 않음
	 *	
	 *	각 날의 저녁 시간 이후, 민트초코우유, 민트초코, 민트우유, 초코우유, 우유, 초코, 민트 순서대로 각 음식의 신봉자들의 신앙심 총합을 출력 
	 */
	
	static final String MINT = "T", CHOCO = "C", MILK = "M";
	static int N,T;
	static Person [][] room;
	
	public static void main(String [] args) throws IOException{
		init();
		solution();
	}
	
	static void solution() throws IOException{
		for(int day = 1;day<=T;day++) {
			morning();
			List<Group> groups = lunch();
			dinner(groups);
			printResult();
		}
	}
	
	private static void printResult() throws IOException{
		long [] sum = new long[7];
		Set<String> sTCM = new HashSet<>(Arrays.asList(MINT, CHOCO, MILK));
        Set<String> sTC  = new HashSet<>(Arrays.asList(MINT, CHOCO));
        Set<String> sTM  = new HashSet<>(Arrays.asList(MINT, MILK));
        Set<String> sCM  = new HashSet<>(Arrays.asList(CHOCO, MILK));
        Set<String> sM   = Collections.singleton(MILK);
        Set<String> sC   = Collections.singleton(CHOCO);
        Set<String> sT   = Collections.singleton(MINT);
        
        for(int i=0;i<N;i++) {
        	for(int j=0;j<N;j++) {
        		Set<String> cur = new HashSet<>(room[i][j].belive);
                int p = room[i][j].belivePoint;
                if      (cur.equals(sTCM)) sum[0] += p;
                else if (cur.equals(sTC))  sum[1] += p;
                else if (cur.equals(sTM))  sum[2] += p;
                else if (cur.equals(sCM))  sum[3] += p;
                else if (cur.equals(sM))   sum[4] += p;
                else if (cur.equals(sC))   sum[5] += p;
                else if (cur.equals(sT))   sum[6] += p;
        	}
        }
        for (int k = 0; k < 7; k++) {
            bw.write(String.valueOf(sum[k]));
            if (k < 6) bw.write(" ");
        }
        bw.newLine();
        bw.flush();
	}

	private static void dinner(List<Group> groups) {
		groups = sort(groups);
		int [] dx = {-1,1,0,0};	//위 아래 왼쪽 오른쪽
		int [] dy = {0,0,-1,1};
		HashSet<Person> defenseMode = new HashSet<>();
		for(Group group : groups) {
			Person exponent = group.exponent;
			
			if(defenseMode.contains(exponent)) continue;
			
			int x = exponent.belivePoint-1;
			int B = exponent.belivePoint;
			exponent.belivePoint = 1;
			
			int dir = B % 4;
			
			int nowX = exponent.x + dx[dir];
			int nowY = exponent.y + dy[dir];
			
			while(inRange(nowX,nowY) && x > 0) {
				Person target = room[nowX][nowY];
				if(checkSameFood(exponent.belive, target.belive)) {
					nowX += dx[dir];
					nowY += dy[dir];
					continue;
				}
				
				int y = target.belivePoint;
				
				defenseMode.add(target);
				if(x > y) {		//강한 전파
					target.belive = new ArrayList<>(exponent.belive);
					x -= (y+1);
					target.belivePoint++;
				}else {					
					HashSet<String> originalFoods = new HashSet<>(target.belive);
					for(String f : exponent.belive) {
						if(originalFoods.contains(f)) continue;
						target.belive.add(f);
					}
					target.belivePoint += x;
					x = 0;
				}
				
				nowX += dx[dir];
				nowY += dy[dir];
			}
		}
	}

	private static List<Group> sort(List<Group> groups) {
		Collections.sort(groups, new Comparator<Group>() {
			public int compare(Group o1, Group o2) {
				if(o1.exponent.belive.size() == o2.exponent.belive.size()) {
					if(o1.exponent.belivePoint == o2.exponent.belivePoint) {
						if(o1.exponent.x == o2.exponent.x) {
							return o1.exponent.y - o2.exponent.y;
						}else return o1.exponent.x - o2.exponent.x;
					}else return o2.exponent.belivePoint - o1.exponent.belivePoint;
				}else return o1.exponent.belive.size() - o2.exponent.belive.size();
			}
		});
		return groups;
	}

	static List<Group> lunch() {
		List<Group> groups = new ArrayList<>();
		boolean [][] visited = new boolean[N][N];
		
		for(int i=0;i<N;i++) {
			for(int j=0;j<N;j++) {
				if(visited[i][j]) continue;
				
				List<Person> groupPerson = createGroup(i,j,visited);
				
				Person exponent = findExponent(groupPerson);
				List<Person> people = new ArrayList<>();
				for(Person p : groupPerson) {
					if(p == exponent) continue;
					people.add(p);
				}
				
				Group group = new Group(people, exponent);
				groups.add(group);
			}
		}
		
		for(Group group : groups) {
			group.exponent.belivePoint += group.people.size();
			for(Person p : group.people) {
				p.belivePoint--;
			}
		}
		
		return groups;
	}
	
	private static Person findExponent(List<Person> groupPerson) {
		Collections.sort(groupPerson,new Comparator<Person>() {
			public int compare(Person o1, Person o2) {
				if(o1.belivePoint == o2.belivePoint) {
					if(o1.x == o2.x) {
						return o1.y - o2.y;
					}else return o1.x - o2.x;
				}else return o2.belivePoint - o1.belivePoint;
			}
		});
		
		return groupPerson.get(0);
	}

	/**
	 * 같은 음식을 신봉하는 사람 찾기
	 * @param i
	 * @param j
	 */
	private static List<Person> createGroup(int i, int j, boolean [][] visited) {
		int [] dx = {0,0,-1,1};
		int [] dy = {-1,1,0,0};
		
		Queue<Person> queue = new ArrayDeque<>();
		List<Person> list = new ArrayList<>();
		
		visited[i][j] = true;
		queue.add(room[i][j]);
		
		while(!queue.isEmpty()) {
			Person curNode = queue.poll();
			list.add(curNode);
			
			for(int d=0;d<4;d++) {
				int nx = curNode.x + dx[d];
				int ny = curNode.y + dy[d];
				
				if(!inRange(nx,ny) || visited[nx][ny]) continue;
				if(checkSameFood(curNode.belive, room[nx][ny].belive)) {
					queue.add(room[nx][ny]);
					visited[nx][ny] = true;
				}
			}
		}
		
		return list;
	}
	
	static boolean checkSameFood(List<String> first, List<String> second) {
		if(first.size() != second.size()) return false;
		HashSet<String> a = new HashSet<>(first);
		
		for(String food : second) {
			if(!a.contains(food)) return false;
		}
		return true;
	}
	
	static boolean inRange(int x,int y) {
		return x >= 0 && x < N && y >= 0 && y < N;
	}

	static void morning() {
		for(int i=0;i<N;i++) {
			for(int j=0;j<N;j++) {
				room[i][j].belivePoint++;
			}
		}
	}
	
	static void init() throws IOException{
		String [] tokens = br.readLine().split(" ");
		N = Integer.parseInt(tokens[0]);
		T = Integer.parseInt(tokens[1]);
		
		room = new Person[N][N];
		for(int i=0;i<N;i++) {
			String input = br.readLine();
			for(int j=0;j<N;j++) {
				String beliveFood = input.charAt(j) + "";
				List<String> beliveFoods = new ArrayList<>();
				beliveFoods.add(beliveFood);
				Person p = new Person(i,j,beliveFoods, 0);
				
				room[i][j] = p;
			}
		}
		
		for(int i=0;i<N;i++) {
			tokens = br.readLine().split(" ");
			for(int j=0;j<N;j++) {
				int belivePoint = Integer.parseInt(tokens[j]);
				room[i][j].belivePoint = belivePoint;
			}
		}
	}
	
	static class Person{
		int x,y;
		List<String> belive;
		int belivePoint;
		public Person(int x, int y, List<String> belive, int belivePoint) {
			super();
			this.x = x;
			this.y = y;
			this.belive = belive;
			this.belivePoint = belivePoint;
		}
		@Override
		public String toString() {
			return "Person [x=" + x + ", y=" + y + ", belive=" + belive + ", belivePoint=" + belivePoint + "]";
		}
	}
	
	static class Node{
		int x,y;
		public Node(int x,int y) {
			this.x = x;
			this.y = y;
		}
		@Override
		public String toString() {
			return "Node [x=" + x + ", y=" + y + "]";
		}
	}
	
	static class Group{
		List<Person> people;
		Person exponent;
		public Group(List<Person> people, Person exponent) {
			super();
			this.people = people;
			this.exponent = exponent;
		}
		@Override
		public String toString() {
			return "Group [people=" + people + ", exponent=" + exponent + "]";
		}
	}
}
