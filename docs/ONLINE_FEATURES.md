# 랭킹·도전과제 실행과 구성

## 기본 실행: 공용 클라우드 서버 (Render + Neon)

게임 기본 주소는 `https://deadlight-java-game.onrender.com`입니다. 2026-11-25 Render 서버 기동과 Neon PostgreSQL 연결을 확인했습니다. 최신 `Hsg` 소스로 각자 `PLAY.cmd` 또는 IntelliJ의 `Lwjgl3Launcher`를 실행하면 같은 서버를 사용합니다. 각자의 노트북에서 `SERVER.cmd`를 실행하거나 같은 와이파이에 연결할 필요가 없습니다. 로컬 DB는 보존하며 Neon으로 자동 이전하지 않습니다. 기존 로컬 서버의 기록·업적은 클라우드에 자동 복사되지 않습니다.

`-Ddeadlight.serverUrl` JVM 옵션 또는 `DEADLIGHT_SERVER_URL` 환경변수를 설정했다면 그 주소가 기본값보다 우선합니다. 클라우드 사용 시 예전 LAN 주소 설정을 지우고 게임을 다시 실행하세요. `PLAY_LAN.cmd`는 주소를 직접 지정하는 LAN 전용 실행 방식입니다.

공용 서버 상태는 `https://deadlight-java-game.onrender.com/api/health`에서 확인합니다. 아래 항목은 서버를 다시 배포하거나 유지보수할 때 사용하는 설정입니다.

- Neon의 Free 프로젝트에 PostgreSQL을 준비합니다. Render 무료 PostgreSQL은 30일 후 만료되므로 이 구성에서는 사용하지 않습니다.
- Render에서 GitHub의 `Hsg` 브랜치를 연결하고 `render.yaml` Blueprint 또는 Docker Web Service로 생성합니다. 요금제는 Free, Dockerfile은 프로젝트 루트의 `Dockerfile`, 상태 확인 경로는 `/api/health`입니다.
- Render의 비밀 환경변수에 `DB_URL`, `DB_USER`, `DB_PASSWORD`를 입력합니다. DB 연결정보·비밀번호는 Git이나 게임 배포물에 넣지 않습니다.
- `SPRING_PROFILES_ACTIVE=postgres`, `SERVER_ADDRESS=0.0.0.0`을 사용합니다. Render의 `PORT` 값은 기존 서버 설정이 읽습니다. Docker 실행 이미지는 서버·공통 라이브러리만 포함하며 게임 이미지·DB 파일을 포함하지 않습니다.
- `DB_URL`은 Neon 주소를 JDBC 형식으로 사용합니다. 예: `jdbc:postgresql://실제-Neon-호스트/neondb?sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory`. 사용자·비밀번호는 별도 환경변수로 지정합니다.
- 무료 Render 서버가 쉬고 있을 때 HTTPS 요청은 3초 간격으로 최대 12회 재시도합니다. 각 HTTP 요청 제한은 5초입니다. 게임은 계속 조작할 수 있고 랭킹·업적 화면에는 연결 준비 상태를 표시합니다. 등록 요청은 PC에 보존하며 Esc로 화면을 나가도 전송을 이어갑니다.
- 팀원 확인: 각자 게임을 실행해 클리어 후 이니셜을 등록하고, 다른 노트북의 **여정의 기록 → 새로고침**에서 같은 기록이 보이는지 확인합니다. 서로 다른 실제 와이파이의 노트북 두 대를 이용한 최종 확인은 팀에서 수행합니다.
- GitHub를 Public Git Repository URL로 연결한 Render 서비스는 코드 Push 후 **Manual Deploy → Deploy latest commit**으로 서버를 갱신합니다. 게임 코드의 수정본도 팀원들이 다시 내려받아야 합니다. DB 비밀번호 변경은 Render 환경변수에서만 처리합니다.

PostgreSQL 검증은 실제 DB 연결정보를 테스트 전용 환경변수 `DEADLIGHT_TEST_PG_URL`, `DEADLIGHT_TEST_PG_USER`, `DEADLIGHT_TEST_PG_PASSWORD`로 전달한 뒤 `gradlew.bat server:test --tests '*GameClientIntegrationTest.postgresRankingAndIdentitySurviveServerRestart'`로 실행합니다. 테스트는 임의 이름의 격리 스키마를 만들고 정리하며 공개 랭킹에 테스트 기록을 추가하지 않습니다. 연결정보가 없으면 이 테스트는 건너뜁니다.

