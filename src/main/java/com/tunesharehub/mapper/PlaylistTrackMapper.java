package com.tunesharehub.mapper;

import com.tunesharehub.entity.PlaylistTrack;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlaylistTrackMapper {

    void insert(PlaylistTrack playlistTrack);

    PlaylistTrack findById(
            @Param("playlistId") Long playlistId,
            @Param("playlistTrackId") Long playlistTrackId
    );

    List<PlaylistTrack> findByPlaylistId(@Param("playlistId") Long playlistId);

    int softDelete(
            @Param("playlistId") Long playlistId,
            @Param("playlistTrackId") Long playlistTrackId
    );

    int countActiveByIds(
            @Param("playlistId") Long playlistId,
            @Param("playlistTrackIds") List<Long> playlistTrackIds
    );

    int countActiveByPlaylistId(@Param("playlistId") Long playlistId);

    void moveActiveTracksToTemporaryPositions(@Param("playlistId") Long playlistId);

    void updatePositions(
            @Param("playlistId") Long playlistId,
            @Param("playlistTrackIds") List<Long> playlistTrackIds
    );
}
