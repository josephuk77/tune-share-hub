import { useEffect, useState } from 'react'
import { AuthProvider } from './contexts/AuthProvider.jsx'
import { useAuth } from './hooks/useAuth.js'
import { HomePage } from './pages/HomePage.jsx'
import { LoginPage } from './pages/LoginPage.jsx'
import { PlaylistBuilderPage } from './pages/PlaylistBuilderPage.jsx'
import { PlaylistDetailPage } from './pages/PlaylistDetailPage.jsx'

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
    return <LoginPage />
  }

  if (hashState.isBuilderRoute) {
    return (
      <PlaylistBuilderPage
        currentUser={user}
        onCreated={(playlistId) => {
          window.location.hash = `playlist/${playlistId}`
        }}
      />
    )
  }

  if (hashState.selectedPlaylistId) {
    return (
      <PlaylistDetailPage
        currentUser={user}
        onBack={() => {
          window.location.hash = 'public-playlists'
        }}
        onSelectPlaylist={(playlistId) => {
          window.location.hash = `playlist/${playlistId}`
        }}
        playlistId={hashState.selectedPlaylistId}
      />
    )
  }

  return (
    <HomePage
      onSelectPlaylist={(playlistId) => {
        window.location.hash = `playlist/${playlistId}`
      }}
    />
  )
}

function getHashState() {
  const match = window.location.hash.match(/^#playlist\/(\d+)$/)
  return {
    isBuilderRoute: window.location.hash === '#playlist-builder',
    isLoginRoute: window.location.hash === '#login',
    selectedPlaylistId: match ? Number(match[1]) : null,
  }
}

export default App
