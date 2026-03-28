# PromptForge v0.1.0 Release Notes

Date: 2026-03-28

## Highlights

- AI can now request project creation directly from chat flow.
- If Eclipse command wiring is unavailable, PromptForge creates a Spring-ready scaffold instead of failing.
- `eclipse-runner` now supports local shell execution via `runShellCommand`.
- `Window > AI Settings` menu is available for faster model/key access.

## Artifact

- `releases/v0.1.0/eclipse-llm-patch-20260328.zip`

## Security / Ops Notes

- API keys are expected in Eclipse Secure Storage.
- If any key was shared in screenshots/logs, rotate and replace immediately.

## Upgrade Notes

- Close Eclipse before install.
- Run installer script from bundle.
- Restart Eclipse with `-clean` once after install.
