# 양손을 뒤로 당기는 찌르기 준비 자세 — 2026-11-04

모드: 내장 imagegen, 투명 배경 이미지 편집.
입력: 현재 돌진 시트(정체성과 화풍), 기존 player_idle.png(의상 참조).
적용: assets/player/player_thrust_prepare_v1.png
전체 시트: Deadlight_prepare_sheet_1104.png
검토용 확대 이미지: Deadlight_prepare_pose_1104.png (위/아래 투명 여백만 잘라 표시)

## 사용한 프롬프트

Use case: precise-object-edit.
Asset type: TWO transparent preparation poses for an existing right-facing pixel-art swordsman, side by side, 1536x1024 canvas.
Input 1 (six-pose thrust sheet) is the exact existing game character and style. Input 2 (standing idle) is immutable costume reference. Create only TWO preparation poses, not a full attack. Keep the same narrow angular charcoal hood, fully black face opening, slim dark armor, square steel belt buckle, long black coat panels and ragged mostly BLACK cape with wine-red torn lining. Same thin slightly curved silver sword with dark hilt and small silver guard. Preserve pixel-cluster texture and restrained dark highlights. No new armor or red costume.

USER'S SPECIFIC CORRECTION: prepare a TWO-HANDED THRUST by pulling BOTH HANDS BACK, NOT holding the weapon one-handed in front of the belly.
Character faces SCREEN RIGHT, so SCREEN LEFT is the rear side.
In BOTH poses, the TWO gloved hands must be clearly visible next to each other wrapping the SAME sword grip, BOTH behind the waist at the REAR/LEFT hip, noticeably LEFT of the torso's vertical centerline. Both forearms and elbows are DRAWN BACK like a coiled spear/sword thrust. The front hand grips immediately behind the guard, the rear hand grips near the pommel. No hand on the blade. No free hand hanging down. No extra hand. Wrists neutral and aligned with the sword. The exposed blade is already unsheathed and points HORIZONTALLY TO SCREEN RIGHT in front of the body. The crossguard/hilt is far BACK near the rear hip, not out in front. The long blade passes forward alongside the waist, not through flesh. This is NOT a sword-drawing/sheathing or iai pose. No scabbard.

Pose 1 (left cell): BEGINNING PREPARATION. Feet planted, modest knee bend, shoulders turn slightly to reveal BOTH gauntlets at rear hip, BOTH elbows fold backward, hilt pulled behind waist. Front of torso is open with no forward-reaching hands. Focused and braced.
Pose 2 (right cell): DEEPER PREPARATION. Same sword/grip behind rear hip, knees more bent, weight on rear leg, body leans a little toward screen-right, both arms compressed BACK even more, clearly about to push both hands forward to thrust. Do not actually thrust yet.
Both poses depict the same physical head size, limb lengths, sword length, costume and scale. Original idle figure is slender; do not bulk up armor or thighs. Full body and entire steel blade visible. One figure per 768x1024 source cell with ample space; no overlap. Keep pelvis near x384 relative to each cell and boots near y900. True transparent alpha, no floor, no text, no labels or grid. Absolutely no VFX: no aura, sparks, glow, trails, blur, afterimages or dust. Character and physical sword only.
