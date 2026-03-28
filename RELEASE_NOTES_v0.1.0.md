# PromptForge v0.1.0 릴리스 노트

Date: 2026-03-28

## 주요 변경점

- AI가 채팅 흐름에서 직접 프로젝트 생성을 요청할 수 있습니다.
- Eclipse 명령 연결이 불가능한 경우 실패하지 않고 Spring 준비 스캐폴드를 생성합니다.
- `eclipse-runner`가 `runShellCommand`를 통한 로컬 셸 실행을 지원합니다.
- `Window > AI Settings` 메뉴로 모델/키 설정에 더 빠르게 접근할 수 있습니다.

## 아티팩트

- `releases/v0.1.0/eclipse-llm-patch-20260328.zip`

## 보안 / 운영 참고

- API 키는 Eclipse Secure Storage에 저장하는 것을 권장합니다.
- 스크린샷/로그로 키가 노출되었으면 즉시 폐기 후 재발급하세요.

## 업그레이드 안내

- 설치 전에 Eclipse를 종료하세요.
- 번들 내 설치 스크립트를 실행하세요.
- 설치 후 Eclipse를 `-clean` 옵션으로 1회 재시작하세요.
