# PromptForge
프롬프트를 실제 Eclipse 프로젝트, 파일, 실행 가능한 워크플로우로 전환합니다.

PromptForge는 에이전트 중심 개발을 위해 구성된 Eclipse + AssistAI 패치 번들입니다.
- 채팅 프롬프트 기반 프로젝트 스캐폴딩
- 워크스페이스 내 코드/파일 자동화
- AI 도구에서 선택적으로 셸 명령 실행

## 저장소 구성

- `releases/v0.1.0/`: 배포용 패치 번들 및 체크섬
- `LICENSE`: Apache-2.0 라이선스
- `CHANGELOG.md`: 릴리스 이력

## 빠른 시작 (macOS)

1. `releases/v0.1.0/eclipse-llm-patch-20260328.zip` 파일을 다운로드합니다.
2. 압축을 해제합니다.
3. 아래 명령을 실행합니다.

```bash
cd eclipse-llm-patch-20260328/scripts
chmod +x install.sh rollback.sh
./install.sh
```

Eclipse 설치 경로가 `/Applications/Eclipse Java.app`가 아니라면:

```bash
ECLIPSE_APP_PATH="/path/to/Eclipse.app" ./install.sh
```

4. Eclipse를 `-clean` 옵션으로 1회 재시작합니다.

## 롤백 (macOS)

```bash
cd eclipse-llm-patch-20260328/scripts
./rollback.sh
```

특정 백업으로 복원하려면:

```bash
./rollback.sh ~/.eclipse_llm_patch_backups/<timestamp>
```

## 설치 (Windows)

1. Eclipse를 완전히 종료합니다.
2. `releases/v0.1.0/eclipse-llm-patch-20260328.zip` 압축을 해제합니다.
3. 압축 해제된 `scripts` 폴더에서 명령 프롬프트(CMD)를 열고 실행합니다.

```bat
install_windows.bat "C:\path\to\eclipse-ide" "C:\path\to\eclipse-llm-patch-20260328"
```

또는 `ECLIPSE_HOME`을 지정한 뒤 인자 없이 실행합니다.

```bat
set ECLIPSE_HOME=C:\path\to\eclipse-ide
install_windows.bat
```

그 다음 Eclipse를 `-clean` 옵션으로 1회 실행합니다.

## 롤백 (Windows)

```bat
rollback_windows.bat
```

특정 백업으로 롤백하려면:

```bat
rollback_windows.bat "C:\Users\<you>\.eclipse_llm_patch_backups\<timestamp>" "C:\path\to\eclipse-ide"
```


## 개발 워크스페이스 모드

실제 업스트림 소스를 `workspace` 아래로 복사해 두고 PromptForge를 제어 저장소로 사용합니다.

```bash
cd scripts
chmod +x bootstrap_workspace.sh apply_source_patches.sh
./bootstrap_workspace.sh
./apply_source_patches.sh
```

다른 환경에서 업스트림 소스 경로가 다르면 환경변수로 지정해 실행하세요:

```bash
ASSISTAI_SRC_DIR="/path/to/assistai-src" \
ECLIPSE_UI_SRC_DIR="/path/to/eclipse.platform.ui" \
./bootstrap_workspace.sh
./apply_source_patches.sh
```

이후 아래 경로에서 수정/빌드합니다.
- `workspace/assistai-src`
- `workspace/eclipse-platform-ui`
