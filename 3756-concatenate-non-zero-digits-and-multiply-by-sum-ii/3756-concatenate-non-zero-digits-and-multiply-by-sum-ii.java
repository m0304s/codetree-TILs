import java.util.*;

class Solution {
    public int[] sumAndMultiply(String s, int[][] queries) {
        int m = s.length();
        long MOD = 1000000007L;
        
        int[] cnt = new int[m + 1];       // non-zero 숫자 개수 누적
        long[] sumPref = new long[m + 1]; // 자릿수 합 누적
        long[] xPref = new long[m + 1];   // 가중치 반영된 x 누적
        
        // 10의 거듭제곱을 미리 계산
        long[] power10 = new long[m + 1];
        power10[0] = 1;
        for (int i = 1; i <= m; i++) {
            power10[i] = (power10[i - 1] * 10) % MOD;
        }
        
        for (int i = 0; i < m; i++) {
            char ch = s.charAt(i);
            int digit = ch - '0';
            
            if (digit != 0) {
                cnt[i + 1] = cnt[i] + 1;
                sumPref[i + 1] = (sumPref[i] + digit) % MOD;
                xPref[i + 1] = (xPref[i] * 10 + digit) % MOD;
            } else {
                cnt[i + 1] = cnt[i];
                sumPref[i + 1] = sumPref[i];
                xPref[i + 1] = xPref[i]; // 0일 때는 이전 값 유지
            }
        }
        
        int q = queries.length;
        int[] answer = new int[q];
        
        for (int i = 0; i < q; i++) {
            int L = queries[i][0];
            int R = queries[i][1];
            
            // 구간 내 non-zero 숫자의 개수
            int nonZeroCount = cnt[R + 1] - cnt[L];
            
            if (nonZeroCount == 0) {
                answer[i] = 0;
                continue;
            }
            
            // 구간 자릿수 합 구하기
            long sum = (sumPref[R + 1] - sumPref[L] + MOD) % MOD;
            
            // 구간 이어 붙인 수 구하기
            long totalX = xPref[R + 1];
            long leftPart = (xPref[L] * power10[nonZeroCount]) % MOD;
            long x = (totalX - leftPart + MOD) % MOD;
            
            answer[i] = (int) ((x * sum) % MOD);
        }
        
        return answer;
    }
}