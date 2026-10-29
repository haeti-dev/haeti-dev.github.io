# 샘플 글 (개발 전용)

레이아웃 확인용 더미 글이다. `-Phaeti.samples=true`일 때만 번들에 섞이며(`./gradlew :app:web:wasmJsBrowserDevelopmentRun -Phaeti.samples=true`),
배포 빌드(`wasmJsBrowserDistribution`, `prepareGitHubPages`)에는 실리지 않는다. 실제 글은 `content/posts/`에 쓴다.
