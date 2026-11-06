# 최종 보스 일식의 처형자 — 2026-11-05

사용자가 선택한 대검 콘셉트를 게임용 포즈·이펙트로 제작하고 기존 최종 보스에 연결했습니다.

## 결과물
- `assets/art/boss_executioner_v1.png`: 투명 RGBA 1774×887, 4열×2행. 대기 / 이동 / 내려찍기 준비 / 내려찍기 / 가로 베기 준비 / 가로 베기 / 올려베기 / 패배.
- `assets/art/executioner_fx_v1.png`: 같은 크기·배치. 기본 후광 / 격화 후광 / 가로 베기 궤적 / 올려베기 궤적 / 내려찍기 궤적 / 지면 충돌 / 전진 잔상 / 후광 해체.
- `assets/art/executioner-layout-v1.json`: 실제 프레임 영역, 공통 배율 .38, 포즈별 발·머리 기준과 효과 중심, 원본 PNG 해시.
- 별도 후광을 포즈별 머리에 맞춰 몸 뒤에 그립니다. 효과는 플레이어와 캐릭터를 그린 다음 일반 셰이더로 그립니다. 원본 PNG의 픽셀과 알파는 수정하지 않았습니다.

## 게임 연결
- 4지역 성당의 기존 최종 보스를 대검을 든 지상형 일식의 처형자로 교체했습니다. 기존 타락한 신의 창 애니메이션과 regent 이미지는 새 실행 경로에서 사용하지 않습니다.
- 체력 240 / 기본 공격 피해 24 / 체력 절반 이하 피해 28. 격화 시 이동과 공격 준비가 빨라지고 후광이 달라집니다.
- 가로 베기: 낮은 전방 베기. 준비 후 방향을 고정하고 짧게 전진합니다.
- 내려찍기: 대검을 들어 올린 뒤 전방 지면으로 내려찍고 충돌 효과를 표시합니다.
- 올려베기: 대각선 위를 베며 높은 발판의 플레이어도 공격할 수 있습니다.
- 전진 베기: 준비 후 방향이 고정된 빠른 돌진 베기. 공격당 피해 1회, 기존 대시 무적을 적용합니다.
- 공격 후 회복 동작 중 반격 1.5배. 일반 공격/Q는 기존 행동당 피해 1회 제한에 연결합니다.
- 처치 후 1.05초간 무릎 꿇은 포즈·후광 해체를 표시한 뒤 클리어 결과로 넘어갑니다. 처치 직후에는 공격하지 않습니다.
- 머리·발·검의 큰 동작이 화면 가장자리에서 잘리는 것을 줄이도록 이동 영역에 여유를 두었습니다. 몸 충돌은 58×124이며 무기와 후광은 별도입니다.
- 기존 API와 메인 흐름은 `FallenGod` 어댑터로 연결했습니다. `Executioner`는 입력/그래픽과 분리된 전투 상태, `ExecutionerSprites`는 포즈와 효과 렌더링을 담당합니다.
- 전투 규칙이 바뀌어 `GAMEPLAY_VERSION=4`를 사용합니다. 기존 rules3 클리어 기록을 삭제하거나 덮어쓰지 않고 rules4에 새 기록을 분리합니다.

## 검증
- 주요 실루엣 알파 > 80 기준 캐릭터 프레임 최소 여백 10px, 효과 프레임 최소 여백 48px. 각 칸 안에 들어오는 것을 읽기 전용으로 검사했습니다.
- 30/60/120fps에서 모든 공격을 양방향으로 실행: 공격당 피해 1회, 점프 회피, 고정 방향 회피, 높은 목표 올려베기, 이동 경계, 격화·반격·피격 간격 확인.
- 실제 `FallenGod` 어댑터의 플레이어 피해/대시 무적, `Main` 일반 공격/Q의 행동당 1회 피해와 피해 통계 확인.
- 실제 `Main.updateGame`에서 사망 연출 전에는 PLAYING 상태이고 연출 후 CLEARED 결과가 되며, 새 기록이 1회 저장되고 rules3 기록은 유지되는 것을 확인했습니다.
- 모든 포즈의 공통 배율·발 기준, 별도 후광 머리 기준, 색상 복원과 각 공격 효과의 렌더링 명령을 검사했습니다.
- 프로덕션 그리기 명령의 Java2D 미리보기로 포즈·후광·성당 배경의 조합을 확인했습니다. 실제 게임 스크린샷은 아닙니다.
- 나방·등불 사수·1/2/3지역 중간 보스의 기존 회귀 검사도 통과했습니다.
- Gradle 실행 JAR과 Eclipse 출력 컴파일 및 리소스 포함을 검사합니다. 실제 게임 창의 GPU 렌더링·수동 전투 체감은 아직 검증하지 못했습니다.
- 실행 중인 게임은 종료 후 다시 실행해야 새 코드가 적용됩니다.

검사 코드/리포트: 작업 공간 `aerial-update/ExecutionerCheck.java`, `executioner-result.txt`. 기존 콘셉트는 `docs/concepts/final_boss_eclipse_executioner_v1.png`입니다.

## 생성 방식과 프롬프트
**built-in image_gen** 사용. 생성 이미지 원본을 그대로 복사했으며 CLI/API 우회나 별도 픽셀 편집 스크립트는 사용하지 않았습니다.

