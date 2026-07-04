# UI Foundation: Mizan.ma
**UX Reference**: docs/ux-mizan.md
**Version**: 1.0 | **Date**: 2026-07-04 | **Author**: UI Designer

## 1. Design Approach
- **Strategy**: Angular Material (per user decision) as the component framework, with a light custom theme layer for brand colors.
- **Rationale**: Angular Material gives production-ready forms, tables, dialogs, and file-upload-adjacent components (useful for the PDF upload flow) out of the box, minimizing custom component work for the MVP. Custom tokens limited to theming (colors/typography), not full custom components — YAGNI.

## 2. Design Tokens (Material theme overrides)
```scss
// Colors — Material custom theme palette
$mizan-primary:    #0F5C4C;  // deep green — trust, legal/financial tone
$mizan-secondary:  #C9A24B;  // muted gold accent
$mizan-background: #F7F7F5;
$mizan-surface:    #FFFFFF;
$mizan-error:      #B3261E;
$mizan-warn:       #B7860B;  // used for MEDIUM risk flags
$mizan-text:       #1B1B1B;
$mizan-text-muted: #5F5F5F;

// Risk flag colors (semantic, not brand)
$risk-high:   #B3261E;
$risk-medium: #B7860B;
$risk-low:    #2E7D32;

// Typography
$font-family: 'Inter', 'Noto Sans Arabic', sans-serif; // Noto Sans Arabic for AR/Darija rendering
$font-size-sm: 0.875rem;
$font-size-md: 1rem;
$font-size-lg: 1.25rem;
$font-size-xl: 1.75rem;

// Spacing scale (Material default 8px grid retained)
$spacing-xs: 4px; $spacing-sm: 8px;
$spacing-md: 16px; $spacing-lg: 24px; $spacing-xl: 40px;
```

Note: Arabic/Darija text requires RTL layout support — Angular's built-in `dir="rtl"` toggling + Material's RTL-aware components handle this; French/Latin script stays LTR. Language switch must also flip document direction.

## 3. Component Inventory
| Component | Reuse Existing | Build New | Notes |
|---|---|---|---|
| Login/Register forms | mat-form-field, mat-card | No | Standard Material form |
| File upload (PDF) | No native Material equivalent | Yes | Custom drag-and-drop wrapper around `<input type="file">`, styled with Material card + progress bar |
| Contract list table | mat-table | No | Sortable by date/status |
| Status badge (Pending/Analyzing/Complete/Failed) | No | Yes (small) | Simple chip component using mat-chip with color mapping |
| Risk flag badge (HIGH/MEDIUM/LOW) | No | Yes (small) | mat-chip variant, colors from risk tokens above |
| Clause accordion (expand for detail) | mat-expansion-panel | No | Used in Analysis View |
| Language selector | mat-select | No | Options: Arabic, French, Darija |
| Admin stats charts | No | Yes (defer to Backend/Frontend Dev choice, e.g. ngx-charts) | Only needed for /admin/stats — keep minimal (bar/line charts) |
| Disclaimer banner | mat-toolbar or plain div | Yes (small) | Persistent footer/banner component, reused on Dashboard + Analysis View |

## 4. Responsive Breakpoints
| Breakpoint | Width | Layout Notes |
|---|---|---|
| Mobile | < 768px | Single-column, contract table becomes stacked cards, upload button full-width |
| Tablet | 768–1024px | Two-column where relevant (e.g., clause list + detail side-by-side becomes stacked) |
| Desktop | > 1024px | Full table view, clause list + expanded detail can sit side-by-side |

## 5. Accessibility Baseline
- Color contrast: AA minimum (4.5:1 normal text, 3:1 large text) — verify custom risk-flag colors against white/surface background.
- Focus indicators: Material's default focus rings retained, not overridden.
- Semantic HTML first; ARIA only where Material doesn't already provide it (e.g., custom file-upload dropzone needs `aria-label` and keyboard alternative to drag-and-drop).
- RTL support is itself an accessibility/usability requirement for Arabic/Darija users — not optional.

### UI Validation Checklist
- [x] Design approach chosen (Angular Material — justified by upload/table/form needs)
- [x] Tokens/framework covers all UX wireframe screens
- [x] Component inventory complete
- [x] Responsive strategy defined for all breakpoints
- [x] Accessibility baseline confirmed (including RTL)
