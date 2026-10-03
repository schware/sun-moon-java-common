# sun-moon-java-common

sun-moon Java 서비스들(Device Server · Order · APP-SERVER …)이 같이 쓰는 **틀**. 2026-10-03 시작.
설계: umbrella `sun-moon-java-platform/docs/design/reprocess-library.md`.

**규칙: 모듈은 프레임워크를 모릅니다.** 순수 Java 21 + slf4j-api 뿐입니다 — Netty 로 도는 Device Server 와
Spring 서비스가 둘 다 가져다 쓰기 때문입니다. (Device Server 의 `sun-moon-platform-core` 는 Netty·MyBatis 를
끌고 오는 커널이라 여기와 다릅니다.)

| 모듈 | 좌표 | 무엇 |
|---|---|---|
| `reprocess/` | `com.sunmoon:sun-moon-reprocess:0.1.0` | 실패 재처리의 틀 — `FailedItem` · `Reprocessor` · `ReprocessService` · `ReprocessViews`(HTTP 응답 모양) |

## 원칙 — 틀은 같이, 처리는 각자

실패를 들고 있는 것도, 다시 하는 것도 **실패한 서비스 자신**입니다(다시 하는 법을 아는 쪽이 주인).
이 라이브러리는 그 실패를 보여 주고 다시 시키는 **모양**만 정합니다. 그래서 BO 재처리 화면이 세 서비스를
같은 타입으로 읽습니다.

## 가져다 쓰는 법

각 서비스 저장소에 submodule 로 두고 Gradle 의 included build 로 붙입니다(Device Server 가 core 를 쓰는 방식과 같음).

```bash
git submodule add git@github.com:schware/sun-moon-java-common.git java-common
```

```kotlin
// settings.gradle.kts
includeBuild("java-common")

// build.gradle.kts
dependencies {
    implementation("com.sunmoon:sun-moon-reprocess:0.1.0")
}
```

Dockerfile 은 `COPY java-common java-common` 이 필요합니다(빌드가 그 폴더를 읽음). 서버의 `deploy.sh` 는
`git submodule update --init` 으로 받습니다.

나중에 Forgejo 가 주 저장소가 되면 그 패키지 저장소에 올리고 submodule 대신 버전 의존성으로 바꿉니다.

## 시험

```bash
./gradlew test
```
