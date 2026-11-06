# 플레이어 지상 3타 베기 — 2026-11-05

## 조작과 결과
- 기본 공격 키 J를 한 번 누르면 가로 베기 1타만 실행합니다. 다음 입력이 있으면 올려베기 2타, 다시 입력하면 전진 내려베기 3타로 연결합니다. 키 재설정과 화면 공격 버튼도 같은 입력 경로를 사용합니다.
- 공격 키를 누르고 있기만 해서는 자동 연속 공격을 하지 않습니다. 후속 타격은 1개만 예약하며, 3타 중 입력을 저장해 4번째 공격을 자동으로 시작하지 않습니다.
- 1·2타는 대시로 즉시 취소할 수 있습니다. 3타는 0.30초까지 동작에 집중하고 마지막 회복 구간부터 대시가 가능합니다.
- 1·2타 중 이동은 기존 속도의 65%입니다. 타격 도중에는 방향을 고정하며 새 타격이 시작할 때 이동 입력 방향으로 전환할 수 있습니다.
- 3타는 0.075~0.225초 사이에 총 20픽셀 전진합니다. 플레이어의 기존 수평 충돌 처리를 거쳐 벽을 통과하지 않습니다.
- 3타의 피해는 기본 공격력×1.4 반올림(기본 6이면 8)이며 강공격으로 처리해 기사의 정면 방어를 뚫습니다. 기존 강한 타격 피드백(히트스톱·스파크)을 사용합니다. 상점/인벤토리의 기본 공격력 표시는 일시적인 3타 배율을 포함하지 않습니다.
- 점프와 공격을 동시에 누르면 기존 공중 일반 공격을 사용합니다. 지상 콤보 중 공중으로 나가면 지상 콤보와 예약 입력을 중단합니다. 아래 베기는 추가하지 않았습니다.
- 피격·사망·대시 취소 때 콤보를 초기화합니다. 일시정지/인벤토리/상점/포커스 변경으로 입력을 지울 때 예약 타격도 제거합니다. Q 스킬 입력/차지/판정은 기존 경로를 유지합니다.

## 타격 시간과 판정
| 타격 | 전체 길이 | 피해 구간 | 모션 |
| --- | --- | --- | --- |
| 1타 | .24초 | .055~.155초 | 가로 베기 |
| 2타 | .26초 | .065~.180초 | 대각선 올려베기 |
| 3타 | .36초 | .105~.235초 | 짧게 전진하며 내려베기 |

각 타격마다 기존 attackCount를 한 번 증가시켜 Main의 대상별 중복 타격 제한을 새로 시작합니다. 한 타격의 여러 프레임에 걸쳐 겹쳐도 대상당 1회만 피해를 줍니다. 2타의 위쪽 판정과 이펙트 위치는 실제 검끝 높이에 맞췄습니다.

## 새 에셋과 구현
- `assets/art/player_combo_v1.png`: RGBA 1254×1254, 3열×3행. 타격별 준비/타격/회수 총 9포즈.
- `assets/art/player_combo_fx_v1.png`: RGBA 1536×1024, 3열×2행. 각 타격의 은백색 검 궤적/잔상 총 6종. 작은 범위와 짧은 표시 시간으로 사용합니다.
- `assets/art/player-combo-layout-v1.json`: 실제 프레임 영역·공통 배율 .203·포즈별 발 기준·효과 중심·원본 해시.
- 기존 플레이어 마스크·후드·망토·무기 외형을 참고했습니다. 고정 배율에서 1타 준비 포즈 몸 높이는 약62.5픽셀로 기존 플레이어 62.4픽셀과 맞춥니다. 포즈마다 독립적으로 늘이거나 줄이지 않습니다.
- 모든 생성 PNG의 픽셀·알파는 수정하지 않고 그대로 복사했습니다. 캐릭터 주요 실루엣 최소 여백 15px, 효과 최소 여백 72px(알파 > 80 기준)입니다.
- `SwordCombo`: 입력 예약, 타격 단계/활성 시간, 대시 취소 가능 시점, 프레임률과 독립적인 전진 거리.
- `SwordComboSprites`: 명시된 프레임 영역, 발 기준, 동일 배율, 포즈와 별도 은백색 효과.
- `Player`: 콤보·기존 공중 공격·이동·대시·피격 상태 연결. `Main`: 타격별 판정과 3타 강공격/피드백, 효과 렌더링. 튜토리얼에 연타와 1·2타 대시 취소 설명을 추가했습니다.
- `GAMEPLAY_VERSION=5`: 변경된 일반 공격의 기록은 rules5에 분리하며 기존 버전 기록을 지우지 않습니다.

