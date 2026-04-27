package com.tunesharehub.mapper;

import com.tunesharehub.entity.Playlist;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlaylistMapper {

    void insert(Playlist playlist);

    Playlist findById(@Param("playlistId") Long playlistId);

    Playlist findAccessById(@Param("playlistId") Long playlistId);

    Playlist findByIdForUpdate(@Param("playlistId") Long playlistId);

    List<Playlist> findByUserId(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    int updateOwned(Playlist playlist);

    int softDeleteOwned(
            @Param("playlistId") Long playlistId,
            @Param("userId") Long userId
    );

    void softDeleteTracksByPlaylistId(@Param("playlistId") Long playlistId);

    void softDeleteCommentsByPlaylistId(@Param("playlistId") Long playlistId);

    void softDeleteLikesByPlaylistId(@Param("playlistId") Long playlistId);

    void increaseCommentCount(@Param("playlistId") Long playlistId);

    void decreaseCommentCount(@Param("playlistId") Long playlistId);
}
