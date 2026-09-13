---
id: governance-files-index
title: "ASFDK Java Governance File Registry"
author: "NeuroLift Technologies"
date: 2026-09-12
version: 1.0.0
metadata:
  oroi: ORG-DEV-OTOI-1.0.3
---
# Governance File Index

This file registry tracks all governance-related files in the `asfdk-java` repository, ensuring compliance with `ORG-DEV-OTOI-1.0.3`.

| File Path | Category | Status | Description |
|---|---|---|---|
| `.nltotoi/README.md` | Discovery | ✅ Implemented | Namespace overview and file registry entry point |
| `.nltotoi/agent-registration.json` | Discovery | ✅ Implemented | Agent registration (OTOI Section 3, session-asfdk-java-001) |
| `.nltotoi/index/governance-files.md` | Discovery | ✅ Implemented | This file — governance file registry |
| `.nltotoi/scripts/validate-governance.sh` | Governance | ✅ Implemented | Validation script (structure checks + Java toolchain gates) |
| `AGENTS.md` | Governance | ✅ Implemented | Agent registry with roles and authority |
| `CLAUDE.md` | Governance | ✅ Implemented | Agent collaboration protocols and commit format |
| `NLT-DEV-OTOI.md` | Governance | ✅ Implemented | Org-level coding agent contract (mirror) |
| `templates/agent-registration.json` | Templates | ✅ Implemented | OTOI Section 3 registration format |
| `templates/handoff-record.json` | Templates | ✅ Implemented | OTOI Section 5 handoff format |
| `templates/escalation.md` | Templates | ✅ Implemented | OTOI Section 4.3 escalation format |
| `templates/intent-log.md` | Templates | ✅ Implemented | Intent logging template |
| `ISSUE_TEMPLATE/agent-escalation.md` | Issues | ✅ Implemented | GitHub escalation issue form |
| `ISSUE_TEMPLATE/governance-proposal.md` | Issues | ✅ Implemented | OTOI amendment proposal form |
| `PULL_REQUEST_TEMPLATE/agent-contribution.md` | PR | ✅ Implemented | Agent PR checklist |
| `.github/workflows/validate-governance.yml` | CI | ✅ Implemented | Governance validation + cargo fmt/clippy/build/test gates |
| `SOPs/new-agent-onboarding.md` | Procedures | ✅ Implemented | New agent onboarding procedure |
| `SOPs/repo-governance-setup.md` | Procedures | ✅ Implemented | How to add governance to a new NLT repo |
| `SOPs/incident-response.md` | Procedures | ✅ Implemented | What to do when an agent goes off-rails |

---

## Crate Deliverables

| Module | Files | Status |
|---|---|---|
| `src/types.java` | Modes, interaction types, channels, crisis levels | ✅ Complete |
| `src/dto.java` | Config, interaction, response, assessment DTOs (serde) | ✅ Complete |
| `src/promptdefense.java` | Strict TOI/OTOI validation, sanitizer, audit log | ✅ Complete |
| `src/sleepwalker.java` | Emotional analysis + provenance + handoff predicate | ✅ Complete |
| `src/rrt.java` | Crisis scoring (Go-calibrated), interventions, response scripts | ✅ Complete |
| `src/foundation.java` | NeuroLiftFoundation pipeline | ✅ Complete |
| `src/tests.java` | 29 tests mirroring the Go suite | ✅ Complete |

---

## Agent Registrations

| Agent | Session | Date | Status |
|---|---|---|---|
| Cline (rust_governance_agent) | session-asfdk-java-001 | 2026-09-12 | ✅ Active |

*Last updated: 2026-09-12*
