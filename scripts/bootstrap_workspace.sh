#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SOURCES_DIR="${ROOT_DIR}/sources"

# Defaults use local source paths in this repository.
ASSISTAI_SRC_DIR="${ASSISTAI_SRC_DIR:-${SOURCES_DIR}/assistai}"
ECLIPSE_UI_SRC_DIR="${ECLIPSE_UI_SRC_DIR:-${SOURCES_DIR}/eclipse-platform-ui}"

mkdir -p "${SOURCES_DIR}"

copy_or_fail() {
  local src="$1"
  local dst="$2"
  if [[ ! -d "${src}" ]]; then
    echo "ERROR: source directory not found: ${src}"
    exit 1
  fi

  local src_resolved
  src_resolved="$(cd "${src}" && pwd -P)"
  local dst_resolved=""
  if [[ -d "${dst}" ]]; then
    dst_resolved="$(cd "${dst}" && pwd -P)"
  fi

  if [[ -n "${dst_resolved}" && "${src_resolved}" == "${dst_resolved}" ]]; then
    echo "Using existing local source: ${dst}"
    return
  fi

  if [[ -L "${dst}" || -e "${dst}" ]]; then
    rm -rf "${dst}"
  fi

  mkdir -p "${dst}"
  rsync -a --delete "${src}/" "${dst}/"
  echo "Copied: ${src} -> ${dst}"
}

copy_or_fail "${ASSISTAI_SRC_DIR}" "${SOURCES_DIR}/assistai"
copy_or_fail "${ECLIPSE_UI_SRC_DIR}" "${SOURCES_DIR}/eclipse-platform-ui"

echo ""
echo "Source bootstrap complete."
echo "Run scripts/apply_source_patches.sh next."
