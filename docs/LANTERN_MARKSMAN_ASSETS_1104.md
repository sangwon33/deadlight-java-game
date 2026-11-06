# 등불 사수 · 탄환 이펙트 이미지 — 2026-11-04

최초에는 사용자 요청에 따라 이미지와 프레임 정보만 제작했습니다. 이후 연결 요청으로 등불 사수와 탄환을 게임에 적용했습니다. 현재 배치와 검사 결과는 LANTERN_MARKSMAN_1104.md를 참고하세요. 아래는 최초 이미지 제작 기록입니다.

## 파일과 실제 규격

내장 image_gen 도구를 사용했으며 두 PNG 모두 transparent_background=true로 생성했습니다. CLI/API 스크립트는 사용하지 않았습니다. 기존 에셋을 참고하여 별도 파일을 만들었고 원본 에셋과 새 PNG의 픽셀·알파를 수정하지 않았습니다.

| 파일 | 실제 규격 | 구성 |
| --- | --- | --- |
| `C:/java game/Deadlight/Deadlight/Deadlight/assets/art/enemy_lantern_marksman_v1.png` | 1536×1024 RGBA, 4열×2행, 칸 384×512 | 대기 / 걷기 A / 걷기 B / 조준 / 충전 / 발사 반동 / 피격 / 사망 |
| `C:/java game/Deadlight/Deadlight/Deadlight/assets/art/lantern_shot_fx_v1.png` | 1536×1024 RGBA, 4열×2행, 칸 384×512 | 탄환 A/B/C / 충전 빛 / 발사 섬광 / 충돌 접촉 / 확산 / 소멸 |
| `C:/java game/Deadlight/Deadlight/Deadlight/assets/art/lantern-marksman-layout-v1.json` | 프레임 영역·그림 경계·여백·SHA-256 | 행 순서, 아직 게임 미연결 |

캐릭터는 은청색 손그림풍이며, 상인과 구분하기 위해 배낭·새 부리 가면을 제외하고 각진 가면과 전방으로 드는 등불을 사용했습니다. 실제 탄환과 발사/충돌 이펙트는 적 모션에 합치지 않았습니다.

## 확인

- 두 시트의 실제 RGBA와 투명 영역, 8개 프레임을 확인했습니다.
- 알파 >32인 그림이 각 칸 안에 있는지 확인했습니다. 적은 최소 42px, 이펙트는 최소 66px 여백이 있으며 손·등불·망토·빛 파편이 칸 경계를 넘지 않습니다.
- 프레임은 전체 384×512 칸으로 읽을 수 있습니다. JSON의 그림 경계는 검사 데이터이며, 캐릭터의 몸/발 기준점과 탄환 중심/발사점은 추후 연결 시 따로 설정해야 합니다. 자세별 그림 높이에 맞춰 크기를 재조정하면 안 됩니다.
- 원본 PNG는 약한 외곽 발광을 포함합니다. 아직 실제 게임에서 배율·발사 방향·프레임 연결·가독성을 검증한 상태는 아닙니다.

## 참조 이미지

- `C:/java game/Deadlight/Deadlight/Deadlight/assets/art/enemies.png`: 기존 적의 윤곽선·명암·팔레트 참고.
- `C:/java game/Deadlight/Deadlight/Deadlight/assets/art/relics.png`: 등불 재질과 차가운 빛 참고. 상인 캐릭터를 복사하는 용도가 아닙니다.
- 새 등불 사수 생성 결과: 이펙트의 광원 색과 그림체 참고.

## 최종 프롬프트: 등불 사수

