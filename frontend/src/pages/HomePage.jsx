import { AppShell } from '../components/layout/AppShell.jsx'
import { EmptyState } from '../components/common/EmptyState.jsx'

const dashboardItems = [
  {
    id: 'public-playlists',
    label: '공개 플레이리스트',
    metric: 'Browse',
    tone: 'green',
  },
  {
    id: 'my-playlists',
    label: '내 플레이리스트',
    metric: 'Create',
    tone: 'cyan',
  },
  {
    id: 'track-search',
    label: 'Spotify 곡 검색',
    metric: 'Search',
    tone: 'rose',
  },
]

export function HomePage() {
  return (
    <AppShell>
      <main className="workspace">
        <section className="workspace-header">
          <div>
            <p className="eyebrow">Dashboard</p>
            <h1>플레이리스트 작업대</h1>
          </div>
          <span className="status-pill">세션 활성</span>
        </section>

        <section className="dashboard-grid" aria-label="주요 작업">
          {dashboardItems.map((item) => (
            <a className={`dashboard-card ${item.tone}`} href={`#${item.id}`} key={item.id}>
              <span>{item.metric}</span>
              <strong>{item.label}</strong>
            </a>
          ))}
        </section>

        <section className="content-grid">
          <div id="public-playlists" className="panel">
            <div className="panel-header">
              <h2>공개 플레이리스트</h2>
              <span>latest</span>
            </div>
            <EmptyState
              title="표시할 플레이리스트가 없습니다"
              description="새로운 공개 플레이리스트를 기다리고 있습니다."
            />
          </div>

          <div id="my-playlists" className="panel">
            <div className="panel-header">
              <h2>내 플레이리스트</h2>
              <span>private</span>
            </div>
            <EmptyState
              title="아직 만든 플레이리스트가 없습니다"
              description="나만의 첫 플레이리스트를 준비할 차례입니다."
            />
          </div>
        </section>
      </main>
    </AppShell>
  )
}
