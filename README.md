# Arcane Survivor

Arcane Survivor is a local 2D survival roguelite made with Java and LibGDX. Move through a ruined arena, defeat enemies with automatic weapons, collect experience, and choose upgrades. Survive until the final boss appears at 12:00, then defeat it to win.

## Features

- Fluid, frame independent movement with a following camera and four-direction idle/walk animation
- Enemy movement animations: slime squash, bat wing motion, skeleton gait, golem sway, and floating boss
- Automatic attacks with four weapons: Magic Bolt, Orbiting Orb, Fire Aura, and Piercing Arrow. Each weapon level adds a bolt, arrow, or orb; Fire Aura gains range instead.
- Four ordinary enemy types, periodic waves and progressive spawning, a mini-boss at 6:00, and a final boss at 12:00
- Experience gems, level ups, three random upgrade choices, weapon levels up to five
- Health, armor, brief invulnerability, HUD, pause, game over, victory, and run statistics
- Keyboard and mouse menus, volume settings, optional fullscreen
- Original illustrated sprites, a subdued ruins floor, and light emitted by player projectiles
- Development shortcuts for faster experience, denser enemy spawns, and immediate final boss testing

## Requirements

- JDK 21 or newer
- Apache Maven 3.9 or newer

## Run

From the project directory:

```bash
mvn test
mvn exec:java
```

On Windows PowerShell, use the same commands. If Maven uses an older Java installation, set `JAVA_HOME` to your JDK 21+ directory first:

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-21'
mvn test
mvn exec:java
```

Maven downloads LibGDX and JUnit from Maven Central on the first build.

## Controls

| Key | Action |
| --- | --- |
| WASD / arrow keys | Move |
| Esc | Pause / resume |
| 1, 2, 3 | Choose an upgrade |
| Mouse | Use menus and choose upgrades |
| F11 | Toggle fullscreen |
| L | Toggle 5x experience from collected gems during a run |
| K | Toggle 2x regular enemy spawns during a run |
| J | Summon the final boss immediately, once per run |
| P | Gain one character level immediately and choose an upgrade |

## Architecture

- `model`: player, enemies, projectiles, and experience gems, with rules independent of rendering
- `weapon`: individual automatic weapon behaviors behind a small common base class
- `system`: run simulation, spawning, difficulty progression, and upgrade selection
- `screen`: LibGDX rendering, HUD, menu states, and player input
- `ArtAssets`: illustrated textures, sprite regions, repeating floor, and reusable glow texture
- `Facing`: shared four-direction state used by the player and enemies; animation time advances with the simulation
- `AudioManager`: optional local music and sound effects; missing files are silent

The render loop updates the simulation only in active play. Menu, pause, and upgrade selection stop the run timer and combat. Collisions use circles. The world has a finite arena, and active enemies are capped at 320.

## Screenshots

### Main menu

![Main menu](docs/screenshots/main-menu.png)

### HUD and art preview

![HUD and art preview](docs/screenshots/hud-art-preview.png)

This preview uses a staged validation scene to show the enemy sprites together. During a normal run, enemy types unlock over time.

### Player directions

![Four player directions](docs/screenshots/direction-preview.png)

The preview shows one walking frame for each direction. The game cycles the remaining walking frames while moving and plays a subtle breathing animation while idle.

## Assets

| Asset | Author / source | License / status |
| --- | --- | --- |
| Ruins floor, character atlas, directional mage sheet, Necromancer, mage portrait | Created for this project with OpenAI's built-in `image_gen`; prompts in [ART_PROMPTS.md](ART_PROMPTS.md) | Project-generated art; no third-party source asset |
| Play Regular and Bold fonts | Jonas Hecksher, Playtypes, e-types AS; [Google Fonts](https://github.com/google/fonts/tree/main/ofl/play) | SIL Open Font License 1.1; license text in `src/main/resources/font/OFL.txt` |
| Glow texture | Generated at runtime by `ArtAssets` | Original project code |

Optional audio can be placed in `src/main/resources/audio/` as `music.ogg`, `attack.wav`, `kill.wav`, `level.wav`, `hurt.wav`, and `boss.wav`. Record the name, author, source, and license of any audio added to the project.

## Future improvements

New characters and weapons, alternate maps, weapon evolutions, achievements, more bosses, local saves and leaderboard, and difficulty modes.
