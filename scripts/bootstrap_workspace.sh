#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORKSPACE_DIR="${ROOT_DIR}/workspace"

# Defaults use the existing local paths from current setup.
ASSISTAI_SRC_DIR="${ASSISTAI_SRC_DIR:-/Users/prismdata/Documents/entec/arch_group/eclipse_llm/assistai-src}"
ECLIPSE_UI_SRC_DIR="${ECLIPSE_UI_SRC_DIR:-/Users/prismdata/Documents/entec/arch_group/eclipse_llm/releng-aggregator/eclipse.platform.ui}"

mkdir -p "${WORKSPACE_DIR}"

link_or_fail() {
  local src="$1"
  local dst="$2"
  if [[ ! -d "${src}" ]]; then
    echo "ERROR: source directory not found: ${src}"
    exit 1
  fi

  if [[ -L "${dst}" || -e "${dst}" ]]; then
    rm -rf "${dst}"
  fi

  ln -s "${src}" "${dst}"
  echo "Linked: ${dst} -> ${src}"
}

link_or_fail "${ASSISTAI_SRC_DIR}" "${WORKSPACE_DIR}/assistai-src"
link_or_fail "${ECLIPSE_UI_SRC_DIR}" "${WORKSPACE_DIR}/eclipse-platform-ui"

echo ""
echo "Workspace bootstrap complete."
echo "Run scripts/apply_source_patches.sh next."
