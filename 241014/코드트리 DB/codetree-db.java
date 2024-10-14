import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

    static int Q;
    static TreeMap<Integer,ArrayList<String>> dbMap = new TreeMap<>();
    static Set<String> dbNameSet = new HashSet<>();
    static Map<String,Integer> itemMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        Q = Integer.parseInt(br.readLine());
        for(int q=1;q<=Q;q++){
            String[] input = br.readLine().split(" ");
            if(input[0].equals("init")){
                dbMap.clear();
                dbNameSet.clear();
                itemMap.clear();
            }else if(input[0].equals("insert")){
                String name = input[1];
                int value = Integer.parseInt(input[2]);

                //value 값을 가진 상품 리스트
                if(dbNameSet.contains(name)){
                    System.out.println("0");
                }else{
                    if(dbMap.get(value) == null){
                        ArrayList<String> itemList = new ArrayList<>();
                        itemList.add(name);
                        dbMap.put(value,itemList);
                        dbNameSet.add(name);
                        itemMap.put(name,value);
                        System.out.println("1");
                    }else if(dbMap.keySet().contains(value)){
                        System.out.println("0");
                    }else{
                        ArrayList<String> itemList = dbMap.get(value);

                        if(itemList.contains(name)){
                            System.out.println("0");
                        }else{
                            itemList.add(name);
                            dbNameSet.add(name);
                            itemMap.put(name,value);
                            System.out.println("1");
                        }
                    }
                }
            }else if(input[0].equals("delete")){
                String name = input[1];
                if(!dbNameSet.contains(name)){
                    //같은 이름을 가지는 Row가 없으면 0출력
                    System.out.println("0");
                }else{
                    int price = itemMap.get(name);
                    itemMap.remove(name);
                    dbNameSet.remove(name);
                    ArrayList<String> itemList = dbMap.get(price);
                    itemList.remove(name);

                    //상품 제거 후 동일한 값을 가지는 상품이 없을 경우 dbMap.keySet()에서도 제거
                    if(itemList.size() == 0){
                        dbMap.remove(price);
                    }
                    System.out.println(price);
                }
            }else if(input[0].equals("rank")){
                int rank = Integer.parseInt(input[1]);
                //만약 row의 수가 k보다 작을 경우 None 출력
                // K번째로 작은 value 가진 Row의 Name 출력
                Set<Integer> keySet = dbMap.keySet();
                int totalCnt = 0;
                for (Integer i : keySet) {
                    totalCnt+=dbMap.get(i).size();
                }
                if(totalCnt<rank){
                    System.out.println("None");
                }else{
                    int cnt = 0;
                    for(Integer i : keySet){
                        boolean stop = false;
                        ArrayList<String> itemList = dbMap.get(i);
                        for (String s : itemList) {
                            cnt++;
                            if(cnt==rank){
                                System.out.println(s);
                                stop = true;
                                break;
                            }
                        }
                        if(stop) break;
                    }
                }
            }else if(input[0].equals("sum")){
                int sum = 0;
                int k = Integer.parseInt(input[1]);
                Set<Integer> keySet = dbMap.keySet();
                for (Integer i : keySet) {
                    if(i>k) break;

                    ArrayList<String> itemList = dbMap.get(i);
                    sum+=(itemList.size())*i;
                }
                System.out.println(sum);
            }
        }
    }
}