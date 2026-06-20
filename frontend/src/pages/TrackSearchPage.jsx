import { useCallback, useEffect, useRef, useState } from 'react'
import { getMySearchHistories, searchSpotifyTracks } from '../api/playlistApi.js'
import { Button } from '../components/common/Button.jsx'
import { EmptyState } from '../components/common/EmptyState.jsx'
import { AppShell } from '../components/layout/AppShell.jsx'
import { useAuth } from '../hooks/useAuth.js'

export function TrackSearchPage() {
  const { isAuthenticated } = useAuth()
  const [trackSearchInput, setTrackSearchInput] = useState('')
  const [trackResults, setTrackResults] = useState([])
  const [isTrackSearching, setIsTrackSearching] = useState(false)
  const [trackSearchMessage, setTrackSearchMessage] = useState('')
  const [searchHistories, setSearchHistories] = useState([])
  const trackSearchRequestRef = useRef(0)
  const isMountedRef = useRef(false)

  useEffect(() => {
    isMountedRef.current = true

    return () => {
      isMountedRef.current = false
      trackSearchRequestRef.current += 1
    }
  }, [])

  const fetchSearchHistories = useCallback(async () => {
    if (!isAuthenticated) {
      return []
    }

    try {
      const response = await getMySearchHistories({ size: 8 })
      return Array.isArray(response) ? response : []
    } catch {
      return []
    }
  }, [isAuthenticated])

  useEffect(() => {
    let isActive = true

    async function loadSearchHistories() {
      const histories = await fetchSearchHistories()
      if (isActive) {
        setSearchHistories(histories)
      }
    }

    loadSearchHistories()

    return () => {
      isActive = false
    }
  }, [fetchSearchHistories])

  async function handleTrackSearchSubmit(event) {
    event.preventDefault()
    await runTrackSearch(trackSearchInput)
  }

  async function runTrackSearch(rawQuery) {
    const query = rawQuery.trim()

    if (!query) {
      setTrackSearchMessage('검색어를 입력해 주세요.')
      return
    }

    setTrackSearchInput(query)
    setIsTrackSearching(true)
    setTrackSearchMessage('')
    const requestId = trackSearchRequestRef.current + 1
    trackSearchRequestRef.current = requestId

    try {
      const response = await searchSpotifyTracks({ query, size: 8 })
      if (!isMountedRef.current || trackSearchRequestRef.current !== requestId) {
        return
      }

      const tracks = Array.isArray(response.tracks) ? response.tracks : []
      setTrackResults(tracks)
      const histories = await fetchSearchHistories()
      if (isMountedRef.current && trackSearchRequestRef.current === requestId) {
        setSearchHistories(histories)
      }
    } catch (error) {
      if (!isMountedRef.current || trackSearchRequestRef.current !== requestId) {
        return
      }

      setTrackSearchMessage(error.message ?? '곡 검색에 실패했습니다.')
    } finally {
      if (isMountedRef.current && trackSearchRequestRef.current === requestId) {
        setIsTrackSearching(false)
      }
    }
  }

  function handleHistorySearch(query) {
    if (isTrackSearching) {
      return
    }
    runTrackSearch(query)
  }

  return (
    <AppShell activePage="track-search">
      <main className="workspace">
        <section className="workspace-header">
          <div>
            <p className="eyebrow">Spotify Search</p>
            <h1>곡 검색</h1>
          </div>
          <span className="status-pill">{trackResults.length}개의 결과</span>
        </section>

        <section className="content-grid page-grid">
          <div id="track-search" className="panel panel-wide">
            <div className="panel-header">
              <h2>Spotify 곡 검색</h2>
              <span>{isAuthenticated ? 'history on' : 'guest search'}</span>
            </div>

            <form className="home-track-search" onSubmit={handleTrackSearchSubmit}>
              <input
                onChange={(event) => setTrackSearchInput(event.target.value)}
                placeholder="곡명 또는 아티스트 검색"
                type="search"
                value={trackSearchInput}
              />
              <Button className="button-secondary" disabled={isTrackSearching} type="submit">
                {isTrackSearching ? '검색 중' : '검색'}
              </Button>
            </form>

            {searchHistories.length > 0 ? (
              <div className="search-history-strip" aria-label="최근 검색어">
                <span>최근 검색어</span>
                <div className="search-history-list">
                  {searchHistories.map((history) => (
                    <button
                      className="search-history-chip"
                      disabled={isTrackSearching}
                      key={history.searchHistoryId}
                      onClick={() => handleHistorySearch(history.query)}
                      title={formatHistoryTitle(history.createdAt)}
                      type="button"
                    >
                      {history.query}
                    </button>
                  ))}
                </div>
              </div>
            ) : null}

            {trackSearchMessage ? <p className="panel-error">{trackSearchMessage}</p> : null}

            {trackResults.length > 0 ? (
              <div className="home-track-list" aria-live="polite">
                {trackResults.map((track) => (
                  <TrackSearchItem key={track.spotifyTrackId} track={track} />
                ))}
              </div>
            ) : (
              <EmptyState title="검색 결과가 없습니다" description="Spotify 카탈로그에서 곡과 아티스트를 찾아볼 수 있습니다." />
            )}
          </div>
        </section>
      </main>
    </AppShell>
  )
}

function TrackSearchItem({ track }) {
  return (
    <article className="home-track-item">
      <div className="home-track-cover" aria-hidden="true">
        {track.albumImageUrl ? <img src={track.albumImageUrl} alt="" /> : <span>{track.title?.slice(0, 1) ?? 'T'}</span>}
      </div>
      <div className="home-track-main">
        <strong>{track.title}</strong>
        <span>{track.artistName}</span>
        <small>
          {track.albumName || '앨범 정보 없음'} · {formatDuration(track.durationMs)}
        </small>
      </div>
      {track.spotifyUrl ? (
        <a className="button button-ghost" href={track.spotifyUrl} rel="noreferrer" target="_blank">
          Spotify
        </a>
      ) : null}
    </article>
  )
}

function formatDuration(durationMs) {
  const totalSeconds = Math.floor(Number(durationMs ?? 0) / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = String(totalSeconds % 60).padStart(2, '0')
  return `${minutes}:${seconds}`
}

const historyDateFormatter = new Intl.DateTimeFormat('ko-KR', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

function formatHistoryTitle(value) {
  if (!value) {
    return '최근 검색어'
  }

  return `검색일 ${historyDateFormatter.format(new Date(value))}`
}
