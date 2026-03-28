# PromptForge
Turn prompts into real Eclipse projects, files, and runnable workflows.

PromptForge is a patched Eclipse + AssistAI bundle focused on agent-first development:
- Project scaffolding from chat prompts
- Code/file automation in workspace
- Optional shell command execution from AI tools

## Repository Layout

- `releases/v0.1.0/`: packaged patch bundle and checksums
- `LICENSE`: Apache-2.0
- `CHANGELOG.md`: release history

## Quick Start (macOS)

1. Download the bundle from `releases/v0.1.0/eclipse-llm-patch-20260328.zip`
2. Unzip it
3. Run:

```bash
cd eclipse-llm-patch-20260328/scripts
chmod +x install.sh rollback.sh
./install.sh
```

If Eclipse is not installed at `/Applications/Eclipse Java.app`, run:

```bash
ECLIPSE_APP_PATH="/path/to/Eclipse.app" ./install.sh
```

4. Restart Eclipse once with `-clean`

## Rollback

```bash
cd eclipse-llm-patch-20260328/scripts
./rollback.sh
```

Or restore from a specific backup:

```bash
./rollback.sh ~/.eclipse_llm_patch_backups/<timestamp>
```

## Verify Integrity

```bash
cd releases/v0.1.0
shasum -a 256 -c checksums.sha256
```

## Notes

- Backups are created automatically during install under `~/.eclipse_llm_patch_backups/`.
- Keep API keys in Eclipse Secure Storage and rotate immediately if exposed.

## Development Workspace Mode

Use PromptForge as the control repo while editing real upstream sources via symlinked workspace.

```bash
cd scripts
chmod +x bootstrap_workspace.sh apply_source_patches.sh
./bootstrap_workspace.sh
./apply_source_patches.sh
```

Then edit/build in:
- `workspace/assistai-src`
- `workspace/eclipse-platform-ui`
