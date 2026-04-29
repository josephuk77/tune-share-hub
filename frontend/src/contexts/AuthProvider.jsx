import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  login as requestLogin,
  logout as requestLogout,
  refresh as requestRefresh,
} from '../api/authApi.js'
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

  const refreshSession = useCallback(async () => {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      throw new Error('refresh token이 없습니다.')
    }

    const response = await requestRefresh(refreshToken)
    saveTokens(response)
    return response.user
  }, [])

  useEffect(() => {
    let isMounted = true

    async function restoreSession() {
      if (!getAccessToken() && !getRefreshToken()) {
        setIsBootstrapping(false)
        return
      }

      try {
        let currentUser
        if (getAccessToken()) {
          try {
            currentUser = await getCurrentUser()
          } catch {
            currentUser = await refreshSession()
          }
        } else {
          currentUser = await refreshSession()
        }

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
  }, [refreshSession])

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
