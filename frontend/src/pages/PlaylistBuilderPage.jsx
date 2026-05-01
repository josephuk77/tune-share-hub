import { useState } from 'react'
import { addPlaylistTrack, createPlaylist, searchSpotifyTracks } from '../api/playlistApi.js'
import { Button } from '../components/common/Button.jsx'
import { EmptyState } from '../components/common/EmptyState.jsx'
import { AppShell } from '../components/layout/AppShell.jsx'

export function PlaylistBuilderPage({ currentUser, onCreated }) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [isPublic, setIsPublic] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [searchResults, setSearchResults] = useState([])
  const [selectedTracks, setSelectedTracks] = useState([])
  const [isSearching, setIsSearching] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [searchMessage, setSearchMessage] = useState('')
  const [saveMessage, setSaveMessage] = useState('')

  async function handleSearchSubmit(event) {
    event.preventDefault()
    const query = searchQuery.trim()
    if (!query) {
      setSearchMessage('검색어를 입력해 주세요.')
      return
    }

    setIsSearching(true)
    setSearchMessage('')

    try {
      const response = await searchSpotifyTracks({ query })
      const tracks = Array.isArray(response.tracks) ? response.tracks : []
      setSearchResults(tracks)
    } catch (error) {
      setSearchMessage(error.message ?? '곡 검색에 실패했습니다.')
    } finally {
      setIsSearching(false)
    }
  }

  async function handleSave() {
    const trimmedTitle = title.trim()
    if (!currentUser) {
      setSaveMessage('플레이리스트를 만들려면 먼저 로그인해 주세요.')
      return
    }
    if (!trimmedTitle) {
      setSaveMessage('제목을 입력해 주세요.')
      return
    }

    setIsSaving(true)
    setSaveMessage('')

    let createdPlaylist
    try {
      createdPlaylist = await createPlaylist({
        coverImageUrl: selectedTracks[0]?.albumImageUrl ?? '',
        description: description.trim(),
        publicYn: isPublic,
        title: trimmedTitle,
      })

      for (const track of selectedTracks) {
        await addPlaylistTrack(createdPlaylist.playlistId, track)
      }

      onCreated(createdPlaylist.playlistId)
    } catch (error) {
      const baseMessage = error.message ?? '플레이리스트를 저장하지 못했습니다.'
      if (createdPlaylist?.playlistId) {
        setSaveMessage(`${baseMessage} 생성된 플레이리스트는 상세 화면에서 확인할 수 있습니다.`)
      } else {
        setSaveMessage(baseMessage)
      }
    } finally {
      setIsSaving(false)
    }
  }

  function addTrack(track) {
    setSaveMessage('')
    setSelectedTracks((currentTracks) => {
      if (currentTracks.some((selectedTrack) => selectedTrack.spotifyTrackId === track.spotifyTrackId)) {
        return currentTracks
      }
      return [...currentTracks, track]
    })
  }

  function removeTrack(spotifyTrackId) {
    setSelectedTracks((currentTracks) => currentTracks.filter((track) => track.spotifyTrackId !== spotifyTrackId))
  }

  function moveTrack(spotifyTrackId, direction) {
    setSelectedTracks((currentTracks) => {
      const index = currentTracks.findIndex((track) => track.spotifyTrackId === spotifyTrackId)
      const nextIndex = index + direction
      if (index < 0 || nextIndex < 0 || nextIndex >= currentTracks.length) {
        return currentTracks
      }

      const nextTracks = [...currentTracks]
      const [track] = nextTracks.splice(index, 1)
      nextTracks.splice(nextIndex, 0, track)
      return nextTracks
    })
  }

  return (
    <AppShell>
      <main className="workspace builder-workspace">
        <section className="workspace-header">
          <div>
            <p className="eyebrow">Playlist Builder</p>
            <h1>새 플레이리스트 만들기</h1>
          </div>
          <a className="button button-secondary" href="#public-playlists">
            목록으로
          </a>
        </section>

        {!currentUser ? (
          <section className="panel builder-auth-panel">
            <EmptyState title="로그인이 필요합니다" description="플레이리스트를 만들려면 로그인 후 다시 시도해 주세요." />
            <a className="button button-primary" href="#login">
              로그인
            </a>
          </section>
        ) : (
          <section className="builder-grid">
            <div className="panel builder-panel">
              <div className="panel-header">
                <h2>기본 정보</h2>
                <span>{isPublic ? 'public' : 'private'}</span>
              </div>

              <div className="builder-form">
                <label>
                  <span>제목</span>
                  <input
                    maxLength={100}
                    onChange={(event) => setTitle(event.target.value)}
                    placeholder="예: 새벽 집중 믹스"
                    value={title}
                  />
                </label>

                <label>
                  <span>설명</span>
                  <textarea
                    onChange={(event) => setDescription(event.target.value)}
                    placeholder="어떤 순간에 어울리는 플레이리스트인지 적어보세요."
                    rows={6}
                    value={description}
                  />
                </label>

                <label className="builder-toggle">
                  <input checked={isPublic} onChange={(event) => setIsPublic(event.target.checked)} type="checkbox" />
                  <span>공개 플레이리스트로 게시</span>
                </label>

                {saveMessage ? <p className="panel-error">{saveMessage}</p> : null}

                <Button className="button-primary" disabled={isSaving} onClick={handleSave}>
                  {isSaving ? '저장 중' : `플레이리스트 저장 · ${selectedTracks.length}곡`}
                </Button>
              </div>
            </div>

            <div className="panel builder-panel">
              <div className="panel-header">
                <h2>Spotify 곡 검색</h2>
                <span>{searchResults.length}개의 결과</span>
              </div>

              <form className="builder-search" onSubmit={handleSearchSubmit}>
                <input
                  onChange={(event) => setSearchQuery(event.target.value)}
                  placeholder="곡명 또는 아티스트 검색"
                  type="search"
                  value={searchQuery}
                />
                <Button className="button-secondary" disabled={isSearching} type="submit">
                  {isSearching ? '검색 중' : '검색'}
                </Button>
              </form>

              {searchMessage ? <p className="panel-error">{searchMessage}</p> : null}

              {searchResults.length > 0 ? (
                <div className="builder-track-list" aria-live="polite">
                  {searchResults.map((track) => (
                    <TrackCandidate
                      isSelected={selectedTracks.some((selectedTrack) => selectedTrack.spotifyTrackId === track.spotifyTrackId)}
                      key={track.spotifyTrackId}
                      onAdd={addTrack}
                      track={track}
                    />
                  ))}
                </div>
              ) : (
                <EmptyState title="검색 결과가 없습니다" description="Spotify 카탈로그에서 곡을 찾아 추가할 수 있습니다." />
              )}
            </div>

            <div className="panel builder-panel builder-selected-panel">
              <div className="panel-header">
                <h2>담은 곡</h2>
                <span>{selectedTracks.length}곡</span>
              </div>

              {selectedTracks.length > 0 ? (
                <div className="builder-selected-list">
                  {selectedTracks.map((track, index) => (
                    <SelectedTrack
                      index={index}
                      isFirst={index === 0}
                      isLast={index === selectedTracks.length - 1}
                      key={track.spotifyTrackId}
                      onMove={moveTrack}
                      onRemove={removeTrack}
                      track={track}
                    />
                  ))}
                </div>
              ) : (
                <EmptyState title="아직 담은 곡이 없습니다" description="검색 결과에서 곡을 추가하면 저장 순서대로 표시됩니다." />
              )}
            </div>
          </section>
        )}
      </main>
    </AppShell>
  )
}

