import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

    static int Q;
    static TreeMap<Integer, String> dbMap = new TreeMap<>(); // 가격-상품 저장
    static Map<String, Integer> itemMap = new HashMap<>(); // 상품-가격 저장

    public static void main(String[] args) throws IOException {
        Q = Integer.parseInt(br.readLine());
        for (int q = 1; q <= Q; q++) {
            String[] input = br.readLine().split(" ");
            if (input[0].equals("init")) {
                dbMap.clear();
                itemMap.clear();
            } else if (input[0].equals("insert")) {
                String name = input[1];
                int value = Integer.parseInt(input[2]);

                if (itemMap.containsKey(name) || dbMap.containsKey(value)) {
                    // 이름이나 가격 중복 시 0 출력
                    System.out.println("0");
                } else {
                    dbMap.put(value, name); // 가격에 해당하는 상품 추가
                    itemMap.put(name, value); // 상품에 해당하는 가격 추가
                    System.out.println("1");
                }
            } else if (input[0].equals("delete")) {
                String name = input[1];
                if (!itemMap.containsKey(name)) {
                    // 이름이 없으면 0 출력
                    System.out.println("0");
                } else {
                    int price = itemMap.get(name);
                    itemMap.remove(name); // 상품 제거
                    dbMap.remove(price);  // 가격에서 해당 상품 제거
                    System.out.println(price);
                }
            } else if (input[0].equals("rank")) {
                int rank = Integer.parseInt(input[1]);

                // 전체 아이템 개수 확인
                if (dbMap.size() < rank) {
                    System.out.println("None");
                } else {
                    int cnt = 0;
                    for (Map.Entry<Integer, String> entry : dbMap.entrySet()) {
                        cnt++;
                        if (cnt == rank) {
                            System.out.println(entry.getValue());
                            break;
                        }
                    }
                }
            } else if (input[0].equals("sum")) {
                int sum = 0;
                int k = Integer.parseInt(input[1]);

                for (Map.Entry<Integer, String> entry : dbMap.entrySet()) {
                    if (entry.getKey() > k) break; // 가격이 k를 초과하면 종료
                    sum += entry.getKey(); // 가격을 더함
                }
                System.out.println(sum);
            }
        }
    }
}