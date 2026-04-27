package com.tunesharehub.mapper;

import com.tunesharehub.entity.PlaylistLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlaylistLikeMapper {

    void insert(PlaylistLike playlistLike);

    PlaylistLike findByPlaylistIdAndUserId(
            @Param("playlistId") Long playlistId,
            @Param("userId") Long userId
    );

    int recover(
            @Param("playlistId") Long playlistId,
            @Param("userId") Long userId
    );

    int softDelete(
            @Param("playlistId") Long playlistId,
            @Param("userId") Long userId
    );
}
