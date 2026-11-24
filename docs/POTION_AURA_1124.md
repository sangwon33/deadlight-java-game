# 2026-11-24 체력바·회복 포션·포션 오라

- 체력바 양끝을 같은 둥근 모양으로 바꾸고, 내부 구분선·비어 보이던 화살촉 끝을 제거했습니다. 현재 체력과 피해 잔상 모두 둥근 내부 영역에 맞춰 채웁니다.
- 하트는 둥근 은색 테두리 안의 곡선형 루비색 하트로 바꿨으며 체력바와 간격을 유지합니다.
- 회복 포션은 기존 병의 형태·은장식을 참고해 청록색 내용물만 붉은색으로 바꾼 별도 이미지입니다. 원본 relics.png는 보존하며 HUD·가방·상점·보상에 같은 회복 포션 텍스처를 사용합니다.
- 새 이미지: assets/art/potion-heal-red-v1.png. built-in image_gen 편집으로 제작했으며 입력은 게임에서 쓰던 회복 포션 텍스처 영역입니다.
- 실제 회복에 성공했을 때만 빨간 오라를 약 0.9초 표시합니다. 체력이 가득 차 사용하지 못하면 오라도 생기지 않습니다.
- 공격력 포션의 보라색, 신속 포션의 초록색 오라는 별도 시간을 중복 집계하지 않고 Player의 실제 버프 잔여 시간을 사용합니다. 10초 갱신·종료, 메뉴/지도 일시정지, 지역 이동 후 남은 시간과 함께 표시됩니다.
- 몸을 따라가는 색 테두리, 배경 빛, 발 주변 고리와 상승 입자로 표현하며 두 버프를 함께 쓰면 보라·초록 연출을 함께 표시합니다.
- 섬광 완화에서는 입자·맥동을 줄이고 고정된 약한 빛으로 표현합니다. 외곽 강조 설정에서도 밝은 테두리를 유지합니다.
- 30/60/120fps에서 실제 포션 성공/실패, 일시정지·이동·만료를 확인하고 1280×720/960×540에서 체력바·새 포션·단독/동시/공중/완화 오라와 GL 오류를 검증했습니다.

## 회복 포션 색 변경 최종 프롬프트

Use case: precise-object-edit. This is the EXISTING healing potion icon from a game. Change COLOR ONLY: recolor the cyan/turquoise liquid and the cyan color cast inside the glass to ruby/crimson RED, including its reflected liquid highlights and bubbles. Keep the exact original bottle silhouette, shape, scale, stopper, silver leaf filigree, every metal ornament, dark contour, glass shading, white highlights, bubble positions, framing and transparent background unchanged. Do NOT redesign, redraw ornaments, add objects, replace the bottle, change proportions, add glow or add a background. Preserve the existing alpha. The result should look like the very same original icon with just its liquid changed from cyan to red. Single complete icon, no text.