## 검증
- 30/60/120fps에서 입력 1회와 3타 예약 구분, 좌우 전진 거리20px, 타격 횟수와 종료 확인.
- 실제 Player.update의 대시 취소/3타 동작 유지/후반 취소, 일시정지·피격 시 예약 제거, 얇은 벽 앞 전진 차단, 동시 점프/공중 공격 확인.
- 실제 Main 공격 경로에서 최종 보스에 6+6+8 피해가 각1회만 적용되고 3타 강한 피드백이 발생함을 확인했습니다. 기사 정면 방어에 3타의 강공격 표시가 전달되는 것도 확인했습니다.
- 실제 Main.updateGame과 키 입력을 사용해 키 재설정 K, 히트스톱 중 입력 예약, 3타 피해 및 공격 통계3회, 키를 누르고 있어도 자동 콤보가 발생하지 않음을 확인했습니다.
- 9포즈 양방향 발 기준·공통 배율·색상 복원과 효과의 그리기 명령을 검사했습니다. 프로덕션 그리기 명령을 Java2D로 확인했으며 실제 게임 스크린샷은 아닙니다.
- 기존 나방·등불 사수·세 지역 중간 보스·최종 보스의 회귀 검사 통과. Gradle 실행 JAR과 Eclipse 출력 컴파일 및 리소스 포함 확인.
- 실제 게임 창의 GPU 렌더링과 수동 조작감은 아직 미검증입니다. 실행 중인 게임은 종료 후 다시 실행해야 합니다.

검사: 작업 공간 `aerial-update/SwordComboCheck.java`, `player-combo-result.txt`. Eclipse 결과: `build/eclipse-player-combo-check.txt`.

## 생성 방식과 프롬프트
**built-in image_gen** 사용. 처음 효과 생성/정리본은 번짐이 강해 채택하지 않았고 별도 새 생성본의 은백색 붓 궤적을 사용했습니다. 아래는 실제 사용한 프롬프트와 시도 기록입니다.

### 캐릭터
```text
Use case: stylized-concept
Asset type: game-ready 2D PLAYER normal-attack THREE-HIT COMBO pose atlas for Deadlight.
Reference image is the EXACT existing PLAYER IDENTITY and style. Keep this small hooded adventurer: large simple elongated IVORY MASK with a single narrow horizontal cyan-white eye slit, dark navy hood, short layered slate-blue shoulder cape fastened by small round silver brooch, dark tunic/belt/gloves/boots and ONE SHORT straight silver sword. Match exact head-to-body proportions (compact/chibi, NOT realistic adult proportions), same cloak lengths/materials/colors and clean dark ink/cel-painted texture. Do not add armor, horns, face, wings or a new hairstyle. Do NOT use a huge sword or a long lance.
Create NINE sequential key poses on EXACT THREE columns THREE rows (3x3 square grid), preferably1536x1536, each cell512square. NO drawn grid lines or labels. All9 face RIGHT in strict side/very slight3/4 view, SAME body scale, same short sword length, same mask/hood size. Body standing anatomical height about235px in512cell, boot baseline near localy=425. Make each full pose INCLUDING weapon comfortably fit central70% of its cell with at least55px empty transparent margin on all4 sides. Keep all poses separate, no cloak/sword crosses another cell. Use bent joints/body lean to change pose, never independently resize characters. Preserve foot placement and center as much as natural stance allows. Every full sword point, hand, hood and boot visible and uncut.
Each ROW is ONE attack with PREPARATION / STRIKE / RECOVERY, left to right:
ROW1 hit1 horizontal quick slash:
0 sword drawn back by shoulder, torso slightly turned, grounded ready stance.
1 fast horizontal sword CUT RIGHT across waist/chest height, clear arm extension and forward weight shift; this is a slash NOT a rapier thrust. Blade slightly angled, free hand balancing.
2 short follow-through with sword lowered forward, returns toward balanced stance, cape settling.
ROW2 hit2 rising diagonal cut:
3 blade low beside rear hip, knees bend, free hand ahead preparing upward rotation.
4 short sword sweeps UP-RIGHT diagonally, torso extends, shoulder/cape rotate with body; keep boots near same baseline, NO actual jump.
5 upper follow-through with elbow bent and sword above shoulder, regaining centered stance.
ROW3 hit3 advancing downward finisher:
6 sword raised overhead, knees flexed, full sword point with generous margin below cell top.
7 forceful diagonal DOWN-RIGHT sword cut, front knee bends into a small forward step, cape trails left. No giant leap, no stretched body, full blade and feet inside cell.
8 recovery after finisher, sword low-forward, body settles back into compact guard.
Do NOT draw slash trails, sparks, smoke or dust attached to these sprites; effects will be separate assets.
TRUE TRANSPARENT RGBA, empty negative spaces and gutters, no black/white matte, no painted checkerboard, no floor/shadow or scenic background, no typography/labels/frame/watermark. Same game hand-drawn style as reference, not pixel art, not realistic3D, not an anime redesign.
```

