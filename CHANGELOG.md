# 변경 이력

## v0.1.1 - 2026-03-28

PromptForge 설치형 번들 갱신 릴리스입니다.

### 변경 사항

- AssistAI 기본 모델 로딩 방식을 XML(`models-defaults.xml`) 기반으로 전환
- 기본 모델 3종 구성: OpenAI / Claude / vLLM
- ChatView 모델 선택 UX 개선: 모델명 본문 클릭으로도 드롭다운 오픈
- Chat 입력 UX 개선:
  - Enter 전송
  - Shift+Enter 줄바꿈
  - Enter 중복 전송 방지

### 패키징

- 릴리스 아티팩트: `releases/v0.1.1/eclipse-llm-patch-20260328-v0.1.1.zip`
- 무결성 파일: `releases/v0.1.1/checksums.sha256`
- Windows 설치/롤백 스크립트(`install_windows.bat`, `rollback_windows.bat`)를 릴리스 번들에 포함

## v0.1.0 - 2026-03-28

PromptForge 첫 공개 패치 번들입니다.

### 추가 사항

- 프롬프트 기반 프로젝트 생성 워크플로우 (`eclipse-ide__createProject`)
- 명령 기반 프로젝트 생성이 불가능할 때 대체 스캐폴드 자동 생성
- 셸 명령 도구 추가 (`eclipse-runner__runShellCommand`)
- 코드 편집 흐름에서 프로젝트가 없을 경우 자동 생성 (`createFile`/`createDirectories`)
- 상단 메뉴 바로가기 추가: `Window > AI Settings`

### 패키징

- 릴리스 아티팩트: `releases/v0.1.0/eclipse-llm-patch-20260328.zip`
- 무결성 파일: `releases/v0.1.0/checksums.sha256`
