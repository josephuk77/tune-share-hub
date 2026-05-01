import { useEffect, useMemo, useRef, useState } from 'react'
import {
  addPlaylistTrack,
  copyPlaylist,
  createPlaylistComment,
  deletePlaylist,
  deletePlaylistComment,
  deletePlaylistTrack,
  getPlaylist,
  getPlaylistComments,
  getPlaylistTracks,
  getSimilarPlaylists,
  isPlaylistLiked,
  likePlaylist,
  reorderPlaylistTracks,
  searchSpotifyTracks,
  unlikePlaylist,
  updatePlaylistComment,
  updatePlaylist as requestPlaylistUpdate,
} from '../api/playlistApi.js'
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
  const [commentContent, setCommentContent] = useState('')
  const [actionMessage, setActionMessage] = useState('')
  const [actionMessageType, setActionMessageType] = useState('error')
  const [isCommentSubmitting, setIsCommentSubmitting] = useState(false)
  const [deletingCommentId, setDeletingCommentId] = useState(null)
  const [editingCommentId, setEditingCommentId] = useState(null)
  const [commentEditContent, setCommentEditContent] = useState('')
  const [updatingCommentId, setUpdatingCommentId] = useState(null)
  const [hasLiked, setHasLiked] = useState(false)
  const [isLikeSubmitting, setIsLikeSubmitting] = useState(false)
  const [isEditingPlaylist, setIsEditingPlaylist] = useState(false)
  const [editTitle, setEditTitle] = useState('')
  const [editDescription, setEditDescription] = useState('')
  const [editPublicYn, setEditPublicYn] = useState(true)
  const [isPlaylistSaving, setIsPlaylistSaving] = useState(false)
  const [isPlaylistCopying, setIsPlaylistCopying] = useState(false)
  const [isPlaylistDeleting, setIsPlaylistDeleting] = useState(false)
  const [trackSearchQuery, setTrackSearchQuery] = useState('')
  const [trackSearchResults, setTrackSearchResults] = useState([])
  const [trackMessage, setTrackMessage] = useState('')
  const [trackMessageType, setTrackMessageType] = useState('error')
  const [isTrackSearching, setIsTrackSearching] = useState(false)
  const [addingTrackId, setAddingTrackId] = useState(null)
  const [deletingTrackId, setDeletingTrackId] = useState(null)
  const [reorderingTrackId, setReorderingTrackId] = useState(null)
  const isMountedRef = useRef(false)
  const activePlaylistIdRef = useRef(playlistId)
  const copyRedirectTimeoutRef = useRef(null)
  const currentUserId = currentUser?.userId
  activePlaylistIdRef.current = playlistId

  useEffect(() => {
    isMountedRef.current = true

    return () => {
      isMountedRef.current = false
      window.clearTimeout(copyRedirectTimeoutRef.current)
    }
  }, [])

  useEffect(() => {
    let isActive = true

    async function fetchDetail() {
      setIsLoading(true)
      setErrorMessage('')
      setCommentContent('')
      setCommentEditContent('')
      setEditingCommentId(null)
      clearActionMessage()
      clearTrackMessage()
      setTrackSearchQuery('')
      setTrackSearchResults([])
      setHasLiked(false)
      setIsEditingPlaylist(false)
      setIsPlaylistCopying(false)

      try {
        const [playlist, tracks, comments, similarPlaylists, liked] = await Promise.all([
          getPlaylist(playlistId),
          getPlaylistTracks(playlistId).catch(() => []),
          getPlaylistComments(playlistId, { size: 20 }).catch(() => []),
          getSimilarPlaylists(playlistId).catch(() => []),
          currentUserId ? isPlaylistLiked(playlistId).catch(() => false) : Promise.resolve(false),
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
        setEditTitle(playlist.title ?? '')
        setEditDescription(playlist.description ?? '')
        setEditPublicYn(Boolean(playlist.publicYn))
        setHasLiked(liked)
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
      window.clearTimeout(copyRedirectTimeoutRef.current)
    }
  }, [currentUserId, playlistId])

  const { comments, playlist, similarPlaylists, tracks } = detail
  const isOwner = currentUserId === playlist?.userId
  const heroInitials = useMemo(() => playlist?.title?.slice(0, 2) || 'TS', [playlist?.title])
  const trimmedComment = commentContent.trim()
  const trimmedEditTitle = editTitle.trim()
  const trimmedTrackSearchQuery = trackSearchQuery.trim()
  const isTrackMutating = Boolean(addingTrackId || deletingTrackId || reorderingTrackId)

  function clearActionMessage() {
    setActionMessage('')
    setActionMessageType('error')
  }

  function showActionMessage(message, type = 'error') {
    setActionMessage(message)
    setActionMessageType(type)
  }

  function clearTrackMessage() {
    setTrackMessage('')
    setTrackMessageType('error')
  }

  function showTrackMessage(message, type = 'error') {
    setTrackMessage(message)
    setTrackMessageType(type)
  }

  async function handleLikeToggle() {
    if (!playlist) {
      return
    }

    if (!currentUser) {
      showActionMessage('좋아요를 남기려면 먼저 로그인해 주세요.')
      return
    }

    setIsLikeSubmitting(true)
    clearActionMessage()

    try {
      const response = hasLiked ? await unlikePlaylist(playlist.playlistId) : await likePlaylist(playlist.playlistId)
      setHasLiked(Boolean(response.liked))
      updatePlaylist({
        likeCount: response.likeCount,
      })
    } catch (error) {
      showActionMessage(error.message ?? '좋아요 상태를 변경하지 못했습니다.')
    } finally {
      setIsLikeSubmitting(false)
    }
  }

  function openPlaylistEditForm() {
    if (!playlist) {
      return
    }

    setEditTitle(playlist.title ?? '')
    setEditDescription(playlist.description ?? '')
    setEditPublicYn(Boolean(playlist.publicYn))
    clearActionMessage()
    setIsEditingPlaylist(true)
  }

  async function handlePlaylistUpdate(event) {
    event.preventDefault()

    if (!playlist || !isOwner || !trimmedEditTitle) {
      return
    }

    setIsPlaylistSaving(true)
    clearActionMessage()

    try {
      const updatedPlaylist = await requestPlaylistUpdate(playlist.playlistId, {
        coverImageUrl: playlist.coverImageUrl ?? '',
        description: editDescription.trim(),
        publicYn: editPublicYn,
        title: trimmedEditTitle,
      })
      updatePlaylist(updatedPlaylist ?? {
        description: editDescription.trim(),
        publicYn: editPublicYn,
        title: trimmedEditTitle,
      })
      setIsEditingPlaylist(false)
      showActionMessage('플레이리스트 정보를 수정했습니다.', 'success')
    } catch (error) {
      showActionMessage(error.message ?? '플레이리스트 정보를 수정하지 못했습니다.')
    } finally {
      setIsPlaylistSaving(false)
    }
  }

  async function handlePlaylistDelete() {
    if (!playlist || !isOwner || !window.confirm('플레이리스트를 삭제할까요? 삭제 후에는 목록에서 사라집니다.')) {
      return
    }

    setIsPlaylistDeleting(true)
    clearActionMessage()

    try {
      await deletePlaylist(playlist.playlistId)
      setIsPlaylistDeleting(false)
      onBack()
    } catch (error) {
      showActionMessage(error.message ?? '플레이리스트를 삭제하지 못했습니다.')
      setIsPlaylistDeleting(false)
    }
  }

  async function handlePlaylistCopy() {
    if (!playlist || isOwner) {
      return
    }

    if (!currentUser) {
      showActionMessage('플레이리스트를 복사하려면 먼저 로그인해 주세요.')
      return
    }

    setIsPlaylistCopying(true)
    clearActionMessage()

    const sourcePlaylistId = playlist.playlistId

    try {
      const copiedPlaylist = await copyPlaylist(sourcePlaylistId)
      if (!isMountedRef.current || activePlaylistIdRef.current !== sourcePlaylistId) {
        return
      }

      updatePlaylist({
        copyCount: Number(playlist.copyCount ?? 0) + 1,
      })
      showActionMessage('플레이리스트를 복사했습니다. 복사본으로 이동합니다.', 'success')
      window.clearTimeout(copyRedirectTimeoutRef.current)
      copyRedirectTimeoutRef.current = window.setTimeout(() => {
        onSelectPlaylist(copiedPlaylist.playlistId)
      }, 500)
    } catch (error) {
      showActionMessage(error.message ?? '플레이리스트를 복사하지 못했습니다.')
      setIsPlaylistCopying(false)
    }
  }

  async function handleCommentSubmit(event) {
    event.preventDefault()

    if (!playlist || !trimmedComment) {
      return
    }

    if (!currentUser) {
      showActionMessage('댓글을 작성하려면 먼저 로그인해 주세요.')
      return
    }

    setIsCommentSubmitting(true)
    clearActionMessage()

    try {
      const createdComment = await createPlaylistComment(playlist.playlistId, trimmedComment)
      setDetail((currentDetail) => ({
        ...currentDetail,
        playlist: currentDetail.playlist
          ? {
              ...currentDetail.playlist,
              commentCount: Number(currentDetail.playlist.commentCount ?? 0) + 1,
            }
          : currentDetail.playlist,
        comments: [...currentDetail.comments, createdComment],
      }))
      setCommentContent('')
    } catch (error) {
      showActionMessage(error.message ?? '댓글을 등록하지 못했습니다.')
    } finally {
      setIsCommentSubmitting(false)
    }
  }

  async function handleCommentDelete(commentId) {
    if (!window.confirm('댓글을 삭제할까요?')) {
      return
    }

    setDeletingCommentId(commentId)
    clearActionMessage()

    try {
      await deletePlaylistComment(commentId)
      setDetail((currentDetail) => ({
        ...currentDetail,
        playlist: currentDetail.playlist
          ? {
              ...currentDetail.playlist,
              commentCount: Math.max(Number(currentDetail.playlist.commentCount ?? 0) - 1, 0),
            }
          : currentDetail.playlist,
        comments: currentDetail.comments.filter((comment) => comment.commentId !== commentId),
      }))
      if (editingCommentId === commentId) {
        setEditingCommentId(null)
        setCommentEditContent('')
      }
    } catch (error) {
      showActionMessage(error.message ?? '댓글을 삭제하지 못했습니다.')
    } finally {
      setDeletingCommentId(null)
    }
  }

  function handleCommentEditStart(comment) {
    setEditingCommentId(comment.commentId)
    setCommentEditContent(comment.content ?? '')
    clearActionMessage()
  }

  function handleCommentEditCancel() {
    setEditingCommentId(null)
    setCommentEditContent('')
  }

  async function handleCommentUpdate(event, commentId) {
    event.preventDefault()
    const trimmedEditContent = commentEditContent.trim()
    if (!trimmedEditContent) {
      return
    }

    setUpdatingCommentId(commentId)
    clearActionMessage()

    try {
      const updatedComment = await updatePlaylistComment(commentId, trimmedEditContent)
      setDetail((currentDetail) => ({
        ...currentDetail,
        comments: currentDetail.comments.map((comment) => (comment.commentId === commentId ? updatedComment : comment)),
      }))
      setEditingCommentId(null)
      setCommentEditContent('')
      showActionMessage('댓글을 수정했습니다.', 'success')
    } catch (error) {
      showActionMessage(error.message ?? '댓글을 수정하지 못했습니다.')
    } finally {
      setUpdatingCommentId(null)
    }
  }

  async function handleTrackSearchSubmit(event) {
    event.preventDefault()

    if (!trimmedTrackSearchQuery) {
      showTrackMessage('검색어를 입력해 주세요.')
      return
    }

    setIsTrackSearching(true)
    clearTrackMessage()

    try {
      const response = await searchSpotifyTracks({ query: trimmedTrackSearchQuery })
      const results = Array.isArray(response.tracks) ? response.tracks : []
      setTrackSearchResults(results)
      if (results.length === 0) {
        showTrackMessage('검색 결과가 없습니다.')
      }
    } catch (error) {
      showTrackMessage(error.message ?? '곡 검색에 실패했습니다.')
    } finally {
      setIsTrackSearching(false)
    }
  }

  async function handleTrackAdd(track) {
    if (!playlist || !isOwner) {
      return
    }

    if (tracks.some((playlistTrack) => playlistTrack.spotifyTrackId === track.spotifyTrackId)) {
      showTrackMessage('이미 수록된 곡입니다.')
      return
    }

    setAddingTrackId(track.spotifyTrackId)
    clearTrackMessage()

    try {
      const createdTrack = await addPlaylistTrack(playlist.playlistId, track)
      setDetail((currentDetail) => ({
        ...currentDetail,
        tracks: [...currentDetail.tracks, createdTrack],
      }))
      showTrackMessage('수록곡을 추가했습니다.', 'success')
    } catch (error) {
      showTrackMessage(error.message ?? '수록곡을 추가하지 못했습니다.')
    } finally {
      setAddingTrackId(null)
    }
  }

  async function handleTrackDelete(track) {
    if (!playlist || !isOwner || !window.confirm('이 곡을 플레이리스트에서 제거할까요?')) {
      return
    }

    setDeletingTrackId(track.playlistTrackId)
    clearTrackMessage()

    try {
      await deletePlaylistTrack(playlist.playlistId, track.playlistTrackId)
      setDetail((currentDetail) => ({
        ...currentDetail,
        tracks: currentDetail.tracks.filter((playlistTrack) => playlistTrack.playlistTrackId !== track.playlistTrackId),
      }))
      showTrackMessage('수록곡을 제거했습니다.', 'success')
    } catch (error) {
      showTrackMessage(error.message ?? '수록곡을 제거하지 못했습니다.')
    } finally {
      setDeletingTrackId(null)
    }
  }

  async function handleTrackMove(track, direction) {
    if (!playlist || !isOwner) {
      return
    }

    const currentIndex = tracks.findIndex((playlistTrack) => playlistTrack.playlistTrackId === track.playlistTrackId)
    const nextIndex = currentIndex + direction
    if (currentIndex < 0 || nextIndex < 0 || nextIndex >= tracks.length) {
      return
    }

    const nextTracks = [...tracks]
    const [movedTrack] = nextTracks.splice(currentIndex, 1)
    nextTracks.splice(nextIndex, 0, movedTrack)

    setReorderingTrackId(track.playlistTrackId)
    clearTrackMessage()

    try {
      const reorderedTracks = await reorderPlaylistTracks(
        playlist.playlistId,
        nextTracks.map((playlistTrack) => playlistTrack.playlistTrackId)
      )
      setDetail((currentDetail) => ({
        ...currentDetail,
        tracks: Array.isArray(reorderedTracks) ? reorderedTracks : nextTracks,
      }))
      showTrackMessage('수록곡 순서를 변경했습니다.', 'success')
    } catch (error) {
      showTrackMessage(error.message ?? '수록곡 순서를 변경하지 못했습니다.')
    } finally {
      setReorderingTrackId(null)
    }
  }

  function updatePlaylist(updates) {
    setDetail((currentDetail) => ({
      ...currentDetail,
      playlist: currentDetail.playlist
        ? {
            ...currentDetail.playlist,
            ...updates,
          }
        : currentDetail.playlist,
    }))
  }

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

                <div className="detail-actions">
                  <Button className={hasLiked ? 'button-primary' : 'button-secondary'} disabled={isLikeSubmitting} onClick={handleLikeToggle}>
                    {isLikeSubmitting ? '처리 중' : hasLiked ? '좋아요 취소' : '좋아요'}
                  </Button>
                  {isOwner ? (
                    <>
                      <Button className="button-secondary" disabled={isPlaylistDeleting} onClick={openPlaylistEditForm}>
                        수정
                      </Button>
                      <Button className="button-danger" disabled={isPlaylistDeleting} onClick={handlePlaylistDelete}>
                        {isPlaylistDeleting ? '삭제 중' : '삭제'}
                      </Button>
                    </>
                  ) : (
                    <Button className="button-secondary" disabled={isPlaylistCopying} onClick={handlePlaylistCopy}>
                      {isPlaylistCopying ? '복사 중' : '복사'}
                    </Button>
                  )}
                </div>
              </div>
            </section>

            {actionMessage ? (
              <p className={`${actionMessageType === 'success' ? 'panel-success' : 'panel-error'} detail-action-message`}>{actionMessage}</p>
            ) : null}

            {isOwner && isEditingPlaylist ? (
              <section className="panel detail-edit-panel">
                <div className="panel-header">
                  <h2>플레이리스트 정보 수정</h2>
                  <span>{trimmedEditTitle.length}/100</span>
                </div>
                <form className="detail-edit-form" onSubmit={handlePlaylistUpdate}>
                  <label>
                    제목
                    <input maxLength={100} onChange={(event) => setEditTitle(event.target.value)} required value={editTitle} />
                  </label>
                  <label>
                    설명
                    <textarea onChange={(event) => setEditDescription(event.target.value)} rows={4} value={editDescription} />
                  </label>
                  <label className="detail-toggle">
                    <input checked={editPublicYn} onChange={(event) => setEditPublicYn(event.target.checked)} type="checkbox" />
                    공개 플레이리스트
                  </label>
                  <div className="detail-edit-actions">
                    <Button disabled={isPlaylistSaving || !trimmedEditTitle} type="submit">
                      {isPlaylistSaving ? '저장 중' : '저장'}
                    </Button>
                    <Button className="button-secondary" disabled={isPlaylistSaving} onClick={() => setIsEditingPlaylist(false)}>
                      취소
                    </Button>
                  </div>
                </form>
              </section>
            ) : null}

            <section className="detail-grid">
              <div className="panel detail-panel">
                <div className="panel-header">
                  <h2>수록곡</h2>
                  <span>{tracks.length}곡</span>
                </div>
                {isOwner ? (
                  <div className="track-manager">
                    <form className="track-search-form" onSubmit={handleTrackSearchSubmit}>
                      <input
                        onChange={(event) => setTrackSearchQuery(event.target.value)}
                        placeholder="곡명 또는 아티스트 검색"
                        type="search"
                        value={trackSearchQuery}
                      />
                      <Button className="button-secondary" disabled={isTrackSearching} type="submit">
                        {isTrackSearching ? '검색 중' : '검색'}
                      </Button>
                    </form>
                    {trackMessage ? (
                      <p className={`${trackMessageType === 'success' ? 'panel-success' : 'panel-error'} track-manager-message`}>
                        {trackMessage}
                      </p>
                    ) : null}
                    {trackSearchResults.length > 0 ? (
                      <div className="track-search-results">
                        {trackSearchResults.map((track) => (
                          <TrackCandidate
                            isAdding={addingTrackId === track.spotifyTrackId}
                            isDisabled={isTrackMutating}
                            isSelected={tracks.some((playlistTrack) => playlistTrack.spotifyTrackId === track.spotifyTrackId)}
                            key={track.spotifyTrackId}
                            onAdd={handleTrackAdd}
                            track={track}
                          />
                        ))}
                      </div>
                    ) : null}
                  </div>
                ) : null}
                {tracks.length > 0 ? (
                  <div className="track-list">
                    {tracks.map((track, index) => (
                      <TrackRow
                        canManage={isOwner}
                        isBusy={isTrackMutating}
                        isDeleting={deletingTrackId === track.playlistTrackId}
                        isFirst={index === 0}
                        isLast={index === tracks.length - 1}
                        isReordering={reorderingTrackId === track.playlistTrackId}
                        key={track.playlistTrackId}
                        onDelete={handleTrackDelete}
                        onMove={handleTrackMove}
                        track={track}
                      />
                    ))}
                  </div>
                ) : (
                  <EmptyState title="수록곡이 없습니다" description="아직 이 플레이리스트에 담긴 곡이 없습니다." />
                )}
              </div>

              <div className="panel detail-panel">
                <div className="panel-header">
                  <h2>댓글</h2>
                  <span>{formatCount(playlist.commentCount)}개</span>
                </div>
                <form className="comment-form" onSubmit={handleCommentSubmit}>
                  <label htmlFor="playlist-comment">댓글 작성</label>
                  <textarea
                    id="playlist-comment"
                    maxLength={1000}
                    onChange={(event) => setCommentContent(event.target.value)}
                    placeholder={currentUser ? '플레이리스트에 대한 감상을 남겨보세요.' : '로그인 후 댓글을 작성할 수 있습니다.'}
                    rows={4}
                    value={commentContent}
                  />
                  <div className="comment-form-actions">
                    <span>{trimmedComment.length}/1000</span>
                    <Button disabled={isCommentSubmitting || !trimmedComment} type="submit">
                      {isCommentSubmitting ? '등록 중' : '댓글 등록'}
                    </Button>
                  </div>
                </form>
                {comments.length > 0 ? (
                  <div className="comment-list">
                    {comments.map((comment) => (
                      <CommentItem
                        comment={comment}
                        commentEditContent={commentEditContent}
                        currentUser={currentUser}
                        deletingCommentId={deletingCommentId}
                        editingCommentId={editingCommentId}
                        key={comment.commentId}
                        onDelete={handleCommentDelete}
                        onEditCancel={handleCommentEditCancel}
                        onEditChange={setCommentEditContent}
                        onEditStart={handleCommentEditStart}
                        onUpdate={handleCommentUpdate}
                        updatingCommentId={updatingCommentId}
                      />
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
                <span>{similarPlaylists.length}개</span>
              </div>
              {similarPlaylists.length > 0 ? (
                <div className="similar-list">
                  {similarPlaylists.map((similarPlaylist) => (
                    <a
                      className="similar-card"
                      href={`#playlist/${similarPlaylist.playlistId}`}
                      key={similarPlaylist.playlistId}
                      onClick={(event) => {
                        if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey || event.button !== 0) {
                          return
                        }

                        event.preventDefault()
                        onSelectPlaylist(similarPlaylist.playlistId)
                      }}
                    >
                      <strong>{similarPlaylist.title}</strong>
                      <span>{similarPlaylist.userNickname}</span>
                    </a>
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

function TrackCandidate({ isAdding, isDisabled, isSelected, onAdd, track }) {
  return (
    <article className="track-candidate">
      <TrackArtwork track={track} />
      <TrackText track={track} />
      <Button className="button-secondary" disabled={isDisabled || isAdding || isSelected} onClick={() => onAdd(track)}>
        {isAdding ? '추가 중' : isSelected ? '추가됨' : '추가'}
      </Button>
    </article>
  )
}

function TrackRow({ canManage, isBusy, isDeleting, isFirst, isLast, isReordering, onDelete, onMove, track }) {
  return (
    <article className="track-row">
      <TrackArtwork track={track} />
      <TrackText track={track} />
      <span className="track-album">{track.albumName}</span>
      <span className="track-duration">{formatDuration(track.durationMs)}</span>
      {canManage ? (
        <div className="track-actions">
          <Button className="button-ghost" disabled={isBusy || isDeleting || isFirst || isReordering} onClick={() => onMove(track, -1)}>
            위
          </Button>
          <Button className="button-ghost" disabled={isBusy || isDeleting || isLast || isReordering} onClick={() => onMove(track, 1)}>
            아래
          </Button>
          <Button className="button-ghost" disabled={isBusy || isDeleting || isReordering} onClick={() => onDelete(track)}>
            {isDeleting ? '제거 중' : '제거'}
          </Button>
        </div>
      ) : null}
    </article>
  )
}

function TrackArtwork({ track }) {
  return (
    <div className="track-cover" aria-hidden="true">
      {track.albumImageUrl ? <img src={track.albumImageUrl} alt="" /> : <span>{track.title?.slice(0, 1) ?? 'T'}</span>}
    </div>
  )
}

function TrackText({ track }) {
  return (
    <div className="track-main">
      <strong>{track.title}</strong>
      <span>{track.artistName}</span>
    </div>
  )
}

function CommentItem({
  comment,
  commentEditContent,
  currentUser,
  deletingCommentId,
  editingCommentId,
  onDelete,
  onEditCancel,
  onEditChange,
  onEditStart,
  onUpdate,
  updatingCommentId,
}) {
  const isCommentOwner = currentUser?.userId === comment.userId
  const isDeleting = deletingCommentId === comment.commentId
  const isEditing = editingCommentId === comment.commentId
  const isUpdating = updatingCommentId === comment.commentId
  const trimmedEditContent = commentEditContent.trim()
  const isEditUnchanged = trimmedEditContent === (comment.content ?? '').trim()

  return (
    <article className="comment-item">
      <div className="comment-header">
        <div className="comment-meta">
          <strong>{comment.userNickname}</strong>
          <span>
            {formatDate(comment.createdAt)}
            {comment.updatedAt ? ` · 수정 ${formatDate(comment.updatedAt)}` : ''}
          </span>
        </div>
        {isCommentOwner ? (
          <div className="comment-actions">
            <Button className="button-ghost" disabled={isDeleting || isEditing} onClick={() => onEditStart(comment)}>
              수정
            </Button>
            <Button className="button-ghost" disabled={isDeleting || isUpdating} onClick={() => onDelete(comment.commentId)}>
              {isDeleting ? '삭제 중' : '삭제'}
            </Button>
          </div>
        ) : null}
      </div>
      {isEditing ? (
        <form className="comment-edit-form" onSubmit={(event) => onUpdate(event, comment.commentId)}>
          <textarea
            maxLength={1000}
            onChange={(event) => onEditChange(event.target.value)}
            rows={4}
            value={commentEditContent}
          />
          <div className="comment-form-actions">
            <span>{commentEditContent.trim().length}/1000</span>
            <div className="comment-actions">
              <Button className="button-secondary" disabled={isUpdating} onClick={onEditCancel}>
                취소
              </Button>
              <Button disabled={isUpdating || !trimmedEditContent || isEditUnchanged} type="submit">
                {isUpdating ? '저장 중' : '저장'}
              </Button>
            </div>
          </div>
        </form>
      ) : (
        <p>{comment.content}</p>
      )}
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
