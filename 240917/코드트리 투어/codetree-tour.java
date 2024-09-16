import java.io.*;
import java.util.*;

public class Main {
	public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	public static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
	public static final int MAX = 2050;
	public static final int MAX_TRAVEL_ID = 30500;
	public static final int AVAILABLE = 0;
	public static final int NOT_USED = 1;
	public static int start;
	public static int [] distance = new int [MAX];
	public static int [] travelStatus = new int[MAX_TRAVEL_ID];
	public static List<Travel> impossibleList = new ArrayList<>();
	public static PriorityQueue<Travel> travelQueue = new PriorityQueue<>();
	
	
	static class Node implements Comparable<Node>{
		int dest;
		int weight;
		
		public Node(int dest,int weight) {
			this.dest =dest;
			this.weight = weight;
		}
		
		public int compareTo(Node o) {
			return this.weight - o.weight;
		}
	}
	
	static class Travel implements Comparable<Travel>{
		int id;
		int revenue;
		int dest;
		int profit;
		
		public Travel(int id, int revenue, int dest, int profit) {
			this.id = id;
			this.revenue = revenue;
			this.dest = dest;
			this.profit = profit;
		}
		
		public int compareTo(Travel o) {
			if(this.profit != o.profit) {
				return Integer.compare(o.profit, this.profit);
			}else {
				return Integer.compare(this.id, o.id);
			}
		}
	}
	
	static ArrayList<ArrayList<Node>> graph;
	
	static void dijkstra() {
        boolean[] visited = new boolean[MAX];
        Arrays.fill(distance, Integer.MAX_VALUE);
        distance[start] = 0;

        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(new Node(start, 0));

        while (!pq.isEmpty()) {
            Node curr = pq.poll();

            if (visited[curr.dest]) continue;
            visited[curr.dest] = true;

            for (Node neighbor : graph.get(curr.dest)) {
                if (distance[neighbor.dest] > distance[curr.dest] + neighbor.weight) {
                	distance[neighbor.dest] = distance[curr.dest] + neighbor.weight;
                    pq.add(new Node(neighbor.dest, distance[neighbor.dest]));
                }
            }
        }
    }
	
	public static void sellTravel() {
		while(!travelQueue.isEmpty()) {
			Travel sell = travelQueue.poll();
			if(travelStatus[sell.id] == NOT_USED) continue;
			
			travelStatus[sell.id] = NOT_USED;
			System.out.println(sell.id);
			return;
		}
			System.out.println("-1");	
	}
	
	public static void main(String[] args) throws IOException{
		int Q = Integer.parseInt(br.readLine());
		
		for(int q=0;q<Q;q++) {
			String [] tokens = br.readLine().split(" ");
			int command = Integer.parseInt(tokens[0]);
			if(command == 100) {	//코드트리 랜드 건설
				int n = Integer.parseInt(tokens[1]);
				int m = Integer.parseInt(tokens[2]);
				
				graph = new ArrayList<>(n);
				start = 0;
				
				for(int i=0;i<n;i++) {
					graph.add(new ArrayList<>());
				}
				
				for (int i = 0; i < 3 * m; i+=3) {
				    int v = Integer.parseInt(tokens[i + 3]);
				    int u = Integer.parseInt(tokens[i + 4]);
				    int w = Integer.parseInt(tokens[i + 5]);
				    
				    graph.get(v).add(new Node(u, w));
				    graph.get(u).add(new Node(v, w));
				}
				
				dijkstra();
				
			}else if(command == 200) {	//여행 상품 생성
				int id = Integer.parseInt(tokens[1]);
				int revenue = Integer.parseInt(tokens[2]);
				int dest = Integer.parseInt(tokens[3]);
				int profit = revenue - distance[dest];
				Travel travel = new Travel(id,revenue,dest,profit);
				travelStatus[id] = AVAILABLE;
				if(profit >= 0) {
					travelQueue.add(travel);
				}else {
					impossibleList.add(travel);
				}
			}else if(command == 300) {	//여행 상품 취소
				int id = Integer.parseInt(tokens[1]);
				travelStatus[id] = NOT_USED;
			}else if(command == 400) {	//최적의 여행 상품판매
				sellTravel();
			}else if(command == 500) {	//여행 상품의 출발지 변경
				start = Integer.parseInt(tokens[1]);
				dijkstra();
				
				List<Travel> tempList = new ArrayList<>(travelQueue);
				tempList.addAll(impossibleList);
				
				travelQueue.clear();
				impossibleList.clear();
				
				for(Travel travel : tempList) {
					if(travelStatus[travel.id] == NOT_USED) continue;
					
					travel.profit = travel.revenue - distance[travel.dest];
					
					if(travel.profit >= 0) {
						travelQueue.add(travel);
					}else {
						impossibleList.add(travel);
					}
				}
			}
		}
		
	}
}