import { useEffect, useMemo, useState } from 'react'
import { getPublicPlaylistRankings, getPublicPlaylists } from '../api/playlistApi.js'
import { Button } from '../components/common/Button.jsx'
import { EmptyState } from '../components/common/EmptyState.jsx'
import { AppShell } from '../components/layout/AppShell.jsx'
import { useAuth } from '../hooks/useAuth.js'

const searchTypeOptions = [
  { value: 'title', label: '제목' },
  { value: 'description', label: '설명' },
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

export function PublicPlaylistsPage({ onSelectPlaylist }) {
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
  const [rankingPlaylists, setRankingPlaylists] = useState([])
  const [rankingMessage, setRankingMessage] = useState('')

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

  useEffect(() => {
    let isActive = true

    async function fetchRankings() {
      setRankingMessage('')

      try {
        const response = await getPublicPlaylistRankings({ size: 5 })
        if (isActive) {
          setRankingPlaylists(Array.isArray(response) ? response : [])
        }
      } catch (error) {
        if (isActive) {
          setRankingMessage(error.message ?? '랭킹을 불러오지 못했습니다.')
        }
      }
    }

    fetchRankings()

    return () => {
      isActive = false
    }
  }, [])

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
    <AppShell activePage="public-playlists">
      <main className="workspace">
        <section className="workspace-header">
          <div>
            <p className="eyebrow">Explore</p>
            <h1>공개 플레이리스트</h1>
          </div>
          <span className="status-pill">{isAuthenticated ? '세션 활성' : '둘러보기'}</span>
        </section>

        <section className="content-grid page-grid">
          <div id="public-playlists" className="panel panel-wide">
            <div className="panel-header">
              <h2>탐색 목록</h2>
              <span>{activeFilterLabel}</span>
            </div>

            <form className="playlist-toolbar" onSubmit={handleSearchSubmit}>
              <label>
                <span>검색</span>
                <input
                  onChange={(event) => setKeywordInput(event.target.value)}
                  placeholder="플레이리스트 검색"
                  type="search"
                  value={keywordInput}
                />
              </label>

              <label>
                <span>대상</span>
                <select
                  onChange={(event) => handleFilterChange('searchType', event.target.value)}
                  value={filters.searchType}
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
                <select onChange={(event) => handleFilterChange('sort', event.target.value)} value={filters.sort}>
                  {sortOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                <span>개수</span>
                <select onChange={(event) => handleFilterChange('size', event.target.value)} value={filters.size}>
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
                description={
                  filters.keyword
                    ? '검색 조건에 맞는 공개 플레이리스트가 없습니다.'
                    : '새로운 공개 플레이리스트를 기다리고 있습니다.'
                }
                title={isLoading ? '플레이리스트를 불러오는 중입니다' : '표시할 플레이리스트가 없습니다'}
              />
            )}
          </div>

          <div id="playlist-rankings" className="panel">
            <div className="panel-header">
              <h2>인기 랭킹</h2>
              <span>좋아요+댓글</span>
            </div>

            {rankingMessage ? <p className="panel-error">{rankingMessage}</p> : null}

            {rankingPlaylists.length > 0 ? (
              <div className="ranking-list" aria-label="공개 플레이리스트 랭킹">
                {rankingPlaylists.map((playlist, index) => (
                  <RankingItem
                    index={index}
                    key={playlist.playlistId}
                    onSelectPlaylist={onSelectPlaylist}
                    playlist={playlist}
                  />
                ))}
              </div>
            ) : (
              <EmptyState title="랭킹 데이터가 없습니다" description="좋아요와 댓글이 쌓이면 인기 플레이리스트가 표시됩니다." />
            )}
          </div>
        </section>
      </main>
    </AppShell>
  )
}

function RankingItem({ index, onSelectPlaylist, playlist }) {
  function handleClick(event) {
    if (!onSelectPlaylist) {
      return
    }

    event.preventDefault()
    onSelectPlaylist(playlist.playlistId)
  }

  return (
    <a className="ranking-item" href={`#playlist/${playlist.playlistId}`} onClick={handleClick}>
      <span className="ranking-number">{index + 1}</span>
      <span className="ranking-main">
        <strong>{playlist.title}</strong>
        <small>{playlist.userNickname}</small>
      </span>
      <span className="ranking-score">
        {formatCount(Number(playlist.likeCount ?? 0) * 3 + Number(playlist.commentCount ?? 0) * 2)}
      </span>
    </a>
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