무료 플랜의 대기 시간·용량·사용량 제한은 서비스 정책에 따릅니다. 원격 서버가 이용 가능한 동안 노트북을 꺼도 기록은 Neon에 보관되고 다른 사용자가 접속할 수 있습니다.

## 같은 와이파이에서 함께 사용하기

1. 서버 역할을 맡은 노트북 한 대에서 `SERVER_LAN.cmd`를 실행합니다. 다른 노트북에서는 서버를 실행하지 않습니다.
2. 서버 노트북에서 `ipconfig`를 실행해 Wi-Fi의 IPv4를 확인합니다. `192.168.0.10`이라면 접속 주소는 `http://192.168.0.10:8080`입니다.
3. 참가자들은 `PLAY_LAN.cmd`를 실행하고 Windows 입력 창에 같은 주소를 입력합니다. 서버 노트북의 게임도 `PLAY_LAN.cmd`에서 `http://127.0.0.1:8080`을 지정합니다. 일반 `PLAY.cmd`의 기본값은 공용 클라우드 주소입니다. LAN 입력 창은 다음 실행에 마지막 주소를 표시합니다.
4. 참가자 노트북의 브라우저에서 `http://서버IP:8080/api/health`에 접속해 `{"status":"ok"}`가 나오는지 확인합니다. 브라우저는 진단에만 쓰이며 게임 GUI는 기존 Windows 게임 창입니다.
5. Windows 방화벽이 묻는 경우 신뢰하는 개인 네트워크에서 Java 접속을 허용합니다. 학교·게스트 와이파이의 기기 간 통신 차단은 별도로 확인합니다.

서버 노트북이 켜져 있는 동안 하나의 DB와 랭킹을 공유합니다. `SERVER_LAN.cmd`는 LAN 접속을 받는 `0.0.0.0` 바인딩을 지정합니다. 일반 `SERVER.cmd`는 기본적으로 로컬 접속만 받습니다. 서버 IPv4가 바뀌면 참가자도 새 주소를 입력해야 합니다. 같은 네트워크용이며 다른 집에서의 인터넷 접속을 자동으로 제공하지 않습니다.

## 다른 폴더에 프로젝트를 내려받은 경우

기본 H2 DB는 **사용자 홈의 `.deadlight/server/deadlight.mv.db`**에 저장합니다. 같은 Windows 사용자라면 프로젝트 위치에 관계없이 같은 DB를 사용합니다. `DB_URL`을 지정하면 그 설정을 우선합니다. 다른 Windows 사용자나 다른 서버 노트북은 별도의 DB를 사용합니다.

이전 프로젝트의 `data/deadlight.mv.db`와 `server/data/deadlight.mv.db`는 자동 삭제하거나 합치지 않습니다. 기록 이전은 서버를 종료하고 원본을 백업한 뒤 수행합니다. DB 파일은 GitHub에 커밋하지 않습니다.

서버가 플레이어를 모르면 게임은 저장된 ID·토큰으로 `/api/players/recover`를 한 번 호출하고 원래 요청을 재시도합니다. 기존 ID에 다른 토큰이 저장돼 있다면 거부하고 기존 플레이어를 덮어쓰지 않습니다. 동시 인증 실패는 복구 요청 하나로 처리합니다. 실패해도 인증정보·미전송 기록을 삭제하지 않습니다. **인증 복구는 접속을 복구하며, 이전 DB의 랭킹·업적을 자동 복원하는 기능은 아닙니다.**

## 별도 로컬 서버로 실행하기 (선택 사항)

1. 프로젝트의 `SERVER.cmd`를 실행하고 서버 시작 로그를 기다립니다. 또는 터미널에서 `gradlew.bat server:run`을 실행합니다.
2. `PLAY_LAN.cmd`에서 `http://127.0.0.1:8080`을 입력하거나 IntelliJ 게임 실행 구성의 VM 옵션에 `-Ddeadlight.serverUrl=http://127.0.0.1:8080`을 지정합니다. 게임 기본값은 클라우드이므로 로컬 서버 주소를 직접 지정해야 합니다.
3. 로비의 **여정의 기록**, **여행자의 업적**에서 접속을 확인합니다.
4. 최종 보스를 처치하면 이니셜 등록 화면이 열립니다. 방향키로 세 글자를 선택하거나 영문 키로 직접 입력하고 Enter로 등록합니다. Esc는 공개 랭킹 등록을 건너뛰고 기존 결과 화면으로 돌아갑니다.

