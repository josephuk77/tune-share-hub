import { useEffect, useMemo, useState } from 'react'
import { AppShell } from '../components/layout/AppShell.jsx'
import { Button } from '../components/common/Button.jsx'
import { EmptyState } from '../components/common/EmptyState.jsx'
import { getPublicPlaylists } from '../api/playlistApi.js'
import { useAuth } from '../hooks/useAuth.js'

const dashboardItems = [
  {
    id: 'public-playlists',
    label: '공개 플레이리스트',
    metric: 'Browse',
    tone: 'green',
  },
  {
    id: 'my-playlists',
    label: '내 플레이리스트',
    metric: 'Create',
    tone: 'cyan',
  },
  {
    id: 'track-search',
    label: 'Spotify 곡 검색',
    metric: 'Search',
    tone: 'rose',
  },
]

const searchTypeOptions = [
  { value: 'title', label: '제목' },
  { value: 'author', label: '작성자' },
]

const sortOptions = [
  { value: 'latest', label: '최신순' },
  { value: 'view', label: '조회순' },
  { value: 'like', label: '좋아요순' },
  { value: 'comment', label: '댓글순' },
]

const sizeOptions = [10, 20, 50]

const filterValueParsers = {
  size: Number,
}

export function HomePage({ onSelectPlaylist }) {
  const { isAuthenticated } = useAuth()
  const [keywordInput, setKeywordInput] = useState('')
  const [filters, setFilters] = useState({
    keyword: '',
    searchType: 'title',
    sort: 'latest',
    size: 10,
  })
  const [page, setPage] = useState(0)
  const [playlists, setPlaylists] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [hasMore, setHasMore] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    let isActive = true

    async function fetchPlaylists() {
      setIsLoading(true)
      setErrorMessage('')

      try {
        const response = await getPublicPlaylists({
          ...filters,
          page,
        })

        if (!isActive) {
          return
        }

        const data = Array.isArray(response) ? response : []
        setPlaylists((current) => (page === 0 ? data : [...current, ...data]))
        setHasMore(data.length === filters.size)
      } catch (error) {
        if (isActive) {
          setErrorMessage(error.message ?? '공개 플레이리스트를 불러오지 못했습니다.')
        }
      } finally {
        if (isActive) {
          setIsLoading(false)
        }
      }
    }

    fetchPlaylists()

    return () => {
      isActive = false
    }
  }, [filters, page])

  const activeFilterLabel = useMemo(() => {
    const sortLabel = sortOptions.find((option) => option.value === filters.sort)?.label
    const typeLabel = searchTypeOptions.find((option) => option.value === filters.searchType)?.label
    return filters.keyword ? `${typeLabel} 검색 · ${sortLabel}` : sortLabel
  }, [filters.keyword, filters.searchType, filters.sort])

  function handleSearchSubmit(event) {
    event.preventDefault()
    setPage(0)
    setHasMore(true)
    setFilters((current) => ({
      ...current,
      keyword: keywordInput.trim(),
    }))
  }

  function handleFilterChange(name, value) {
    setPage(0)
    setHasMore(true)
    setFilters((current) => ({
      ...current,
      [name]: filterValueParsers[name]?.(value) ?? value,
    }))
  }

  return (
    <AppShell>
      <main className="workspace">
        <section className="workspace-header">
          <div>
            <p className="eyebrow">Dashboard</p>
            <h1>플레이리스트 작업대</h1>
          </div>
          <span className="status-pill">{isAuthenticated ? '세션 활성' : '둘러보기'}</span>
        </section>

        <section className="dashboard-grid" aria-label="주요 작업">
          {dashboardItems.map((item) => (
            <a className={`dashboard-card ${item.tone}`} href={`#${item.id}`} key={item.id}>
              <span>{item.metric}</span>
              <strong>{item.label}</strong>
            </a>
          ))}
        </section>

        <section className="content-grid">
          <div id="public-playlists" className="panel panel-wide">
            <div className="panel-header">
              <h2>공개 플레이리스트</h2>
              <span>{activeFilterLabel}</span>
            </div>

            <form className="playlist-toolbar" onSubmit={handleSearchSubmit}>
              <label>
                <span>검색</span>
                <input
                  type="search"
                  value={keywordInput}
                  onChange={(event) => setKeywordInput(event.target.value)}
                  placeholder="플레이리스트 검색"
                />
              </label>

              <label>
                <span>대상</span>
                <select
                  value={filters.searchType}
                  onChange={(event) => handleFilterChange('searchType', event.target.value)}
                >
                  {searchTypeOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                <span>정렬</span>
                <select value={filters.sort} onChange={(event) => handleFilterChange('sort', event.target.value)}>
                  {sortOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                <span>개수</span>
                <select value={filters.size} onChange={(event) => handleFilterChange('size', event.target.value)}>
                  {sizeOptions.map((option) => (
                    <option key={option} value={option}>
                      {option}개
                    </option>
                  ))}
                </select>
              </label>

              <Button className="button-primary" disabled={isLoading} type="submit">
                검색
              </Button>
            </form>

            {errorMessage ? <p className="panel-error">{errorMessage}</p> : null}

            {playlists.length > 0 ? (
              <>
                <div className="playlist-list" aria-live="polite">
                  {playlists.map((playlist) => (
                    <PlaylistCard key={playlist.playlistId} onSelectPlaylist={onSelectPlaylist} playlist={playlist} />
                  ))}
                </div>
                <div className="list-actions">
                  <Button className="button-secondary" disabled={isLoading || !hasMore} onClick={() => setPage(page + 1)}>
                    {isLoading ? '불러오는 중' : hasMore ? '더 보기' : '마지막 목록'}
                  </Button>
                </div>
              </>
            ) : (
              <EmptyState
                title={isLoading ? '플레이리스트를 불러오는 중입니다' : '표시할 플레이리스트가 없습니다'}
                description={
                  filters.keyword
                    ? '검색 조건에 맞는 공개 플레이리스트가 없습니다.'
                    : '새로운 공개 플레이리스트를 기다리고 있습니다.'
                }
              />
            )}
          </div>

          <div id="my-playlists" className="panel">
            <div className="panel-header">
              <h2>내 플레이리스트</h2>
              <span>private</span>
            </div>
            <EmptyState
              title="아직 만든 플레이리스트가 없습니다"
              description="나만의 첫 플레이리스트를 준비할 차례입니다."
            />
          </div>
        </section>
      </main>
    </AppShell>
  )
}

function PlaylistCard({ onSelectPlaylist, playlist }) {
  function handleClick(event) {
    if (!onSelectPlaylist) {
      return
    }

    event.preventDefault()
    onSelectPlaylist(playlist.playlistId)
  }

  return (
    <a className="playlist-card" href={`#playlist/${playlist.playlistId}`} onClick={handleClick}>
      <div className="playlist-cover" aria-hidden="true">
        {playlist.coverImageUrl ? <img src={playlist.coverImageUrl} alt="" /> : <span>{playlist.title?.slice(0, 2) ?? ''}</span>}
      </div>

      <div className="playlist-content">
        <div>
          <h3>{playlist.title}</h3>
          <p>{playlist.description || '설명이 없는 공개 플레이리스트입니다.'}</p>
        </div>
        <div className="playlist-meta">
          <span>{playlist.userNickname}</span>
          <span>조회 {formatCount(playlist.viewCount)}</span>
          <span>좋아요 {formatCount(playlist.likeCount)}</span>
          <span>댓글 {formatCount(playlist.commentCount)}</span>
        </div>
      </div>
    </a>
  )
}

function formatCount(value) {
  return Number(value ?? 0).toLocaleString('ko-KR')
}
