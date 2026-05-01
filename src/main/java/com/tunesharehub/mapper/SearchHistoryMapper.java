package com.tunesharehub.mapper;

import com.tunesharehub.entity.SearchHistory;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SearchHistoryMapper {

    void insert(SearchHistory searchHistory);

    List<SearchHistory> findByUserId(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size
    );
}
