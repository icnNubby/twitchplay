package ru.nubby.playstream.utils;

import android.util.Log;

import java.util.HashMap;

import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import io.lindstrom.m3u8.parser.PlaylistParserException;
import ru.nubby.playstream.domain.entities.Quality;

/**
 * Helper class to make non standard parsing.
 * Uses <a href = "https://github.com/carlanton/m3u8-parser">third party library.</a>
 */
public class M3U8Parser {

    /**
     * Parses kinda non standart twitch M3U8 playlist.
     * Maps it to HashMap indexed by {@link Quality}, valued by url of HLS resource.
     */
    public static HashMap<Quality, String> parseTwitchApiResponse(String playlist) {
        String[] lines = playlist.split("\n");
        StringBuilder fixedPlaylist = new StringBuilder();
        for (String line : lines)
            if (!line.contains("#EXT-X-TWITCH-INFO")) // yeye
                fixedPlaylist.append(line).append("\n");
        playlist = fixedPlaylist.toString();
        MasterPlaylist parsed = null;
        MasterPlaylistParser parser = new MasterPlaylistParser();
        try {
            MasterPlaylist parsedPlaylist = parser.readPlaylist(playlist);
            parsed = MasterPlaylist.builder()
                    .from(parsedPlaylist)
                    .version(2)
                    .build();
        } catch (PlaylistParserException e) {
            Log.e("M3U8Parser", "Error while parsing", e);
            e.printStackTrace();
        }

        HashMap<Quality, String> qualityList = new HashMap<>();
        if (parsed != null) {
            for (Variant quality : parsed.variants()) {
                String qualityFormatted = quality.video().toString();
                if (qualityFormatted.contains("Optional"))
                    qualityFormatted = qualityFormatted.substring(9, qualityFormatted.length() - 1);
                if (qualityFormatted.toLowerCase().equals("chunked"))
                    qualityFormatted = "default";
                if (qualityFormatted.contains("default")) {
                    qualityList.put(Quality.DEFAULT, quality.uri());
                }
                if (qualityFormatted.contains("360p30")) {
                    qualityList.put(Quality.QUALITY360, quality.uri());
                }
                if (qualityFormatted.contains("480p30")) {
                    qualityList.put(Quality.QUALITY480, quality.uri());
                }
                if (qualityFormatted.contains("720p30")) {
                    qualityList.put(Quality.QUALITY72030, quality.uri());
                }
                if (qualityFormatted.contains("720p60")) {
                    qualityList.put(Quality.QUALITY72060, quality.uri());
                }
                if (qualityFormatted.contains("1080p30")) {
                    qualityList.put(Quality.QUALITY108030, quality.uri());
                }
                if (qualityFormatted.contains("1080p60")) {
                    qualityList.put(Quality.QUALITY108060, quality.uri());
                }
                if (qualityFormatted.contains("audio_only")) {
                    qualityList.put(Quality.QUALITY_AUDIO_ONLY, quality.uri());
                }
                //todo add smaller stuff
            }
        }
        return qualityList;
    }

}
