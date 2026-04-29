import { apiRequest } from './client.js'

export function getPublicPlaylists({ keyword = '', searchType = 'title', sort = 'latest', page = 0, size = 10 } = {}) {
  const params = new URLSearchParams({
    keyword,
    searchType,
    sort,
    page: String(page),
    size: String(size),
  })

  return apiRequest(`/api/playlists?${params.toString()}`)
}