### 캐릭터 시트
```text
Use case: stylized-concept
Asset type: actual transparent 2D game animation KEYFRAME atlas for Deadlight final boss "The Eclipse Executioner". The user approved the referenced concept; keep its identity.
Reference image1 is the EXACT CHARACTER DESIGN and texture reference. Preserve the faceless tall black angular helmet with ONE thin vertical white slit, asymmetrical broad blackened metal pauldrons, layered dark silver armor, navy half-cloak, long trailing waist strips, armored boots, and enormous BROAD TWO-HANDED GREATSWORD with long grip, simple crossguard, wide flat dark blade and silver cutting edges. Do not turn sword into a lance, do not redesign into a stone statue. Same hand-inked weathered painterly cel shading.
Reference image2 is final cathedral palette reference ONLY; no scenery.
IMPORTANT: the broken halo will be a SEPARATE asset. Do NOT bake any halo, glow cloud or particles behind the character in these cells.
Canvas wide2:1 preferably2048x1024, EXACT FOUR uniform columns TWO uniform rows = EIGHT separate square cells. Each ENTIRE pose/armor/cloth/GREATSWORD fits within central65% of its cell, leaving very generous transparent gutter on ALL sides including along row boundary. Full sword point AND pommel always inside its OWN cell. Keep SAME body scale AND SAME sword length/width in all8 frames. Use body roughly235px tall in512cell; no auto-enlarging any pose. Fixed strict side/very slight3/4 profile facing RIGHT in every frame. Grounded boot baseline around localy=415, ground/body pivot near localx=220. Shoulder and leg anatomy consistent; bend joints to pose, never stretch body or shrink weapon to fit. A few quiet cloak strips, avoid gigantic billowing cloth.
Frame0 idle: grounded imposing guard, both hands hold sword angled down-right, blade point close to boot ground level.
Frame1 stride: one heavy forward step, sword still angled down-right, same armor volume.
Frame2 overhead windup: both hands raise greatsword above and slightly behind helmet; full blade tip stays well below cell top, knees flex.
Frame3 downward slam strike: forward weight, both hands low, wide blade angled steeply down-forward, point reaches same ground baseline, no ground explosion/trail.
Frame4 horizontal sweep preparation: torso draws back, sword held across/back at waist-to-shoulder height; full pommel and blade contained.
Frame5 broad horizontal sweep strike RIGHT: two-hand grip, strong forward lunge, entire broad flat blade clearly visible, no attached slash effect, complete blade and rear foot inside cell.
Frame6 rising diagonal uppercut RIGHT: sword angles up-right, knees extend, complete weapon within cell with safe top/right margin.
Frame7 defeated: one knee on ground, helmet bowed, heavy sword resting down diagonally beside body; no decapitation, no gore, no parts scattered outside cell.
Opaque dark armor with cool blue-grey edge highlights for legibility, silver sword edges, tiny white helmet slit. NO red/orange flames, no vivid neon, no gold trim. Match reference surface texture rather than smooth vector or photoreal3D. NOT pixel art.
TRUE TRANSPARENT RGBA background, zero-alpha empty gutters, no matte/painted checkerboard, no floor, no shadows, no labels/text/grid/borders/watermark. Eight complete separate usable pose sprites, not scenic panels.
```

### 후광·대검 이펙트 시트
```text
Use case: stylized-concept
Asset type: actual transparent game VFX atlas for final boss "Eclipse Executioner" in Deadlight. Separate halo + greatsword effects, NO characters.
Reference1 is exact approved boss design/equipment style: dark blackened iron, worn silver cutting edges, navy cloth, empty face and thin cold-white light. Reference2 is its new pose atlas for same style only. Create NEW isolated effects matching hand-inked weathered painterly cel art.
Canvas wide2:1 preferably2048x1024, FOUR uniform columns TWO uniform rows = EIGHT separate square cells. ALL objects/effects fit inside central65% of each cell, leaving wide CLEAR transparent gutters on every side, no spill or contact across cells. Do NOT auto-trim canvas.
Row-major effects:
0 calm broken eclipse HALO: thin aged silver circular ring about220px diameter in512cell, broken at3 short gaps, tiny cold-white inner edge, 3 floating narrow ring shards. The CENTER is EMPTY TRANSPARENT (no black matte disk) so it can sit behind helmet. No huge soft aura, just crisp ring.
1 enraged version of SAME ring at EXACT SAME diameter and center: gaps wider, ring segments slightly separated, brighter narrow white inner edge and a few small glints, still empty center. No fiery red or purple.
2 horizontal greatsword sweep crescent directed RIGHT: broad near-black/navy inner curved stroke with sharply outlined SILVER-WHITE cutting rim, two small chips, transparent empty curved center. Strong readable arc, no literal sword and no fog cloud.
3 rising uppercut arc sweeping from bottom-left to top-right: same silver-white rim and dark inner stroke, elegant diagonal curved crescent, no weapon/character.
4 heavy downward cut arc top-left to bottom-right, slightly thicker and shorter than frame3, same silver-white/black palette, no floor/weapon.
5 ground-impact burst: compact angular silver/black stone chips with a few cold-white vertical sparks, low base anchoring point at lower center, no ground plane or dust haze.
6 forward dash streak pointing RIGHT: short tapered near-black/slate stream with 3 sharp thin silver-white speed lines and a few hard-edged dark fragments, not broad translucent smoke.
7 defeated eclipse dispersal: loose partial circle of dark silver ring shards and tiny fading cold-white embers, same approximate scale as halo, mostly empty transparent space.
Palette only dark steel/slate/near-black, silver and cold white. NO red/orange fire, no neon blue/purple, no gold. Effects should remain game-readable at small size and not blanket the character.
TRUE transparent RGBA, zero-alpha negative space in rings/arcs/between pieces, no grey/black/white matte, no painted checkerboard, no broad glow/fog/shadows. All8 elements entirely inside own cell with generous margins. No characters, no typography/labels/grid/borders/logo/watermark. Hand-drawn 2D textures, NOT photorealistic, NOT3D, NOT pixel art.
```

