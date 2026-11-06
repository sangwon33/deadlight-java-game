# 공중 아래 베기 · 비행 적 이미지 — 2026-11-04

처음에는 사용자의 요청에 따라 이미지와 프레임 정보만 제작했습니다. 이후 가면 나방만 연결하라는 요청으로 비행 적을 게임에 연결했습니다. 아래 베기는 여전히 이미지 상태입니다. 연결 코드와 검증은 GLASSWING_1104.md를 참고하세요. 아래는 최초 이미지 제작 기록입니다.

## 생성 방식과 저장 위치

내장 image_gen 도구를 사용했습니다. CLI/API 스크립트는 사용하지 않았으며 두 이미지 모두 transparent_background=true로 생성했습니다. 기존 hero.png, enemies.png는 참고 이미지이며 편집 대상이 아닙니다. 새 PNG의 픽셀·알파는 생성 결과를 그대로 복사했습니다.

| 파일 | 실제 규격 | 프레임 |
| --- | --- | --- |
| `C:/java game/Deadlight/Deadlight/Deadlight/assets/art/hero_downslash_v1.png` | 1254×1254 RGBA, 2열×2행, 칸 627×627 | 준비 / 아래 베기 / 후속 자세 / 반동 상승 |
| `C:/java game/Deadlight/Deadlight/Deadlight/assets/art/enemy_glasswing_v1.png` | 1536×1024 RGBA, 4열×2행, 칸 384×512 | 날갯짓 4 / 돌진 준비 / 돌진 / 피격 / 사망 |
| `C:/java game/Deadlight/Deadlight/Deadlight/assets/art/aerial-combat-layout-v1.json` | 프레임별 원본 영역·그림 경계·여백·SHA-256 | 왼쪽에서 오른쪽, 위에서 아래 순서 |

플레이어는 기존 가면·후드·망토·검의 정체성을 참고했고, 비행 적은 은청색 가면 나방으로 만들었습니다. 별도 플레이어 공격 이펙트는 제작하지 않았습니다.

## 확인한 사항

- 두 PNG의 RGBA와 실제 투명 배경을 확인했습니다.
- 각 프레임에서 알파 >32인 그림의 경계와 칸 끝 사이 여백을 확인했습니다. 플레이어 최소 43px, 비행 적 최소 36px이며 검끝·날개가 칸 경계를 넘지 않습니다.
- 요청한 플레이어 캔버스는 1024×1024였지만 실제 생성 크기는 1254×1254입니다. 연결 시 반드시 실제 627×627 칸 및 JSON 영역을 사용해야 합니다.
- 프레임의 그림 경계는 확인용입니다. 공중 모션은 자세가 달라지므로 이후 연결 단계에서 몸 기준점·공통 배율을 맞춰야 하며, 각 프레임 전체 실루엣 높이에 맞춰 확대/축소하지 않아야 합니다.
- 아직 실제 게임에서 애니메이션·피격 판정·크기·반동을 검증한 상태는 아닙니다.

## 참고 파일

- 플레이어: `C:/java game/Deadlight/Deadlight/Deadlight/assets/art/hero.png` — 캐릭터 정체성과 그림체.
- 비행 적: `C:/java game/Deadlight/Deadlight/Deadlight/assets/art/enemies.png` — 기존 적의 윤곽선·명암·색.
- 배경: `C:/java game/Deadlight/Deadlight/Deadlight/assets/art/grotto.png` — 푸른 폐허의 환경 분위기.

## 최종 프롬프트: 플레이어

