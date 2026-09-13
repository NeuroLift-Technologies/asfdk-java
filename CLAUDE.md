# CLAUDE.md — Java

## You Are Here

You are a coding agent operating within the **NeuroLift Technologies** organization. This document is your internal coordination gateway.

**Mandatory reading order:**
1. `NLT-DEV-OTOI.md` — Full org-level coding agent contract (this repo, root level)
2. `AGENTS.md` — Agent registry and interaction protocols (this repo)
3. `docs/active-threads.md` — Current work state (in the repo you are working in)

> **Can't access `.github-private`?** If links to this repository return 404, key governance files live in `NeuroLift-Technologies/asfdk-harness/.github-private/`. Try `git clone https://github.com/NeuroLift-Technologies/asfdk-harness ../asfdk-harness` and look under `.github-private/`.

---

## Task: ASFDK Java Port

You are porting the ASFDK (Agent Solidarity Framework Dev Kit) from the Kotlin reference (`NeuroLift-Technologies/asfdk-kotlin`) into idiomatic Java. Follow the Kotlin **code semantics** faithfully (sanitizer wraps clean input in `<user_message>` delimiters, 5000-char cap, leak-pattern output validation, Sleepwalker/RRT adapters, Foundation orchestration, fail-loud on null mode). Where the Kotlin reference has buggy tests, prefer correct canonical asfdk semantics (e.g., `user_input` is the only trusted channel; absent channel = UNKNOWN = untrusted).

**Reference ports** (canonical-behavior cross-checks):
- `NeuroLift-Technologies/asfdk` — canonical Python/TS source
- `NeuroLift-Technologies/asfdk-kotlin` — primary Kotlin reference (use `src/commonMain/kotlin/com/neurolift/asfdk/`)

---

## Project Layout

```
.
├── pom.xml                    # Maven build (jackson-databind, junit-jupiter)
├── src/main/java/com/neurolift/asfdk/
│   ├── ASFDK.java             # createFoundation entry point
│   ├── foundation/NeuroLiftFoundation.java
│   ├── prompt/                # PromptDefense, SanitizationResult, RiskLevel, ValidationResult, OutputSchema, SecurityEventType, InjectionDetection
│   ├── types/                 # FoundationMode, InteractionType, Channel, ComponentOverrides, FoundationConfig, UserInteraction, FoundationResponse, ComponentStatus, HealthCheckResult
│   └── integration/           # ToiOtoiAdapter, SleepwalkerAdapter, RrtAdvocateAdapter, EmotionalState, CrisisLevel, CrisisAssessment, ToiDocument, Json
└── src/test/java/com/neurolift/asfdk/
    ├── FoundationTest.java
    ├── prompt/PromptDefenseTest.java
    └── integration/IntegrationTest.java
```

---

## Build & Test

```bash
# Compile
mvn compile

# Run tests
mvn test

# Governance validation
bash .nltotoi/scripts/validate-governance.sh
```

Commit format: `[AGENT_NAME] type(scope): description` (e.g. `[JAVA] feat(java): port ASFDK core to Java`).

---

## Escalation

Escalate to Joshua W. Dorsey, Sr. if:
- The Kotlin reference semantics conflict with canonical asfdk and the correct resolution is unclear.
- Architecture decisions (build tool, module split, dependency choices) arise.
