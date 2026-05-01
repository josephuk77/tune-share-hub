package com.tunesharehub.entity;

import java.time.LocalDateTime;

public class SearchHistory {

    private Long searchHistoryId;
    private Long userId;
    private String query;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public Long getSearchHistoryId() {
        return searchHistoryId;
    }

    public void setSearchHistoryId(Long searchHistoryId) {
        this.searchHistoryId = searchHistoryId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
