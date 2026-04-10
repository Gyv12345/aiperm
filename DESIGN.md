# Design System Document: The Sovereign Admin

## 1. Overview & Creative North Star

### Creative North Star: "The Architectural Ledger"
In the world of RBAC (Role-Based Access Control), complexity is the enemy of security. "The Architectural Ledger" seeks to transform the traditional, cluttered admin dashboard into a serene, high-end editorial experience. We are moving away from the "clunky enterprise" look and toward a system defined by **intentional negative space**, **chromatic depth**, and **structural silence**.

This design system leverages Element Plus components but strips away their "standard" feel by applying a custom UnoCSS utility layer. The goal is to create a digital workspace that feels like a bespoke architectural blueprint—precise, authoritative, and sophisticated. We break the grid through the use of wide margins and "floating" content blocks that rely on tonal contrast rather than rigid lines to define boundaries.
 
---

## 2. Colors

The palette is designed to reduce cognitive load while providing a rich, immersive environment.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using `1px` solid borders for sectioning. Boundaries must be established through background shifts using the `surface-container` tokens or subtle tonal transitions. A section is defined by its physical volume (color), not its outline.

### Surface Hierarchy & Nesting
Instead of a flat grid, we treat the UI as layered sheets of material:
- **Base Level:** `background` (#f7f9fc) – The canvas.
- **Section Level:** `surface-container-low` (#f2f4f7) – Used for large content areas like the main dashboard body.
- **Action Level:** `surface-container-lowest` (#ffffff) – Used for cards, search filters, and data tables to make them "pop" against the background.

### Signature Accents
- **The Deep Sidebar:** Use `inverse_surface` (#2d3133) for the primary navigation. This creates a powerful anchor for the layout.
- **Action Primary:** Use `primary` (#0060a9) for active states and critical CTAs. For a premium touch, apply a subtle linear gradient from `primary` to `primary_container` (#409eff) on large action buttons.
- **Glassmorphism:** For floating menus (like the user profile dropdown), use `surface_container_lowest` at 80% opacity with a `backdrop-blur-md` effect to create a "frosted" high-end feel.

---

## 3. Typography

The typography uses **Inter** to maintain functional clarity, but we apply an editorial scale to create hierarchy.

- **Display & Headlines:** Use `headline-sm` (1.5rem) for page titles (e.g., "User Management"). Ensure generous tracking (-0.02em) to give it a modern, "tight" feel.
- **Titles:** Use `title-md` (1.125rem) for section headers within cards.
- **The Body:** `body-md` (0.875rem) is the workhorse for table data. Use `on_surface_variant` (#404752) for secondary text to create a soft, readable contrast.
- **Labels:** `label-sm` (0.6875rem) should be used for table headers and metadata, always in `uppercase` with +0.05em letter spacing to denote authority.

---

## 4. Elevation & Depth

We eschew traditional shadows in favor of **Tonal Layering**.

### The Layering Principle
Depth is achieved by stacking:
1. **Sidebar:** `inverse_surface` (Deepest depth).
2. **Main Canvas:** `surface` (Base).
3. **Content Blocks:** `surface-container-lowest` (The "Lifted" layer).

### Ambient Shadows
When a component must float (e.g., a modal or a floating action button), use a "Signature Glow":
- **Blur:** 24px to 40px.
- **Opacity:** 4% - 6% of `on_surface`.
- **Offset:** Y-axis only (4px to 8px) to mimic a natural overhead light source.

### The "Ghost Border" Fallback
If a boundary is visually necessary for accessibility, use the `outline_variant` token (#c0c7d4) at **10% opacity**. This creates a "suggestion" of a line rather than a hard break.
 
---

## 5. Components

### Sidebar (The Anchor)
*   **Background:** `inverse_surface` (#2d3133).
*   **Active Item:** `surface_variant` at 10% opacity with a `primary` (#0060a9) left-border accent (4px).
*   **Typography:** `body-md`, color `inverse_on_surface`.

### Multi-Tab Header
*   **Style:** No borders. Active tabs are styled using `surface_container_lowest` with a soft `rounded-t-md`.
*   **Inactive Tabs:** `surface_container_high`, blending into the header background.

### Search Filters & Data Tables
*   **Container:** `surface_container_lowest` (#ffffff).
*   **Inputs:** Use `outline_variant` at 20% for the frame. On focus, transition to `primary` with a 2px outer "glow" using `primary_fixed`.
*   **Tables:** **Strictly forbid horizontal/vertical divider lines.** Use `surface_container_low` for the table header background and `body-sm` for row content. Use vertical whitespace (padding: `py-4`) to separate rows.

### Chips (Status Indicators)
*   **Success:** `primary_fixed` background with `on_primary_fixed_variant` text.
*   **Error/Inactive:** `error_container` background with `on_error_container` text.
*   **Shape:** `rounded-full` for a soft, modern aesthetic.

### Buttons
*   **Primary:** Gradient from `primary` to `primary_container`. `rounded-md`.
*   **Secondary/Ghost:** No background. Use `on_surface_variant` text. On hover, apply a 5% `primary` tint background.

---

## 6. Do’s and Don’ts

### Do:
*   **Do** use `UnoCSS` classes like `bg-surface-container-low` and `backdrop-blur` to build layers.
*   **Do** prioritize white space. If an element feels crowded, increase the padding rather than adding a line.
*   **Do** use `inter` with slightly tighter letter-spacing for headlines to achieve a "premium print" look.

### Don’t:
*   **Don’t** use `#000000` for shadows. Always use a tinted shadow based on the surface color.
*   **Don’t** use Element Plus default borders. Always override with `border-none` or our "Ghost Border" spec.
*   **Don’t** use high-saturation colors for background surfaces. Keep the canvas neutral (`#f7f9fc`) to let the `primary` blue actions lead the user's eye.
*   **Don’t** crowd the sidebar. Use `label-sm` headers for groups (e.g., "SYSTEM MANAGEMENT") with generous top-margin.
