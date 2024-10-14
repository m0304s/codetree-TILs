import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

    static int Q;
    static TreeMap<Integer, String> dbMap = new TreeMap<>(); // 가격-상품 저장
    static Map<String, Integer> itemMap = new HashMap<>(); // 상품-가격 저장
    static long totalSum = 0;  // 모든 가격의 누적 합 저장

    public static void main(String[] args) throws IOException {
        Q = Integer.parseInt(br.readLine());
        for (int q = 1; q <= Q; q++) {
            String[] input = br.readLine().split(" ");
            if (input[0].equals("init")) {
                dbMap.clear();
                itemMap.clear();
                totalSum = 0;  // 초기화 시 누적합도 초기화
            } else if (input[0].equals("insert")) {
                String name = input[1];
                int value = Integer.parseInt(input[2]);

                if (itemMap.containsKey(name) || dbMap.containsKey(value)) {
                    // 이름이나 가격 중복 시 0 출력
                    System.out.println("0");
                } else {
                    dbMap.put(value, name); // 가격에 해당하는 상품 추가
                    itemMap.put(name, value); // 상품에 해당하는 가격 추가
                    totalSum += value; // 누적합에 가격 추가
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
                    totalSum -= price; // 누적합에서 가격 제거
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
                int k = Integer.parseInt(input[1]);
                long sum = totalSum;  // 전체 누적합을 미리 계산

                // k보다 큰 가격들을 제외
                NavigableMap<Integer, String> overPriceMap = dbMap.tailMap(k, false);
                for (Integer price : overPriceMap.keySet()) {
                    sum -= price;
                }

                System.out.println(sum);
            }
        }
    }
}