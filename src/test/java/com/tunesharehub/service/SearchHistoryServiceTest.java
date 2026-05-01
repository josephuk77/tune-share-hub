package com.tunesharehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tunesharehub.dto.SearchHistoryResponse;
import com.tunesharehub.entity.SearchHistory;
import com.tunesharehub.exception.BusinessException;
import com.tunesharehub.mapper.SearchHistoryMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchHistoryServiceTest {

    @Mock
    private SearchHistoryMapper searchHistoryMapper;

    @InjectMocks
    private SearchHistoryService searchHistoryService;

    @Test
    void recordSearchIgnoresAnonymousUser() {
        searchHistoryService.recordSearch(null, "iu");

        verifyNoInteractions(searchHistoryMapper);
    }

    @Test
    void recordSearchTrimsQueryAndSavesHistory() {
        searchHistoryService.recordSearch(1L, "  iu  ");

        ArgumentCaptor<SearchHistory> captor = ArgumentCaptor.forClass(SearchHistory.class);
        verify(searchHistoryMapper).insert(captor.capture());
        SearchHistory searchHistory = captor.getValue();
        assertThat(searchHistory.getUserId()).isEqualTo(1L);
        assertThat(searchHistory.getQuery()).isEqualTo("iu");
    }

    @Test
    void recordSearchRejectsTooLongQuery() {
        String longQuery = "a".repeat(256);

        assertThatThrownBy(() -> searchHistoryService.recordSearch(1L, longQuery))
                .isInstanceOf(BusinessException.class)
                .hasMessage("검색어는 255자를 초과할 수 없습니다.");
    }

    @Test
    void getMySearchHistoriesReturnsRecentQueries() {
        SearchHistory searchHistory = new SearchHistory();
        searchHistory.setSearchHistoryId(10L);
        searchHistory.setUserId(1L);
        searchHistory.setQuery("iu");
        searchHistory.setCreatedAt(LocalDateTime.of(2026, 5, 1, 14, 30));
        when(searchHistoryMapper.findByUserId(1L, 20, 10)).thenReturn(List.of(searchHistory));

        List<SearchHistoryResponse> responses = searchHistoryService.getMySearchHistories(1L, 2, 10);

        verify(searchHistoryMapper).findByUserId(1L, 20, 10);
        assertThat(responses).containsExactly(new SearchHistoryResponse(
                10L,
                "iu",
                LocalDateTime.of(2026, 5, 1, 14, 30)
        ));
    }
}
