import { AuthProvider } from './contexts/AuthProvider.jsx'
import { useAuth } from './hooks/useAuth.js'
import { HomePage } from './pages/HomePage.jsx'
import { LoginPage } from './pages/LoginPage.jsx'

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  )
}

function AppContent() {
  const { isAuthenticated, isBootstrapping } = useAuth()

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

  return <HomePage />
}

export default App
