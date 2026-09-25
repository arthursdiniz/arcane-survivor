# Art generation record

The PNG assets in `src/main/resources/art/` were generated for this project with OpenAI's built-in `image_gen` tool. No image from another game was used as a reference. The atlas is split at runtime into six equal cells; the dark colors visible in some image viewers outside the figures have transparent alpha.

## `ruins-floor.png`

> Use case: stylized-concept. Asset type: seamless repeating floor texture for a top-down 2D action roguelite game. Primary request: original arcane-ruins ground tile, dark weathered slate and old mossy flagstones with fine cracks, a few tiny muted teal mineral glints and scattered sparse leaves. Composition: perfectly straight top-down orthographic view, uniform material across the entire square image, no focal object, no borders, designed to repeat on all four edges with matching edge patterns. Style: high quality hand-painted game texture with crisp readable forms at small scale, subtle painterly grain, restrained detail. Color palette: deep desaturated blue-grey stone, charcoal crevices, subdued pine-green moss, very small cyan accents. Lighting: even diffuse ambient lighting with no directional shadows. Constraints: tileable seamless square texture, full bleed, no characters, no props, no text, no watermark, no UI, no perspective.

The game alternates mirrored tiles to hide edge seams.

## `characters.png`

> Use case: stylized-concept. Asset type: transparent top-down sprite atlas for an original 2D fantasy survivor game. Create a clean 3 by 2 grid of SIX separate full-body character sprites, one sprite centered in each equal cell, wide empty transparent padding and no overlap. Top row from left to right: blue-robed arcane mage carrying a glowing cyan staff; green gelatinous slime with glossy highlights; purple bat with wings spread. Bottom row from left to right: ivory skeleton warrior with rusty sword; broad red-brown armored stone golem; orange-glowing oversized elite golem. Top-down three-quarter game camera, consistent hand-painted pixel-art-inspired style, clear silhouettes readable at 48 pixels, polished materials and subtle highlights, no ground, no shadows beyond each character, transparent background with real alpha. No text, no labels, no borders, no watermark.

## `mage-directions.png`

Generated with `characters.png` as a visual reference for the mage in its top-left cell:

> Use case: stylized-concept. Create a NEW transparent sprite sheet for the blue hooded mage in the TOP LEFT of the reference image. Match his dark royal-blue robe, gold trim, hooded shadowed face and cyan crystal staff; do not include any other reference character. Exactly 4 rows and 4 columns of equally spaced, separate, full-body sprites, one mage centered in each cell, no overlap. Rows from TOP to BOTTOM: facing DOWN toward camera, facing LEFT, facing RIGHT, facing UP showing the back of the robe and hood. Columns from LEFT to RIGHT: idle resting stance, walking left foot forward, walking passing pose, walking right foot forward. Keep one coherent character design, same proportions, equipment and size in every cell. Clear visible leg and robe changes between walk frames. Orthographic three-quarter overhead game camera, hand-painted fantasy game sprite style matching reference, strong readable silhouette when scaled to 80 pixels. ACTUAL transparent alpha background in all empty space; absolutely no scenery, gradient, checkerboard, ground, shadows, captions, labels, borders, or watermark. Strict regular grid with generous transparent padding around each figure.

## `necromancer.png`

> Use case: stylized-concept. Asset type: one transparent-background top-down enemy sprite for a 2D fantasy survivor game. Subject: a singular final boss called the Necromancer, a tall hooded sorcerer in layered charcoal and deep violet robes, bone crown, pale skull-like face, floating shards and two hands emitting eerie magenta arcane energy. Three-quarter overhead view matching hand-painted fantasy game sprites. Centered, full-body with all details inside canvas and generous transparent margin. Strong readable silhouette at 80 pixels, crisp hand-painted game-art detailing, controlled rim light. ACTUAL TRANSPARENT ALPHA everywhere outside the figure, no floor, no backdrop, no gradient, no shadow rectangle, no text, no border, no watermark.

## `mage-portrait.png`

> Use case: stylized-concept. Asset type: isolated 2D top-down game character sprite PNG with ACTUAL TRANSPARENT ALPHA BACKGROUND. A single blue-robed arcane mage, visible full body from head to feet, carrying one ornate staff with a cyan crystal. Three-quarter overhead view appropriate for a top-down survivor game. Hand-painted high-end fantasy game art with crisp silhouette, polished cloth folds, subtle gold trim, readable face shadow and cyan glow. Center the ONE character in the frame with generous empty transparent margin. No scenery, no floor, no shadow cast on a floor, no gradient background, no colored backdrop, no dark rectangle. Transparent pixels everywhere outside the character. No text, no labels, no watermark.
