import java.util.*;
import java.io.*;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

	static class Santa {
		int num;
		int r;
		int c;
		int score;
		boolean status;
		int movableTurn;

		public Santa(int num, int r, int c, boolean status, int score, int movableTurn) {
			this.num = num;
			this.r = r;
			this.c = c;
			this.status = status;
			this.score = score;
			this.movableTurn = movableTurn;
		}

		public void exchangeStatus() {
			this.status = !this.status;
		}

		public void plusScore(int additionalScore) {
			this.score += additionalScore;
		}

		public String toString() {
			return "Num : " + this.num + " R : " + this.r + " C : " + this.c + " Status : " + this.status + " Score : "
					+ this.score + " MovableTurn : " + this.movableTurn;
		}
	}

	static class Rudolph {
		int r;
		int c;

		public Rudolph(int r, int c) {
			this.r = r;
			this.c = c;
		}

		public String toString() {
			return "R : " + this.r + " C : " + this.c;
		}
	}

	static Santa nearSanta;

	static int N; // 게임판의 크기
	static int M; // 게임 턴 수
	static int P; // 산타의 수
	static int C; // 루돌프의 힘
	static int D; // 산타의 힘

	static List<Santa> santaList = new ArrayList<>(P); // 산타 리스트
	static Santa[][] santaMap;

	// 남 동남 동 동북 북 북서 서 남서
	static int[] dx = { 1, 1, 0, -1, -1, -1, 0, 1 };
	static int[] dy = { 0, 1, 1, 1, 0, -1, -1, -1 };
	
	// 상 우 하 좌
	
	static int[] santaDx = { -1, 0, 1, 0 };
	static int[] santaDy = { 0, 1, 0, -1 };

	static Rudolph rudolph;

	public static void main(String[] args) throws IOException {
		String[] tokens = br.readLine().split(" ");
		N = Integer.parseInt(tokens[0]);
		M = Integer.parseInt(tokens[1]);
		P = Integer.parseInt(tokens[2]);
		C = Integer.parseInt(tokens[3]);
		D = Integer.parseInt(tokens[4]);

		tokens = br.readLine().split(" ");
		int rx = Integer.parseInt(tokens[0]) - 1; // 루돌프의 초기위치
		int ry = Integer.parseInt(tokens[1]) - 1; // 루돌프의 초기위치
		santaMap = new Santa[N][N];

		rudolph = new Rudolph(rx, ry); // 루돌프 객체 생성

		for (int i = 0; i < P; i++) {
			tokens = br.readLine().split(" ");
			int santaNum = Integer.parseInt(tokens[0]);
			int santaX = Integer.parseInt(tokens[1]) - 1;
			int santaY = Integer.parseInt(tokens[2]) - 1;
			Santa santa = new Santa(santaNum, santaX, santaY, true, 0, 1);
			santaList.add(santa);
			santaMap[santa.r][santa.c] = santa;
		}

		Collections.sort(santaList, new Comparator<Santa>() {
			public int compare(Santa o1, Santa o2) {
				return o1.num - o2.num;
			}
		});

		for (int i = 1; i <= M; i++) {
			moveRudolph(i);
			moveSanta(i);
			for (Santa santa : santaList) {
				if (santa.status) {
					santa.score++;
				}
			}
			if(!checkStatus()) {
				break;
			}
		}
		for(Santa santa : santaList) {
			System.out.print(santa.score + " ");
		}
	}

	static boolean checkStatus() {
		for(Santa santa : santaList) {
			if(santa.status) {
				return true;
			}
		}
		return false;
	}
	public static void moveRudolph(int turn) {
		int minDistance = Integer.MAX_VALUE;

		// 가장 가까운 산타 선택
		for (Santa santa : santaList) {
			if (!santa.status)
				continue; // 탈락한 산타는 무시

			int rudolphToSantaDistance = calcDistance(santa.r, santa.c, rudolph.r, rudolph.c);
			if (minDistance > rudolphToSantaDistance) {
				nearSanta = santa;
				minDistance = rudolphToSantaDistance;
			} else if (minDistance == rudolphToSantaDistance) {
				// 거리가 같은 경우 r 값이 큰 산타를 선택, r 값이 같으면 c 값이 큰 산타
				if (nearSanta.r < santa.r || (nearSanta.r == santa.r && nearSanta.c < santa.c)) {
					nearSanta = santa;
				}
			}
		}

		// 가까운 산타를 향해 돌진할 방향 계산
		int direction = checkDirection();

		// 루돌프 한 칸 이동
		rudolph.r += dx[direction];
		rudolph.c += dy[direction];

		// 루돌프가 이동한 위치에 산타가 있다면 충돌 처리
		if (santaMap[rudolph.r][rudolph.c] != null) {
			Santa santa = santaMap[rudolph.r][rudolph.c];

			// 산타가 C만큼 밀려나는 처리
			santaMap[santa.r][santa.c] = null; // 기존 위치에서 산타 제거
			santa.score += C; // 점수 추가
			santa.movableTurn = turn + 2; // 기절 처리 (2턴 기절)

			// 산타가 C칸만큼 밀려남 (루돌프가 온 방향으로)
			int newSantaR = santa.r + dx[direction] * C;
			int newSantaC = santa.c + dy[direction] * C;

			// 경계 안에 있는지 체크
			if (inRange(newSantaR, newSantaC)) {
				// 밀려난 위치에 다른 산타가 있는지 체크
				if (santaMap[newSantaR][newSantaC] != null) {
					Santa nextSanta = santaMap[newSantaR][newSantaC];
					chainReaction(nextSanta, direction); // 연쇄 반응 처리
				}

				// 밀려난 위치에 산타를 업데이트
				santa.r = newSantaR;
				santa.c = newSantaC;
				santa.status = true; // 경계 안에 있으므로 상태를 정상으로 유지
				santaMap[santa.r][santa.c] = santa; // 새로운 위치에 산타 저장
			} else {
				// 경계 밖으로 나가면 산타 탈락
				santa.status = false;
			}
		}
	}

	public static void moveSanta(int turn) {
	    for (int i = 0; i < santaList.size(); i++) {
	        Santa santa = santaList.get(i);

	        if (!santa.status) continue; // 탈락한 산타는 움직이지 않음
	        if (santa.movableTurn > turn) continue; // 기절 중인 산타는 움직이지 않음

	        int minDistance = calcDistance(santa.r,santa.c, rudolph.r, rudolph.c);
	        int bestDirection = -1;

	        // 상, 우, 하, 좌 순으로 움직일 수 있는 방향 탐색 (우선순위)
	        for (int j = 0; j < 4; j++) {
	            int newSantaR = santa.r + santaDx[j];
	            int newSantaC = santa.c + santaDy[j];

	            // 경계를 벗어나는 경우 건너뜀
	            if (!inRange(newSantaR, newSantaC)) continue;

	            // 다른 산타가 있는 경우 건너뜀
	            if (santaMap[newSantaR][newSantaC] != null) continue;

	            // 루돌프와 가까워지는지 확인
	            int newDistance = calcDistance(newSantaR, newSantaC, rudolph.r, rudolph.c);
	            if (newDistance < minDistance) {
	                minDistance = newDistance;
	                bestDirection = j;
	            }
	        }

	        // 가장 가까워지는 방향으로 이동
	        if (bestDirection != -1) {
	            santaMap[santa.r][santa.c] = null; // 기존 위치 제거
	            santa.r += santaDx[bestDirection];
	            santa.c += santaDy[bestDirection];
	            santaMap[santa.r][santa.c] = santa; // 새 위치 등록
	        }

	        // 루돌프와 충돌 처리 (필요 시)
	        if (rudolph.r == santa.r && rudolph.c == santa.c) {
	            santa.score += D; // 충돌 시 점수 추가

	            // 산타가 움직인 반대 방향으로 팅겨나감
	            if (bestDirection != -1) {
	                int reverseDirection = getReverseDirection(bestDirection);
	                
	                // 반대 방향으로 D - 산타와 루돌프 간의 거리만큼 이동
	                int newSantaR = santa.r + santaDx[reverseDirection] * D;
	                int newSantaC = santa.c + santaDy[reverseDirection] * D;

	                // 팅겨나간 좌표가 바깥이면 탈락 처리
	                if (!inRange(newSantaR, newSantaC)) {
	                    santa.status = false;
	                    santaMap[santa.r][santa.c] = null; // 기존 위치 제거
	                } else {
	                    // 팅겨나간 위치에 다른 산타가 있으면 연쇄 반응 처리
	                    santaMap[santa.r][santa.c] = null; // 기존 위치 제거
	                    if (santaMap[newSantaR][newSantaC] != null) {
	                    	chainSantaReaction(santaMap[newSantaR][newSantaC], reverseDirection);
	                    }

	                    // 산타의 새로운 위치 업데이트
	                    santa.r = newSantaR;
	                    santa.c = newSantaC;
	                    santaMap[santa.r][santa.c] = santa;

	                    // 기절 처리 (2턴 기절)
	                    santa.movableTurn = turn + 2;
	                }
	            }
	        }
	    }
	}
	
	static int getReverseDirection(int direction) {
	    switch (direction) {
	        case 0: // 상 -> 하
	            return 2;
	        case 1: // 우 -> 좌
	            return 3;
	        case 2: // 하 -> 상
	            return 0;
	        case 3: // 좌 -> 우
	            return 1;
	        default:
	            return -1; // 잘못된 방향인 경우
	    }
	}
	//연쇄 작용 처리
	//루돌프가 산타를 박았을 경우
	static void chainReaction(Santa originSanta, int direction) {
		int originSantaR = originSanta.r;
		int originSantaC = originSanta.c;

		int newSantaR = originSantaR + dx[direction];
		int newSantaC = originSantaC + dy[direction];

		// 경계 체크
		if (!inRange(newSantaR, newSantaC)) {
			originSanta.status = false; // 경계 밖으로 나가면 status를 false로 설정
			santaMap[originSantaR][originSantaC] = null; // 기존 위치에서 제거
			return; // 경계 밖이면 연쇄작용 중단
		}

		if (santaMap[newSantaR][newSantaC] != null) { // 연쇄작용 발생
			Santa moveSanta = santaMap[newSantaR][newSantaC]; // 이동해야 하는 산타
			chainReaction(moveSanta,direction);

			// originSanta의 새로운 위치에 저장
			santaMap[originSantaR][originSantaC] = null; // 기존 위치에서 제거
			santaMap[newSantaR][newSantaC] = originSanta; // 새로운 위치에 저장
			originSanta.r = newSantaR;
			originSanta.c = newSantaC;

		} else {
			// 만약 연쇄작용이 일어나지 않으면 originSanta가 이동
			santaMap[originSantaR][originSantaC] = null; // 기존 위치에서 제거
			santaMap[newSantaR][newSantaC] = originSanta; // 새로운 위치에 저장
			originSanta.r = newSantaR;
			originSanta.c = newSantaC;
			originSanta.status = true; // 경계 안에 있으므로 status를 true로 설정
		}
	}
	
	//연쇄 작용 처리
	//산타가 루돌프를 박았을 경우
	static void chainSantaReaction(Santa originSanta, int direction) {
		int originSantaR = originSanta.r;
		int originSantaC = originSanta.c;

		int newSantaR = originSantaR + santaDx[direction];
		int newSantaC = originSantaC + santaDy[direction];

		// 경계 체크
		if (!inRange(newSantaR, newSantaC)) {
//			System.out.println("originSanta가 경계 밖으로 이동");
			originSanta.status = false; // 경계 밖으로 나가면 status를 false로 설정
			santaMap[originSantaR][originSantaC] = null; // 기존 위치에서 제거
			return; // 경계 밖이면 연쇄작용 중단
		}

		if (santaMap[newSantaR][newSantaC] != null) { // 연쇄작용 발생
			Santa moveSanta = santaMap[newSantaR][newSantaC]; // 이동해야 하는 산타
			int newMoveSantaR = moveSanta.r + dx[direction];
			int newMoveSantaC = moveSanta.c + dy[direction];

			// 경계 체크
			if (!inRange(newMoveSantaR, newMoveSantaC)) {
//				System.out.println("moveSanta가 경계 밖으로 이동");
				moveSanta.status = false; // 경계 밖으로 나가면 status를 false로 설정
				santaMap[moveSanta.r][moveSanta.c] = null; // 기존 위치에서 제거
				return; // 경계 밖이면 연쇄작용 중단
			}

			// moveSanta를 새로운 위치로 이동
			santaMap[moveSanta.r][moveSanta.c] = null; // 기존 위치에서 제거
			santaMap[newMoveSantaR][newMoveSantaC] = moveSanta; // 새로운 위치에 저장
			moveSanta.r = newMoveSantaR;
			moveSanta.c = newMoveSantaC;
			moveSanta.status = true; // 경계 안에 있으므로 status를 true로 설정

			// originSanta의 새로운 위치에 저장
			santaMap[originSantaR][originSantaC] = null; // 기존 위치에서 제거
			santaMap[newSantaR][newSantaC] = originSanta; // 새로운 위치에 저장
			originSanta.r = newSantaR;
			originSanta.c = newSantaC;

			// moveSanta에 대해 다시 연쇄작용 호출
			chainSantaReaction(moveSanta, direction);
		} else {
			// 만약 연쇄작용이 일어나지 않으면 originSanta가 이동
			santaMap[originSantaR][originSantaC] = null; // 기존 위치에서 제거
			santaMap[newSantaR][newSantaC] = originSanta; // 새로운 위치에 저장
			originSanta.r = newSantaR;
			originSanta.c = newSantaC;
			originSanta.status = true; // 경계 안에 있으므로 status를 true로 설정
		}
	}

	// 새로운 산타의 좌표
	public static Santa calcNewSantaPosition(Santa santa, int direction) {
		int newSantaR = santa.r + dx[direction];
		int newSantaC = santa.c + dy[direction];
		boolean status = inRange(newSantaR, newSantaC);

		return new Santa(santa.num, newSantaR, newSantaC, status, santa.score, santa.movableTurn);
	}

	public static int checkDirection() {
		int differenceR = rudolph.r - nearSanta.r; // 가로
		int differenceC = rudolph.c - nearSanta.c; // 세로

		int signR = Integer.signum(differenceR); // differenceR의 부호 (-1, 0, 1)
		int signC = Integer.signum(differenceC); // differenceC의 부호 (-1, 0, 1)

		if (signR == -1 && signC == 0) { // 남
			return 0;
		} else if (signR == -1 && signC == -1) { // 남동
			return 1;
		} else if (signR == 0 && signC == -1) { // 동
			return 2;
		} else if (signR == 1 && signC == -1) { // 동북
			return 3;
		} else if (signR == 1 && signC == 0) { // 북
			return 4;
		} else if (signR == 1 && signC == 1) { // 북서
			return 5;
		} else if (signR == 0 && signC == 1) { // 서
			return 6;
		} else if (signR == -1 && signC == 1) { // 남서
			return 7;
		} else {
			return 0;
		}
	}

	public static boolean inRange(int r, int c) {
		if (r >= 0 && r < N && c >= 0 && c < N) {
			return true;
		}
		return false;
	}

	public static int calcDistance(int sx, int sy, int ex, int ey) {
		int absX = Math.abs(sx - ex);
		int absY = Math.abs(sy - ey);

		return (int) Math.pow(absX, 2) + (int) Math.pow(absY, 2);
	}

//	상 우 하 좌 
	static int calcSantaDirection(int direction) {
		switch (direction) {
		case 0:
			return 2;
		case 1:
			return 3;
		case 2:
			return 0;
		case 3:
			return 1;
		default:
			return -1;
		}
	}
}