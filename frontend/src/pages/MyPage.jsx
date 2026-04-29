import { useEffect, useState } from 'react'
import { getLikedPlaylists, getMyComments, getMyPlaylists } from '../api/playlistApi.js'
import { EmptyState } from '../components/common/EmptyState.jsx'
import { AppShell } from '../components/layout/AppShell.jsx'

export function MyPage({ currentUser, onSelectPlaylist }) {
  const [myPlaylists, setMyPlaylists] = useState([])
  const [myComments, setMyComments] = useState([])
  const [likedPlaylists, setLikedPlaylists] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const currentUserId = currentUser?.userId

  useEffect(() => {
    let isActive = true

    async function fetchMyActivity() {
      if (!currentUserId) {
        return
      }

      setIsLoading(true)
      setErrorMessage('')

      try {
        const [playlists, comments, liked] = await Promise.all([
          getMyPlaylists({ size: 20 }).catch(() => []),
          getMyComments({ size: 20 }).catch(() => []),
          getLikedPlaylists({ size: 20 }).catch(() => []),
        ])

        if (!isActive) {
          return
        }

        setMyPlaylists(Array.isArray(playlists) ? playlists : [])
        setMyComments(Array.isArray(comments) ? comments : [])
        setLikedPlaylists(Array.isArray(liked) ? liked : [])
      } catch (error) {
        if (isActive) {
          setErrorMessage(error.message ?? '내 활동을 불러오지 못했습니다.')
        }
      } finally {
        if (isActive) {
          setIsLoading(false)
        }
      }
    }

    fetchMyActivity()

    return () => {
      isActive = false
    }
  }, [currentUserId])

  return (
    <AppShell>
      <main className="workspace my-workspace">
        <section className="workspace-header">
          <div>
            <p className="eyebrow">My Page</p>
            <h1>내 활동</h1>
          </div>
          <a className="button button-primary" href="#playlist-builder">
            새 플레이리스트
          </a>
        </section>

        {!currentUser ? (
          <section className="panel my-auth-panel">
            <EmptyState title="로그인이 필요합니다" description="내 플레이리스트와 활동을 보려면 로그인해 주세요." />
            <a className="button button-primary" href="#login">
              로그인
            </a>
          </section>
        ) : (
          <>
            <section className="my-summary-grid" aria-label="내 활동 요약">
              <SummaryCard label="최근 플레이리스트" value={myPlaylists.length} />
              <SummaryCard label="최근 댓글" value={myComments.length} />
              <SummaryCard label="최근 좋아요" value={likedPlaylists.length} />
            </section>

            {errorMessage ? <p className="panel-error">{errorMessage}</p> : null}

            <section className="my-grid">
              <ActivityPanel
                emptyDescription="새 플레이리스트 빌더에서 첫 목록을 만들어 보세요."
                emptyTitle={isLoading ? '내 플레이리스트를 불러오는 중입니다' : '아직 만든 플레이리스트가 없습니다'}
                eyebrow="created"
                title="내 플레이리스트"
              >
                {myPlaylists.length > 0 ? (
                  <div className="my-list">
                    {myPlaylists.map((playlist) => (
                      <PlaylistActivityItem key={playlist.playlistId} onSelectPlaylist={onSelectPlaylist} playlist={playlist} />
                    ))}
                  </div>
                ) : null}
              </ActivityPanel>

              <ActivityPanel
                emptyDescription="상세 화면에서 플레이리스트에 의견을 남기면 여기에 모입니다."
                emptyTitle={isLoading ? '댓글을 불러오는 중입니다' : '아직 남긴 댓글이 없습니다'}
                eyebrow="comments"
                title="내 댓글"
              >
                {myComments.length > 0 ? (
                  <div className="my-list">
                    {myComments.map((comment) => (
                      <CommentActivityItem comment={comment} key={comment.commentId} onSelectPlaylist={onSelectPlaylist} />
                    ))}
                  </div>
                ) : null}
              </ActivityPanel>

              <ActivityPanel
                emptyDescription="마음에 드는 공개 플레이리스트에 좋아요를 남겨보세요."
                emptyTitle={isLoading ? '좋아요한 목록을 불러오는 중입니다' : '좋아요한 플레이리스트가 없습니다'}
                eyebrow="liked"
                title="좋아요한 플레이리스트"
              >
                {likedPlaylists.length > 0 ? (
                  <div className="my-list">
                    {likedPlaylists.map((playlist) => (
                      <PlaylistActivityItem key={playlist.playlistId} onSelectPlaylist={onSelectPlaylist} playlist={playlist} />
                    ))}
                  </div>
                ) : null}
              </ActivityPanel>
            </section>
          </>
        )}
      </main>
    </AppShell>
  )
}

function SummaryCard({ label, value }) {
  return (
    <div className="my-summary-card">
      <strong>{formatCount(value)}</strong>
      <span>{label}</span>
    </div>
  )
}

function ActivityPanel({ children, emptyDescription, emptyTitle, eyebrow, title }) {
  const hasChildren = Boolean(children)

  return (
    <div className="panel my-panel">
      <div className="panel-header">
        <h2>{title}</h2>
        <span>{eyebrow}</span>
      </div>
      {hasChildren ? children : <EmptyState title={emptyTitle} description={emptyDescription} />}
    </div>
  )
}

function PlaylistActivityItem({ onSelectPlaylist, playlist }) {
  return (
    <button className="my-activity-item" onClick={() => onSelectPlaylist(playlist.playlistId)} type="button">
      <div className="my-activity-cover" aria-hidden="true">
        {playlist.coverImageUrl ? <img src={playlist.coverImageUrl} alt="" /> : <span>{playlist.title?.slice(0, 2) || 'TS'}</span>}
      </div>
      <div className="my-activity-main">
        <strong>{playlist.title}</strong>
        <span>{playlist.description || '설명이 없는 플레이리스트입니다.'}</span>
        <small>
          좋아요 {formatCount(playlist.likeCount)} · 댓글 {formatCount(playlist.commentCount)} · 조회 {formatCount(playlist.viewCount)}
        </small>
      </div>
    </button>
  )
}

function CommentActivityItem({ comment, onSelectPlaylist }) {
  return (
    <button className="my-activity-item" onClick={() => onSelectPlaylist(comment.playlistId)} type="button">
      <div className="my-comment-mark" aria-hidden="true">
        C
      </div>
      <div className="my-activity-main">
        <strong>{comment.playlistTitle}</strong>
        <span>{comment.content}</span>
        <small>작성 {formatDate(comment.createdAt)}</small>
      </div>
    </button>
  )
}

function formatCount(value) {
  return Number(value ?? 0).toLocaleString('ko-KR')
}

const dateFormatter = new Intl.DateTimeFormat('ko-KR', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

function formatDate(value) {
  if (!value) {
    return '-'
  }

  return dateFormatter.format(new Date(value))
}
