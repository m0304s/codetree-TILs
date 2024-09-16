import java.io.*;
import java.util.*;

public class Main {
	public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	public static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
	public static final int MAX = 3000;
	public static final int MAX_TRAVEL_ID = 30500;
	public static final int NOT_USED = 1;
	public static final int AVAILABLE = 0;
	
	public static int start;
	public static int [] distance = new int[MAX];
	public static int [] travelStatus = new int[MAX_TRAVEL_ID];
	public static ArrayList<ArrayList<Node>> graph;
	public static List<Travel> impossibleList = new ArrayList<>();
	public static PriorityQueue<Travel> travelQueue = new PriorityQueue<>();
	
	static class Node implements Comparable<Node>{
		int dest;
		int weight;
		public Node(int dest,int weight) {
			this.dest = dest;
			this.weight = weight;
		}
		public int compareTo(Node o) {
			return Integer.compare(this.weight, o.weight);
		}
	}
	
	static class Travel implements Comparable<Travel>{
		int id;
		int revenue;
		int dest;
		int profit;
		
		public Travel(int id,int revenue,int dest, int profit) {
			this.id = id;
			this.revenue = revenue;
			this.dest = dest;
			this.profit = profit;
		}
		public int compareTo(Travel o) {
			if(this.profit == o.profit) {
				return Integer.compare(this.id, o.id);
			}else {
				return Integer.compare(o.profit, this.profit);
			}
		}
	}
	
	public static void dijkstra() {
		boolean [] visited = new boolean[MAX];
		Arrays.fill(distance, Integer.MAX_VALUE);
		distance[start] = 0;
		
		PriorityQueue<Node> pq = new PriorityQueue<>();
		pq.add(new Node(start,0));
		
		while(!pq.isEmpty()) {
			Node cur = pq.poll();
			if(visited[cur.dest]) continue;
			
			visited[cur.dest] = true;
			
			for(Node neighbor : graph.get(cur.dest)) {
				if(distance[neighbor.dest] > distance[cur.dest] + neighbor.weight) {
					distance[neighbor.dest] = distance[cur.dest] + neighbor.weight;
					pq.add(new Node(neighbor.dest , distance[neighbor.dest]));
				}
			}
		}
	}
	public static void sellTravel() throws IOException{
		while(!travelQueue.isEmpty()) {
			Travel product = travelQueue.poll();
			if(travelStatus[product.id] == NOT_USED) continue;
			
			travelStatus[product.id] = NOT_USED;
			bw.write(product.id + "\n");
			return;
		}
		bw.write("-1\n");
	}
	
	public static void main(String[] args) throws IOException{
		int Q = Integer.parseInt(br.readLine());
		for(int q=0;q<Q;q++) {
			String [] tokens = br.readLine().split(" ");
			int command = Integer.parseInt(tokens[0]);
			if(command == 100) {
				int n = Integer.parseInt(tokens[1]);
				int m = Integer.parseInt(tokens[2]);
				graph = new ArrayList<>(n);
				for(int i=0;i<n;i++) {
					graph.add(new ArrayList<>());
				}
				start = 0;
				for(int i=0;i<3*m;i+=3) {
					int u = Integer.parseInt(tokens[i+3]);
					int v = Integer.parseInt(tokens[i+4]);
					int w = Integer.parseInt(tokens[i+5]);
					
					graph.get(u).add(new Node(v,w));
					graph.get(v).add(new Node(u,w));
				}
				
				dijkstra();
			}else if(command == 200) {
				int id = Integer.parseInt(tokens[1]);
				int revenue = Integer.parseInt(tokens[2]);
				int dest = Integer.parseInt(tokens[3]);
				int profit = revenue - distance[dest];
				
				Travel travel = new Travel(id,revenue,dest,profit);
				
				travelStatus[id] = AVAILABLE;
				
				if(profit>=0) {
					travelQueue.add(travel);
				}else {
					impossibleList.add(travel);
				}
				
			}else if(command == 300) {
				int id = Integer.parseInt(tokens[1]);
				travelStatus[id] = NOT_USED;
			}else if(command == 400) {
				sellTravel();
			}else if(command == 500) {
				start = Integer.parseInt(tokens[1]);
				dijkstra();
				
				List<Travel> tempTravelList = new ArrayList<>(travelQueue);
				tempTravelList.addAll(impossibleList);
				
				travelQueue.clear();
				impossibleList.clear();
				
				for(Travel travel : tempTravelList) {
					if(travelStatus[travel.id] == NOT_USED) continue;
					travel.profit = travel.revenue - distance[travel.dest];
					
					if(travel.profit >=0) {
						travelQueue.add(travel);
					}else {
						impossibleList.add(travel);
					}
				}
			}
		}
		bw.flush();
	}
}