```text
Use case: stylized-concept. Asset type: production transparent animation sprite sheet for the existing Deadlight player. The attached image is the EXACT CHARACTER IDENTITY and drawing-style reference, not an edit target: preserve its same small masked swordsman, ivory oval mask with a thin horizontal cyan eye slit, dark navy hood, layered ragged slate-blue short cloak with round silver neck clasp, charcoal tunic and belt, dark gloves and boots, and ONE short straight silver sword. Match this reference's hand-drawn 2D game look, thin dark ink contours and restrained flat cel shading. Do not make pixel art or photorealism, do not redesign the costume or head, do not add horns. Create ONE 1024x1024 RGBA sprite sheet, strict TWO COLUMNS by TWO ROWS, FOUR equal 512x512 cells. All four frames show the SAME player facing RIGHT in a side-view aerial DOWNWARD SWORD ATTACK and rebound. Reading order top-left, top-right, bottom-left, bottom-right. Frame1 anticipation in midair: knees bent, sword drawn back above the right shoulder ready to cut below. Frame2 active downstroke: torso pitched slightly right, both hands visibly gripping ONE hilt in front of body, single silver blade pointing almost straight DOWN below the boots, cloak lifting upward with the swing. Frame3 follow-through: same blade tip straight DOWN, arms extended toward the area below the player, knees bent slightly apart, cape lifted, clear downward attack silhouette. Frame4 upward rebound after a successful hit: torso rises and knees tuck toward body, sword returns to a relaxed down-right angle, cloth still lifted. All four poses AIRBORNE, no ground. Maintain identical anatomical body size and head size in all cells: head-to-boot standing-equivalent about250px, mask about55px tall. Keep hips centered near local x256,y280 in each cell; motion expressed by limbs and cloth, not camera or changing scale. Every head, cape edge, boot, hilt and full blade must fit strictly inside local x64..448 and y64..448 in EVERY cell, at least64px transparent margin from ALL cell borders; blade tips must never touch a cell edge. Exactly two arms, two hands, two legs, one sword per frame, anatomically clean sword grip, no duplicated sword or ghost limbs. True alpha transparent background around sprites and through gaps. No attack VFX, slash arcs, particles, floor, glow clouds, grid, panel borders, numbers, text, logos, labels or watermark. Crisp readable artwork suitable for small on-screen display. This is an animation asset, not a montage or illustration.
```

## 최종 프롬프트: 비행 적

```text
Use case: stylized-concept. Asset type: one transparent 8-frame animation atlas of ONE NEW flying enemy for the 2D side-view platformer Deadlight. Reference image1 is ONLY the existing enemies' hand-drawn ink and cel-shading style, slate navy/silver palette and readable pale masks. Reference image2 is ONLY environmental palette/mood: moonlit blue root caverns and ruined arches. Create a distinct flying enemy called a glasswing wraith moth: a small tapered dark navy spectral insect torso, a modest ivory shield-shaped face mask with TWO narrow cyan eye slits, two short swept-back antennae, and TWO broad tattered silver-blue moth wings with dark veins and ragged navy outer edges. No humanoid hands, legs, weapons, cloak hood or giant head. Pale wing edges and light ivory face must read clearly against dark scenery, cyan glow restricted to the two eye slits and a tiny chest jewel. Wings must visibly attach to the same thorax. One coherent 3/4 SIDE-VIEW moth looking/facing RIGHT in every live pose. Hand-drawn 2D game sprite with thin dark ink outlines and limited flat cel shading, delicate parchment-like wing panels; match the reference finish, NOT pixel art, no smooth airbrush/photo/3D. Create ONE 1536x1024 RGBA atlas with a strict FOUR COLUMNS x TWO ROWS equal cells (384x512 each). Exactly one isolated full-body moth per cell. Reading order: top row frames1-4 are a seamless slow flight cycle: 1 wings swept upward, 2 wings opening level, 3 wings beating down, 4 wings rising back halfway. Bottom row: 5 attack preparation with wings folded back and cyan eyes bright, body pitched toward RIGHT; 6 short forward diving-lunge attack, wings streamlined back and body pointing down-right; 7 hurt recoil with mask intact, wings opened unevenly but no lost parts; 8 defeat, the same mask tipped down and wings folded/broken into a few nearby floating fragments, no gore. Keep exact same mask/body dimensions and identical drawing scale in every frame. Body/head about110px total length in every cell, wingspan up to265px, pivot/thorax centered near cell-local x192,y250; animations change wing pose, NOT overall scale or screen zoom. Every wing tip, antenna, tail tip and death fragment MUST be contained inside local x48..336,y64..448, giving at least48px of transparent border left/right and64px top/bottom in EACH cell. No overlap across cells, no clipped wings. True alpha transparent background around every object and through gaps. No floor, ground shadows, decorative frame, panel border, grid, text, labels, numbers, logos or watermark. Do not include the existing ground slime/knight enemies or player. Production sprite atlas, not a scene or character concept board.
```

