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

export function createPlaylist({ coverImageUrl = '', description = '', publicYn = true, title }) {
  return apiRequest('/api/playlists', {
    method: 'POST',
    body: JSON.stringify({
      coverImageUrl,
      description,
      publicYn,
      title,
    }),
  })
}

export function updatePlaylist(playlistId, { coverImageUrl = '', description = '', publicYn = true, title }) {
  return apiRequest(`/api/playlists/${playlistId}`, {
    method: 'PUT',
    body: JSON.stringify({
      coverImageUrl,
      description,
      publicYn,
      title,
    }),
  })
}

export function deletePlaylist(playlistId) {
  return apiRequest(`/api/playlists/${playlistId}`, {
    method: 'DELETE',
  })
}

export function getPlaylistTracks(playlistId) {
  return apiRequest(`/api/playlists/${playlistId}/tracks`)
}

export function addPlaylistTrack(playlistId, track) {
  return apiRequest(`/api/playlists/${playlistId}/tracks`, {
    method: 'POST',
    body: JSON.stringify({
      albumImageUrl: track.albumImageUrl,
      albumName: track.albumName,
      artistName: track.artistName,
      durationMs: track.durationMs,
      previewUrl: track.previewUrl,
      spotifyTrackId: track.spotifyTrackId,
      spotifyUrl: track.spotifyUrl,
      title: track.title,
    }),
  })
}

export function deletePlaylistTrack(playlistId, playlistTrackId) {
  return apiRequest(`/api/playlists/${playlistId}/tracks/${playlistTrackId}`, {
    method: 'DELETE',
  })
}

export function reorderPlaylistTracks(playlistId, playlistTrackIds) {
  return apiRequest(`/api/playlists/${playlistId}/tracks/positions`, {
    method: 'PUT',
    body: JSON.stringify({
      playlistTrackIds,
    }),
  })
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

export function getMyPlaylists({ page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return apiRequest(`/api/me/playlists?${params.toString()}`)
}

export function getMyComments({ page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return apiRequest(`/api/me/comments?${params.toString()}`)
}

export async function isPlaylistLiked(playlistId) {
  const pageSize = 100
  let page = 0

  while (true) {
    const likedPlaylists = await getLikedPlaylists({ page, size: pageSize })
    if (!Array.isArray(likedPlaylists)) {
      return false
    }

    if (likedPlaylists.some((playlist) => playlist.playlistId === Number(playlistId))) {
      return true
    }

    if (likedPlaylists.length < pageSize) {
      return false
    }

    page += 1
  }
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

export function searchSpotifyTracks({ page = 0, query, size = 10 }) {
  const params = new URLSearchParams({
    q: query,
    page: String(page),
    size: String(size),
  })

  return apiRequest(`/api/spotify/search/tracks?${params.toString()}`, { skipAuth: true })
}
