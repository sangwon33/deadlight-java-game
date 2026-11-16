# 월드 에셋 제작 프롬프트 — 2026-11-05

기본 제공 image_gen 사용. 참조는 assets/art/terrain.png, 결과는 assets/art/world-props.png입니다. 투명 알파를 유지했습니다. 실제 프레임 분리는 4열, 세로 576 경계를 사용합니다.

```text
Use case: stylized-concept. Asset type: 4 columns by 2 rows transparent sprite atlas, landscape 1536x1024, eight equal cells with generous gutters.
Reference image is our existing Deadlight terrain atlas; use its hand-inked moonlit gray stone / blue-charcoal palette and detailed 2D side-view gothic ruined game art style. Create NEW world props, not UI icons, all grounded at bottom of their own cell, true alpha transparency, no floor slab, no text, no checkerboard, no outside glow.
Read left to right:
row1 cell0: training scarecrow, straw-stuffed burlap torso and round head with two stitched eyes, ragged dark indigo training tabard, wooden crossbar arms and upright post on sturdy stone weighted base. Full side/front three-quarter shape facing RIGHT, no monster, no slime. 
row1 cell1: same exact scarecrow in a struck reaction, tabard and straw slightly disheveled but same fixed base, proportions and placement; no relocation or tilt of the base.
row1 cell2: same scarecrow broken, slumped burlap target with exposed straw on same standing wooden post and base. Same scale, no effects.
row1 cell3: small worn charcoal leather supply satchel resting on ground, silver buckle and subtle cyan tied cloth ribbon, a single short bottle neck peeks from the bag, not a floating potion icon.
row2 cell0: small ancient wooden supply coffer with worn metal bands, pale fern at base and subtle violet cloth, closed lid; no UI frame.
row2 cell1: small stone healing shrine, low carved pedestal and shallow basin of faint cyan water with a pale rune, roots at bottom, no giant beam.
row2 cell2: iron chain and stone bracket assembly with chipped stone beam across top and two narrow hanging support chains, for side-scrolling platform scenery.
row2 cell3: cracked stone hazard vent, low circular squat stone base with a central narrow crevice and faint red-violet light INSIDE the crevice; no flame or smoke so it can be animated in game code.
Rendering: cohesive crisp silhouette, natural weathered material shading and muted colors, match provided terrain. Scarecrow stands about 1.3 player heights; other props will display at 20-70 game pixels, so emphasize readable silhouette and avoid excessive tiny detail. Each object entirely within its own cell; make alpha holes between wood/chain elements truly transparent.
```
