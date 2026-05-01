import { Button } from '../common/Button.jsx'
import { useAuth } from '../../hooks/useAuth.js'

const navItems = [
  { href: '#public-playlists', id: 'public-playlists', label: '공개 플레이리스트' },
  { href: '#track-search', id: 'track-search', label: '곡 검색' },
  { href: '#playlist-builder', id: 'playlist-builder', label: '새 플레이리스트' },
  { href: '#my-playlists', id: 'my-playlists', label: '내 플레이리스트' },
]

export function AppShell({ activePage, children }) {
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
          {navItems.map((item) => (
            <a
              aria-current={activePage === item.id ? 'page' : undefined}
              className={activePage === item.id ? 'active' : undefined}
              href={item.href}
              key={item.id}
            >
              {item.label}
            </a>
          ))}
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
