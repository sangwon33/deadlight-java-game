# 뒤쪽 허리 발도 에셋 제작 프롬프트

방식: 내장 imagegen 도구, 투명 배경 편집. 편집 대상은 이전 6포즈 시트, 캐릭터 정체성 참조는 기존 player_idle.png입니다. 생성 후 Java로 연결된 불투명 픽셀을 각 셀에 옮겨 발 기준을 맞췄습니다.

최종 적용 경로: C:\Users\USER\Desktop\Java\Deadlight\assets\player\player_iaido_v2.png

시트 사본: Deadlight_iaido_rear_sheet_1103.png

## 사용한 프롬프트

Use case: precise-object-edit.
Asset type: production transparent 2D side-scroller character animation sheet, six poses, exactly 3 columns x 2 rows, 1536x1024.
Edit target: first reference image (existing six-pose iai atlas). Second reference (idle character) is identity/style reference only.
Primary request: REDRAW THE WEAPON POSITION AND ARMS in all six poses so this RIGHT-FACING hooded swordsman draws his katana from his REAR hip, not from in front of his stomach. Keep the same dark charcoal hood, concealed face, worn black plate armor, torn crimson/black cape, pixel-painted game art and right-facing identity. The earlier sheet's hands and sword guard are too far forward. Deliberately replace that posture.
Critical anatomy: for a right-facing character, screen LEFT is behind him. The scabbard mouth/guard in the ready pose must sit at the LEFT/REAR SIDE of his waist, distinctly LEFT of the center of his torso, at the rear hip, visible beneath the cape. The long sheathed blade extends diagonally backward/down toward SCREEN LEFT. His right hand reaches across/back to the handle at this rear-left hip, his left hand braces the scabbard mouth there. BOTH HANDS are clustered behind his waist at screen-left, NOT sticking out in front toward screen-right. The sword is NOT a sword worn on the back/shoulder: it is a rearward hip scabbard. His chest faces screen-right and is unobstructed by a horizontal sword across the belly. Show a clear sword/scabbard junction at the rear hip.
Pose order left to right then next row:
1 Ready: knees bent, body leaning forward to screen-right, both boots planted, shoulders coiled, sword completely sheathed at rear-left hip, hands reaching back to the rear-left hilt. 
2 Draw initiation: same planted feet, hand pulling sword forward FROM rear-left hip, blade emerging in a clear sweep past the waist, rear scabbard stays anchored behind.
3 Full draw strike: right sword arm extends to screen-right at chest level, clearly unsheathed long blade, left hand remains near rear scabbard, dynamic coiled torso, feet planted.
4 Follow-through: completed wide sweeping draw cut, sword extended screen-right, cape flared behind, left hand rearward, same feet.
5 Resheathing: guiding blade back into rear-left scabbard mouth, body recovers.
6 Sheathed idle: sword returned to rear-left hip, hands at rear-left waist, quieter ready stance.
Layout: six isolated full-body figures, equal scale, one in each 512x512 source cell, 40px breathing room above, boots baseline near y480 in each row, enough margin for extended sword. No overlap between adjacent figures. Keep sword within its cell. Side profile with subtle three-quarter torso for readable rear hip. Pixel art with crisp defined pixels.
Background genuinely fully transparent alpha. No backdrop, shadows, floor, text, labels, borders, grids, glow, energy trails or baked-in VFX. Only the physical character/sword/scabbard. Keep sprite scale and foot placement consistent.
