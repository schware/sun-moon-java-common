// sun-moon 서비스들이 같이 쓰는 틀 (umbrella docs/design/reprocess-library.md).
//
// 규칙: 모듈은 프레임워크를 모릅니다 — Spring 도 Netty 도 아닌 순수 Java + slf4j-api.
// Device Server(Netty)와 Spring 서비스가 둘 다 가져다 쓰기 때문입니다.
subprojects {
    apply(plugin = "java-library")

    group = "com.sunmoon"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        "testImplementation"(platform("org.junit:junit-bom:5.11.0"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
