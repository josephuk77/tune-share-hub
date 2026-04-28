package com.tunesharehub.entity;

import java.time.LocalDateTime;

public class Playlist {

    private Long playlistId;
    private Long userId;
    private String userNickname;
    private String title;
    private String description;
    private String coverImageUrl;
    private String publicYn;
    private Long originPlaylistId;
    private String originUserNickname;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long copyCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(Long playlistId) {
        this.playlistId = playlistId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getPublicYn() {
        return publicYn;
    }

    public void setPublicYn(String publicYn) {
        this.publicYn = publicYn;
    }

    public Long getOriginPlaylistId() {
        return originPlaylistId;
    }

    public void setOriginPlaylistId(Long originPlaylistId) {
        this.originPlaylistId = originPlaylistId;
    }

    public String getOriginUserNickname() {
        return originUserNickname;
    }

    public void setOriginUserNickname(String originUserNickname) {
        this.originUserNickname = originUserNickname;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getCopyCount() {
        return copyCount;
    }

    public void setCopyCount(Long copyCount) {
        this.copyCount = copyCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
