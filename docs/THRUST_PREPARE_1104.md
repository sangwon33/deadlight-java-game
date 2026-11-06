# 쿨타임 1초와 양손을 뒤로 당긴 준비 자세 — 2026-11-04

적용 위치: C:\Users\USER\Desktop\Java\Deadlight

- 스킬 쿨타임을 8초에서 1초로 변경했습니다. GameBalance.SKILL_COOLDOWN에서 조정할 수 있습니다. 피격 취소 후 쿨타임도 같은 값을 사용합니다.
- 집중 시작/깊은 집중의 두 포즈를 새로 만들었습니다. 양손을 같은 손잡이에 모아 뒤쪽 허리로 당기고, 팔꿈치를 뒤로 접으며, 검끝은 전방을 향합니다.
- 1~2단계는 첫 자세, 3~4단계는 더 낮게 웅크린 자세를 사용합니다.
- 기존 돌진/제동/회복 네 프레임은 원래 시트를 사용합니다. 준비 이미지가 별도 시트이므로 다음 피드백 때 준비 동작만 수정할 수 있습니다.
- 두 시트의 원본 해상도가 다르므로 프레임별 표시 크기를 사용하며, 공통 0.8배와 발 기준은 유지합니다.
- 충전은 현행 1.2초/4단계입니다. 이펙트는 추가하지 않았습니다.

## 에셋 제작과 파일

내장 imagegen의 투명 배경 편집으로 제작했습니다. Java는 연결된 불투명 픽셀을 각각의 셀로 옮겨 몸 중심과 발 기준을 정렬하는 데 사용했습니다. 확대 미리보기는 시트의 투명 여백만 잘랐습니다.

- 새 준비 시트: assets/player/player_thrust_prepare_v1.png (1536×1024, 768×1024 셀 두 개)
- 준비 크기 설정: assets/player/thrust-prepare-layout.properties (몸 높이 595, 표시 높이 78)
- 기존 돌진 시트: assets/player/player_thrust_v2.png
- 로더/렌더링: ThrustSprites.java, Player.java
- 쿨타임: GameBalance.java
- 검토 이미지: Deadlight_prepare_pose_1104.png
- 전체 시트: Deadlight_prepare_sheet_1104.png
- 전체 프롬프트: Deadlight_prepare_prompts_1104.md
- 실제 게임 미리보기: Deadlight_prepare_1104.gif

## 검증

Java/Gradle 컴파일, 실제 OpenGL 게임 창에서 자동 입력 검증을 통과했습니다.

- 새 준비 두 프레임과 유지한 네 프레임의 선택/표시 크기/발 기준
- 실제 게임에서 양손 준비/깊은 집중/돌진의 연결 및 좌우 반전
- 발동 후 쿨타임 1초, 1초 이전 재시전 차단, 1초 경과 후 두 번째 집중 시작, 피격 취소 쿨타임
- 기존 30/60/120fps 돌진 거리 210, 적/보스 각 20 피해와 중복 방지
- 좌우/맵 경계/발판/키 변경/화면 버튼/조기 해제/일시정지 처리

검증은 자동 입력과 상태 구성을 사용했으며 사용자의 설정과 기록을 저장하지 않았습니다. 직전 코드 세 파일은 work/prepare-1104/before에 보존했습니다.

게임 종료 → Eclipse F5 → 다시 실행하면 반영됩니다. 최신 ZIP: Deadlight_updated_1104.zip.
