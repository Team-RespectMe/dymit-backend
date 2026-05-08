#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT" || exit 1

SESSION="${AGENT_SESSION:-dymit-agents}"
LOG_DIR="$ROOT/.agent-logs"
ROLE_DIR="$ROOT/agent-roles"
RUNNER_SCRIPT="$ROOT/scripts/run-agent-task.sh"
TMUX_SOCKET_DIR="${TMUX_SOCKET_DIR:-$ROOT/.tmux}"
TMUX_SOCKET="${TMUX_SOCKET:-$TMUX_SOCKET_DIR/dymit-agents.sock}"
WORKSPACE_PREFIX="${WORKSPACE_PREFIX:-DYMIT-AGENT}"
SHELL_BIN="${AGENT_SHELL:-${SHELL:-/bin/zsh}}"
MUX=""

if [ ! -x "$SHELL_BIN" ]; then
  SHELL_BIN="/bin/bash"
fi

usage() {
  cat <<EOF
Usage: $(basename "$0") start|monitor|list <agent> [options]

Commands:
  start <agent> [-i 'instructions'] [-f instructions_file] [-b branch] [-s session]
    Spawn or reuse an agent pane and run the task in that pane.

  monitor <agent|all>
    Open a new monitor tab that tails the latest agent log, or all logs.

  list
    List existing .agent-logs files.

Examples:
  ./scripts/spawn-agent.sh start coder -i "Implement feature X"
  ./scripts/spawn-agent.sh monitor coder
  ./scripts/spawn-agent.sh monitor all
EOF
}

command_exists() {
  command -v "$1" >/dev/null 2>&1
}

can_use_cmux() {
  command_exists cmux && cmux ping >/dev/null 2>&1
}

detect_mux() {
  if can_use_cmux; then
    MUX="cmux"
    return
  fi

  if command_exists tmux; then
    MUX="tmux"
    return
  fi

  if command_exists cmux; then
    echo "Error: cmux is installed but not controllable from this shell." >&2
  else
    echo "Error: Neither a usable cmux nor tmux was found." >&2
  fi
  exit 1
}

require_arg_value() {
  local flag="$1"
  local value="${2:-}"

  if [ -z "$value" ]; then
    echo "Missing value for $flag" >&2
    exit 1
  fi
}

branch_name() {
  local branch=""

  if command_exists git && git rev-parse --git-dir >/dev/null 2>&1; then
    branch="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || true)"
  fi

  printf '%s\n' "${branch:-unknown}"
}

absolute_path() {
  local path="$1"
  printf '%s\n' "$(cd "$(dirname "$path")" && pwd)/$(basename "$path")"
}

agent_upper() {
  printf '%s\n' "$1" | awk '{print toupper($0)}'
}

role_file_for() {
  local agent="$1"
  printf '%s\n' "$ROLE_DIR/${agent}.md"
}

create_log_file() {
  local agent="$1"
  local branch="$2"
  local instructions="$3"
  local branch_safe="${branch//\//_}"
  local timestamp
  local logfile

  timestamp="$(date '+%Y_%m_%d_%H_%M_%S')"
  logfile="$LOG_DIR/${agent}_${branch_safe}_${timestamp}.md"

  mkdir -p "$LOG_DIR"
  {
    echo "# ${agent} Agent Log"
    echo ""
    echo "Branch: ${branch}"
    echo "Started: $(date '+%Y-%m-%d %H:%M:%S %z')"
    echo ""
    if [ -n "$instructions" ]; then
      echo "## Instructions"
      echo ""
      echo "$instructions"
      echo ""
    fi
    echo "## Console"
    echo ""
  } > "$logfile"

  printf '%s\n' "$logfile"
}

create_prompt_file() {
  local agent="$1"
  local branch="$2"
  local instructions="$3"
  local logfile="$4"
  local role_file
  local prompt_file

  role_file="$(role_file_for "$agent")"
  prompt_file="${logfile%.md}.prompt.txt"

  {
    echo "You are the ${agent} sub-agent for the DYMIT backend repository."
    echo "Repository root: $ROOT"
    echo "Current branch: $branch"
    echo "Write your execution log to: $logfile"
    echo ""
    echo "Mandatory references:"
    echo "- $ROOT/AGENTS.md"
    if [ -f "$role_file" ]; then
      echo "- $role_file"
    fi
    echo ""
    echo "Execution rules:"
    echo "- Follow AGENTS.md and your role file before taking any action."
    echo "- Stay on the current branch and do not commit, switch branches, or push."
    echo "- Record concrete outcomes in the assigned log file."
    echo ""
    echo "Task:"
    if [ -n "$instructions" ]; then
      echo "$instructions"
    else
      echo "No task instructions were provided. Inspect the repository context and wait for PM direction."
    fi
  } > "$prompt_file"

  printf '%s\n' "$prompt_file"
}

