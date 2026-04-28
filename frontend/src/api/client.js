import { getAccessToken } from '../utils/tokenStorage.js'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export class ApiError extends Error {
  constructor(message, { status, code, data } = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.data = data
  }
}

export async function apiRequest(path, options = {}) {
  const { skipAuth = false, ...fetchOptions } = options
  const headers = new Headers(options.headers)
  const accessToken = getAccessToken()

  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  if (!skipAuth && accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...fetchOptions,
    headers,
  })

  if (response.status === 204) {
    return null
  }

  const data = await parseResponseBody(response)

  if (!response.ok) {
    const message = data?.message ?? '요청을 처리하지 못했습니다.'
    throw new ApiError(message, {
      status: response.status,
      code: data?.code,
      data,
    })
  }

  return data
}

async function parseResponseBody(response) {
  const body = await response.text()
  if (!body) {
    return null
  }

  const contentType = response.headers.get('Content-Type') ?? ''
  if (!contentType.includes('application/json')) {
    return body
  }

  try {
    return JSON.parse(body)
  } catch {
    throw new ApiError('응답 형식이 올바르지 않습니다.', {
      status: response.status,
      code: 'INVALID_RESPONSE',
    })
  }
}
