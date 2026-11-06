# 등불지기 중간 보스 에셋 — 2026-11-05

현재 프로젝트의 성벽 지역과 등불 사수에 맞춰 만든 선택형 중간 보스의 에셋 제작 기록입니다. 후속 요청으로 2026-11-05에 2스테이지에 연결했습니다. 현재 기능과 검증 내용은 `LAMPLIGHTER_STAGE2_1105.md`를 참고하세요. 아래는 원본 이미지 제작 당시 기록입니다.

## 결과물
- `assets/art/miniboss_lamplighter_v1.png`: RGBA 1774×887, 4열×2행, 캐릭터 포즈 8개.
- `assets/art/lamplighter_fx_v1.png`: RGBA 1774×887, 4열×2행, 분리된 장비/효과 8개.
- `assets/art/lamplighter-layout-v1.json`: 실제 출력 크기에 맞춘 프레임 영역과 연결 시 주의점.

캐릭터: 대기 / 전진 / 찌르기 준비 / 찌르기 / 등불 투척 / 사슬 당기기 / 피격 / 쓰러짐.
장비·이펙트: 켜진 등불 / 투척 등불 / 꺼진 등불 / 사슬 조각 / 투척 호 / 찌르기 섬광 / 충돌 / 잔불.

빈 갑옷, 상아색 면갑, 가슴의 청백색 불꽃, 남청색 천, 창과 사슬 등불을 사용했습니다. 기존 적과 성벽 배경을 그림체 참고로 사용했습니다. 기존 이미지는 덮어쓰지 않았습니다.

## 확인과 연결 시 주의점
- PNG의 실제 알파 채널과 프레임별 가시 영역을 읽기 전용으로 검사했습니다. 주요 실루엣(알파 > 80)의 잘림은 발견하지 못했습니다.
- 효과 첫 생성본은 넓은 빛 번짐이 칸을 넘어서서 채택하지 않았습니다. 번짐을 제거하고 간격을 확보한 수정본을 최종 파일로 저장했습니다.
- 생성 출력은 요청한 2048×1024와 달리 1774×887입니다. 따라서 단순 정수 나눗셈 대신 JSON의 명시된 프레임 영역을 사용해야 합니다.
- 부위 비율과 애니메이션 연결이 실제 게임에서 검증된 것은 아닙니다. 연결할 때 동일 배율을 유지하고 발·몸체 기준점을 보정해야 합니다. 프레임마다 가시 영역에 맞춰 늘이거나 줄이지 마세요.
- 현재는 모션 키프레임 8개이며 중간 프레임을 모두 갖춘 부드러운 애니메이션은 아닙니다.
- 생성 PNG의 픽셀과 알파는 추가 편집하지 않고 그대로 복사했습니다.

## 생성 방식
**built-in image_gen** 사용. CLI/API 우회 또는 별도 이미지 편집 스크립트는 사용하지 않았습니다.

참고: `enemy_lantern_marksman_v1.png`, `rampart.png`, `lantern_shot_fx_v1.png`.

## 최종 프롬프트 기록

### 캐릭터 생성
```text
Use case: stylized-concept
Asset type: production 2D side-scroller miniboss animation sprite atlas for Deadlight, titled internally "The Undying Lamplighter". NEW CHARACTER, not an edit of the references.
Input images: image 1 is STYLE/PALETTE reference only (existing lantern marksman); image 2 is ENVIRONMENT reference only (blue ruined rampart). Match their hand-inked outlines, restrained painterly cel shading, slightly weathered surfaces and silver/slate/ivory/cyan palette. Do not reproduce background or reference character.
Subject: an imposing EMPTY suit of elegant ruined gothic plate armor. Narrow enclosed ivory faceplate with ONE vertical cyan eye slit, tall broken collar, broad but lean armored shoulders, ragged indigo waist cloth, articulated legs and large boots. Cool cyan flame visible inside the hollow chest through a small broken rib-shaped armor opening. No human skin, no hood, no wings, no crown. This is a unique miniboss related to lantern enemies, NOT simply the little hooded marksman enlarged.
Equipment: slim steel thrusting spear in the forward hand, and a heavy hexagonal lantern in the other hand attached by steel chain to a cuff on that wrist. Light desaturated icy cyan lantern, limited glow. Both weapons remain consistent. Spear is roughly body-height, not an excessively long lance.
Composition: landscape 2048x1024 canvas, EXACT uniform FOUR columns x TWO rows = EIGHT separate square 512x512 cells. No drawn grid, no labels. ALL poses face RIGHT in strict side view, no perspective camera changes. Fixed anatomical proportions and SAME pixel scale, no resizing character to fit a pose. Standing head approximately local y=150 and grounded boot baseline y=450 in each cell, torso/hip ground pivot around local x=205. Kneeling/death intentionally lower but same physical limb lengths. Reserve at least 28px transparent gutter inside EVERY edge of each cell; NO part, spear, lantern, chain or glow crosses cell borders. For thrust frame, extend spear to the right within x<482 with rear shaft in x>28, move arms instead of changing spear length. Compact readable silhouette, useful at small gameplay size. No enemies other than these 8 copies of SAME boss.
Frames in row-major order:
0 idle: upright calm guard, spear diagonally raised forward, lantern low near rear hand.
1 stride: one purposeful advancing step, torso same size, spear guarded, cloth trailing.
2 thrust preparation: knees flexed, torso slightly drawn back, spear aimed horizontally right, lantern near hip.
3 thrust strike: deep forward lunge with same limb scale, spear fully horizontal right, entire spear point and rear end comfortably inside cell.
4 lantern cast: rear arm swings outward throwing lantern to right in a short arc; visible short loose curved chain from cuff to lantern, spear held compact diagonally behind/above body.
5 chain pull: braced low stance pulling taut chain toward a lantern on right, no stretched arms; spear still retained, chain and lantern wholly within cell. Grounded fast-pull pose.
6 stagger: chest recoils and cyan chest flame flickers, armor still same shape, spear lowered and lantern droops.
7 defeated: collapsed kneeling armor with bowed helmet and dim chest, spear resting diagonally on ground, lantern dark beside boot; intact readable character, no debris outside cell.
Background: TRUE TRANSPARENT RGBA, not black, no environment, no shadows cast on a floor, no checkerboard painted into pixels, no vignette, no text, no watermark. Character details opaque; only restrained flame halo translucent. Keep complete outlines and weapon tips UNCROPPED. These must be separate usable game sprites, not concept illustration panels.
```

