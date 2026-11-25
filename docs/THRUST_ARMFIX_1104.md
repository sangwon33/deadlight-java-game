# 준비 자세의 중복 오른팔 보정 — 2026-11-04

양손을 뒤로 당긴 준비 자세에서 가까운 쪽 팔이 중복되어 보이던 그림을 수정했습니다.

- 가까운 오른팔은 어깨→위팔→팔꿈치→전완→가드 쪽 손으로 이어지는 한 개의 팔로 정리했습니다.
- 반대쪽 팔의 위팔은 몸/망토 뒤에 가리고, 뒤쪽 손잡이를 잡는 손과 짧은 전완만 보이도록 했습니다.
- 시작/깊은 집중의 두 프레임을 같은 방식으로 수정했습니다.
- 스킬 쿨타임 1초와 기존 돌진 네 프레임을 사용합니다.

내장 imagegen의 투명 배경 편집 모드로 수정했습니다. Java로 두 그림을 셀에 옮겨 발/몸 기준만 정렬했습니다.

적용: assets/player/player_thrust_prepare_v2.png
위치 설정: assets/player/thrust-prepare-layout.properties
로더: ThrustSprites.java
시트: Deadlight_prepare_armfix_sheet_1104.png
확대 미리보기: Deadlight_prepare_armfix_1104.png
프롬프트 전체: Deadlight_prepare_armfix_prompts_1104.md
게임 미리보기: Deadlight_armfix_1104.gif

새 그림의 두 팔 연결을 직접 확인하고, 실제 게임에서 준비/집중/돌진 연결을 렌더링했습니다. Java/Gradle 컴파일과 기존 프레임 선택·크기·발 기준·좌우 반전·1초 재사용 자동 입력 검사를 통과했습니다. 해부학적 외형은 이미지 확인으로 검사했으며 자동 입력 검사가 팔 모양을 판별하는 것은 아닙니다.

직전 준비 시트/로더/위치 설정은 work/armfix-1104/before에 보존했습니다. 게임 종료 → Eclipse F5 → 다시 실행하면 반영됩니다.
