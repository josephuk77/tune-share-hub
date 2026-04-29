import { useEffect, useMemo, useState } from 'react'
import { getPlaylist, getPlaylistComments, getPlaylistTracks, getSimilarPlaylists } from '../api/playlistApi.js'
import { Button } from '../components/common/Button.jsx'
import { EmptyState } from '../components/common/EmptyState.jsx'
import { AppShell } from '../components/layout/AppShell.jsx'

export function PlaylistDetailPage({ currentUser, onBack, onSelectPlaylist, playlistId }) {
  const [detail, setDetail] = useState({
    playlist: null,
    tracks: [],
    comments: [],
    similarPlaylists: [],
  })
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    let isActive = true

    async function fetchDetail() {
      setIsLoading(true)
      setErrorMessage('')

      try {
        const [playlist, tracks, comments, similarPlaylists] = await Promise.all([
          getPlaylist(playlistId),
          getPlaylistTracks(playlistId).catch(() => []),
          getPlaylistComments(playlistId, { size: 20 }).catch(() => []),
          getSimilarPlaylists(playlistId).catch(() => []),
        ])

        if (!isActive) {
          return
        }

        setDetail({
          playlist,
          tracks: Array.isArray(tracks) ? tracks : [],
          comments: Array.isArray(comments) ? comments : [],
          similarPlaylists: Array.isArray(similarPlaylists) ? similarPlaylists : [],
        })
      } catch (error) {
        if (isActive) {
          setErrorMessage(error.message ?? '플레이리스트 상세를 불러오지 못했습니다.')
        }
      } finally {
        if (isActive) {
          setIsLoading(false)
        }
      }
    }

    fetchDetail()

    return () => {
      isActive = false
    }
  }, [playlistId])

  const { comments, playlist, similarPlaylists, tracks } = detail
  const isOwner = currentUser?.userId === playlist?.userId
  const heroInitials = useMemo(() => playlist?.title?.slice(0, 2) ?? 'TS', [playlist?.title])

  return (
    <AppShell>
      <main className="workspace detail-workspace">
        <section className="workspace-header">
          <div>
            <p className="eyebrow">Playlist Detail</p>
            <h1>{playlist?.title ?? '플레이리스트 상세'}</h1>
          </div>
          <Button className="button-secondary" onClick={onBack}>
            목록으로
          </Button>
        </section>

        {errorMessage ? <p className="panel-error detail-error">{errorMessage}</p> : null}

        {isLoading ? (
          <section className="panel detail-loading">
            <EmptyState title="상세 정보를 불러오는 중입니다" description="수록곡과 댓글을 함께 준비하고 있습니다." />
          </section>
        ) : playlist ? (
          <>
            <section className="detail-hero">
              <div className="detail-cover" aria-hidden="true">
                {playlist.coverImageUrl ? <img src={playlist.coverImageUrl} alt="" /> : <span>{heroInitials}</span>}
              </div>

              <div className="detail-copy">
                <div className="detail-title-row">
                  <div>
                    <p className="eyebrow">{playlist.publicYn ? 'Public Playlist' : 'Private Playlist'}</p>
                    <h2>{playlist.title}</h2>
                  </div>
                  {isOwner ? <span className="owner-pill">내 플레이리스트</span> : null}
                </div>

                <p className="detail-description">{playlist.description || '설명이 없는 공개 플레이리스트입니다.'}</p>

                <div className="detail-meta">
                  <span>{playlist.userNickname}</span>
                  <span>생성 {formatDate(playlist.createdAt)}</span>
                  {playlist.originUserNickname ? <span>원작자 {playlist.originUserNickname}</span> : null}
                </div>

                <div className="detail-stats" aria-label="플레이리스트 통계">
                  <Stat label="조회" value={playlist.viewCount} />
                  <Stat label="좋아요" value={playlist.likeCount} />
                  <Stat label="댓글" value={playlist.commentCount} />
                  <Stat label="복사" value={playlist.copyCount} />
                </div>
              </div>
            </section>

            <section className="detail-grid">
              <div className="panel detail-panel">
                <div className="panel-header">
                  <h2>수록곡</h2>
                  <span>{tracks.length} tracks</span>
                </div>
                {tracks.length > 0 ? (
                  <div className="track-list">
                    {tracks.map((track) => (
                      <TrackRow key={track.playlistTrackId} track={track} />
                    ))}
                  </div>
                ) : (
                  <EmptyState title="수록곡이 없습니다" description="아직 이 플레이리스트에 담긴 곡이 없습니다." />
                )}
              </div>

              <div className="panel detail-panel">
                <div className="panel-header">
                  <h2>댓글</h2>
                  <span>{comments.length} comments</span>
                </div>
                {comments.length > 0 ? (
                  <div className="comment-list">
                    {comments.map((comment) => (
                      <CommentItem comment={comment} key={comment.commentId} />
                    ))}
                  </div>
                ) : (
                  <EmptyState title="댓글이 없습니다" description="첫 반응을 기다리고 있습니다." />
                )}
              </div>
            </section>

            <section className="panel detail-panel">
              <div className="panel-header">
                <h2>유사 플레이리스트</h2>
                <span>{similarPlaylists.length} similar</span>
              </div>
              {similarPlaylists.length > 0 ? (
                <div className="similar-list">
                  {similarPlaylists.map((similarPlaylist) => (
                    <button
                      className="similar-card"
                      key={similarPlaylist.playlistId}
                      onClick={() => onSelectPlaylist(similarPlaylist.playlistId)}
                      type="button"
                    >
                      <strong>{similarPlaylist.title}</strong>
                      <span>{similarPlaylist.userNickname}</span>
                    </button>
                  ))}
                </div>
              ) : (
                <EmptyState title="아직 유사한 플레이리스트가 없습니다" description="공통 수록곡이 생기면 여기에 표시됩니다." />
              )}
            </section>
          </>
        ) : null}
      </main>
    </AppShell>
  )
}

function Stat({ label, value }) {
  return (
    <span>
      <strong>{formatCount(value)}</strong>
      {label}
    </span>
  )
}

function TrackRow({ track }) {
  return (
    <article className="track-row">
      <div className="track-cover" aria-hidden="true">
        {track.albumImageUrl ? <img src={track.albumImageUrl} alt="" /> : <span>{track.title?.slice(0, 1) ?? 'T'}</span>}
      </div>
      <div className="track-main">
        <strong>{track.title}</strong>
        <span>{track.artistName}</span>
      </div>
      <span className="track-album">{track.albumName}</span>
      <span className="track-duration">{formatDuration(track.durationMs)}</span>
    </article>
  )
}

function CommentItem({ comment }) {
  return (
    <article className="comment-item">
      <div>
        <strong>{comment.userNickname}</strong>
        <span>{formatDate(comment.createdAt)}</span>
      </div>
      <p>{comment.content}</p>
    </article>
  )
}

function formatCount(value) {
  return Number(value ?? 0).toLocaleString('ko-KR')
}

function formatDate(value) {
  if (!value) {
    return '-'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '-'
  }

  return new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

function formatDuration(durationMs) {
  const totalSeconds = Math.floor(Number(durationMs ?? 0) / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = String(totalSeconds % 60).padStart(2, '0')
  return `${minutes}:${seconds}`
}
