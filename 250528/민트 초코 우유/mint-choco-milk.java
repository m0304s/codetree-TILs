import java.io.*;
import java.util.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

	static int N,T;
	static int [] dx = {-1,1,0,0};	//위 아래 왼쪽 오른쪽
	static int [] dy = {0,0,-1,1};
	static Person [][] map;
	
	public static void main(String[] args) throws IOException{
		init();
		for(int t=1;t<=T;t++) {
			morning();
			List<Group> groups = lunch();
			night(groups);
			printResult();
		}
		bw.flush();
	}
	
    private static void printResult() throws IOException {
        long[] sum = new long[7];
        Set<String> sTCM = new HashSet<>(Arrays.asList("T","C","M"));
        Set<String> sTC  = new HashSet<>(Arrays.asList("T","C"));
        Set<String> sTM  = new HashSet<>(Arrays.asList("T","M"));
        Set<String> sCM  = new HashSet<>(Arrays.asList("C","M"));
        Set<String> sM   = Collections.singleton("M");
        Set<String> sC   = Collections.singleton("C");
        Set<String> sT   = Collections.singleton("T");

        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= N; j++) {
                Set<String> cur = new HashSet<>(map[i][j].belive);
                int p = map[i][j].belivePoint;
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
	
	private static void night(List<Group> groups) {
		groups = sort(groups);
		
		HashSet<Person> defenseMode = new HashSet<>();
		for(Group group : groups) {
			Person exponent = group.exponent;
			if(defenseMode.contains(exponent)) continue;	//방어 모드에 들어간 대표자는 전파할 수 없음
			
			int x = exponent.belivePoint - 1;	//전파 대상 간절함
			int B = exponent.belivePoint;		//초기 신앙심
			exponent.belivePoint = 1;
			
			int dir = B % 4;	//전파 방향
			
			int nowX = exponent.point.x + dx[dir];
			int nowY = exponent.point.y + dy[dir];
			
			while(inRange(nowX,nowY) && x > 0) {
				Person target = map[nowX][nowY];
				
				if(checkBeliveSameFood(exponent.belive,  target.belive)) {
					nowX += dx[dir];
					nowY += dy[dir];
					continue;	//같은 음식을 신봉할 경우, 전파X
				}
				int y = target.belivePoint;
				
				//전파 당함
				defenseMode.add(target);
				if(x > y) {	//강한 전파
					target.belive = new ArrayList<>(exponent.belive);
					x -= (y+1);
					target.belivePoint++;
				}else {		//약한 전파
					HashSet<String> originalFood = new HashSet<>(target.belive);
					
					for(String additinalFood : exponent.belive) {
						if(originalFood.contains(additinalFood)) continue;
						target.belive.add(additinalFood);
					}
					
					target.belivePoint += x;
					x = 0;
				}
				nowX += dx[dir];
				nowY += dy[dir];
			}
		}
	}
	
	private static List<Group> sort(List<Group> groups){
		Collections.sort(groups, (o1, o2) -> {
			int c = o1.exponent.belive.size() - o2.exponent.belive.size();
			if(c != 0) return c;
			
			c = o2.exponent.belivePoint - o1.exponent.belivePoint;
			if(c != 0) return c;
			
			c = o1.exponent.point.x - o2.exponent.point.x;
			if(c != 0) return c;
			
			c = o1.exponent.point.y - o2.exponent.point.y;
			return c;
		});
		return groups;
	}

	private static List<Group> lunch() {
		//인접한 학생들과 신봉 음식이 완전히 같은 경우에만 그룹 형성
		boolean [][] visited = new boolean[N+1][N+1];
		
		List<Group> groupList = new ArrayList<>();
		
		for(int i=1;i<=N;i++) {
			for(int j=1;j<=N;j++) {
				if(visited[i][j]) continue;
				
				List<Person> groupPerson = createGroup(i,j, visited);
				
				//대표자 선정
				Person exponent = findExponent(groupPerson);
				List<Person> groupPersons = new ArrayList<>();
				for(Person candidate : groupPerson) {
					if(candidate == exponent) continue;
					groupPersons.add(map[candidate.point.x][candidate.point.y]);
				}
				groupList.add(new Group(groupPersons,exponent));
			}
		}
		
		for(Group group : groupList) {
			group.exponent.belivePoint += (group.persons.size());
			// 각 그룹별로 대표자는 그룹원 수 -1 만큼 신앙심 추가 & 나머지 그룹원은 신앙심 -1
			List<Person> persons = group.persons;
			for(Person person : persons) {
				person.belivePoint--;
			}
		}
		return groupList;
	}

	/**
	 * 	그룹 내에서 대표자 1명 선정
	 * 	선정 기준 :
	 * 		1. 신앙심이 가장 큰 사람
	 * 		2. X좌표 값이 가장 작은 사람
	 * 		3. Y좌표 값이 가장 작은 사람
	 */
	private static Person findExponent(List<Person> groupPerson) {
		Collections.sort(groupPerson, new Comparator<Person>() {
			public int compare(Person o1, Person o2) {
				if(o1.belivePoint == o2.belivePoint) {
					if(o1.point.x == o2.point.x) {
						return o1.point.y - o2.point.y;
					}else {
						return o1.point.x - o2.point.x;
					}
				}else {
					return o2.belivePoint - o1.belivePoint;
				}
			}
		});
		return groupPerson.get(0);
	}

	/**
	 * 그룹 생성
	 */
	private static List<Person> createGroup(int i, int j, boolean [][] visited) {
		Person startPerson = map[i][j];
		List<Person> groupPersons = new ArrayList<>();		
		Queue<Person> queue = new ArrayDeque<>();
		
		visited[i][j] = true;
		queue.add(startPerson);
		
		while(!queue.isEmpty()) {
			Person curNode = queue.poll();
			groupPersons.add(curNode);
			
			for(int d=0;d<4;d++) {
				int nx = curNode.point.x + dx[d];
				int ny = curNode.point.y + dy[d];
				
				if(!inRange(nx, ny)) continue;
				if(visited[nx][ny]) continue;
				if(checkBeliveSameFood(curNode.belive, map[nx][ny].belive)) {
					//같은 음식을 신봉할 경우에만 추가
					visited[nx][ny] = true;
					queue.add(map[nx][ny]);
				}
			}
		}
		return groupPersons;
	}

	private static boolean checkBeliveSameFood(List<String> a, List<String> b) {
		if (a.size() != b.size()) return false;
		HashSet<String> hs = new HashSet<>(a);
		for (String food : b) {
		  if (!hs.contains(food)) return false;
		}
		return true;
	}


	/**
	 * 모두의 신앙심 1 추가
	 */
	private static void morning() {
		for(int i=1;i<=N;i++) {
			for(int j=1;j<=N;j++) {
				map[i][j].belivePoint++;
			}
		}
	}

	/**
	 * 초기 입력
	 */
	private static void init() throws IOException{
		String [] tokens = br.readLine().split(" ");
		N = Integer.parseInt(tokens[0]);
		T = Integer.parseInt(tokens[1]);
		
		map = new Person[N+1][N+1];
		
		for(int i=0;i<N;i++) {
			String input = br.readLine();
			for(int j=0;j<N;j++) {
				String beliveFood = input.charAt(j)+"";
				
				Person person = new Person(beliveFood,0,new Node(i+1,j+1));
				map[i+1][j+1] = person;
			}
		}
		
		//신앙심 초기화
		for(int i=0;i<N;i++) {
			tokens = br.readLine().split(" ");
			for(int j=0;j<N;j++) {
				int belivePoint = Integer.parseInt(tokens[j]);
				
				map[i+1][j+1].belivePoint = belivePoint;
			}
		}
	}
	
	private static void debug() {
		for(int i=1;i<=N;i++) {
			for(int j=1;j<=N;j++) {
				System.out.println(map[i][j]);
			}
		}
		System.out.println("===================");
	}
	
	/**
	 * 좌표의 범위 검사
	 */
	private static boolean inRange(int x,int y) {
		return x >= 1 && x <= N && y >= 1 && y <= N;
	}

	static class Person{
		List<String> belive;	//신봉 음식 목록
		int belivePoint;		//신앙심
		Node point;
		
		public Person(String beliveFood, int belivePoint, Node point) {
			belive = new ArrayList<>();
			belive.add(beliveFood);
			this.belivePoint = belivePoint;
			this.point = point;
		}

		@Override
		public String toString() {
			return "Person [belive=" + belive + ", belivePoint=" + belivePoint + ", point=" + point + "]";
		}
	}
	
	static class Group{
		List<Person> persons;	//그룹원
		Person exponent;	//대표자
		
		public Group(List<Person> persons, Person exponent) {
			this.persons = persons;
			this.exponent = exponent;
		}

		@Override
		public String toString() {
			return "Group [persons=" + persons + ", exponent=" + exponent + "]";
		}
	}
	
	static class Node{
		int x,y;

		public Node(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "Node [x=" + x + ", y=" + y + "]";
		}
		
	}
}
