# 허리 옆에서 양팔을 연결하는 준비 자세 — 2026-11-04

직전 수정에서 반대팔을 몸 뒤로 숨긴 처리는 이 준비 자세에 맞지 않았습니다. 양손을 척추 뒤로 두는 구도를 버리고, 손잡이를 허리 옆으로 당기는 구도로 수정했습니다.

상체를 약간 정면으로 틀어 두 어깨를 드러내고, 반대쪽 위팔과 팔꿈치 및 배 앞을 가로지르는 전완을 그려 손잡이로 연결했습니다. 가까운 팔도 어깨에서 손목까지 이어지도록 그렸습니다. 시작/깊은 집중 두 프레임을 같은 방식으로 수정했습니다.

제작: 내장 imagegen, 투명 배경 편집. Java로 각 그림을 셀에 옮겨 몸/발 기준만 정렬했습니다. 확대 미리보기는 투명 여백만 잘랐습니다.

- 적용: assets/player/player_thrust_prepare_v3.png
- 표시 설정: assets/player/thrust-prepare-layout.properties
- 로더: ThrustSprites.java
- 전체 시트: Deadlight_prepare_anatomy_sheet_1104.png
- 확대 미리보기: Deadlight_prepare_anatomy_1104.png
- 프롬프트: Deadlight_prepare_anatomy_prompts_1104.md
- 게임 미리보기: Deadlight_anatomy_1104.gif

Java/Gradle 컴파일 및 실제 게임의 프레임 선택/표시 크기/발 기준/좌우/1초 재사용 검사를 통과했습니다. 팔 형태는 확대 이미지에서 두 어깨→팔꿈치→손잡이의 연결을 확인했습니다. 자동 입력 검사는 해부학적 정확성을 판별하지 않습니다.

직전 시트/크기 설정/로더는 work/anatomy-1104/before에 보존했습니다. 게임 종료 → Eclipse F5 → 다시 실행하면 반영됩니다.
