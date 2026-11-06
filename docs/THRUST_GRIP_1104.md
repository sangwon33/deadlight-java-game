# 왼손 그립과 손목 수정 — 2026-11-04

준비 자세의 왼손이 손잡이 위에 주먹을 얹은 것처럼 보이던 부분을 수정했습니다. 왼손을 손잡이 아래에서 받치며 감싸는 형태로 돌리고, 손가락과 엄지의 맞물림 및 대각선 전완에서 손목으로 이어지는 형태를 다시 그렸습니다. 두 준비 프레임에 적용했습니다.

제작은 내장 imagegen의 투명 배경 편집 모드로 수행했습니다. 그립 방향을 수정한 뒤 장갑 재질을 복구하고 투명 배경을 정리했습니다. Java는 두 그림을 게임 시트에 옮겨 몸/발 기준을 정렬하고 검토용 확대 이미지를 만드는 데 사용했습니다.

- 게임 적용: assets/player/player_thrust_prepare_v4.png
- 위치 설정: assets/player/thrust-prepare-layout.properties
- 로더: ThrustSprites.java
- 전체 시트: Deadlight_prepare_grip_sheet_1104.png
- 전체 자세: Deadlight_prepare_grip_1104.png
- 왼손 확대 전후 비교: Deadlight_left_grip_detail_1104.png
- 프롬프트 전체: Deadlight_prepare_grip_prompts_1104.md

Java/Gradle 컴파일과 실제 게임의 프레임 선택/표시 크기/발 기준/좌우/1초 재사용 검사를 통과했습니다. 왼손 모양은 확대 이미지에서 확인했으며 자동 입력 검사가 해부학적 정확성을 보증하는 것은 아닙니다.

직전 시트/위치 설정/로더는 work/grip-1104/before에 보존했습니다. 게임 종료 → Eclipse F5 → 다시 실행하면 반영됩니다.
