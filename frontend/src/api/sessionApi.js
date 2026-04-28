import { apiRequest } from './client.js'

export function getCurrentUser() {
  return apiRequest('/api/session/me')
}
