# 1·3스테이지 선택형 중간 보스 에셋 — 2026-11-05

1·3스테이지 중간 보스의 이미지 제작 기록입니다. 후속 요청으로 2026-11-05에 게임에 연결했습니다. 현재 전투·진입 경로·보상·검증은 `SIDE_BOSSES_STAGE1_3_1105.md`를 참고하세요. 아래는 에셋 제작 당시의 기록이며 원본 PNG는 보존했습니다.

## 최종 파일
| 대상 | 캐릭터 8포즈 | 장비·이펙트 8종 | 프레임 정보 |
| --- | --- | --- | --- |
| 1스테이지 뿌리에 잠든 순례자 | `assets/art/miniboss_root_pilgrim_v1.png` | `assets/art/root_pilgrim_fx_v1.png` | `assets/art/root-pilgrim-layout-v1.json` |
| 3스테이지 재를 꿰매는 자 | `assets/art/miniboss_ashen_stitcher_v2.png` | `assets/art/ashen_stitcher_fx_v1.png` | `assets/art/ashen-stitcher-layout-v1.json` |

4개 PNG 모두 실제 출력은 RGBA 1774×887입니다. 4열×2행의 포즈/효과 시트이며, 8개 키프레임을 갖습니다. 모든 중간 동작을 갖춘 완성 애니메이션은 아닙니다.

## 디자인과 모션
- 순례자: 현재 1스테이지의 `grotto.png`에 맞춘 청회색 뿌리·차가운 이끼·상아색 가면·등에 묶인 작은 석문. 기존 등불지기의 손그림 선과 채색 질감을 참고했습니다.
- 순례자 포즈: 대기 / 전진 / 돌진 준비 / 어깨 돌진 / 뿌리 팔 준비 / 전방 휘두르기 / 피격 / 무릎 꿇은 패배.
- 순례자 효과: 뿌리 봉오리 / 솟는 뿌리 가시 / 뿌리 손톱 / 석재 파편 / 뿌리 베기 궤적 / 돌진 궤적 / 지면 충돌 / 씨앗 유물 외형.
- 봉합자: 현재 3스테이지의 `orchard.png`에 맞춘 잿빛·남청색 장례복, 먼지 낀 연보라색 옷자락, 실로 이어진 상아색 가면, 바늘 무기와 보라색 실.
- 봉합자 포즈: 대기 / 전진 / 찌르기 준비 / 전방 찌르기 / 도약 / 대각선 급강하 / 피격 / 무릎 꿇은 패배.
- 봉합자 효과: 수평 바늘 / 대각선 바늘 / 실 조각 / 매듭 구슬 / 급강하 궤적 / 착지 충돌 / 가면 파편 / 잔광과 재 꽃잎.
- 유물·매듭 등은 외형 프로토타입 에셋입니다. 지급 효과나 전투 기능이 구현된 것은 아닙니다.

## 확인 및 연결 시 주의
- 내장 이미지 생성 도구 **built-in image_gen**만 사용했습니다. 생성 후 PNG의 픽셀·알파를 스크립트로 수정하지 않고 프로젝트로 복사했습니다.
- 봉합자 첫 시트는 바늘·망토가 프레임 경계를 넘어 채택하지 않았습니다. 같은 외형과 그림체를 유지하며 여백을 넓힌 수정본 v2를 최종으로 선택했습니다. 프로젝트에는 최종 v2를 보관합니다.
- 각 시트의 알파 채널을 읽기 전용으로 검사했습니다. 알파 > 80 기준 주요 실루엣의 프레임 가장자리 최소 여백은 순례자 캐릭터 20px / 순례자 효과 26px / 봉합자 캐릭터 34px / 봉합자 효과 31px입니다. 낮은 알파의 가장자리 픽셀은 별도일 수 있습니다.
- 실제 크기가 4와 2로 정확히 나누어지지 않으므로 JSON의 명시된 프레임 영역을 사용하세요. 칸 너비/높이는 반올림에 따라 1px 차이가 있습니다.
- 연결 시 포즈마다 하나의 공통 배율을 유지하고 발·몸통·무기 기준점을 따로 보정해야 합니다. 각 칸의 가시 영역 크기에 맞춰 캐릭터를 늘이거나 줄이지 마세요.
- 날아가는 자세의 높이 변화는 점프 포즈입니다. 실제 비행 경로, 충돌 범위, 이펙트 회전 중심, 바닥 기준점은 게임 연결 단계에서 확인해야 합니다.
- PNG 잘림/알파와 그림체를 확인했으며 실제 게임 렌더링·보스 AI·전투 난이도는 아직 검증 대상이 아닙니다.

