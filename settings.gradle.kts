rootProject.name = "sun-moon-java-common"

// 모듈 하나가 artifact 하나입니다. 가져다 쓰는 쪽은 includeBuild 로 붙이고
// com.sunmoon:<모듈 이름> 으로 의존합니다 — 이름이 곧 좌표라서 폴더 이름과 따로 둡니다.
include("sun-moon-reprocess")
project(":sun-moon-reprocess").projectDir = file("reprocess")
