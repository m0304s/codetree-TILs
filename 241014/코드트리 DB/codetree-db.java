import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

    static int Q;
    static TreeMap<Integer, String> dbMap = new TreeMap<>(); // value-상품명 저장
    static HashMap<String, Integer> itemMap = new HashMap<>(); // 상품명-value 저장

    public static void main(String[] args) throws IOException {
        Q = Integer.parseInt(br.readLine()); // 쿼리 수 입력

        for (int q = 0; q < Q; q++) {
            String[] input = br.readLine().split(" "); // 쿼리 입력 분리

            if (input[0].equals("init")) {
                dbMap.clear();
                itemMap.clear();
            } else if (input[0].equals("insert")) {
                String name = input[1];
                int value = Integer.parseInt(input[2]);

                // 중복 여부 확인
                if (itemMap.containsKey(name) || dbMap.containsKey(value)) {
                    bw.write("0\n");
                } else {
                    dbMap.put(value, name);  // value에 해당하는 상품명 추가
                    itemMap.put(name, value); // 상품명에 해당하는 value 추가
                    bw.write("1\n");
                }
            } else if (input[0].equals("delete")) {
                String name = input[1];

                if (!itemMap.containsKey(name)) {
                    bw.write("0\n");
                } else {
                    int value = itemMap.get(name);
                    itemMap.remove(name); // 상품명 제거
                    dbMap.remove(value);  // 해당하는 value 제거
                    bw.write(value + "\n");
                }
            } else if (input[0].equals("rank")) {
                int rank = Integer.parseInt(input[1]);

                if (dbMap.size() < rank) {
                    bw.write("None\n");
                } else {
                    int cnt = 0;
                    for (Map.Entry<Integer, String> entry : dbMap.entrySet()) {
                        cnt++;
                        if (cnt == rank) {
                            bw.write(entry.getValue() + "\n");
                            break;
                        }
                    }
                }
            } else if (input[0].equals("sum")) {
                int k = Integer.parseInt(input[1]);
                int sum = 0;

                // k 이하의 value 합산
                for (Map.Entry<Integer, String> entry : dbMap.headMap(k, true).entrySet()) {
                    sum += entry.getKey();
                }
                bw.write(sum + "\n");
            }
        }
        bw.flush();
    }
}