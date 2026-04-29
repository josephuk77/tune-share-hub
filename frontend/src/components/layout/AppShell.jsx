import { Button } from '../common/Button.jsx'
import { useAuth } from '../../hooks/useAuth.js'

export function AppShell({ children }) {
  const { isAuthenticated, logout, user } = useAuth()

  return (
    <div className="app-shell">
      <aside className="sidebar" aria-label="주요 메뉴">
        <div className="brand-block">
          <div className="brand-mark" aria-hidden="true">
            TS
          </div>
          <div>
            <strong>Tune Share Hub</strong>
            <span>Playlist Community</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          <a href="#public-playlists">공개 플레이리스트</a>
          <a href="#my-playlists">내 플레이리스트</a>
          <a href="#track-search">곡 검색</a>
        </nav>
      </aside>

      <div className="main-column">
        <header className="top-bar">
          <div>
            <span className="eyebrow">{isAuthenticated ? 'Signed in' : 'Guest'}</span>
            <strong>{user?.nickname ?? '둘러보기 모드'}</strong>
          </div>
          {isAuthenticated ? (
            <Button className="button-secondary" onClick={logout}>
              로그아웃
            </Button>
          ) : (
            <a className="button button-secondary top-login-link" href="#login">
              로그인
            </a>
          )}
        </header>
        {children}
      </div>
    </div>
  )
}
