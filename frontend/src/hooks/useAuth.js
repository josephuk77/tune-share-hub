import { useContext } from 'react'
import { AuthContext } from '../contexts/AuthContext.js'

export function useAuth() {
  const authContext = useContext(AuthContext)
  if (!authContext) {
    throw new Error('AuthProvider 내부에서만 useAuth를 사용할 수 있습니다.')
  }
  return authContext
}
