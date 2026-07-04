# PRD: Mizan.ma — AI Legal Document Assistant
**Version**: 1.0 | **Date**: 2026-07-04 | **Author**: PM | **Status**: Draft

## 1. Problem Statement
Moroccan freelancers, SMEs, and informal businesses (Moqawil/Kasb users) routinely sign contracts they don't fully understand. Legal counsel costs 500–2000 MAD per consultation, so most skip legal review entirely and absorb the risk of unfavorable or exploitative clauses.

## 2. Goals & Success Metrics
| Goal | Metric | Target |
|---|---|---|
| Make contract review accessible | Contracts analyzed / month | 500 in first 3 months post-launch |
| Reduce risk exposure for users | % of analyses that flag ≥1 risky clause | Baseline measurement (informational) |
| Multilingual usability | % of analyses requested in Darija/French/Arabic | Track split, no hard target yet |
| Trust / repeat usage | Users with ≥2 uploads | 25% of registered users |

## 3. User Stories
- As an **Individual User**, I want to upload a contract PDF, so that I get a plain-language summary of its clauses.
- As an **Individual User**, I want risky terms flagged clearly, so that I know what to negotiate or question before signing.
- As a **Business User**, I want to manage multiple contracts under one account, so that I can track analyses across my clients/vendors.
- As a **Business User**, I want the analysis in my preferred language (Darija, French, or Arabic), so that I can actually understand it.
- As an **Admin**, I want to see usage and flagged-content trends, so that I can monitor platform health and abuse.
- As any User, I want a clear disclaimer that this is not legal advice, so that expectations are set correctly.

## 4. Scope
### In Scope
- PDF upload and storage (Cloudflare R2)
- AI-based clause extraction, summarization, and risk flagging (Anthropic Claude API)
- Multilingual output: Darija, French, Modern Standard Arabic
- User accounts: Individual, Business, Admin roles
- Contract history per user/business account
- Suggested corrections/alternative phrasing for flagged clauses (informational, not legal advice)

### Out of Scope (v1)
- Actual legal advice or lawyer marketplace/referral
- E-signature or contract execution workflow
- Contract drafting from scratch (analysis of existing PDFs only)
- Mobile native apps (web-responsive only for v1)
- Payment/billing (assume free or handled externally in v1)

## 5. Requirements
### Functional
- FR-1: User can register/login as Individual or Business.
- FR-2: User can upload a PDF contract (max size TBD, e.g. 10MB).
- FR-3: System extracts text from PDF and sends it for AI analysis.
- FR-4: System returns a clause-by-clause summary, risk flags (severity levels), and suggested corrections.
- FR-5: User can select output language (Darija, French, Arabic).
- FR-6: User can view history of past uploaded contracts and their analyses.
- FR-7: Admin can view aggregate usage stats and flagged-content trends.
- FR-8: System displays a persistent "not legal advice" disclaimer on every analysis result.

### Non-Functional
- NFR-1: Performance — Analysis result returned within 30s for a typical contract (< 20 pages).
- NFR-2: Security — PDFs and analysis results are private to the owning user/business account; JWT-based auth on all endpoints.
- NFR-3: Accessibility — WCAG AA baseline on Angular frontend.
- NFR-4: Availability — Best-effort MVP; no formal SLA yet (see System Design doc).
- NFR-5: Data residency — Not a hard requirement for v1, but PII (user data, contract content) must be encrypted in transit and at rest.

## 6. Constraints & Assumptions
- Constraint: Must use Anthropic Claude API (claude-sonnet-4-6) for document analysis — carried over from original product decision.
- Constraint: Backend = Java 21 / Spring Boot 3.x (microservices); Frontend = Angular + Angular Material.
- Assumption: Users have PDFs already (scanned or digital); OCR for scanned/image-only PDFs is a stretch goal, not committed for v1.
- Assumption: Target users are comfortable with a web app on desktop or mobile browser.

## 7. Risks
| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Users mistake AI output for real legal advice | M | H | Persistent disclaimer, no "advice" language in UI copy, ToS clause |
| Claude API cost scales faster than revenue | M | M | Cap PDF size/page count, monitor cost per analysis |
| Microservices overhead slows MVP delivery | H | M | Keep service boundaries simple, shared libraries for common concerns, defer service mesh/discovery tooling |
| Multilingual output quality (Darija especially) is inconsistent | M | H | Prompt engineering + manual QA sample review before launch |
| PDF parsing fails on scanned/image contracts | M | M | Detect non-extractable PDFs, return clear error asking for a text-based PDF |

## 8. Timeline
| Milestone | Target Date |
|---|---|
| PRD Approved | 2026-07-04 |
| Foundation Docs Approved | 2026-07-04 |
| Architecture Done | 2026-07-04 |
| Implementation Start (Sprint 1) | 2026-07-05 |
| MVP Ready | TBD (post Sprint 3, per Stories doc) |