build_run_command() {
  local agent="$1"
  local logfile="$2"
  local prompt_file="$3"

  printf "cd %q && %q %q %q %q" \
    "$ROOT" \
    "$RUNNER_SCRIPT" \
    "$agent" \
    "$logfile" \
    "$prompt_file"
}

build_interactive_command() {
  local run_command="$1"

  printf "%q -lc %q" "$SHELL_BIN" "$run_command; status=\$?; echo; echo \"[spawn-agent] exit code: \$status\"; exec $SHELL_BIN -l"
}

agent_workspace_name() {
  local agent="$1"
  printf '%s\n' "${WORKSPACE_PREFIX}-${agent}"
}

current_cmux_workspace() {
  cmux current-workspace 2>/dev/null | awk 'NR == 1 { print $1 }'
}

current_cmux_pane() {
  cmux list-panes 2>/dev/null | awk '/\[focused\]/ { print $2; exit }'
}

current_cmux_surface() {
  local workspace_ref="$1"
  local pane_ref="$2"

  cmux list-pane-surfaces --workspace "$workspace_ref" --pane "$pane_ref" 2>/dev/null | awk '/\[selected\]/ { print $2; exit }'
}

find_cmux_surface_by_title() {
  local workspace_ref="$1"
  local title="$2"

  cmux tree --workspace "$workspace_ref" 2>/dev/null | awk -v wanted="$title" '
    /surface surface:/ {
      ref = ""
      label = ""

      for (i = 1; i <= NF; i++) {
        if ($i == "surface") {
          ref = $(i + 1)
          sub(/^"/, "", $(NF - 3))
        }
      }

      if (match($0, /surface:[0-9]+/)) {
        ref = substr($0, RSTART, RLENGTH)
      }

      if (match($0, /"[^"]+"/)) {
        label = substr($0, RSTART + 1, RLENGTH - 2)
      }

      if (label == wanted) {
        print ref
        exit
      }
    }
  '
}

pane_for_cmux_surface() {
  local workspace_ref="$1"
  local surface_ref="$2"

  cmux tree --workspace "$workspace_ref" 2>/dev/null | awk -v target="$surface_ref" '
    /pane pane:/ {
      if (match($0, /pane:[0-9]+/)) {
        current_pane = substr($0, RSTART, RLENGTH)
      }
      next
    }

    /surface surface:/ {
      if (match($0, /surface:[0-9]+/)) {
        current_surface = substr($0, RSTART, RLENGTH)
        if (current_surface == target) {
          print current_pane
          exit
        }
      }
    }
  '
}

ensure_tmux_session() {
  mkdir -p "$TMUX_SOCKET_DIR"

  if ! tmux -S "$TMUX_SOCKET" has-session -t "$SESSION" 2>/dev/null; then
    tmux -S "$TMUX_SOCKET" new-session -d -s "$SESSION" -n "ctl"
  fi

  if ! tmux -S "$TMUX_SOCKET" has-session -t "$SESSION" >/dev/null 2>&1; then
    echo "Failed to create or access tmux session '$SESSION' via $TMUX_SOCKET" >&2
    exit 1
  fi
}

tmux_window_exists() {
  local window_name="$1"

  tmux -S "$TMUX_SOCKET" list-windows -t "$SESSION" -F '#{window_name}' | awk -v name="$window_name" '$0 == name { found = 1 } END { exit(found ? 0 : 1) }'
}

start_tmux_agent() {
  local window_name="$1"
  local run_command="$2"
  local interactive_command
  local action="created"

  ensure_tmux_session
  interactive_command="$(build_interactive_command "$run_command")"

  if tmux_window_exists "$window_name"; then
    action="reused"
  else
    tmux -S "$TMUX_SOCKET" new-window -d -t "$SESSION" -n "$window_name" -c "$ROOT" "$interactive_command"
  fi

  if ! tmux_window_exists "$window_name"; then
    echo "Failed to create or access tmux window '$window_name'" >&2
    exit 1
  fi

  if [ "$action" = "reused" ]; then
    tmux -S "$TMUX_SOCKET" respawn-pane -k -t "$SESSION:$window_name" "$interactive_command"
  fi
  tmux -S "$TMUX_SOCKET" select-window -t "$SESSION:$window_name" >/dev/null 2>&1 || true

  printf '%s\n' "$action"
}

start_tmux_monitor() {
  local window_name="$1"
  local monitor_command="$2"

  ensure_tmux_session
  tmux -S "$TMUX_SOCKET" new-window -d -t "$SESSION" -n "$window_name" -c "$ROOT" "$monitor_command"

  if ! tmux_window_exists "$window_name"; then
    echo "Failed to create tmux monitor window '$window_name'" >&2
    exit 1
  fi

  tmux -S "$TMUX_SOCKET" select-window -t "$SESSION:$window_name" >/dev/null 2>&1 || true
}

start_cmux_agent() {
  local surface_title="$1"
  local run_command="$2"
  local interactive_command
  local workspace_ref
  local source_pane_ref
  local source_surface_ref
  local target_pane_ref
  local surface_ref
  local action="created"

  interactive_command="$(build_interactive_command "$run_command")"
  workspace_ref="$(current_cmux_workspace)"
  source_pane_ref="$(current_cmux_pane)"

  if [ -z "$workspace_ref" ] || [ -z "$source_pane_ref" ]; then
    echo "Failed to resolve current cmux workspace or pane" >&2
    exit 1
  fi

  source_surface_ref="$(current_cmux_surface "$workspace_ref" "$source_pane_ref")"
  if [ -z "$source_surface_ref" ]; then
    echo "Failed to resolve current cmux surface" >&2
    exit 1
  fi

  surface_ref="$(find_cmux_surface_by_title "$workspace_ref" "$surface_title")"

  if [ -n "$surface_ref" ]; then
    action="reused"
    target_pane_ref="$(pane_for_cmux_surface "$workspace_ref" "$surface_ref")"
  else
    cmux new-split right --workspace "$workspace_ref" --surface "$source_surface_ref" >/dev/null
    target_pane_ref="$(current_cmux_pane)"
    surface_ref="$(current_cmux_surface "$workspace_ref" "$target_pane_ref")"
    if [ -z "$surface_ref" ]; then
      echo "Failed to resolve newly created cmux split surface" >&2
      exit 1
    fi
    cmux rename-tab --workspace "$workspace_ref" --surface "$surface_ref" "$surface_title" >/dev/null
  fi

  if [ -z "$surface_ref" ] || [ -z "$target_pane_ref" ]; then
    echo "Failed to resolve cmux split target for $surface_title" >&2
    exit 1
  fi

  cmux respawn-pane --workspace "$workspace_ref" --surface "$surface_ref" --command "$interactive_command" >/dev/null
  if [ "$source_pane_ref" != "$target_pane_ref" ]; then
    cmux focus-pane --workspace "$workspace_ref" --pane "$source_pane_ref" >/dev/null
  fi

  printf '%s\n' "$action"
}

start_cmux_monitor() {
  local workspace_name="$1"
  local monitor_command="$2"

  cmux new-workspace --name "$workspace_name" --cwd "$ROOT" --command "$monitor_command" >/dev/null
}

print_start_result() {
  local agent="$1"
  local action="$2"
  local logfile="$3"
  local action_title

  action_title="$(printf '%s' "$action" | awk '{print toupper(substr($0, 1, 1)) substr($0, 2)}')"

  if [ "$MUX" = "tmux" ]; then
    echo "$action_title ${agent} in tmux session '$SESSION', window '$agent'"
    echo "Attach: tmux -S $TMUX_SOCKET attach -t $SESSION"
  else
    echo "$action_title ${agent} in current cmux workspace pane '$(agent_workspace_name "$agent")'"
  fi
  echo "Log: $logfile"
}

if [ $# -lt 1 ]; then
  usage
  exit 1
fi

if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
  detect_mux
  usage
  exit 0
fi

detect_mux

CMD="$1"
shift

case "$CMD" in
  list)
    mkdir -p "$LOG_DIR"
    ls -1t "$LOG_DIR"/*.md 2>/dev/null || true
    ;;
  start)
    if [ $# -lt 1 ]; then
      echo "start requires <agent>" >&2
      usage
      exit 1
    fi

    AGENT_RAW="$1"
    shift
    AGENT="$(agent_upper "$AGENT_RAW")"
    INSTRUCTIONS=""
    INSTR_FILE=""
    BRANCH=""

    while [ $# -gt 0 ]; do
      case "$1" in
        -i|--instructions)
          require_arg_value "$1" "${2:-}"
          INSTRUCTIONS="$2"
          shift 2
          ;;
        -f|--file)
          require_arg_value "$1" "${2:-}"
          INSTR_FILE="$2"
          shift 2
          ;;
        -b|--branch)
          require_arg_value "$1" "${2:-}"
          BRANCH="$2"
          shift 2
          ;;
        -s|--session)
          require_arg_value "$1" "${2:-}"
          SESSION="$2"
          shift 2
          ;;
        -h|--help)
          usage
          exit 0
          ;;
        *)
          echo "Unknown arg $1" >&2
          usage
          exit 1
          ;;
      esac
    done

    if [ -n "$INSTR_FILE" ]; then
      if [ ! -f "$INSTR_FILE" ]; then
        echo "Instructions file not found: $INSTR_FILE" >&2
        exit 1
      fi
      INSTRUCTIONS="$(cat "$INSTR_FILE")"
    fi

    if [ -z "$BRANCH" ]; then
      BRANCH="$(branch_name)"
    fi

    WORKSPACE_NAME="$(agent_workspace_name "$AGENT")"
    LOGFILE="$(create_log_file "$AGENT" "$BRANCH" "$INSTRUCTIONS")"
    PROMPT_FILE="$(create_prompt_file "$AGENT" "$BRANCH" "$INSTRUCTIONS" "$LOGFILE")"
    RUN_COMMAND="$(build_run_command "$AGENT" "$(absolute_path "$LOGFILE")" "$(absolute_path "$PROMPT_FILE")")"

    if [ "$MUX" = "tmux" ]; then
      ACTION="$(start_tmux_agent "$AGENT" "$RUN_COMMAND")"
    else
      ACTION="$(start_cmux_agent "$WORKSPACE_NAME" "$RUN_COMMAND")"
    fi

    print_start_result "$AGENT" "$ACTION" "$LOGFILE"
    ;;
  monitor)
    if [ $# -lt 1 ]; then
      echo "monitor requires <agent|all>" >&2
      usage
      exit 1
    fi

    TARGET="$1"
    shift
    mkdir -p "$LOG_DIR"

    if [ "$TARGET" = "all" ]; then
      WINDOW_NAME="MONITOR-ALL-$(date '+%H%M%S')"
      MONITOR_COMMAND="bash -lc 'tail -f .agent-logs/*.md 2>/dev/null || echo \"No logs yet\"; exec /bin/bash'"

      if [ "$MUX" = "tmux" ]; then
        start_tmux_monitor "$WINDOW_NAME" "$MONITOR_COMMAND"
        echo "Spawned monitor for all logs in tmux session '$SESSION', window '$WINDOW_NAME'"
        echo "Attach: tmux -S $TMUX_SOCKET attach -t $SESSION"
      else
        start_cmux_monitor "$WINDOW_NAME" "$MONITOR_COMMAND"
        echo "Spawned monitor for all logs in cmux workspace '$WINDOW_NAME'"
      fi
      exit 0
    fi

    AGENT="$(agent_upper "$TARGET")"
    LATEST="$(ls -1t "$LOG_DIR"/"${AGENT}"_*.md 2>/dev/null | head -n 1 || true)"
    if [ -z "$LATEST" ]; then
      echo "No log found for agent $AGENT" >&2
      exit 1
    fi

    WINDOW_NAME="MONITOR-${AGENT}-$(date '+%H%M%S')"
    MONITOR_COMMAND="$(printf "bash -lc 'tail -f %q || true; exec /bin/bash'" "$LATEST")"

    if [ "$MUX" = "tmux" ]; then
      start_tmux_monitor "$WINDOW_NAME" "$MONITOR_COMMAND"
      echo "Monitoring $LATEST in tmux session '$SESSION', window '$WINDOW_NAME'"
      echo "Attach: tmux -S $TMUX_SOCKET attach -t $SESSION"
    else
      start_cmux_monitor "$WINDOW_NAME" "$MONITOR_COMMAND"
      echo "Monitoring $LATEST in cmux workspace '$WINDOW_NAME'"
    fi
    ;;
  *)
    echo "Unknown command: $CMD" >&2
    usage
    exit 1
    ;;
esac