기본 서버는 `127.0.0.1:8080`에만 열립니다. 설치 없이 실행할 수 있는 **H2 파일 DB**에 실제로 적재하며 파일은 사용자 홈의 `.deadlight/server/deadlight.mv.db`입니다. 서버를 종료했다 다시 시작해도 기록을 유지합니다. H2 콘솔과 임의 DB 조회 API는 공개하지 않습니다. 실행한 서버는 해당 터미널에서 Ctrl+C로 종료합니다.

## 랭킹 규칙

- 전체 모험과 2·3·4지역에서 시작한 연습 기록을 분리합니다. 게임 규칙 버전도 분리합니다.
- 최종 보스를 처치한 성공 기록만 등록합니다. 디버그 모드의 게임은 전송하지 않고 서버도 assisted 기록을 거부합니다.
- 시간은 기존 `RunStats`를 사용합니다. 튜토리얼·일시정지·지도·상점·가방·지역 보상·화면 전환은 제외하고, 실패한 재도전의 플레이 시간은 합산합니다.
- 기존 표시와 같은 **0.1초 단위**로 저장·정렬합니다. 동일 시간은 받은 피해가 적은 기록, 먼저 수신한 기록, 기록 ID 순으로 정렬합니다.
- 플레이어별 최고 기록 하나를 TOP 10에 표시하고, 10위 밖에서도 자신의 최고 기록과 순위를 따로 조회합니다.
- 이니셜은 A–Z 세 글자이며 중복 이름을 허용합니다. 별도의 설치별 플레이어 ID와 비밀 토큰으로 구분합니다. 서버에는 토큰의 SHA-256 해시만 저장합니다.
- 회원가입·계정 복구·기기 간 계정 공유 기능은 이번 범위에 없습니다. PC의 온라인 설정을 지우면 새 플레이어가 됩니다. 토큰을 다른 사람과 공유하면 안 됩니다.

## 도전과제

전체 모험 완료 결과를 서버가 판정해 최초 달성 일시와 함께 저장합니다. 첫 여정의 끝(첫 클리어), 꺾이지 않는 여정(재도전 0), 물약 없는 순례(포션 0), 닿지 않는 그림자(받은 피해 0)의 네 가지입니다. 연습 기록으로는 해금되지 않습니다. 능력치 보상은 없습니다.

클리어 결과는 도전과제 처리를 위해 공개되지 않은 상태로 먼저 동기화됩니다. 이니셜 화면에서 **기록 등록**을 선택해야 공개 랭킹에 나옵니다. 공개 등록을 건너뛰어도 달성한 업적은 저장됩니다. 공개된 기록의 이니셜은 해당 기록에 유지됩니다.

## 연결 실패와 재전송

HTTP 요청은 게임의 렌더링을 막지 않습니다. 응답 처리는 렌더 스레드로 전달합니다. 연결되지 않아도 플레이와 기존 개인 기록은 유지됩니다. 미전송 클리어는 로컬 outbox에 보관하며 30초 간격 또는 새로고침에서 재시도합니다. 서버 주소별로 플레이어 ID와 outbox를 분리합니다.

기록 UUID를 재사용해 같은 결과를 여러 번 보내도 기록과 업적이 중복 생성되지 않습니다. 등록 도중 프로그램이 종료되어도 재시도할 수 있습니다. 기록 내용을 바꾸어 같은 UUID로 제출하면 거부합니다. 유효하지 않은 내용은 자동 재전송 대상에서 제외하고 오류를 표시합니다. 로비 랭킹의 **기록 등록**으로 최근 클리어를 나중에 등록할 수 있습니다.

## 다른 PC에서 같은 서버 사용하기

각자의 PC에서 따로 서버를 실행하면 DB도 따로 생깁니다. **한 개의 공용 서버**를 실행하고 모든 게임이 같은 주소를 사용해야 합니다.