function TrackCandidate({ isSelected, onAdd, track }) {
  return (
    <article className="builder-track-card">
      <TrackArtwork track={track} />
      <TrackText track={track} />
      <Button className="button-secondary" disabled={isSelected} onClick={() => onAdd(track)}>
        {isSelected ? '추가됨' : '추가'}
      </Button>
    </article>
  )
}

function SelectedTrack({ index, isFirst, isLast, onMove, onRemove, track }) {
  return (
    <article className="builder-selected-track">
      <span className="builder-track-index">{index + 1}</span>
      <TrackArtwork track={track} />
      <TrackText track={track} />
      <div className="builder-track-actions">
        <Button className="button-ghost" disabled={isFirst} onClick={() => onMove(track.spotifyTrackId, -1)}>
          위
        </Button>
        <Button className="button-ghost" disabled={isLast} onClick={() => onMove(track.spotifyTrackId, 1)}>
          아래
        </Button>
        <Button className="button-ghost" onClick={() => onRemove(track.spotifyTrackId)}>
          제거
        </Button>
      </div>
    </article>
  )
}

function TrackArtwork({ track }) {
  return (
    <div className="builder-track-cover" aria-hidden="true">
      {track.albumImageUrl ? <img src={track.albumImageUrl} alt="" /> : <span>{track.title?.slice(0, 1) ?? 'T'}</span>}
    </div>
  )
}

function TrackText({ track }) {
  return (
    <div className="builder-track-text">
      <strong>{track.title}</strong>
      <span>{track.artistName}</span>
      <small>
        {track.albumName || '앨범 정보 없음'} · {formatDuration(track.durationMs)}
      </small>
      {track.spotifyUrl ? (
        <a className="builder-track-source" href={track.spotifyUrl} rel="noreferrer" target="_blank">
          Spotify에서 보기
        </a>
      ) : null}
    </div>
  )
}

function formatDuration(durationMs) {
  const totalSeconds = Math.floor(Number(durationMs ?? 0) / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = String(totalSeconds % 60).padStart(2, '0')
  return `${minutes}:${seconds}`
}
