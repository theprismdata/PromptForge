# PromptForge v0.1.1 릴리스 노트

Date: 2026-03-28

## 주요 변경점

- AssistAI 기본 모델 설정을 하드코딩에서 XML(`models-defaults.xml`) 로딩 방식으로 전환했습니다.
- 기본 모델 3종(OpenAI / Claude / vLLM)으로 정리했습니다.
- AI Chat 모델 선택 드롭다운을 모델명 클릭에서도 열리도록 개선했습니다.
- AI Chat 입력 UX를 `Enter 전송`, `Shift+Enter 줄바꿈`으로 개선하고 중복 전송을 방지했습니다.

## 아티팩트

- `releases/v0.1.1/eclipse-llm-patch-20260328-v0.1.1.zip`
- `releases/v0.1.1/checksums.sha256`

## 업그레이드 안내 (macOS)

1. Eclipse를 종료합니다.
2. 기존 설치 방식과 동일하게 번들 내 `scripts/install.sh`를 실행합니다.
3. 설치 후 Eclipse를 `-clean` 옵션으로 1회 재시작합니다.

## 업그레이드 안내 (Windows)

1. Eclipse를 종료합니다.
2. `releases/v0.1.1/eclipse-llm-patch-20260328-v0.1.1.zip` 압축을 해제합니다.
3. `scripts` 폴더에서 CMD를 열고 아래를 실행합니다.

```bat
install_windows.bat "C:\path\to\eclipse-ide" "C:\path\to\eclipse-llm-patch-20260328-v0.1.1"
```

4. 설치 후 Eclipse를 `-clean` 옵션으로 1회 재시작합니다.

## 롤백 (macOS)

- 번들 내 `scripts/rollback.sh`를 사용해 마지막 백업으로 복원할 수 있습니다.

## 롤백 (Windows)

```bat
rollback_windows.bat
```

특정 백업으로 롤백:

```bat
rollback_windows.bat "C:\Users\<you>\.eclipse_llm_patch_backups\<timestamp>" "C:\path\to\eclipse-ide"
```