## 최종 프롬프트 세트

### 순례자 캐릭터
```text
Use case: stylized-concept
Asset type: transparent game-ready animation keyframe atlas, stage1 optional miniboss for Deadlight, "The Rootbound Pilgrim / 뿌리에 잠든 순례자".
Input image1: existing Lamplighter sprite sheet is STYLE reference ONLY, not a character to edit/copy. Match its fine dark hand-inked contours, muted painterly cel shading and slightly weathered gothic storybook detail.
Input image2: stage1 root grotto ENVIRONMENT reference ONLY. Use its desaturated moss/blue-grey/icy-cyan mood, no environment in output.
Design a NEW distinctive stocky pilgrim, not another metal knight. A hunched humanoid wrapped in dark slate-blue burial cloth and aged bark roots. One cracked elongated IVORY ceramic mask with TWO small dim cyan slits, no visible human face. Carries on its back a SMALL broken gothic stone arch fragment like an ancient burden, bound tightly by roots. The arch is attached to its back in EVERY frame at same size, not a separate doorway. Broad bark forearms with wooden claw fingers, sturdy short wrapped legs and grounded stone-like feet. Sparse pale teal moss at shoulder/back, very little foliage. Chest has one muted icy-cyan seed-shaped core. NO carried sword, no spear, no lantern, no antlers, no tree crown, no leafy giant canopy. Its root fist is its weapon.
Canvas: wide 2:1 sprite sheet, prefer2048x1024, FOUR uniform columns and TWO uniform rows = EIGHT separate square cells, no drawn grid. Same character identity, root pattern, arch fragment, mask and SAME anatomical scale across all8 poses. All face RIGHT in strict side view. Standing body roughly280px tall in512cell, boots baseline local y=440, pelvis ground pivot near x=230; arch fits above shoulders. Reserve generous EMPTY transparent 50px margins on EVERY edge of EVERY cell. Do not enlarge poses to fill space. Arms and roots never cross cell boundaries. Include entire arch and hands in every pose. Fixed camera, no perspective rotations.
Row-major8 poses:
0 idle hunched guard, big root hands hanging ahead, back arch clearly visible.
1 lumbering forward stride, one foot forward, torso same width/height.
2 low compact charge preparation: knees bent, mask facing right, one root shoulder forward.
3 forward low shoulder rush, rear foot pushing off, root arms folded compactly for motion; no long speed trail.
4 overhead root-fist windup, one bent bark arm lifted, stone arch and raised fist comfortably inside cell with margin.
5 strong forward sweeping root-arm strike, arm extended right with broad wooden fingers; no infinitely stretching roots, entire fist inside cell.
6 stagger recoiling backward, cyan chest seed flickers, SAME mask and body proportions.
7 defeated crouched/kneeling pilgrim, arch leaning with torso, head bowed, hands on ground, cyan core dim. Keep complete feet/arch and avoid dissolving into a different object.
Background: TRUE transparent RGBA, not black or painted checkerboard. No floor/shadow, no scenic decoration, no text, no labels, no watermarks, no border. Mostly opaque painted objects, no broad glow clouds. Uncropped isolated sprites with clean transparent gutters and original hand-drawn texture, NOT pixel art and NOT photorealistic.
```

### 순례자 장비·효과
```text
Use case: stylized-concept
Asset type: clean isolated transparent 2D game VFX atlas, Rootbound Pilgrim miniboss stage1 Deadlight.
Input image1 is CHARACTER / MATERIAL reference: match its dark weathered twisted roots, slate cloth, ancient grey stone and pale teal moss.
Input image2 is ENVIRONMENT palette reference only: blue desaturated root grotto, no scenery in output.
NEW sprite atlas of EIGHT SEPARATE effects and props. Match fine dark hand-inked outlines and painterly cel shading; cool slate-grey bark, muted pale teal moss, tiny icy-cyan seed highlights. No orange fire, no saturated green neon, no broad glow. Silhouettes clean enough for small gameplay.
Canvas wide2:1 preferably2048x1024, uniform FOUR columns TWO rows, each512square. No grid drawn, no labels. Each sprite entirely inside the CENTER60% of its cell, at least70px CLEAR TRANSPARENT gutters on every side. Same drawing scale for related roots/shards. No object, dust or halo crosses a boundary. Do not crop outer transparent margins.
Row-major frames:
0 compact curled seed/root bud on a small stone base, dim cyan seam: dormant root prop.
1 SAME root species erupting into one tall hooked root spike from a narrow twisted base; full point and base visible, isolated not a ground scene.
2 TWO lower intertwined root claws sweeping toward RIGHT, short self-contained root lash, entire left cut end and right claw visible.
3 small cluster of three airborne carved grey stone shards with tiny teal moss flakes, no explosion cloud.
4 broad curved SLASH arc made of thin desaturated teal and grey bark splinters, empty transparent center and gaps, NO surrounding fog.
5 low compact shoulder-charge streak directed RIGHT, angular pale-cyan core with short slate-grey dust strokes, no character, no smoke cloud.
6 ground-hit impact: small radial burst of angular grey stone and dark root fragments around one subtle cyan spark, all pieces cleanly isolated.
7 fallen seed-shaped chest relic encased in tiny broken ivory stone cradle with a faint cyan crack, no halo; a small readable reward icon/prototype prop, not text.
TRUE transparent RGBA background, zero alpha negative space, mostly opaque props. No black/grey/white matte or painted checkerboard. No floor plane, no background vines, no cast shadow, no large mist or glow clouds, no characters, no text, no border, no watermarks. Preserve gothic hand-drawn style, not pixel art, not photorealistic.
```

