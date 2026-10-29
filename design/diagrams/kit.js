// ?theme=dark를 읽어 다크 토큰을 켜고, svg.links 안의 경로를 노드 위치에 맞춰 그린다.
//
// <path data-from="a" data-to="b" data-shape="under" data-via="label">
//   under: a와 b의 아래 가운데에서 내려가 via 요소의 세로 가운데에서 만난다.
//   drop:  a의 아래 가운데에서 내려가 a와 b 사이 틈에서 꺾어 b의 위 가운데로 들어간다(화살표).
const theme = new URLSearchParams(location.search).get('theme');
if (theme === 'dark') document.documentElement.dataset.theme = 'dark';

function box(id, origin) {
  const r = document.getElementById(id).getBoundingClientRect();
  return { cx: r.left + r.width / 2 - origin.left, top: r.top - origin.top, bottom: r.bottom - origin.top, mid: r.top + r.height / 2 - origin.top };
}

function drawLinks() {
  for (const svg of document.querySelectorAll('svg.links')) {
    const origin = svg.getBoundingClientRect();
    for (const path of svg.querySelectorAll('path[data-from]')) {
      const a = box(path.dataset.from, origin), b = box(path.dataset.to, origin);
      if (path.dataset.shape === 'under') {
        const y = box(path.dataset.via, origin).mid;
        path.setAttribute('d', `M ${a.cx} ${a.bottom} L ${a.cx} ${y} L ${b.cx} ${y} L ${b.cx} ${b.bottom}`);
      } else {
        const y = (a.bottom + b.top) / 2;
        path.setAttribute('d', `M ${a.cx} ${a.bottom} L ${a.cx} ${y} L ${b.cx} ${y} L ${b.cx} ${b.top - 2}`);
        path.setAttribute('marker-end', 'url(#arrow)');
      }
    }
  }
}

document.fonts.ready.then(() => {
  drawLinks();
  document.body.dataset.ready = 'true';
});
