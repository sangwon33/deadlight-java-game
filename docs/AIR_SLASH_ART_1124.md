# 공중 베기 에셋 — 2026-11-24

- 제작: built-in `image_gen` (CLI/API 사용 안 함).
- 입력: `assets/art/hero.png` 캐릭터 동일성 참고.
- 최종 게임 에셋: `assets/art/hero-air-slash-v1.png`, 1536×1024 RGBA, 3열×2행, 512×512 프레임 6개.
- 연결: `AirAttackSprites.java`, `Player.java`. 전 프레임 고정 배율 .155, 발 기준 앵커. 좌우 반전, 피격 색·무적 투명도, 실제 화면 경계 계산에 연결합니다.
- 순서: 준비 → 내려베기 시작 → 전방 베기 → 마무리 → 검 회수 → 공중 복귀. 실제 공격의 활성 구간은 기존 공중 공격 판정을 유지합니다.

## 생성 프롬프트

Use case: identity-preserve. Asset type: production 2D game sprite sheet. Reference image is the EXISTING player: preserve exactly its dark navy hood, pale full mask with narrow blue eyeslit, short ragged blue grey cape, dark belted tunic, boots and straight short silver sword, cartoon outlined painted style, head/body proportions. Generate a NEW transparent sprite sheet with SIX poses in EXACT 3 columns x 2 rows equal cells on a 1536x1024 canvas, each cell512x512; all facing RIGHT, complete body and sword fully inside each cell with40px gutters. This is an AIRBORNE sword slash animation. Poses in reading order: 1 windup arm raises sword behind head, torso upright knees bent tucked at different heights and boots CLEARLY FLOATING; 2 slash starts diagonally downward forward, body rotates slightly; 3 sword extends horizontally to the right, nearer arm straight, far arm balanced behind, knees remain bent; 4 blade finishes sweeping forward down, cape trailing left; 5 recovery draws sword back toward hip, knees still tucked; 6 neutral airborne recovery, sword down forward and knees bent. Exactly TWO arms and TWO legs per figure, one sword per figure, natural grips. Head width and body scale identical in every cell; masked head centered around x230,y135 cell-relative, lower boot soles around y405, body occupies about300px tall, sword can reach x460. Thin dark contour, restrained flat soft shaded material, no effects, no trails, no sparkles, NO floor, NO platform, NO text, NO labels, NO black backdrop. Actual alpha background.

## 투명도 정리 프롬프트

Use case background-extraction. Edit ONLY the transparency of this exact 1536x1024 six-frame 3 columns by2 rows sprite sheet. KEEP every character, sword, pose, cell placement, scale, drawn pixel detail, colors unchanged. REMOVE ALL gray mist, hazy halos, luminous clouds and black backdrop surrounding sprites. Truly clean zero-alpha empty gutters and space everywhere except the character, sword and thin dark contour. No shadows/no glow. This is a game atlas so background MUST be entirely invisible. Do not rearrange or resize any sprites. Return same1536x1024 transparent PNG.