### 봉합자 최초 캐릭터 생성
```text
Use case: stylized-concept
Asset type: transparent 2D side-scroller miniboss pose atlas, stage3 optional miniboss Deadlight, "The Ashen Stitcher / 재를 꿰매는 자".
Input image1 is hand-drawn STYLE reference ONLY (existing Lamplighter). Use same detailed dark ink contours, opaque desaturated cel-painted surfaces and restrained weathered texture. Do NOT make another plated knight.
Input image2 is stage3 ENVIRONMENT reference only: violet-grey ruined orchard, pale bark, hanging mourning flowers. No scenery in output.
Design a completely NEW slender agile funeral-weaver. Tall narrow silhouette, elongated thin wrapped arms, ragged layered charcoal/navy mourning robe with petal-like lavender-grey hems. Face is an asymmetrical ivory MASK split into THREE visibly distinct ceramic pieces held together by thin DARK VIOLET stitch lines, with one narrow dim lavender eye slit; no human skin, no horns, no crown. A small faint pale-lilac knot at chest. A short tattered shawl curves backward like faded mourning petals, NOT wings. Thin wrapped boots visible under robe. Holds ONE long steel sewing needle like a rapier, threaded at the handle through a large needle eye by one short dark-violet strand. Needle length roughly standing body height, slim pointed silver shaft, no ornate spearhead, no cross guard, NO sword blade, no scissors. Needle and thread stay the SAME design in every frame. Color: charcoal/slate, dusty lavender, weathered ivory/silver; tiny pale lilac accents, NO red gore, no green roots, no orange fire, no neon purple fog.
Composition: wide2:1 canvas preferably2048x1024, EXACT FOUR columns TWO rows, eight separate equal square cells, no grid lines. All poses RIGHT-facing strict side view, same body and weapon scale, fixed camera. Standing anatomical height approximately285px within512cell, grounded boot baseline localy=440, ground/body pivot near localx=210. For lunges bend joints, never stretch or shrink limbs or needle. Leave at least45px CLEAR transparent gutter on every edge of every cell, complete needle point/back end and loose thread never reach or cross borders. Hover/jump poses rise within the same source scale, not camera zoom. No long trails attached to character.
Frames row-major:
0 idle standing lightly poised, rapier-needle angled upward right, short loose thread curls near free hand.
1 agile forward step with robe trailing slightly left, needle held in guard.
2 needle thrust preparation: low poised stance with elbow drawn back, needle horizontal pointed right.
3 rapid forward needle thrust, long narrow lunge, entire needle fits cell with safe margins; robe stays same volume, free hand behind.
4 leap-rise pose: knees tuck, robe lifts, needle drawn diagonally above/forward right; keep all within cell.
5 diagonal down-right plunge pose: leaning diagonally with knees bent, needle directed down-right and inside cell, same limb proportions, no huge ground explosion.
6 hit/stagger: upper body recoils, separated mask pieces slightly loosen but stay around head; needle still held, readable full figure.
7 defeat: kneeling folded mourning robe, bowed fragmented mask, needle lowered/resting along ground completely inside cell; a few small ivory fragments nearby contained in cell. No gore, no giant transformation.
TRUE transparent RGBA background, not black, not checkerboard painted into pixels, no ground/shadows, no scenery, no text/labels/grid/watermarks. Fine hand-drawn gothic game art consistent with references, NOT pixel art and NOT realistic3D. Mostly opaque clean outlines, no broad glow clouds.
```

