# Deadlight: 노트북 서버 + 터널

호스트 한 명이 Spring Boot 기록 서버와 터널을 실행합니다. 참가자는 Windows 게임에서 전달받은 HTTPS 주소로 접속합니다. 서로 다른 와이파이에서도 같은 랭킹을 사용하며, 호스트가 게임을 켜둘 필요는 없습니다.

## 호스트

1. 프로젝트 루트의 `SERVER.cmd`를 실행하고 `Started RecordServer` 메시지를 기다립니다.
2. `TUNNEL.cmd`를 실행합니다. GitHub 소스를 처음 받은 경우 공식 cloudflared를 다운로드하고 저장된 SHA-256을 확인합니다. Java 포함 실행 ZIP에는 도구도 포함됩니다.
3. 같은 폴더의 `tunnel-address.txt`에 저장된 `https://...trycloudflare.com` 주소를 팀원에게 전달합니다.
4. 본인도 플레이하려면 `PLAY_HOST.cmd`를 실행합니다. 서버 주소 입력 없이 `http://127.0.0.1:8080`에 연결합니다.
5. 팀원이 온라인 기능을 사용하는 동안 서버 창·터널 창·노트북·인터넷을 유지합니다. 종료할 때는 각 콘솔에서 Ctrl+C를 누릅니다.

서버는 기본적으로 `127.0.0.1:8080`에서만 요청을 받고 터널이 외부 접속을 전달합니다. 터널은 밖으로 나가는 연결을 사용하므로 별도의 공유기 포트 개방을 요구하지 않습니다. 네트워크가 터널 통신을 차단하면 연결되지 않을 수 있습니다.

## 팀원

1. 프로젝트 또는 Java 포함 실행 ZIP을 전체 압축 해제합니다.
2. 파일 탐색기에서 `PLAY.cmd`를 실행합니다.
3. **여정에 합류하기**에서 받은 HTTPS 주소를 붙여 넣고 **연결 후 실행**을 선택합니다. 연결에 성공한 HTTPS 주소는 다음 실행 때 자동으로 채워집니다.
4. 로비의 **여정의 기록**, **여행자의 업적**을 사용합니다. 클리어 후 이니셜을 등록하면 다른 참가자가 같은 기록을 볼 수 있습니다.

팀원은 `SERVER.cmd`, `TUNNEL.cmd`를 실행하지 않습니다. Enter로 연결, Tab으로 항목 이동, Esc로 닫기를 지원합니다. 게임의 성당 배경·한글 폰트·은청색 문양을 연결 창에도 사용합니다. 연결 확인은 백그라운드에서 수행하며 실패하면 같은 화면에서 다시 시도할 수 있습니다. **오프라인 실행**은 서버 접속 없이 게임을 시작하고 미전송 클리어를 보관합니다. 다음 온라인 실행에서 전송할 수 있습니다.

Java 포함 ZIP은 Java·Eclipse·Gradle 설치가 필요하지 않습니다. 개발 소스 실행에는 64비트 JDK 17 이상이 필요합니다. 공통 실행기는 유효한 JAVA_HOME을 우선하고 일반 설치 경로를 탐색합니다.

## 기록 보관과 주소 변경

- 운영 DB는 서버를 실행한 프로젝트 또는 실행본의 `data/deadlight.mv.db`입니다. 서버를 껐다 켜도 같은 파일을 다시 사용하므로 랭킹과 업적은 남습니다. 서버가 꺼져 있는 동안에는 다른 사람이 조회·등록할 수 없습니다.
- 서버 폴더를 바꿀 때는 서버를 정상 종료한 뒤 `data` 폴더를 백업해 새 서버 폴더로 옮깁니다. DB가 없는 다른 폴더에서 서버를 켜면 새 DB가 만들어집니다. 배포 ZIP에는 호스트의 DB를 넣지 않습니다.
- 터널을 다시 켜면 주소가 바뀔 수 있습니다. 새 주소를 팀원에게 다시 전달하세요. 한 호스트의 DB를 계속 사용하는 동안 주소가 바뀌어도 같은 플레이어 ID·미전송 기록을 사용합니다.
- 다른 사람이 별도 서버를 켜면 각각 다른 DB입니다. 모든 참가자가 한 호스트의 같은 주소를 사용해야 합니다.
- 기존 게임 설정 저장 방식은 유지합니다. 새 운영용 온라인 프로필은 `deadlight.hosted.online`, 연결 창의 주소 설정은 Java Preferences의 `com/example/pixelgame/hosted`입니다. 기존 클라우드 및 GameTest 프로필·DB는 지우거나 자동으로 합치지 않습니다. 예전 로컬 DB인 사용자 홈의 `.deadlight/server`도 그대로 보관합니다.
- `data`, 실제 터널 주소, 로그, 환경변수의 비밀번호, 실행 ZIP은 Git 커밋에서 제외합니다. 새로 받은 팀원에게는 실행 ZIP 또는 소스를 전달하고 현재 터널 주소는 따로 알려줍니다.

## 개발과 실행본 만들기

- `PLAY.cmd` / Gradle `lwjgl3:lanRun` / Java `LanLauncher`: 팀원용 연결 창.
- `PLAY_HOST.cmd` / Gradle `lwjgl3:run` / Java `Lwjgl3Launcher`: 호스트의 로컬 서버에 바로 연결.
- `SERVER.cmd` / Gradle `server:run`: Spring Boot 기록 서버. `SERVER.cmd`는 과거 클라우드 환경변수를 무시하고 프로젝트의 로컬 H2 DB를 사용합니다.
- `TUNNEL.cmd`: cloudflared 확인 → 로컬 서버 상태 확인 → 임시 공개 HTTPS 주소 생성.
- `BUILD_PORTABLE.cmd`: Java 런타임·게임·서버·터널 도구를 포함한 Windows x64 ZIP을 `build/portable`에 생성. 생성된 ZIP과 런타임은 소스 커밋에 포함하지 않습니다.

IntelliJ와 Eclipse에서 같은 Gradle 작업을 사용할 수 있습니다. Eclipse 편집기에서 `.cmd`를 더블클릭하면 실행 대신 파일 내용이 열릴 수 있으므로 파일 탐색기에서 실행하세요. 게임과 연결 창은 Windows 데스크톱 GUI이며 브라우저는 진단용입니다. 서버 주소의 `/`가 404인 것은 정상입니다. `/api/health`는 `status: ok`, `service: deadlight-records`를 반환합니다.

기존 `SERVER_LAN.cmd` / `PLAY_LAN.cmd`는 같은 네트워크에서 직접 접속하는 보조 방식으로 남습니다. 기본 운영 방식은 SERVER → TUNNEL이며 팀원용 창에는 로컬 서버 예시를 표시하지 않습니다.

cloudflared의 고정 버전·공식 다운로드 주소·체크섬은 `tools/CLOUDFLARED-INFO.json`, 라이선스는 `tools/CLOUDFLARED-LICENSE.txt`에 있습니다. 실행 파일은 Git에 넣지 않고 호스트의 첫 실행 또는 실행 ZIP 빌드 때 준비합니다. 임시 터널은 프로세스를 종료하면 접속이 중단됩니다. 공식 설명: https://developers.cloudflare.com/tunnel/get-started/quick-tunnels/
