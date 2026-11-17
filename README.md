# NUBCC Logo Studio

**Official photo watermark & logo overlay studio for NUB Computer Club (Northern University Bangladesh)**

## Overview

NUBCC Logo Studio is a production-ready Android application built in Kotlin and Jetpack Compose. It allows students, club media managers, and event photographers to seamlessly watermark photos with official NUB Computer Club emblems, custom transparent PNG logos, or university seals.

---

## Key Features

1. **Single Photo Mode**:
   - Modern Android Photo Picker (zero-permission storage model).
   - Drag & pinch-to-scale interactive watermark positioning.
   - 9-preset instant corner & center positioning (Top Left, Top Center, Top Right, Center Left, Center, Center Right, Bottom Left, Bottom Center, Bottom Right).
   - Automatic corner placement: default 18% width with 3.5% edge margins.
   - Opacity slider (10% to 100%).
   - BEFORE / AFTER preview toggle.
   - Multi-level Undo & Redo stack.
   - **True Full-Resolution Export**: Never exports downscaled preview bitmaps. Loads and composites onto the original full-resolution photo in memory, preserving maximum fidelity and EXIF orientation, and saves to `Pictures/LogoOverlay` via Android MediaStore.

2. **⭐ Batch / Multi-Photo Logo Mode**:
   - Multi-photo selection from gallery.
   - Thumbnail grid with selection count, ordering, and individual removal.
   - "Apply to All Photos" using normalized relative coordinates $(X, Y, W, H)$. Works seamlessly across photos with different aspect ratios (portrait, landscape, square) and varying resolutions.
   - **Individual Photo Override**: Override the placement for a specific photo while keeping the rest aligned to the global configuration.
   - Real-time batch progress dialog with responsive background coroutines.
   - Summary of exported images with quick access to view in Gallery.

3. **Built-in Official NUBCC Logos**:
   - NUBCC Official Emblem (Full color with laptop, quill, and club wordmark).
   - NUBCC White Minimalist (High contrast for dark photos).
   - NUBCC Laptop & Quill Crest (Classic university seal).
   - Support for custom user-selected PNG logos with transparency.

4. **Presets System**:
   - Local Room Database persistence.
   - Save custom watermark configurations (Corner Classic, Signature, Top Crest, Subtle Center).
   - Quick one-tap apply to Single or Batch mode.

5. **Recent Exports Gallery**:
   - Tracks recent exports with timestamp, resolution, and one-tap Share or View in Gallery.

6. **Branded Visual Identity**:
   - NUBCC visual identity: Vertical gradient (Top `#0D3B8C` to Bottom `#060B16`).
   - Deep purple `#4A2E83` and electric violet `#7C3AED` accents with cyber cyan `#38BDF8` highlights.
   - Dark theme + glassmorphism surfaces.
   - Adaptive launcher icons with laptop + quill insignia across all mipmap densities.

---

## Architecture

- **Clean Architecture & MVVM**:
  - `model/`: Domain models (`LogoPlacement`, `PhotoItem`, `LogoItem`, `ExportSettings`).
  - `engine/`: `ImageUtils` (Exif, safe downscaling, memory-efficient decoding), `WatermarkCompositor` (normalized coordinate mapping), `PhotoExporter` (MediaStore export engine & Flow progress).
  - `data/`: Room database (`AppDatabase`, `PresetDao`, `RecentExportDao`, `PresetEntity`, `RecentExportEntity`) and `BuiltInLogos`.
  - `ui/`:
    - `screens/`: `HomeScreen`, `SingleEditorScreen`, `BatchStudioScreen`, `PresetsScreen`, `RecentExportsScreen`, `SettingsScreen`.
    - `components/`: `InteractiveWatermarkCanvas`, `PlacementControls`, `LogoSelectorRow`, `BeforeAfterComparison`, `GlassCard`, `NubccButtons`.
    - `theme/`: Official NUBCC colors, typography, and Material 3 dark color scheme.
    - `viewmodel/`: `SingleEditorViewModel`, `BatchStudioViewModel`.

---

## Build & Run

- Target SDK: 36, Min SDK: 24.
- Build tool: Gradle with Kotlin DSL.
- To compile the project:
  ```bash
  gradle assembleDebug
  ```
- To run tests:
  ```bash
  gradle :app:testDebugUnitTest
  ```
