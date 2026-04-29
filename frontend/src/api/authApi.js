import { apiRequest } from './client.js'

export function login(email, password) {
  return apiRequest('/api/auth/login', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify({ email, password }),
  })
}

export function refresh(refreshToken) {
  return apiRequest('/api/auth/refresh', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify({ refreshToken }),
  })
}

export function logout(refreshToken) {
  return apiRequest('/api/auth/logout', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify({ refreshToken }),
  })
}
