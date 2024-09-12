import java.util.*;
import java.io.*;

public class Main {
    public static class Node{
        int id;
        int parent;
        int color;
        int maxDepth;
        List<Integer> childNode;

        public Node(){
            this.childNode = new ArrayList<>();
        }
    }    
    static final int MAX = 100000 + 5000;
    static final int MAX_DEPTH = 100 + 10;
    static final int MAX_COLOR = 5;
    
    static Node[] node = new Node[MAX];
    
    static{
        for(int i=0;i<MAX;i++){
            node[i] = new Node();
        }
    }
    static boolean [] checkRoot = new boolean[MAX];

    public static void addNode(int mId, int pId, int color, int maxDepth){
        if(pId == -1){  //루트 노드일 경우
            checkRoot[mId] = true;
        }
        if(checkRoot[mId] == true || checkMakeChild(node[pId],1)){  //노드를 생성할 수 있을 경우
            node[mId].id = mId;
            node[mId].parent = (checkRoot[mId])? 0 : pId;
            node[mId].color = color;
            node[mId].maxDepth = maxDepth;

            if(!checkRoot[mId]){
                node[pId].childNode.add(mId);
            }
        }
    }

    public static boolean checkMakeChild(Node cur, int depth){
        if(cur.id == 0) return true;
        if(cur.maxDepth <= depth) return false;

        return checkMakeChild(node[cur.parent],depth+1);
    }

    public static void changeColor(int mId, int color){
        node[mId].color = color;
        for(int i : node[mId].childNode){
            changeColor(i,color);
        }
    }
    public static void findColor(int mId) throws IOException{
        bw.write(node[mId].color+"\n");
    }

    public static int calculateScore() throws IOException{
        int score = 0;
        for(int i=1;i<=100000;i++){
            if(checkRoot[i]){   //루트일 경우에만 점수 계산
                HashSet<Integer> colorSet = new HashSet<>();
                score += calculate(node[i],colorSet);
            }
        }
        return score;
    }
    public static int calculate(Node cur, HashSet<Integer> colorSet){
        colorSet.add(cur.color);

        int sum = 0;
        for(int child :cur.childNode){
                HashSet<Integer> childColorSet = new HashSet<>();
                sum += calculate(node[child],childColorSet);
                colorSet.addAll(childColorSet);
        }
        return sum+(colorSet.size()*colorSet.size());
    }

    public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    public static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

    public static void main(String[] args) throws IOException{
        int Q = Integer.parseInt(br.readLine());    //명령의 수
        for(int i=0;i<Q;i++){
            String [] tokens = br.readLine().split(" ");
            if(tokens[0].equals("100")){    //노드 추가
                int mId = Integer.parseInt(tokens[1]);
                int pId = Integer.parseInt(tokens[2]);
                int color = Integer.parseInt(tokens[3]);
                int maxDepth = Integer.parseInt(tokens[4]);

                addNode(mId,pId,color,maxDepth);

            }else if(tokens[0].equals("200")){  //색깔 변경
                int mId = Integer.parseInt(tokens[1]);
                int color = Integer.parseInt(tokens[2]);

                changeColor(mId,color);

            }else if(tokens[0].equals("300")){  //색깔 조회
                int mId = Integer.parseInt(tokens[1]);
                findColor(mId);

            }else if(tokens[0].equals("400")){  //점수 조회
                int score = calculateScore();
                bw.write(score+"\n");
            }
        }
        bw.flush();
    }
}