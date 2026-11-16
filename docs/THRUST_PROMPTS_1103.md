# 돌진 찌르기 에셋 프롬프트 — 2026-11-03

모드: 내장 imagegen, 투명 배경 이미지 편집. 기존 대기 캐릭터는 정체성/크기 참조, 이전 발도 시트는 화풍 참조로만 사용했습니다. 첫 생성 후 팔을 뻗을 때 달라지던 검 길이를 보정했습니다.

최종 게임 경로: C:\Users\USER\Desktop\Java\Deadlight\assets\player\player_thrust_v1.png

시트 사본: Deadlight_thrust_sheet_1103.png

## 최초 생성

Use case: precise-object-edit.
Asset type: transparent 2D side-scroller game animation sprite sheet, exactly SIX isolated poses in a 3 columns x 2 rows grid, 1536x1024.
Input image 1 (standing hooded swordsman): primary identity, anatomy, armor and physical scale reference. Input image 2 (old iai sheet): secondary style/cape reference ONLY. REPLACE the old sword-draw action completely with a charged FORWARD THRUST DASH. Not an iai draw and NOT a horizontal slash.
Character: same charcoal hood hiding face, lean/slender human build, worn dark plate gauntlets and greaves, torn red-and-black cape. Match image 1's head/body/armor proportions. Do NOT make the charge character bulky or gigantic. Crisp pixel-painted fantasy game art.
Facing: screen RIGHT in every cell. Feet near source cell y470. Same hood/head size, body bone lengths, hand size and sword length in all six poses. Full body and whole sword visible inside each 512x512 cell with safe margins. Pose changes do not move the entire sprite horizontally within its cell; pelvis stays near x250.
Weapon mechanics extremely important: ONE single long narrow sword held in BOTH hands on the HANDLE, both gauntlets touching the same grip behind the crossguard. Right hand nearest the guard, left hand at pommel behind it. The crossguard separates the hands from the exposed blade, no hand gripping the sharp blade. Neutral straight wrists, believable bent elbows, two arms total, two hands total. Sword tip aims screen-right along the thrust direction. No drawing from a scabbard. No extra sword, no detached hands, no blade through torso or forearm.
Sequence, reading order:
1 CONCENTRATION/GUARD: upright but slight knees bend, sword already unsheathed, both hands hold hilt near rear/right hip/waist at screen-left of chest, elbows bent and drawn backward, blade points horizontally toward screen-right at lower chest height. Quiet focused pose, not attacking.
2 DEEP FOCUS: knees lower modestly, weight shifted to back leg, both hands pull same hilt nearer rear waist, tip still pointing screen-right, torso coils to launch. Same sword line, compact guard, no slashing wind-up.
3 PUSH-OFF: back leg extends to propel forward, torso leans screen-right, elbows beginning to extend, both hands push sword forward along its own length, blade remains horizontal.
4 FULL DASH THRUST: pronounced forward lean, two hands extended together pushing the hilt directly screen-right, sword TIP farthest screen-right, straight continuous thrust line from rear elbow through both wrists to blade, front knee bent, rear leg stretched back. Cape streams physically behind, body only.
5 THRUST FOLLOW-THROUGH/BRAKING: both hands still forward on hilt and tip forward, front foot plants to slow down, torso begins to rise, rear leg comes in. Clear thrust, no sweep.
6 RECOVER GUARD: arms bend to draw hilt back into ready guard, torso returns upright, sword remains unsheathed and points diagonally slightly down-right, boots planted. No sheathing.
Transparent alpha background, no floor, no shadows, no text, grid or border. ABSOLUTELY NO VFX: no glow, no auras, no particles, no streaks, no trails, no motion blur, no duplication/afterimages, no energy blade, no shockwave, no screen flash. Only physical character, cape and steel sword. Keep every isolated figure within its own cell.

## 검 길이 보정

Use case: precise-object-edit. Edit the attached six-pose charged thrust sheet.
Preserve the character identity, two-handed grip, armor, hood, red cape, pose sequence and transparent alpha. This is a production sprite sheet for a side-scroller.
Make a wider sheet 2048x1024 with exactly 3 columns x 2 rows. Give each pose a roughly 682x512 cell. Keep all six bodies at the same physical scale and head size (roughly 340px tall in upright pose). More horizontal empty space is needed.
TARGETED FIX: the steel blade must have the SAME LENGTH AND THICKNESS IN EVERY POSE. In the previous sheet the full thrust blade became much shorter than the ready blade. Fix this. Use one sword design with a slender exposed blade approximately 190px long from guard to tip, same hilt and straight crossguard. Extend the thrust-pose blade to full length. The added cell width gives it room. Keep the whole sword tip and trailing boots safely inside each cell, no cropped tips. Both gauntlets stay adjacent on the grip behind the guard, no hand on the blade, no extra fingers or arms. Tip points right, blade aligned with wrists. A thrust is a straight push, never a sideways slash.
Six poses in the same reading order: 1 focused upright two-hand guard pulled near waist, 2 deeper focused crouch, 3 push-off beginning thrust, 4 full forward lean with two hands extended and rear leg stretched, 5 forward thrust braking/planting foot, 6 recovery unsheathed guard. Pelvis near horizontal center-left of each cell, feet near row baseline y470. Do not change the figure's size between poses. The character is lean like the reference.
Absolutely no VFX, no glow, no trails, no motion blur, no particles, no shadow, no backdrop, no labels or grid. True transparent alpha.
