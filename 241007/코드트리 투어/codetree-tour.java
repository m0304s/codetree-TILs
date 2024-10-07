import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
    
    static final int MAX = 3000;
    static final int MAX_TRAVEL_ID = 30500;
    static final int NOT_USED = 1;
    static final int AVAILABLE = 0;

    static int [] distance = new int[MAX];
    static int [] travelStatus = new int[MAX_TRAVEL_ID];
    static ArrayList<ArrayList<Node>> graph;
    static List<Travel> impossibleList = new ArrayList<>();
    static PriorityQueue<Travel> travelQueue = new PriorityQueue<>();

    static int Q;
    static int start;

    static class Node implements Comparable<Node>{
        int dest;
        int weight;

        public Node(int dest, int weight){
            this.dest = dest;
            this.weight = weight;
        }

        public int compareTo(Node o){
            return Integer.compare(this.weight, o.weight);
        }
    }
    
    static class Travel implements Comparable<Travel>{
        int travelId;
        int revenue;
        int dest;
        int profit;

        public Travel(int travelId, int revenue, int dest, int profit){
            this.travelId = travelId;
            this.revenue = revenue;
            this.dest = dest;
            this.profit = profit;
        }

        public int compareTo(Travel o){
            if(this.profit == o.profit){
                return Integer.compare(this.travelId, o.travelId);
            }else{
                return Integer.compare(o.profit, this.profit);
            }
        }
    }

    static void dijkstra(){
        boolean [] visited = new boolean[MAX];
        Arrays.fill(distance, Integer.MAX_VALUE);

        distance[start] = 0;    //시작 지점은 거리가 0임
        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(new Node(start,0));

        while(!pq.isEmpty()){
            Node cur = pq.poll();

            if(visited[cur.dest]) continue;

            visited[cur.dest] = true;

            for(Node neighbor : graph.get(cur.dest)){
                if(distance[neighbor.dest] > distance[cur.dest] + neighbor.weight){
                    distance[neighbor.dest] = distance[cur.dest] + neighbor.weight;
                    pq.add(new Node(neighbor.dest,distance[neighbor.dest]));
                }
            }
        }
    }
    //최적 여행 상품 판매
    static void sellTravel() throws IOException{
        while(!travelQueue.isEmpty()){
            Travel product = travelQueue.poll();
            if(travelStatus[product.travelId] == NOT_USED) continue;

            travelStatus[product.travelId] = NOT_USED;
            bw.write(product.travelId + "\n");
            return;
        }
        bw.write("-1\n");
    }

    public static void main(String[] args) throws IOException{
        Q = Integer.parseInt(br.readLine());
        for(int q=0;q<Q;q++){
            String [] tokens = br.readLine().split(" ");
            int command = Integer.parseInt(tokens[0]);
            switch (command) {
                case 100:
                    int n = Integer.parseInt(tokens[1]);
                    int m = Integer.parseInt(tokens[2]);

                    start = 0;
                    graph = new ArrayList<>(n);

                    for(int i=0;i<n;i++){
                        graph.add(new ArrayList<>());
                    }

                    for(int i=0;i<m*3;i+=3){
                        int v = Integer.parseInt(tokens[i+3]);
                        int u = Integer.parseInt(tokens[i+4]);
                        int w = Integer.parseInt(tokens[i+5]);
                        
                        graph.get(v).add(new Node(u, w));
                        graph.get(u).add(new Node(v, w));
                    }
                    dijkstra();
                    break;
                case 200:
                    int travelId = Integer.parseInt(tokens[1]);
                    int revenue = Integer.parseInt(tokens[2]);
                    int dest = Integer.parseInt(tokens[3]);
                    int profit = revenue - distance[dest];

                    Travel travel = new Travel(travelId, revenue, dest, profit);
                    travelStatus[travelId] = AVAILABLE;

                    if(profit >= 0){
                        travelQueue.add(travel);
                    }else{
                        impossibleList.add(travel);
                    }
                    break;
                case 300:
                    int id = Integer.parseInt(tokens[1]);
                    travelStatus[id] = NOT_USED;
                    break;
                case 400:
                    sellTravel();
                    break;
                case 500:
                    start = Integer.parseInt(tokens[1]);
                    //출발지 변경 -> 거리 새롭게 측정
                    dijkstra();

                    List<Travel> tempTravelList = new ArrayList<>(travelQueue);
                    tempTravelList.addAll(impossibleList);

                    travelQueue.clear();
                    impossibleList.clear();

                    for(Travel t : tempTravelList){
                        if(travelStatus[t.travelId] == NOT_USED) continue;

                        t.profit = t.revenue - distance[t.dest];
                        if(t.profit >= 0){
                            travelQueue.add(t);
                        }else{
                            impossibleList.add(t);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        bw.flush();
        bw.close();
        br.close();
    }
}