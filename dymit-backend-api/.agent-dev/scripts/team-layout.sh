#!/usr/bin/env bash
# team-layout.sh — set up the 4-agent team layout in cmux.
#
#   ┌─────────────────┬──────────────────┐
#   │                 │  Codex (Coder)   │
#   │  codex (PM)     │  dashboard       │
#   │  shell —        ├──────────────────┤
#   │  run 'codex'    │  Tester(Codex)   │
#   │  yourself       │  dashboard       │
#   │                 |──────────────────|
#   │  yourself       │  Reviewer (Codex)│
#   │  dashboard      │  dashboard       │
#   └────────────────────────────────────┘
#
# Default behavior:
#   - creates a new cmux workspace named "agents"
#   - switches to that workspace
#   - builds the layout there
#
# Use --here to apply the layout to the CURRENT workspace instead.

set -euo pipefail

SESSION="agents"
ATTACH=1
HERE=0

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
DASH="$SCRIPT_DIR/dashboard.sh"

echo "script dir : ${SCRIPT_DIR}"
echo "repo dir : $REPO_DIR" 

usage() {
cat <<EOF
Usage: $(basename "$0") [options]

Options:
  -n NAME        Workspace name
  --here         Apply layout to CURRENT workspace
  --no-attach    Do not focus workspace
  -h, --help     Show this help
EOF
}

#
# Helpers
#

extract_surface() {
  grep -o 'surface:[0-9]\+' | head -n1
}

extract_workspace() {
  grep -o 'workspace:[0-9]\+' | head -n1
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -n)
      SESSION="$2"
      shift 2
      ;;
    --here)
      HERE=1
      shift
      ;;
    --no-attach)
      ATTACH=0
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown arg: $1"
      exit 1
      ;;
  esac
done

command -v cmux >/dev/null || {
  echo "error: cmux not installed"
  exit 1
}

[ -x "$DASH" ] || {
  echo "error: dashboard script not executable: $DASH"
  exit 1
}

#
# Workspace selection
#

if [[ "$HERE" == "1" ]]; then
  #
  # Reuse current workspace
  #

  WORKSPACE=$(
    cmux current-workspace --id-format refs \
    | extract_workspace
  )

  echo "✓ Using current workspace: $WORKSPACE"

else
  #
  # Create dedicated workspace
  #

  cmux new-workspace \
    --name "$SESSION" \
    --cwd "$REPO_DIR" >/dev/null

  #
  # IMPORTANT:
  # Explicitly resolve and switch into the new workspace
  # before issuing split commands.
  #

  WORKSPACE=$(
    cmux current-workspace --id-format refs \
    | extract_workspace
  )

  cmux select-workspace \
    --workspace "$WORKSPACE" >/dev/null

  echo "✓ Created workspace: $WORKSPACE"
fi

#
# Initial surface = PM pane
#
# After selecting the target workspace,
# resolve the currently focused surface there.
#

PM_SURFACE=$(
  cmux list-pane-surfaces \
    --workspace "$WORKSPACE" \
    --id-format refs \
  | extract_surface
)

echo "✓ PM surface: $PM_SURFACE"

#
# Right split = coder
#
# Layout:
#
#   PM | CODER
#

cmux focus-pane \
  --workspace "$WORKSPACE" \
  --pane "$PM_SURFACE" >/dev/null 2>&1 || true

CODER_SURFACE=$(
  cmux new-split right \
    --workspace "$WORKSPACE" \
    --id-format refs \
  | extract_surface
)

echo "✓ Coder surface: $CODER_SURFACE"

cmux send \
  --workspace "$WORKSPACE" \
  --surface "$CODER_SURFACE" \
  "$DASH  coder -a codex "

cmux send-key \
  --workspace "$WORKSPACE" \
  --surface "$CODER_SURFACE" \
  Enter

echo "✓ PM and Coder panes ready"

#
# Bottom split = tester
#
# Layout:
#
#   PM | CODER
#      | TESTER
#

cmux focus-pane \
  --workspace "$WORKSPACE" \
  --pane "$CODER_SURFACE" >/dev/null 2>&1 || true

TEST_SURFACE=$(
  cmux new-split down \
    --workspace "$WORKSPACE" \
    --id-format refs \
  | extract_surface
)

echo "✓ Tester surface: $TEST_SURFACE"

cmux send \
  --workspace "$WORKSPACE" \
  --surface "$TEST_SURFACE" \
  "$DASH tester -a codex"

cmux send-key \
  --workspace "$WORKSPACE" \
  --surface "$TEST_SURFACE" \
  Enter

echo "✓ Tester pane ready"

#
# Another bottom split = reviewer
#
# Final layout:
#
#   PM | CODER
#      | TESTER
#      | REVIEWER
#

cmux focus-pane \
  --workspace "$WORKSPACE" \
  --pane "$TEST_SURFACE" >/dev/null 2>&1 || true

REVIEW_SURFACE=$(
  cmux new-split down \
    --workspace "$WORKSPACE" \
    --id-format refs \
  | extract_surface
)

echo "✓ Reviewer surface: $REVIEW_SURFACE"

cmux send \
  --workspace "$WORKSPACE" \
  --surface "$REVIEW_SURFACE" \
  "$DASH reviewer -a codex"

cmux send-key \
  --workspace "$WORKSPACE" \
  --surface "$REVIEW_SURFACE" \
  Enter

echo "✓ Reviewer pane ready"

#
# PM pane instructions
#

cmux send \
  --workspace "$WORKSPACE" \
  --surface "$PM_SURFACE" \
  "# Team ready. Run 'codex' here."

cmux send-key \
  --workspace "$WORKSPACE" \
  --surface "$PM_SURFACE" \
  Enter

#
# Focus workspace
#

if [[ "$ATTACH" == "1" ]]; then
  cmux select-workspace \
    --workspace "$WORKSPACE" >/dev/null
fi

echo
echo "✓ cmux workspace '$SESSION' ready"
echo "  workspace: $WORKSPACE"
echo "  pm:        $PM_SURFACE"
echo "  coder:     $CODER_SURFACE"
echo "  tester:    $TEST_SURFACE"
echo "  reviewer:  $REVIEW_SURFACE"