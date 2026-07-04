# UX Foundation: Mizan.ma
**PRD Reference**: docs/prd-mizan.md
**Version**: 1.0 | **Date**: 2026-07-04 | **Author**: UX Designer

## 1. User Personas (minimal — YAGNI)
| Persona | Role | Goal | Pain Point |
|---|---|---|---|
| Karim, freelance contractor (Moqawil) | Individual | Understand a client contract before signing | Can't afford 500-2000 MAD legal review for every small gig |
| Fatima Zahra, informal shop owner (Kasb) | Business | Review supplier/lease contracts, keep them organized | No legal background, French/Arabic legal jargon is opaque |
| Youssef, platform admin | Admin | Monitor usage and flagged-content trends | Needs a lightweight dashboard, not a full BI tool |

## 2. Information Architecture / Site Map
```
[App Root]
├── /login, /register
├── /dashboard (contract history)
│   ├── /contracts/upload
│   └── /contracts/:id (analysis view)
├── /profile (language preference, business info)
└── /admin (admin-only)
    └── /admin/stats
```

## 3. Core User Flows (top 3 journeys)

### Flow 1: Upload & Analyze a Contract
```
[Dashboard] → [Click "Upload Contract"] → [Select PDF + language] → [Submit]
     → [Uploading/Analyzing spinner] → [Analysis Ready] → [View clause summary + risk flags]
                                              ↓ (parse fails / non-text PDF)
                                        [Error: "Couldn't read this PDF" → retry/upload different file]
```

### Flow 2: Review Flagged Clauses
```
[Analysis View] → [Scroll clause list] → [Click a HIGH risk clause]
     → [Expand: original text + explanation + suggested correction] → [Back to list]
```

### Flow 3: Register & First Upload (new user)
```
[Landing] → [Register: Individual or Business] → [Choose language pref] → [Login]
     → [Empty Dashboard: "Upload your first contract"] → (Flow 1)
```

## 4. Key Screen Wireframes (text-based)

### Screen: Dashboard (Contract History)
```
┌─────────────────────────────────────┐
│ Mizan.ma      [Profile] [Logout]    │
├─────────────────────────────────────┤
│  [+ Upload Contract]                │
│                                      │
│  Contract Name   Status   Date      │
│  Lease_2026.pdf  Complete 07/01     │
│  Supply_deal.pdf Analyzing 07/04    │
│                                      │
│  (empty state: "No contracts yet —  │
│   upload your first PDF")           │
├─────────────────────────────────────┤
│ Not legal advice — informational    │
│ analysis only.                      │
└─────────────────────────────────────┘
```

### Screen: Analysis View
```
┌─────────────────────────────────────┐
│ ← Back      Lease_2026.pdf          │
├─────────────────────────────────────┤
│ Summary: [plain-language paragraph] │
│                                      │
│ Clauses:                            │
│  🔴 HIGH   Clause 4.2 — Penalty ...  │
│  🟡 MEDIUM Clause 7.1 — Renewal ...  │
│  🟢 LOW    Clause 9 — Notice period  │
│                                      │
│ [Expanded clause]:                   │
│   Original text | Explanation |      │
│   Suggested correction              │
├─────────────────────────────────────┤
│ Not legal advice — informational    │
│ analysis only.                      │
└─────────────────────────────────────┘
```

## 5. Screen States
| Screen | Empty State | Loading | Error | Success |
|---|---|---|---|---|
| Dashboard | "No contracts yet — upload your first PDF" | Skeleton rows | "Couldn't load contracts, retry" | List renders with status badges |
| Upload | N/A | Progress bar (upload) → spinner (analyzing) | "Upload failed" / "Couldn't read this PDF" | Redirect to Analysis View |
| Analysis View | N/A (never empty once loaded) | Skeleton summary + clause list | "Analysis failed, retry" | Full summary + clause list rendered |
| Admin Stats | "No data yet" | Skeleton chart | "Couldn't load stats" | Charts render |

### UX Validation Checklist
- [x] Personas match PRD target users (Individual/Business/Admin)
- [x] All PRD user stories map to a flow
- [x] Wireframes cover happy path + at least one error state
- [x] Navigation hierarchy is clear and shallow (2 levels max)
