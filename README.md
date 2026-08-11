# ColorGrade BD

**Client-side color grading and visual adjustment mod for Minecraft**

Author: **Ahmed** (AhmedNotXGamer)  
Target: Minecraft **1.21.1 – 1.21.x** · Fabric · Java 21

---

## Features

### 🎨 Game Color Grading (Global)
Full GPU-side post-processing applied to the entire rendered game view.

| Control      | Range         | Description                          |
|--------------|---------------|--------------------------------------|
| Brightness   | -100% → +100% | Additive brightness adjustment       |
| Contrast     | -100% → +100% | S-curve contrast around mid-grey     |
| Saturation   | -100% → +100% | Perceptual colour saturation         |
| Hue Shift    | -180° → +180° | Rotates hue for all colours          |
| Sharpness    | 0% → 100%     | Laplacian unsharp mask               |
| Color R/G/B  | 0.0 → 2.0     | Per-channel colour multiplier        |
| Intensity    | 0% → 100%     | Blend between original and graded    |
| Gamma        | 0.5 → 2.0     | Power-curve gamma correction         |
| Temperature  | -100% → +100% | Warm (+) / Cool (-) colour balance   |
| Vignette     | 0% → 100%     | Darkened edge vignette               |

### 🧱 Element / Block Grading
Per-block colour grading overlays. Each block carries its own independent settings including all the above plus a **3D/Depth Effect** slider.

- **Dynamic registry** — block list is populated from Minecraft's actual runtime registry. No hardcoded lists. Works with vanilla and modded blocks.
- **Searchable picker** — type to filter hundreds of blocks instantly.
- Scrollable list supports any number of configured elements.

### Other
- **Keybind**: `F7` (configurable in Controls) to open the GUI anywhere in-game.
- **Mod Menu** integration — configure from the mods list.
- **Persistent config** stored as human-readable JSON at `config/colorgradebd.json`.
- **Global enable/disable** — instantly bypasses the entire rendering pipeline.
- **Per-element enable/disable**.
- **Reset** for individual settings, elements, or everything at once.
- Vanilla UI style throughout.

---

## Performance

ColorGrade BD uses **GPU-side GLSL post-processing** via Minecraft's `PostEffectProcessor`.  
No CPU-side per-pixel processing. Shader uniforms are only updated when settings actually change.  
The pipeline is completely skipped when the mod is disabled.

---

## Building

**Requirements:** Java 21 · Git

```bash
git clone https://github.com/AhmedNotXGamer/ColorGradeBD.git
cd ColorGradeBD
./gradlew build         # Linux / macOS / Termux
gradlew.bat build       # Windows
```

Output JAR: `build/libs/colorgradebd-<version>.jar`

### GitHub Actions
Every push to `main` / `dev` automatically builds and uploads the JAR as a workflow artifact.  
Pushing a tag `v*` creates a GitHub Release with the JAR attached.

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.1.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api).
3. *(Optional)* Install [Mod Menu](https://modrinth.com/mod/modmenu) for in-game config access.
4. Drop `colorgradebd-*.jar` into your `mods/` folder.
5. Launch Minecraft.

---

## Config

Config file: `.minecraft/config/colorgradebd.json`

Human-readable JSON. You can edit it directly while Minecraft is closed.

---

## Future Plans

- Phase 2: Minecraft 26.x / Java 25 port (separate branch/module).
- HLS-based colour picker with saturation/lightness canvas.
- Per-biome grading presets.

---

## License

MIT — see [LICENSE](LICENSE).
