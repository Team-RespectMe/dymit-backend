#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [ $# -ne 3 ]; then
  echo "Usage: $(basename "$0") <agent> <logfile> <prompt-file>" >&2
  exit 1
fi

AGENT="$1"
LOGFILE="$2"
PROMPT_FILE="$3"
CODEX_BIN="${CODEX_BIN:-$(command -v codex || true)}"
SHELL_BIN="${AGENT_SHELL:-${SHELL:-/bin/zsh}}"

if [ ! -x "$SHELL_BIN" ]; then
  SHELL_BIN="/bin/bash"
fi

if [ -z "$CODEX_BIN" ]; then
  echo "codex executable not found in PATH" | tee -a "$LOGFILE" >&2
  exit 1
fi

if [ ! -f "$PROMPT_FILE" ]; then
  echo "Prompt file not found: $PROMPT_FILE" | tee -a "$LOGFILE" >&2
  exit 1
fi

mkdir -p "$(dirname "$LOGFILE")"

{
  echo ""
  echo "[$AGENT] task dispatched at $(date '+%Y-%m-%d %H:%M:%S %z')"
  echo "[$AGENT] prompt file: $PROMPT_FILE"
  echo ""
} >> "$LOGFILE"

set +e
"$SHELL_BIN" -lc "
  set -o pipefail
  cd \"$ROOT\" &&
  \"$CODEX_BIN\" exec \
    -C \"$ROOT\" \
    -s workspace-write \
    - < \"$PROMPT_FILE\"
" 2>&1 | tee -a "$LOGFILE"
STATUS=${PIPESTATUS[0]}
set -e

{
  echo ""
  echo "[$AGENT] task finished with exit code $STATUS at $(date '+%Y-%m-%d %H:%M:%S %z')"
  echo ""
} >> "$LOGFILE"

exit "$STATUS"
