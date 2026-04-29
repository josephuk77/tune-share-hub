import { useEffect, useState } from 'react'
import { AuthProvider } from './contexts/AuthProvider.jsx'
import { useAuth } from './hooks/useAuth.js'
import { HomePage } from './pages/HomePage.jsx'
import { LoginPage } from './pages/LoginPage.jsx'
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
  const [selectedPlaylistId, setSelectedPlaylistId] = useState(() => getPlaylistIdFromHash())

  useEffect(() => {
    function handleHashChange() {
      setSelectedPlaylistId(getPlaylistIdFromHash())
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

  if (!isAuthenticated) {
    return <LoginPage />
  }

  if (selectedPlaylistId) {
    return (
      <PlaylistDetailPage
        currentUser={user}
        onBack={() => {
          window.location.hash = 'public-playlists'
          setSelectedPlaylistId(null)
        }}
        onSelectPlaylist={(playlistId) => {
          window.location.hash = `playlist/${playlistId}`
          setSelectedPlaylistId(playlistId)
        }}
        playlistId={selectedPlaylistId}
      />
    )
  }

  return (
    <HomePage
      onSelectPlaylist={(playlistId) => {
        window.location.hash = `playlist/${playlistId}`
        setSelectedPlaylistId(playlistId)
      }}
    />
  )
}

function getPlaylistIdFromHash() {
  const match = window.location.hash.match(/^#playlist\/([1-9]\d*)$/)
  return match ? Number(match[1]) : null
}

export default App
