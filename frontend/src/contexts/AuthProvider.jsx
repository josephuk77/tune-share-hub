import { useCallback, useEffect, useMemo, useState } from 'react'
import { login as requestLogin, logout as requestLogout } from '../api/authApi.js'
import { getCurrentUser } from '../api/sessionApi.js'
import { AuthContext } from './AuthContext.js'
import {
  clearTokens,
  getAccessToken,
  getRefreshToken,
  saveTokens,
} from '../utils/tokenStorage.js'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [isBootstrapping, setIsBootstrapping] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    let isMounted = true

    async function restoreSession() {
      if (!getAccessToken()) {
        setIsBootstrapping(false)
        return
      }

      try {
        const currentUser = await getCurrentUser()
        if (isMounted) {
          setUser(currentUser)
        }
      } catch {
        clearTokens()
        if (isMounted) {
          setUser(null)
        }
      } finally {
        if (isMounted) {
          setIsBootstrapping(false)
        }
      }
    }

    restoreSession()

    return () => {
      isMounted = false
    }
  }, [])

  const login = useCallback(async ({ email, password }) => {
    setErrorMessage('')
    const response = await requestLogin(email, password)
    saveTokens(response)
    setUser(response.user)
  }, [])

  const logout = useCallback(async () => {
    const refreshToken = getRefreshToken()
    clearTokens()
    setUser(null)
    setErrorMessage('')

    if (refreshToken) {
      await requestLogout(refreshToken).catch(() => undefined)
    }
  }, [])

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      isBootstrapping,
      errorMessage,
      setErrorMessage,
      login,
      logout,
    }),
    [errorMessage, isBootstrapping, login, logout, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
