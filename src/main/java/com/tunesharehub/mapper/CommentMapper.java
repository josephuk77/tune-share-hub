package com.tunesharehub.mapper;

import com.tunesharehub.entity.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {

    void insert(Comment comment);

    Comment findById(@Param("commentId") Long commentId);

    Comment findByIdForUpdate(@Param("commentId") Long commentId);

    List<Comment> findByPlaylistId(
            @Param("playlistId") Long playlistId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    int updateOwned(Comment comment);

    int softDeleteOwned(
            @Param("commentId") Long commentId,
            @Param("userId") Long userId
    );
}