```text
Use case: stylized-concept. Asset type: ONE transparent 8-frame animation sprite sheet for a NEW ranged ENEMY called the Lantern Marksman in the 2D side-view game Deadlight. Image1 is ONLY a reference for the existing hand-drawn enemy style, thin ink contours, cel-shaded navy/silver palette, readable masks. Image2 is ONLY a reference for wrought lantern materials and cool light, NOT a character to copy. Make a hostile narrow-shouldered hooded sentinel: weathered dark slate-blue long tunic, ragged short mantle, charcoal gloves/boots, a slim angular IVORY face mask with TWO vertical pale-blue eye slits, one muted violet shoulder wrap. Distinct from the game's friendly merchant: NO backpack, bedroll, pouches, plague-doctor beak or dangling shopping lantern. Also distinct from the player: no sword, no horizontal single eye slit. This enemy carries ONE compact hexagonal iron lantern with silver ribs and a round glass front, used like a handheld magical emitter. One hand grips its rear/top handle, the other hand supports its base. Lantern points RIGHT when aiming, held clearly in front of the body, visible cyan-white light core inside. Exactly two arms, two hands and two legs; no floating or extra weapons. Strict side-view / slight three-quarter side view looking RIGHT. Match the reference's hand-drawn 2D outlined cel-shading, subtle worn material details and restrained silver highlights; not pixel art, 3D, glossy render, smooth airbrush or photo. ONE 1536x1024 RGBA atlas, strict FOUR COLUMNS x TWO ROWS, eight equal 384x512 cells. Exactly one FULL character per cell, row-major order: 1 idle holding dim lantern at waist; 2 short cautious walking step A; 3 walking step B; 4 aim/raise the lantern toward RIGHT at chest height; 5 charge telegraph, feet planted and lantern glass brighter cyan-white, same aiming pose; 6 firing recoil, slight backwards lean, lantern still held securely and pointing RIGHT, no projectile or muzzle flash drawn into character; 7 hurt recoil, mask intact, lantern lowered but still held; 8 defeated collapse, kneeling/slumped with dim lantern beside the same body, no gore or blood. Maintain IDENTICAL character/head proportions and lantern size in every cell, standing-equivalent head-to-boots about270px, head/mask about55px tall. Standing/moving/aiming poses share boots baseline near local y416, body center near local x172. All hood, cloak, hands, feet and FULL lantern must be strictly contained inside local x48..336,y64..448 of EVERY cell, with at least48px empty transparent padding on every side. No clipped limbs, no cross-cell overlap, no scaling up for shooting. True alpha transparent background and gaps. No floor, ground shadow, broad glow fog, attack effects, projectile, text, numbers, labels, grid lines, panel borders, logos or watermark. Production animation assets, not a concept board.
```

## 최종 프롬프트: 탄환과 이펙트

```text
Use case: stylized-concept. Asset type: ONE transparent 8-cell projectile/VFX sprite atlas for a side-view 2D game, designed for the lantern ranged enemy in the reference image. Reference image is ONLY the matching lantern emitter, cyan-white light color and hand-drawn ink/cel-shading finish; DO NOT draw the enemy, lantern object, mask, character, weapon or scenery. Create clean isolated small magical LIGHT-BOLT game effects that can be drawn separately over gameplay. Palette: opaque bright ivory-white central core, icy cyan middle shapes, restrained steel-blue outer contours and a tiny hint of violet in dispersing sparks. Readable clear silhouettes on a blue/gray background; tight edge glow only, no huge halos, fog, gradient rectangles, blur, bloom clouds, painted black background, or white background. Hand-drawn 2D cel-shaded effects with hard shaped light ribbons and tapered shards, NOT pixel art, photorealistic fire, 3D or explosions. ONE 1536x1024 RGBA image with strict FOUR COLUMNS by TWO ROWS, eight equal384x512 cells. No grid/borders/text. Row-major cells1-3: THREE successive flight frames of ONE compact RIGHT-moving bolt, same exact size and core position in all three cells, luminous teardrop/short needle with pointed nose toward RIGHT and two short tapering ribbons trailing LEFT. Bolt dimensions about180px wide x65px tall, core centered local x230,y256 in each of the three cells; only short trail shapes and light pulse vary, no multiple projectiles. Cell4: one small charging spark cluster centered x192,y256, an 80px ivory/cyan orb with four short inward wisps, restrained clear shape. Bottom row cell5: one short RIGHT-directed muzzle flash, emission point at local x112,y256, a narrow hand-drawn cyan-white fan expanding toward x290, maximum100px height, no gun or lantern. Cell6: impact frame A, one small tight star/contact flash centered x192,y256, about75px wide. Cell7: impact frame B, a broken expanding crescent/ring with 5 tiny cyan shards, centered at the same x192,y256, about170px wide x145px tall, transparent middle. Cell8: impact frame C, only several thin fading blue-white shards and short wisps scattered around that same center, maximum180px wide x150px tall, no solid orb remaining. EVERY tip, spark, ribbon and visible glow must stay inside cell-local x64..320,y96..416 with minimum64px empty TRANSPARENT margins from EVERY cell edge. Every sprite fully separated and uncut. Genuine alpha transparency around ALL effects and through gaps. No checkerboard drawn in, labels, numbers, logos, captions, background scenery or ground shadows. Production game effects sheet, not a preview montage.
```

