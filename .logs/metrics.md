# METRICS — Mizan.ma



## SPRINT_SNAPSHOT — 2026-07-15 (Sprint 3)
Stories shipped: 3.3 (contract history/analysis view), 4.1 (admin usage/flag stats), 4.2 (K8s staging manifests + NetworkPolicy, verified).
Coverage: backend + frontend both passed CI's enforced 80% gate (see CI run 29392058083) — no local re-run needed, gate is automated per Story 1.3.
CI: 11/11 jobs green, first attempt.

## COVERAGE_HARDENING — 2026-07-15
Went beyond the bare 80% CI gate to close real gaps in error/validation paths (previously untested exception handlers, PDF validation edge cases, JPA persistence base class):
- mizan-common: line 88.0% → 96.0%, method 85.7% → 92.9% (UuidEntity no-arg/JPA-hydration constructor path now covered)
- contracts-service: branch 82.4% → 91.2%, line 90.9% → 94.3% (new tests: ContractsExceptionHandlerTest x6, PdfValidatorTest x6 covering all rejection paths incl. unreadable stream)
- ai-analysis-service: line 83.5% → 91.1%, method 87.0% → 100% (new tests: AiAnalysisExceptionHandlerTest x3, AnthropicApiExceptionTest x2)
- frontend: branch 98.36% → 99.18% (upload.ts file-picker-cancelled edge case now covered, was previously untested — now 100% on that file)
Note: local JDK is 25, which broke Mockito.mock() for several classes (ByteBuddy doesn't yet support Java 25 bytecode) — CI runs on JDK 21 so this wouldn't have surfaced there, but rewrote the 3 affected tests to use real objects (reflection-built MethodParameter, BeanPropertyBindingResult, a hand-written MultipartFile test double) instead of mocks, so they're not relying on that environment gap either way.
