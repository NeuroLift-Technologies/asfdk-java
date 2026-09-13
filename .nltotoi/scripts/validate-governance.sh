#!/bin/bash
# validate-governance.sh — ASFDK Java Governance Validation
# Run: bash .nltotoi/scripts/validate-governance.sh
# Returns: 0 if all checks pass, 1 if any fail

set -euo pipefail

OTOI_VERSION="ORG-DEV-OTOI-1.0.3"
REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
PASS=0
FAIL=0

check() {
    local name="$1"
    shift
    if "$@" > /dev/null 2>&1; then
        echo "  ✅ PASS: $name"
        PASS=$((PASS + 1))
    else
        echo "  ❌ FAIL: $name"
        FAIL=$((FAIL + 1))
    fi
}

echo "=== ASFDK Java Governance Validation ==="
echo "Document ID: $OTOI_VERSION"
echo ""

# 1. OTOI contract
check "NLT-DEV-OTOI.md exists" test -f "$REPO_ROOT/NLT-DEV-OTOI.md"
check "OTOI version is 1.0.3" grep -q "ORG-DEV-OTOI-1.0.3" "$REPO_ROOT/NLT-DEV-OTOI.md"

# 2. Agent registry and protocols
check "AGENTS.md exists" test -f "$REPO_ROOT/AGENTS.md"
check "AGENTS.md has agent registry" grep -q "### Registered Agents" "$REPO_ROOT/AGENTS.md"
check "CLAUDE.md exists" test -f "$REPO_ROOT/CLAUDE.md"
check "CLAUDE.md references commit format" grep -q "type(scope): description" "$REPO_ROOT/CLAUDE.md"

# 3. Governance directory
check "nltotoi directory exists" test -d "$REPO_ROOT/.nltotoi"
check "nltotoi/README.md exists" test -f "$REPO_ROOT/.nltotoi/README.md"
check "nltotoi/index/governance-files.md exists" test -f "$REPO_ROOT/.nltotoi/index/governance-files.md"

# 4. Templates
check "templates/agent-registration.json exists" test -f "$REPO_ROOT/templates/agent-registration.json"
check "templates/handoff-record.json exists" test -f "$REPO_ROOT/templates/handoff-record.json"
check "templates/escalation.md exists" test -f "$REPO_ROOT/templates/escalation.md"
check "templates/intent-log.md exists" test -f "$REPO_ROOT/templates/intent-log.md"

# 5. Issue/PR templates and SOPs
check "ISSUE_TEMPLATE/agent-escalation.md exists" test -f "$REPO_ROOT/ISSUE_TEMPLATE/agent-escalation.md"
check "ISSUE_TEMPLATE/governance-proposal.md exists" test -f "$REPO_ROOT/ISSUE_TEMPLATE/governance-proposal.md"
check "PULL_REQUEST_TEMPLATE/agent-contribution.md exists" test -f "$REPO_ROOT/PULL_REQUEST_TEMPLATE/agent-contribution.md"
check "SOPs/new-agent-onboarding.md exists" test -f "$REPO_ROOT/SOPs/new-agent-onboarding.md"
check "SOPs/repo-governance-setup.md exists" test -f "$REPO_ROOT/SOPs/repo-governance-setup.md"
check "SOPs/incident-response.md exists" test -f "$REPO_ROOT/SOPs/incident-response.md"

# 6. CI and discovery manifest
check "workflows/validate-governance.yml exists" test -f "$REPO_ROOT/.github/workflows/validate-governance.yml"
check "nltotoi.json exists" test -f "$REPO_ROOT/nltotoi.json"
check "nltotoi.json declares the Java repo" grep -q '"Java"' "$REPO_ROOT/nltotoi.json"

# 7. Java/Maven project structure
check "pom.xml exists" test -f "$REPO_ROOT/pom.xml"
check "src/main/java/com/neurolift/asfdk/ASFDK.java exists" test -f "$REPO_ROOT/src/main/java/com/neurolift/asfdk/ASFDK.java"
check "src/main/java/com/neurolift/asfdk/foundation/NeuroLiftFoundation.java exists" test -f "$REPO_ROOT/src/main/java/com/neurolift/asfdk/foundation/NeuroLiftFoundation.java"
for f in types foundation prompt integration; do
    check "src/main/java/com/neurolift/asfdk/$f/ exists" test -d "$REPO_ROOT/src/main/java/com/neurolift/asfdk/$f"
done
check "src/test/java/com/neurolift/asfdk/FoundationTest.java exists" test -f "$REPO_ROOT/src/test/java/com/neurolift/asfdk/FoundationTest.java"

# 8. Toolchain gates (run when maven is available; skip with notice otherwise)
if command -v mvn > /dev/null 2>&1; then
    check "mvn compile" bash -c "cd \"$REPO_ROOT\" && mvn -q compile"
    check "mvn test" bash -c "cd \"$REPO_ROOT\" && mvn -q test"
else
    echo "  ⚠️  SKIP: maven not on PATH — build/test gates run in CI"
fi

echo ""
echo "=== Results ==="
echo "Passed: $PASS"
echo "Failed: $FAIL"
echo ""

if [ "$FAIL" -gt 0 ]; then
    echo "❌ Governance validation FAILED — $FAIL check(s) failed"
    exit 1
else
    echo "✅ Governance validation PASSED — all $PASS checks OK"
    exit 0
fi
