import { useEffect, useState } from 'react'
import { AuthProvider } from './contexts/AuthProvider.jsx'
import { useAuth } from './hooks/useAuth.js'
import { LoginPage } from './pages/LoginPage.jsx'
import { MyPage } from './pages/MyPage.jsx'
import { PlaylistBuilderPage } from './pages/PlaylistBuilderPage.jsx'
import { PlaylistDetailPage } from './pages/PlaylistDetailPage.jsx'
import { PublicPlaylistsPage } from './pages/PublicPlaylistsPage.jsx'
import { TrackSearchPage } from './pages/TrackSearchPage.jsx'

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  )
}

function AppContent() {
  const { isAuthenticated, isBootstrapping, user } = useAuth()
  const [hashState, setHashState] = useState(() => getHashState())

  useEffect(() => {
    function handleHashChange() {
      setHashState(getHashState())
    }

    window.addEventListener('hashchange', handleHashChange)
    return () => window.removeEventListener('hashchange', handleHashChange)
  }, [])

  if (isBootstrapping) {
    return (
      <main className="boot-screen" aria-label="앱 초기화">
        <div className="boot-mark" aria-hidden="true">
          TS
        </div>
      </main>
    )
  }

  if (!isAuthenticated && hashState.isLoginRoute) {
    return <LoginPage onClose={() => navigateTo('public-playlists')} />
  }

  if (hashState.isBuilderRoute) {
    return (
      <PlaylistBuilderPage
        currentUser={user}
        onCreated={(playlistId) => {
          navigateTo(`playlist/${playlistId}`)
        }}
      />
    )
  }

  if (hashState.isMyPageRoute) {
    return (
      <MyPage
        currentUser={user}
        onSelectPlaylist={(playlistId) => {
          navigateTo(`playlist/${playlistId}`)
        }}
      />
    )
  }

  if (hashState.isTrackSearchRoute) {
    return <TrackSearchPage />
  }

  if (hashState.selectedPlaylistId) {
    return (
      <PlaylistDetailPage
        currentUser={user}
        onBack={() => {
          navigateTo('public-playlists')
        }}
        onSelectPlaylist={(playlistId) => {
          navigateTo(`playlist/${playlistId}`)
        }}
        playlistId={hashState.selectedPlaylistId}
      />
    )
  }

  return (
    <PublicPlaylistsPage
      onSelectPlaylist={(playlistId) => {
        navigateTo(`playlist/${playlistId}`)
      }}
    />
  )
}

function getHashState() {
  const match = window.location.hash.match(/^#playlist\/(\d+)$/)
  return {
    isBuilderRoute: window.location.hash === '#playlist-builder',
    isLoginRoute: window.location.hash === '#login',
    isMyPageRoute: window.location.hash === '#my-playlists',
    isTrackSearchRoute: window.location.hash === '#track-search',
    selectedPlaylistId: match ? Number(match[1]) : null,
  }
}

function navigateTo(route) {
  window.location.hash = route
}

export default App
