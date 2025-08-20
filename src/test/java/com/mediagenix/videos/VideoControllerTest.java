package com.mediagenix.videos;

import com.mediagenix.videos.model.Video;
import com.mediagenix.videos.model.VideoType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private String toJson(Object o) throws Exception {
        return mapper.writeValueAsString(o);
    }



    @Test
    void addBaseVideo_ok() throws Exception {
        Video v = new Video();
        v.setId(UUID.randomUUID());
        v.setTitle("Base content");
        v.setType(VideoType.BASE);
        v.setLabels(List.of("test"));

        mockMvc.perform(post("/api/v1/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(v)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Base content"));
    }

    @Test
    void addMovie_ok() throws Exception {
        Video v = new Video();
        v.setId(UUID.randomUUID());
        v.setTitle("Movie test");
        v.setType(VideoType.MOVIE);
        v.setDirector("Someone");
        v.setReleaseDate(Instant.now());
        v.setLabels(List.of("cinema"));

        mockMvc.perform(post("/api/v1/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(v)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.director").value("Someone"));
    }

    @Test
    void addSeries_ok() throws Exception {
        Video v = new Video();
        v.setId(UUID.randomUUID());
        v.setTitle("Series test");
        v.setType(VideoType.SERIES);
        v.setNumberOfEpisodes(10);
        v.setLabels(List.of("tv"));

        mockMvc.perform(post("/api/v1/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(v)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numberOfEpisodes").value(10));
    }



    @Test
    void addMovieWithoutDirector_badRequest() throws Exception {
        Video v = new Video();
        v.setId(UUID.randomUUID());
        v.setTitle("Invalid Movie");
        v.setType(VideoType.MOVIE);
        v.setReleaseDate(Instant.now());

        mockMvc.perform(post("/api/v1/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(v)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addSeriesWithDirector_badRequest() throws Exception {
        Video v = new Video();
        v.setId(UUID.randomUUID());
        v.setTitle("Invalid Series");
        v.setType(VideoType.SERIES);
        v.setNumberOfEpisodes(10);
        v.setDirector("Not allowed");

        mockMvc.perform(post("/api/v1/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(v)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBaseWithEpisodes_badRequest() throws Exception {
        Video v = new Video();
        v.setId(UUID.randomUUID());
        v.setTitle("Invalid Base");
        v.setType(VideoType.BASE);
        v.setNumberOfEpisodes(5);

        mockMvc.perform(post("/api/v1/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(v)))
                .andExpect(status().isBadRequest());
    }



    @Test
    void searchShortToken_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/videos/search").param("title", "ab"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void searchValidToken_returnsResults() throws Exception {
        mockMvc.perform(get("/api/v1/videos/search").param("title", "Matrix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Matrix"));
    }



    @Test
    void deleteAndDeletedIds() throws Exception {
        UUID id = UUID.randomUUID();
        Video v = new Video();
        v.setId(id);
        v.setTitle("To delete");
        v.setType(VideoType.BASE);

        mockMvc.perform(post("/api/v1/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(v)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/videos/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/videos/deleted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(id.toString()));
    }



    @Test
    void moviesEndpoint_returnsOnlyMovies() throws Exception {
        mockMvc.perform(get("/api/v1/videos/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("MOVIE"));
    }

    @Test
    void seriesEndpoint_returnsOnlySeries() throws Exception {
        mockMvc.perform(get("/api/v1/videos/series"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("SERIES"));
    }



    @Test
    void similarVideos_returnsExpected() throws Exception {
        mockMvc.perform(get("/api/v1/videos/97e343ac-3141-45d1-aff6-68a7465d55ec/similar")
                        .param("minLabels", "1"))
                .andExpect(status().isOk());
    }
}
