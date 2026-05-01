package com.tunesharehub.controller;

import com.tunesharehub.auth.AuthUser;
import com.tunesharehub.auth.CurrentUser;
import com.tunesharehub.dto.SearchHistoryResponse;
import com.tunesharehub.service.SearchHistoryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/me/search-histories")
public class SearchHistoryController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    private final SearchHistoryService searchHistoryService;

    public SearchHistoryController(SearchHistoryService searchHistoryService) {
        this.searchHistoryService = searchHistoryService;
    }

    @GetMapping
    public List<SearchHistoryResponse> getMySearchHistories(
            @CurrentUser AuthUser authUser,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) @Min(0) @Max(10000) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) @Min(1) @Max(50) int size
    ) {
        return searchHistoryService.getMySearchHistories(authUser.userId(), page, size);
    }
}
