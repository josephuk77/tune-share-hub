package com.tunesharehub.service;

import com.tunesharehub.dto.SearchHistoryResponse;
import com.tunesharehub.entity.SearchHistory;
import com.tunesharehub.exception.BusinessException;
import com.tunesharehub.mapper.SearchHistoryMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchHistoryService {

    private static final int MAX_QUERY_LENGTH = 255;

    private final SearchHistoryMapper searchHistoryMapper;

    public SearchHistoryService(SearchHistoryMapper searchHistoryMapper) {
        this.searchHistoryMapper = searchHistoryMapper;
    }

    @Transactional
    public void recordSearch(Long userId, String query) {
        if (userId == null) {
            return;
        }

        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery.isBlank()) {
            return;
        }

        SearchHistory searchHistory = new SearchHistory();
        searchHistory.setUserId(userId);
        searchHistory.setQuery(normalizedQuery);
        searchHistoryMapper.insert(searchHistory);
    }

    @Transactional(readOnly = true)
    public List<SearchHistoryResponse> getMySearchHistories(Long userId, int page, int size) {
        int offset = page * size;
        return searchHistoryMapper.findByUserId(userId, offset, size)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }

        String normalizedQuery = query.trim();
        if (normalizedQuery.length() > MAX_QUERY_LENGTH) {
            throw new BusinessException("INVALID_SEARCH_QUERY", "검색어는 255자를 초과할 수 없습니다.");
        }
        return normalizedQuery;
    }

    private SearchHistoryResponse toResponse(SearchHistory searchHistory) {
        return new SearchHistoryResponse(
                searchHistory.getSearchHistoryId(),
                searchHistory.getQuery(),
                searchHistory.getCreatedAt()
        );
    }
}