### 장비·효과 생성
```text
Use case: stylized-concept
Asset type: transparent production 2D side-scroller weapon-and-effects atlas for Deadlight's OPTIONAL miniboss "The Undying Lamplighter".
Input images: image1 is the newly created miniboss CHARACTER/EQUIPMENT reference: precisely match its hexagonal silver lantern, slim oval chain links and icy cyan chest/lantern flame. Image2 is a STYLE reference for effects only: outlined hand-painted cel-shaded steel blue/cyan fantasy effects. These references are not edit targets.
Create NEW separate weapon and effect sprites, NO character bodies. Same inked slightly weathered gothic storybook art as references; no pixel art, no realistic CGI, no purple neon or orange fire. Dark blue outlines, silver metal, cyan-white flame core, small restrained translucent halo.
Composition: wide 2:1 atlas with FOUR equal columns x TWO equal rows. Prefer 2048x1024 pixels with uniform512x512 cells. Each sprite centered inside its own cell with generous EMPTY TRANSPARENT margins at least 60 pixels in every direction. NO cell borders or labels. All sprites fully isolated, no touching or bleeding between cells. Do not trim the canvas or crop outer transparent margins. All eight objects shown completely.
Row-major frames:
0 silver HEXAGONAL lantern upright, handle at top, pale cyan glass flame, precise shape from boss reference, no attached chain or ground.
1 exact SAME lantern at SAME size, tilted clockwise approximately40degrees as an airborne thrown weapon, cyan glass bright, a SHORT curved pale-cyan motion wake extending behind it to upper-left, no chain attached.
2 exact SAME lantern upright SAME size but extinguished charcoal-blue glass, no glow; this is a physical unlit lantern sprite not an empty square.
3 one short horizontal strip of 6 overlapping slim oval steel chain links, drawn side-on, at cell center. No lantern, no spike, no bar. Ends clearly visible, straight readable chain segment suitable for later repeated placement.
4 standalone compact icy-cyan curved slash arc for lantern casting/swing; white-blue inner stroke, dark steel-blue ink contour, left-to-right crescent with empty center, no physical objects.
5 standalone horizontal spear-thrust streak aimed RIGHT: thin bright-white cyan spearpoint-shaped flash with two short blue trailing lines, all inside cell, no spear or body.
6 impact effect: compact icy-blue radial starburst with small silver angular shards and a broken circular shock ring, all clearly separated from other cells.
7 a small fading upward cyan wisp with 4 subtle blue embers, diminishing lantern flame end effect, restrained glow no broad fog.
Lantern frames0,1,2 have an identical physical design and scale, about150pixels high in a512cell; other effects200-300pixels wide. These are small game-readable VFX, not full scenic paintings.
Background: actual TRANSPARENT RGBA, zero-alpha outside clean sprites, no black matte, no painted checkerboard, no floor, no environment, no text, no decorative frame, no labels, no watermark. Do NOT repeat the knight from reference.
```

### 최종 장비·효과 정리
```text
Use case: precise-object-edit
Edit target: FIRST reference image is the effects atlas to fix. SECOND reference is equipment/style reference only.
Keep the SAME eight sprites in SAME row-major order and SAME 4x2 uniform-cell layout, preserve hand-inked cel-shaded art, silver/steel/icy-cyan palette and lantern identity.
CRITICAL FIX ONLY: remove ALL broad grey/white/blue fog, outer glow clouds, mist, soft aureoles, shadows and background mattes surrounding the objects. The original's enormous cloudy halos overlap adjacent cells, which is unusable. Every pixel outside the sharply outlined actual lantern/chain/flame/streak/shards should be fully transparent. Preserve the bright cyan glass INSIDE the lantern and the colored core INSIDE each effect stroke, but NO aura outside outlines.
Scale each complete sprite silhouette down uniformly around its own cell center to leave CLEAR GENEROUS TRANSPARENT GUTTERS. Fit every sprite including trails and shards inside the central60% of its cell. The first lantern, thrown lantern and unlit lantern are the same physical size. Keep complete silhouette including all outer shards and motion tail. No contour clipping, no overlap at any cell boundary. Leave ALL outer canvas borders EMPTY.
Frame0 lit upright silver lantern;1 same thrown lantern tilting right with small outlined blue streak behind;2 same unlit upright lantern;3 short straight horizontal strip of steel chain links;4 clean cyan curved slash arc;5 rightward cyan thrust streak;6 compact cyan impact with outlined shards;7 small thin cyan fading wisp and embers.
TRUE TRANSPARENT RGBA background, transparent negative space in chain links and curved arcs. No painted checkerboard, no black background, no white background, no grids, labels or text. Preserve FOUR columns TWO rows. Wide2:1 canvas ideally2048x1024; do not crop transparent margins. This is a clean isolated game VFX atlas, NOT a glowing atmospheric illustration.
```

