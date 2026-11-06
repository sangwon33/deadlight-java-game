# 돌진 캐릭터 외형 통일 프롬프트 — 2026-11-04

모드: 내장 imagegen, 투명 배경 이미지 편집. 기존 player_idle.png를 디자인 기준으로, player_run.png를 동일 캐릭터의 화풍·동작 참조로 사용했습니다. 두 번째 편집은 검날의 두께와 형태를 원본에 가깝게 맞췄습니다.

최종 적용 파일: C:\Users\USER\Desktop\Java\Deadlight\assets\player\player_thrust_v2.png
작업 공간 사본: Deadlight_identity_sheet_1104.png

## 캐릭터 외형 통일

Use case: identity-preserve.
Asset type: transparent pixel-art animation sheet for an existing 2D game character. Exactly SIX poses, 3 columns x 2 rows, wide 2048x1024 sheet.
Image 1 is the IMMUTABLE CHARACTER DESIGN MASTER: the standing player_idle.png. Image 2 is the same character's running animation, supporting identity and pixel texture reference. The user says the skill currently turns into a different character. Your task is to make skill poses that unmistakably belong to THIS EXACT EXISTING CHARACTER, not a new character inspired by him.
Preserve the master's specific silhouette and equipment: tall angular narrow charcoal hood with deep black face opening; slim long-limbed build; matte black/charcoal layered armor and leather with small cold silver edge highlights; narrow waist and the SAME square dark metal belt buckle; the SAME layered long black coat/waist panels; black outer cape with small deep wine-red inner rips and ragged tips. The cape is mostly black, not bright red. The sword is the master's SAME long narrow slightly curved silver blade, same dark simple grip and small SILVER crossguard. NO gold/bronze hilt, NO shiny rounded new plate armor, NO bulky thighs, NO exaggerated giant round kneecaps, NO red chest armor, NO red scarf, NO new costume design.
Match the original's COARSE crisp pixel clusters, dark low-saturation palette, restrained highlights, texture detail, head proportions, gauntlet shapes, boots and cloth lengths. Do not turn it into smooth bright high-resolution concept art. Keep exact physical character scale across all poses. The standing first pose is nearly as tall as the master, and kneeling/leaning later lowers height naturally with the SAME head and bone lengths.
Action: charged FORWARD THRUST DASH, sword already drawn; NOT an iai/sheathed draw, NOT a slash. Facing screen RIGHT. Both hands adjacent on the same hilt behind the guard, no hand on blade, wrists and elbow aligned to push sword forward. Same sword length (about 190px exposed blade in source art) across every pose; enough cell width for entire tip and trailing boot.
Six poses reading left-to-right:
1 Focused guard: almost upright like the original standing body, knees slightly bent, both hands pull hilt close to waist, blade points horizontally right. Keep the original torso/hood/cape as recognizable as possible.
2 Deep focus: slightly lower hips and back knee, hilt drawn near waist, tip still points right. Body coils to launch.
3 Push-off: back leg drives forward, torso tilts right, elbows start to extend, both hands push sword forward along blade axis.
4 Full dash thrust: pronounced forward lean, both hands extended together straight toward right, sword tip furthest right, rear leg stretched, same black ragged cape physically trails behind. No trails/VFX.
5 Thrust braking: front foot plants, both hands still forward on same grip, torso begins to rise.
6 Recover: return to the original character's guard with hilt pulled close and blade angled slightly down-right, no sheathing.
Layout: six isolated full-body figures, three in upper row and three lower row. Equal source scale, one figure per approx682x512 cell with safe margins. Pelvis near 45% cell width; boots baseline near row y470. No figure overlaps another cell. True transparent alpha background. No floor, text, labels, border or grid.
ABSOLUTELY NO SPECIAL EFFECTS: no glow, streaks, blur, particles, auras, afterimages, flash or dust. Only the exact existing character, clothes and sword, reposed.

## 검날 보정

Use case: precise-object-edit. Edit image 1, the six-pose black-armored hooded sprite sheet. Image 2 is the original standing character and is the weapon design reference.
TARGETED CHANGE ONLY: replace the exposed sword blade beyond the crossguard in each of the six cells with the ORIGINAL CHARACTER'S SAME slender slightly curved silver single-edged sword from image 2. The current blades in image 1 look too wide and double-edged, like broadswords. The original blade is long, very thin, dark steel with a narrow pale silver cutting edge, slight elegant curvature near the tip. On this source sheet each blade should be about 190px long and only 4–6px thick through most of its length, not 15–25px thick. Keep its point in roughly the same forward direction, blade axis continuing from the hands. Only one thin blade, not two parallel swords.
KEEP ALL CHARACTER PIXELS, hand positions, gloves, small silver crossguard, grip, hood, armor, square buckle, cape, clothing, anatomy, pose order, body scale, cell placement and foot baselines as unchanged as possible. Do not redesign or enlarge the character. Retain the dark coarse pixel-art style and all six original poses. Do not alter the mostly black cape with deep burgundy torn tips. No gold.
Six isolated figures remain 3 columns x 2 rows on the same wide canvas with fully transparent alpha. No text, no labels, no floor and absolutely NO effects: no glow, no trails, particles, streaks, blur or shadows. This is only a thin-sword correction, not a new character generation.