### 최초 효과 시도 (미채택)
```text
Use case: stylized-concept
Asset type: transparent 2D game PLAYER silver-white sword-combo VFX atlas for Deadlight, separate from character sprites.
Reference image is exact current player and new three-hit combo poses; use only for small scale/material/style context. Do NOT redraw any character, hands or weapon.
Create SIX isolated effects on EXACT THREE uniform columns TWO uniform rows, wide3:2 canvas preferably1536x1024, each512square. No grid, labels or text. Every full arc/fragment stays within central60% of its cell with wide fully transparent gutters. Thin clean hand-inked silver-white blade trails, pale desaturated steel-blue contour, very subtle navy underside; no neon cyan flame, no black/red magic, no broad fog glow. Distinct small gameplay-scale normal attack effects.
Row1 active arcs for hit1/hit2/hit3:
0 quick horizontal slash toward RIGHT: compact shallow silver-white crescent sweeping left-to-right, thin rim, empty transparent curve center.
1 diagonal rising slash from bottom-left to TOP-RIGHT: slender upward silver-white curved stroke, same restrained width/brightness as frame0.
2 forceful diagonal downward slash from top-left to BOTTOM-RIGHT: slightly broader silver-white crescent than0/1, a few tiny bright silver edge chips. Strong finisher but still compact, no huge explosion.
Row2 matching trailing/fade versions:
3 SAME horizontal arc shape/direction as0 but broken into two thin fading silver ribbons, smaller visual weight.
4 SAME rising arc direction as1 with a short faint split tail and2tiny silver flecks.
5 SAME downward arc direction as2 with a short fading edge and3small silver shards. No physical ground or dust.
The three effects should be easy to distinguish by orientation and should remain clean at about30-50 world pixels wide in game. Effects do NOT contain sword blades, characters, particle clouds, lightning, circular halos, UI indicators or ground markings.
Actual TRUE transparent RGBA background with zero-alpha empty space inside and around arcs. No black/white matte, no painted checkerboard, no shadows or scenery, no text/logo/watermark/border. Hand-drawn ink/cel painted style matching the player, not photorealistic, not3D, not pixel art.
```

### 최초 효과 정리 시도 (미채택)
```text
Use case: precise-object-edit
Edit target: the attached 3-column2-row sword trail sheet.
Keep EXACT same six arc silhouettes, positions, orientations, pale silver-white color and thin hand-inked steel-blue edge texture. Do not redesign shapes or add objects.
ONLY FIX: REMOVE the broad blurry grey/blue/white glow CLOUDS around every arc. All pixels outside actual inked curve strokes and the few small silver flecks should be true zero-alpha transparent. Preserve luminous white INSIDE the solid blade stroke but no fog or aura outside its contour. Remove black/grey matte completely. The image is for small clean player normal sword attacks, not magic smoke.
Maintain3x2 equal cells, same wide3:2 canvas preferably1536x1024, each arc contained inside its own cell with empty transparent margin, no grid or labels. Row1 active horizontal/rising/downward arcs; row2 same three fading broken arc variants. Preserve light thin fading strokes but no broad cloudy halos.
TRUE transparent RGBA negative space around and within arcs. No characters, swords, backgrounds, text, borders or watermark.
```

### 최종 효과 생성
```text
Create a CLEAN FLAT TRANSPARENT GAME SPRITE SHEET of six sword slash brush-strokes. The slash marks are PAINTED SOLID WHITE/SILVER INK, not light sources. NO GLOW, NO AURA, NO BLUR, NO FOG, NO SHADOW, NO GRADIENT BACKGROUND. Every pixel outside the actual sharply outlined brush-strokes and tiny chips must be zero-alpha transparent.
Layout EXACT3 columns x2 rows, wide3:2 canvas1536x1024, each cell512square. Each mark occupies only central50% of its cell with at least100px EMPTY transparent margins. No text, grid or borders. No characters or weapons.
Style: crisp hand-inked fantasy game effects, opaque ivory/silver-white dry-brush stroke with a narrow dark desaturated steel-blue contour; tiny hard-edged hatching INSIDE painted stroke only. Minimal simple shapes, NOT luminous energy or 3D lighting. Match a small navy-hooded swordsman in a hand-drawn gothic game.
Top row: 0 shallow horizontal crescent cutting toward RIGHT, 1 slender curved rising diagonal brush slash from bottom-left to TOP-RIGHT, 2 slightly thicker curved downward slash from top-left to BOTTOM-RIGHT.
Bottom row: 3 same horizontal curve broken into two thinner strokes, 4 same rising curve with split thin tail and two tiny white flecks, 5 same downward curve with split tail and three tiny white chips.
All curves have clear empty TRANSPARENT centers. Keep each mark contained in its cell. No circles, no symbols, no lightning. No surrounding hazy pixels at all; this is solid white ink with a thin blue outline cutout on ACTUAL TRANSPARENT RGBA. Do not simulate transparency with black or checkerboard.
```