서버를 제공하는 PC에서 `SERVER_ADDRESS=0.0.0.0`으로 지정하면 외부 인터페이스에 바인딩합니다. 게임 PC에서는 `DEADLIGHT_SERVER_URL` 환경 변수에 공용 서버의 기본 주소를 지정하고 게임을 다시 실행합니다. JVM 옵션 `-Ddeadlight.serverUrl=...`도 지원합니다. 예를 들어 같은 LAN의 서버 주소가 192.168.0.10이면 `http://192.168.0.10:8080`을 사용합니다. 서버 주소를 바꿔도 예전 주소의 플레이어 설정을 덮어쓰지 않습니다.

기본 공용 서버는 Render의 HTTPS 주소를 사용하고 DB는 Neon에 저장합니다. 별도로 다른 서버를 인터넷에 공개한다면 호스팅·HTTPS·접근 설정이 필요합니다. 이 프로젝트에는 별도 요청 속도 제한이나 리플레이 검증이 구현되어 있지 않습니다.

서버는 형식·범위·인증·중복 제출을 검증하지만, 클라이언트가 보낸 플레이 결과를 완전히 입증하는 리플레이 검증 서버는 아닙니다. 공개 경쟁 서비스의 부정행위 방지는 후속 범위입니다.

## MySQL로 전환

MySQL 8 이상에서 `deadlight` 데이터베이스와 전용 사용자를 준비한 뒤, 서버 실행 환경에 아래 값을 지정합니다. DB 테이블은 서버가 생성합니다. 클라이언트 게임에는 DB 비밀번호를 넣지 않습니다.

```powershell
$env:SPRING_PROFILES_ACTIVE='mysql'
$env:DB_URL='jdbc:mysql://127.0.0.1:3306/deadlight?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC'
$env:DB_USER='deadlight'
$env:DB_PASSWORD='본인이_지정한_DB_비밀번호'
.\gradlew.bat server:run
```

선택적으로 `server/compose.yaml`을 사용해 MySQL을 띄울 수 있습니다. `DB_PASSWORD`, `MYSQL_ROOT_PASSWORD`를 먼저 설정하고 `docker compose -f server/compose.yaml up -d`를 실행합니다. Docker/MySQL 설치나 컨테이너 실행은 자동 수행하지 않습니다. 기본 H2 데이터를 MySQL로 자동 이전하지는 않습니다.

## 서버 API와 DB

| API | 용도 |
|---|---|
| `GET /api/health` | 서버 상태 |
| `POST /api/players` | 플레이어 ID·비밀 토큰 생성 |
| `POST /api/players/recover` | 저장된 인증정보로 누락된 플레이어 복구 |
| `POST /api/runs` | 클리어 동기화·공개 등록·업적 판정 |
| `GET /api/leaderboard?rulesVersion=11&startStage=1&limit=10` | TOP 10과 인증한 사용자의 최고 기록 |
| `GET /api/achievements` | 개인 업적 목록·달성 일시 |

개인 API는 `Authorization: Bearer <token>`을 사용합니다. 공개 랭킹 조회에 토큰이 없으면 개인 기록을 제외합니다. DB는 `players`, `game_runs`, `player_achievements` 세 테이블이며 `server/src/main/resources/schema.sql`에서 확인할 수 있습니다. 게임에 개인 전적 목록 화면이나 이어하기 기능을 추가하지 않습니다.

## 개발·검증

- 서버 구현: `server` 모듈의 Spring Boot·JDBC, 기본 H2 및 MySQL 프로파일.
- 공통 요청·응답·도전과제 정의: `online-common` 모듈. 게임 런타임에 Spring을 포함하지 않습니다.
- 게임 연결: `OnlineService`의 비동기 통신/재전송, `OnlineScreen`의 등록/랭킹/업적 화면, 기존 `Main`·`LobbyView`의 진입점.
- `gradlew.bat core:test server:test lwjgl3:classes`로 실제 HTTP·SQL, 서버 재시작, 두 게임 클라이언트의 공유 랭킹, 인증·중복·연습 분리·클라이언트 재전송을 검증합니다.
- `gradlew.bat lwjgl3:onlinePreview`는 사용자 설정을 사용하지 않고 숨겨진 OpenGL 창에서 실제 UI 코드를 렌더링합니다. `lwjgl3/build/online-preview`에 기본·작은 화면·큰 글씨의 화면 PNG를 저장합니다.
- 배포용 서버 실행 파일/라이브러리는 `gradlew.bat server:installDist`로 만들 수 있습니다. DB 연결과 서버 주소는 배포 환경에서 지정합니다.
