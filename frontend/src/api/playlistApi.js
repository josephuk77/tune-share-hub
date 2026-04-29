import { apiRequest } from './client.js'

export function getPublicPlaylists({ keyword = '', searchType = 'title', sort = 'latest', page = 0, size = 10 } = {}) {
  const params = new URLSearchParams({
    keyword,
    searchType,
    sort,
    page: String(page),
    size: String(size),
  })

  return apiRequest(`/api/playlists?${params.toString()}`, { skipAuth: true })
}

export function getPlaylist(playlistId) {
  return apiRequest(`/api/playlists/${playlistId}`)
}

export function getPlaylistTracks(playlistId) {
  return apiRequest(`/api/playlists/${playlistId}/tracks`)
}

export function getPlaylistComments(playlistId, { page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return apiRequest(`/api/playlists/${playlistId}/comments?${params.toString()}`)
}

export function getSimilarPlaylists(playlistId, { size = 6 } = {}) {
  const params = new URLSearchParams({
    size: String(size),
  })

  return apiRequest(`/api/playlists/${playlistId}/similar?${params.toString()}`)
}

export function getLikedPlaylists({ page = 0, size = 100 } = {}) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return apiRequest(`/api/me/liked-playlists?${params.toString()}`)
}

export function createPlaylistComment(playlistId, content) {
  return apiRequest(`/api/playlists/${playlistId}/comments`, {
    method: 'POST',
    body: JSON.stringify({ content }),
  })
}

export function deletePlaylistComment(commentId) {
  return apiRequest(`/api/comments/${commentId}`, {
    method: 'DELETE',
  })
}

export function likePlaylist(playlistId) {
  return apiRequest(`/api/playlists/${playlistId}/likes`, {
    method: 'POST',
  })
}

export function unlikePlaylist(playlistId) {
  return apiRequest(`/api/playlists/${playlistId}/likes`, {
    method: 'DELETE',
  })
}
