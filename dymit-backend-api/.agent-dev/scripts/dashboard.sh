#!/usr/bin/env bash
# dashboard.sh — live Codex agent dashboard for cmux
#
# Supported roles:
#   - coder
#   - tester
#   - reviewer
#
# Supported agent backends:
#   - codex
#
# Examples:
#
#   dashboard.sh coder -a codex
#   dashboard.sh tester -a codex
#   dashboard.sh reviewer -a codex
#
# Environment:
#
#   AGENT=codex
#     Override backend agent type.
#
# Controls:
#
#   l       open full log in less
#   space   pause / resume refresh
#   q       quit

set -uo pipefail

#
# Usage
#

usage() {
cat <<EOF
Usage:
  $(basename "$0") <role> [options]

Roles:
  coder
  tester
  reviewer

Options:
  -a, --agent NAME   Agent backend (default: codex)
  -h, --help         Show this help

Examples:
  $(basename "$0") coder
  $(basename "$0") tester
  $(basename "$0") reviewer

Environment:
  AGENT=codex
EOF
}

#
# Args
#

ROLE=""
AGENT="${AGENT:-codex}"

while [[ $# -gt 0 ]]; do
  case "$1" in

    coder|tester|reviewer)
      ROLE="$1"
      shift
      ;;

    -a|--agent)
      AGENT="$2"
      shift 2
      ;;

    -h|--help)
      usage
      exit 0
      ;;

    *)
      echo "Unknown argument: $1" >&2
      echo >&2
      usage
      exit 2
      ;;
  esac
done

if [[ -z "$ROLE" ]]; then
  usage
  exit 2
fi

#
# Agent backend validation
#

case "$AGENT" in
  codex)
    ;;
  *)
    echo "Unsupported agent backend: $AGENT" >&2
    exit 2
    ;;
esac

#
# Role metadata
#

case "$ROLE" in

  coder)
    ICON="🛠"
    TITLE="CODER"
    HEADER_COLOR=$'\033[1;34m'
    ;;

  tester)
    ICON="🧪"
    TITLE="TESTER"
    HEADER_COLOR=$'\033[1;33m'
    ;;

  reviewer)
    ICON="🧐"
    TITLE="REVIEWER"
    HEADER_COLOR=$'\033[1;35m'
    ;;

esac

LABEL="Focus"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AGENTS_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

#
# Team / workspace detection
#
# Priority:
#   1. AGENT_TEAM env
#   2. CMUX_WORKSPACE_ID
#   3. default
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
# Log layout
#
# Example:
#
#   .agent-logs/latest-coder.log
#   .agent-logs/latest-reviewer.log
#   .agent-logs/latest-tester.log
#

LOG_DIR="$AGENTS_DIR/.agent-logs"
LATEST="$LOG_DIR/latest-$ROLE.log"

#
# ANSI styles
#

RESET=$'\033[0m'
DIM=$'\033[2m'
BOLD=$'\033[1m'

GREEN=$'\033[1;32m'
YELLOW=$'\033[1;33m'
RED=$'\033[1;31m'
CYAN=$'\033[1;36m'

cleanup() {
  printf '\033[?25h\033[H\033[2J'
  exit 0
}

trap cleanup INT TERM

#
# Hide cursor
#

printf '\033[?25l'

get_wrap_width() {

  local c

  c=$(tput cols 2>/dev/null || echo 80)

  [[ "$c" -lt 40 ]] && c=40

  echo $((c - 6))
}

LAST_HASH=""
PAUSED=0