### 봉합자 캐릭터 최종 수정
```text
Use case: precise-object-edit
Edit target: image1 is the Ashen Stitcher 4x2 animation sheet to repair. Keep EXACT same character, all8 pose identities, detailed hand-inked painterly shading, ivory segmented stitched mask, dusty violet mourning robe, slender sewing needle and violet threads. No redesign, no palette or texture changes, no new poses.
PROBLEM: long robe/needle silhouettes spill across individual cell boundaries. Specifically row1 column4 cloak extends left into column3; row2 column1 raised needle protrudes up into row1 column1; row2 column4 death needle/robe meets column3; the long thrust tip is too close to outer right canvas edge. These are separate animation sprites and must NEVER touch neighbouring cells.
FIX: rebuild same FOUR columns TWO rows atlas with a LOT of EMPTY transparent gutter. UNIFORMLY REDUCE all8 entire figures/equipment to 72% of previous source scale, identical common scale factor for every pose. Recenter each complete pose within its OWN equal square cell. Maintain character and needle anatomical proportions, do not shorten weapon or crop cloak. Restore full outline where it crossed former cell. Every pose including full needle, robe and loose threads must fit inside central70% of its own cell, leaving at least60pixels clear gutter on every side in a512cell. No pixels from any other pose in that cell. Standing feet share consistent local baseline near y=410; jump pose may rise but remains contained. No independent auto-enlargement of any pose.
Row-major poses remain0 idle,1 stride,2 thrust prepare,3 thrust strike,4 leap rise,5 downward diagonal plunge,6 stagger,7 kneeling defeat.
Keep a wide2:1 transparent canvas, ideally2048x1024, no auto-trimming. All8 isolated complete sprites on actual zero-alpha TRANSPARENT background, no black matte or painted checkerboard, NO grid lines, no labels/text, no new glow or fog. Preserve the original drawing's line quality and surface texture. This edit is to fix animation slicing/cropping only.
```

### 봉합자 장비·효과
```text
Use case: stylized-concept
Asset type: isolated transparent weapon/effect atlas, stage3 optional miniboss Deadlight "The Ashen Stitcher".
Input image1 is EQUIPMENT and STYLE reference: match this thin silver sewing-needle weapon with large oval needle eye, violet thread and fragmented ivory mask. Input image2 is stage3 ENVIRONMENT palette reference only, dusty purple ruined orchard, NO scenery.
Create NEW EIGHT separate props/VFX in hand-inked slightly weathered cel-painted gothic art. Fine dark navy outlines, opaque silver/ivory parts, charcoal thread edged in dusty lilac for readability. Effects pale lavender-white cores, restrained navy-violet contour. No broad glow, no fog, no blood, no orange fire, no chains.
Canvas wide2:1 preferably2048x1024, FOUR uniform columns TWO rows, eight square cells. Each complete effect fits central60% of its own cell, huge empty transparent gutters at least70px every edge; no trail or shard crosses cell border. No labels, no drawn grid, no text. No crop of outer empty margins.
Row-major:
0 physical sewing-needle weapon, horizontal pointed RIGHT, long slim silver straight shaft and elongated oval eye at left, a tiny short violet thread tied at eye. Exactly a sewing needle not a sword/spear.
1 SAME needle at SAME physical scale pointing diagonally DOWN-RIGHT about55degrees; no background ground or attached large impact.
2 short taut horizontal dark-violet thread segment with pale lilac edge, one tiny loose curl at each end. No metal chain, no needle, no symbols.
3 small intertwined knot of violet thread around one weathered ivory ceramic bead with a hairline lavender glow INSIDE the bead only, isolated readable prop.
4 diagonal down-right SLASH crescent made from fine violet-white strokes, transparent empty center, no physical needle or cloud.
5 compact landing impact, pale lavender star with a few sharp silver/ivory shards and 2 thin curling stitch threads, no surrounding fog or floor.
6 cluster of THREE distinct small cracked ivory mask fragments from the boss face, bound loosely together by thin violet threads, all contained centrally.
7 small fading lavender-white wisp and FOUR tiny pale ash petals, hard-edged hand-drawn strokes with only tiny translucent edges, NOT a smoky large fog patch.
Background TRUE transparent RGBA, zero-alpha negative spaces between thread, shards and arcs. Mostly opaque sharp objects and effects with NO halo outside ink contours. No matte black/white, no painted checkerboard, no characters, no environment, no floor or shadow, no letters, no watermark, no border. Same refined hand-drawn texture as reference, NOT pixel art or realistic3D.
```

