#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PATCH_DIR="${ROOT_DIR}/source-patches"
WORKSPACE_DIR="${ROOT_DIR}/workspace"

ASSISTAI_DST="${WORKSPACE_DIR}/assistai-src/plugins/com.github.gradusnikov.eclipse.plugin.assistai.main"
ECLIPSE_UI_DST="${WORKSPACE_DIR}/eclipse-platform-ui/bundles/org.eclipse.ui.ide"

if [[ ! -d "${ASSISTAI_DST}" ]]; then
  echo "ERROR: AssistAI target not found: ${ASSISTAI_DST}"
  echo "Run scripts/bootstrap_workspace.sh first."
  exit 1
fi

if [[ ! -d "${ECLIPSE_UI_DST}" ]]; then
  echo "ERROR: Eclipse UI target not found: ${ECLIPSE_UI_DST}"
  echo "Run scripts/bootstrap_workspace.sh first."
  exit 1
fi

rsync -a --delete \
  "${PATCH_DIR}/assistai/com.github.gradusnikov.eclipse.plugin.assistai.main/" \
  "${ASSISTAI_DST}/"

rsync -a --delete \
  "${PATCH_DIR}/eclipse-ui-ide/org.eclipse.ui.ide/" \
  "${ECLIPSE_UI_DST}/"

echo "Patch sync complete."
echo "- AssistAI source updated at: ${ASSISTAI_DST}"
echo "- Eclipse UI source updated at: ${ECLIPSE_UI_DST}"
