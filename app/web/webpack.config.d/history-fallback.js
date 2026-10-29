// dev 서버 설정 (개발 전용).
if (config.devServer) {
    // SPA 라우팅: /post/* 직접 진입·새로고침 시 index.html 폴백. <base href="/">가 상대 경로를 루트로 되돌린다.
    config.devServer.historyApiFallback = true;
    // 서버 (재)시작 때마다 브라우저 탭이 새로 열리는 것을 막는다. 미리보기는 수동으로 localhost:8080 접속.
    config.devServer.open = false;
}
