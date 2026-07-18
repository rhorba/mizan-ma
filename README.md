# Mizan.ma — AI Legal Document Assistant

Moroccan freelancers, SMEs, and informal businesses sign contracts they don't fully understand.

## Problem
Legal counsel costs 500–2000 MAD per consultation. Most Moqawil/Kasb users skip legal review entirely.

## Solution
Upload a contract PDF → Claude API summarizes clauses in Darija/French/Arabic, flags risky terms, suggests corrections. No legal advice — pure document analysis.

## Stack
Java 21 (Spring Boot 3.x, microservices: auth-service, user-service, contracts-service, ai-analysis-service + Spring Cloud Gateway), Angular (latest) + Angular Material, PostgreSQL 16 (Spring Data JPA, database-per-service), Anthropic Claude API (claude-sonnet-5), Cloudflare R2 (PDF storage), Spring Security + JWT, Docker Compose (local) + Kubernetes (staging/prod)

See `docs/` for full foundation documents (PRD, system design, architecture, security, database, UX, UI, test strategy, DevOps, stories).

## Completes
Moqawil (contractors sign contracts) + Kasb (informal businesses need contract literacy)

## Key Roles
Individual User | Business User | Admin
