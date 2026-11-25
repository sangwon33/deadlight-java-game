# 새 자세와 폐허 장식 — 최종 프롬프트

기본 제공 image_gen 도구 사용. 참조 이미지를 읽은 후 투명 배경으로 생성했습니다. 생성된 PNG의 알파를 유지해 프로젝트로 복사했습니다.

## assets/art/hero-look.png

참조: assets/art/hero.png.

```text
Use case: identity-preserve. Asset type: transparent game sprite atlas, 2 columns by 2 rows, square 1024x1024.
Reference image: existing 4x4 sprite atlas of our original Deadlight hooded masked wanderer. This is a character identity/style reference. Create a NEW supplementary atlas of the SAME small navy-gray hooded wanderer, ivory oval mask with one thin cyan eye slit, silver round cloak clasp, charcoal tunic/belt, gloves and boots, short straight sword. Match exactly the original hand-drawn 2D side-view inked style, muted shading, body proportions and costume. All four poses face RIGHT, strict side view, no scenery, no text, transparent alpha background.
Four equal 512x512 cells with generous transparent margins, one complete character per cell and no overlap:
top-left: exact neutral standing side-view posture, relaxed sword down, head level. Serves as scale reference.
top-right: standing looking UP, neck and hood tilted back clearly, ivory mask and cyan eye slit face diagonally upward, body still upright, sword held relaxed low. Same body height and boots as neutral (only natural slight head tilt).
bottom-left: lying flat on belly to peek DOWN over a ledge on the right. Legs and boots stretch left along ground, torso and elbows rest on ground, both gloved hands visibly brace near the right edge, mask looks DOWN and slightly forward over imaginary ledge. Sword lies secured alongside hip pointing back left, not in hands. Clear prone silhouette, only ~one third neutral height, extended body width, two arms two hands only.
bottom-right: alternate subtle breathing version of same prone looking-down pose; same placement/proportions.
Each pose grounded on an imaginary horizontal baseline near bottom of its cell, no actual ground/shadow. Small stylized character, not realistic adult anatomy. Keep same head size, boot size and limb thickness across all cells. No extra arms/weapons, no magic effects, no background or checkerboard.
```

## assets/art/ruin-props.png

참조: assets/art/terrain.png.

```text
Use case: stylized-concept. Asset type: transparent environment prop sprite atlas for an original 2D side-scrolling game Deadlight.
Reference: existing terrain atlas, use only as a visual style/material reference, not as the output layout. Create a NEW 2 by 2 equal-cell atlas on square 1024x1024 transparent alpha canvas. Four fully isolated ruined environment props, generous gutters, NO backdrop, NO floor slab extending between cells, no text. Side-view, readable crisp dark hand-inked shapes with painted moonlit blue-gray stone and subtle violet shadows, match the reference chipped masonry/rooted gothic ruins. No characters, no monsters. Each grounded at bottom of its cell.
Top left: ancient ruined pointed gothic archway, two chunky carved stone pillars, broken upper edge, a few hanging pale roots, transparent empty arch opening.
Top right: small weathered seated faceless hooded stone statue on a cracked square plinth, roots and ferns at base, peaceful solemn silhouette.
Bottom left: cluster of uneven ruined gravestones and broken urns with pale ferns and rubble, wide low silhouette.
Bottom right: ornate dark iron lantern hanging from a short carved stone post, small cool silver-blue glow confined inside lantern, roots at stone base.
Keep silhouettes detailed but readable when displayed around 80-150 pixels tall, not photorealistic or pixel art. Similar finish/detail to the provided terrain. Avoid heavy glow, white outlines, extra symbols, floating ground shadows. Preserve true alpha transparency both outside props and inside archway opening.
```
