#!/usr/bin/env bash
# ask-codex.sh — cmux-compatible Codex agent runner
#
# Supports:
#   coder | tester | reviewer
#
# MODEL support:
#   --model <model>
#   or ENV MODEL=xxx
#
# Works with:
#   team-layout.sh
#   dashboard.sh
#   cmux workspace model

set -euo pipefail

#
# Defaults
#

ROLE="${1:-coder}"
shift || true

MODEL="${MODEL:-codex}"

#
# Optional --model parsing
#

if [[ "${1:-}" == "--model" ]]; then
  MODEL="${2:-codex}"
  shift 2
fi

FOCUS="${*:-Review current workspace state and changes.}"

#
# Validate role
#

case "$ROLE" in
  coder|tester|reviewer)
    ;;
  *)
    echo "invalid role: $ROLE" >&2
    exit 2
    ;;
esac

#
# Detect team/workspace
#

detect_team() {

  if [[ -n "${AGENT_TEAM:-}" ]]; then
    echo "$AGENT_TEAM"
    return
  fi

  if [[ -n "${CMUX_WORKSPACE_ID:-}" ]]; then
    echo "$CMUX_WORKSPACE_ID"
    return
  fi

  echo "default"
}

TEAM=$(detect_team)

#
# Paths
#

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AGENTS_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

LOG_DIR="$AGENTS_DIR/.agent-logs/$TEAM"
mkdir -p "$LOG_DIR"

TS="$(date +%Y%m%d-%H%M%S)"

LOG="$LOG_DIR/$ROLE-$TS.log"
LATEST="$LOG_DIR/latest-$ROLE.log"

ln -sfn "$ROLE-$TS.log" "$LATEST"

#
# Prompt
#

PROMPT="
You are a $ROLE agent running under model: $MODEL

ROLE CONTRACT:
- coder: implement changes
- tester: validate behavior
- reviewer: review correctness, security, design

WORK CONTEXT:
$FOCUS

Return structured output:
- Summary
- Findings
- Verdict (SHIP / NEEDS-FIX / DISCUSS)
"

#
# Log header
#

{
  echo "=== ask-codex.sh @ $TS ==="
  echo "=== ROLE ==="
  echo "$ROLE"
  echo "=== MODEL ==="
  echo "$MODEL"
  echo "=== FOCUS ==="
  echo "$FOCUS"
  echo "=== RESPONSE ==="
} > "$LOG"

echo "[ask-codex] role=$ROLE model=$MODEL team=$TEAM" >&2
echo "[ask-codex] log=$LOG" >&2

#
# Execute Codex with model support
#

RC=0

"${CODEX_CLI:-codex}" exec \
  --model "$MODEL" \
  "$PROMPT" 2>&1 | tee -a "$LOG" || RC=$?

#
# End marker
#

printf '\n=== END (rc=%d) ===\n' "$RC" >> "$LOG"

echo
echo "log: $LOG"
echo "rc: $RC"