while true; do

  if [[ "$PAUSED" == "0" ]]; then

    WRAP_W=$(get_wrap_width)

    BUF=""

    BUF+="${HEADER_COLOR}═══════════════════════════════════════════════${RESET}"$'\n'
    BUF+="${HEADER_COLOR}  ${ICON}  ${TITLE}${RESET}  ${DIM}[team: ${TEAM}]${RESET}"$'\n'
    BUF+="${HEADER_COLOR}═══════════════════════════════════════════════${RESET}"$'\n\n'

    #
    # Waiting state
    #

    if [[ ! -e "$LATEST" ]]; then

      BUF+="  ${DIM}(no runs yet — waiting for first call)${RESET}"$'\n'
      BUF+="  ${DIM}path: $LATEST${RESET}"$'\n\n'

    else

      #
      # Start timestamp
      #

      TS=$(
        grep "^=== ask-${AGENT}.sh @ " "$LATEST" 2>/dev/null \
        | tail -1 \
        | awk '{print $4}'
      )

      BUF+="  ${BOLD}Started:${RESET} ${TS:-unknown}"$'\n\n'

      #
      # Focus block
      #

      BODY=$(
        awk '
          /^=== FOCUS ===$/ { flag=1; next }
          /^=== /           { flag=0 }
          flag
        ' "$LATEST" 2>/dev/null
      )

      BUF+="  ${BOLD}${LABEL}:${RESET}"$'\n'

      if [[ -n "$BODY" ]]; then

        WRAPPED=$(
          echo "$BODY" \
          | fold -s -w "$WRAP_W" \
          | head -5
        )

        while IFS= read -r line; do
          BUF+="    $line"$'\n'
        done <<< "$WRAPPED"
      fi

      BUF+=$'\n'

      #
      # Extract response section
      #

      RESPONSE=$(
        awk '
          /^=== RESPONSE ===$/ { flag=1; next }
          /^=== END /          { flag=0 }
          flag
        ' "$LATEST" 2>/dev/null
      )

      #
      # Status
      #

      DONE=0
      RC=""

      if grep -q '^=== END ' "$LATEST" 2>/dev/null; then

        DONE=1

        RC=$(
          grep '^=== END ' "$LATEST" \
          | tail -1 \
          | sed 's/.*rc=\([0-9]*\).*/\1/'
        )

        if [[ "$RC" == "0" ]]; then
          BUF+="  ${BOLD}Status:${RESET} ${GREEN}✓ done${RESET}"$'\n\n'
        else
          BUF+="  ${BOLD}Status:${RESET} ${RED}✗ failed (rc=$RC)${RESET}"$'\n\n'
        fi

      else
        BUF+="  ${BOLD}Status:${RESET} ${YELLOW}⏳ running...${RESET}"$'\n\n'
      fi

      #
      # Verdict
      #

      VERDICT_LINE=$(
        echo "$RESPONSE" \
        | grep -A 1 '^## Verdict' 2>/dev/null \
        | tail -1 \
        | sed 's/^[[:space:]]*//'
      )

      if [[ -n "$VERDICT_LINE" ]]; then

        VERB=$(echo "$VERDICT_LINE" | awk '{print $1}')

        case "$VERB" in
          SHIP)      VC="$GREEN" ;;
          NEEDS-FIX) VC="$RED" ;;
          DISCUSS)   VC="$YELLOW" ;;
          *)         VC="$CYAN" ;;
        esac

        BUF+="  ${VC}┃${RESET} ${BOLD}Verdict:${RESET} ${VERDICT_LINE}"$'\n\n'
      fi

      #
      # Findings extraction
      #

      SECS=$(
        echo "$RESPONSE" | awk '
          /^### Blocker/ { sec="bl"; next }
          /^### Major/   { sec="mj"; next }
          /^### Minor/   { sec="mn"; next }
          /^## /         { sec=""; next }

          sec=="bl" && /^- / && tolower($0) !~ /^- none/ {
            print "BL:" $0
            next
          }

          sec=="mj" && /^- / && tolower($0) !~ /^- none/ {
            print "MJ:" $0
            next
          }

          sec=="mn" && /^- / && tolower($0) !~ /^- none/ {
            print "MN:" $0
            next
          }
        '
      )

      BL=$(echo "$SECS" | grep -c '^BL:' || true)
      MJ=$(echo "$SECS" | grep -c '^MJ:' || true)
      MN=$(echo "$SECS" | grep -c '^MN:' || true)

      BUF+="  ${BOLD}Findings:${RESET} "
      BUF+="${RED}${BL} blocker${RESET} · "
      BUF+="${YELLOW}${MJ} major${RESET} · "
      BUF+="${DIM}${MN} minor${RESET}"$'\n\n'

      #
      # Blocker/Major details
      #

      if [[ "$BL" -gt 0 || "$MJ" -gt 0 ]]; then

        BUF+="  ${BOLD}${RED}Blockers + Major:${RESET}"$'\n'

        BLMJ=$(
          echo "$SECS" \
          | grep -E '^(BL|MJ):' \
          | sed 's/^BL://; s/^MJ://'
        )

        WRAPPED=$(
          echo "$BLMJ" \
          | fold -s -w "$WRAP_W"
        )

        while IFS= read -r line; do
          [[ -n "$line" ]] && BUF+="    $line"$'\n'
        done <<< "$WRAPPED"

        BUF+=$'\n'
      fi

      REAL=$(readlink "$LATEST" 2>/dev/null || basename "$LATEST")

      BUF+="  ${DIM}log: $REAL${RESET}"$'\n'
    fi

    #
    # Footer
    #

    BUF+=$'\n'
    BUF+="  ${DIM}controls: ${BOLD}l${RESET}${DIM}=full log · ${BOLD}space${RESET}${DIM}=pause · ${BOLD}q${RESET}${DIM}=quit${RESET}"$'\n'

    #
    # Flicker-free redraw
    #

    HASH=$(printf '%s' "$BUF" | cksum | awk '{print $1}')

    if [[ "$HASH" != "$LAST_HASH" ]]; then

      RENDERED="${BUF//$'\n'/$'\033[K\n'}"

      printf '\033[H%s\033[J' "$RENDERED"

      LAST_HASH="$HASH"
    fi
  fi

  #
  # Keyboard controls
  #

  KEY=""

  IFS= read -rs -t 1 -n 1 KEY 2>/dev/null || true

  case "$KEY" in

    l)
      printf '\033[?25h\033[H\033[2J'

      if [[ -e "$LATEST" ]]; then
        less -R "$LATEST" || true
      else
        echo "(no log yet)"
        sleep 1
      fi

      printf '\033[?25l'

      LAST_HASH=""
      ;;

    ' ')
      if [[ "$PAUSED" == "0" ]]; then
        PAUSED=1
        printf "\n  %s[PAUSED]%s press space to resume\n" "$YELLOW" "$RESET"
        printf '\033[?25h'
      else
        PAUSED=0
        printf '\033[?25l'
        LAST_HASH=""
      fi
      ;;

    q)
      cleanup
      ;;
  esac
done