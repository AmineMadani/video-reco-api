package com.mediagenix.videos.service;

import com.mediagenix.videos.model.Video;
import com.mediagenix.videos.model.VideoType;
import com.mediagenix.videos.repo.InMemoryRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoService {
    private final InMemoryRepo repo = new InMemoryRepo();

    public VideoService(ObjectMapper mapper){
        try {
            ClassPathResource res = new ClassPathResource("videos.json");
            if (res.exists()) {
                try (InputStream is = res.getInputStream()) {
                    // On charge comme Video, puis on infère le type quand il est absent
                    List<Video> initial = mapper.readValue(is, new TypeReference<List<Video>>() {});
                    for (Video v : initial) {
                        inferTypeIfMissing(v);  //
                        validate(v);
                        repo.save(v);
                    }
                }
            }
        } catch (Exception e){
            System.err.println("Failed to preload videos.json: " + e.getMessage());
        }
    }

    public Video add(Video v){
        inferTypeIfMissing(v);  //  (pour les POST sans type)
        validate(v);
        if (repo.find(v.getId()).isPresent()) {
            throw new IllegalStateException("Video already exists with this ID");
        }
        repo.save(v);
        return v;
    }

    public Optional<Video> get(UUID id){ return repo.find(id).filter(v -> !v.isDeleted()); }

    public List<Video> searchByTitleToken(String token){
        String t = token == null ? "" : token.trim().toLowerCase();
        if (t.length() < 3) return List.of();
        return repo.all().stream()
                .filter(v -> !v.isDeleted())
                .filter(v -> Arrays.stream(v.getTitle().toLowerCase().split("\\s+"))
                        .anyMatch(w -> w.contains(t)))
                .collect(Collectors.toList());
    }

    public boolean delete(UUID id){ return repo.softDelete(id); }
    public List<UUID> deletedIds(){ return repo.deletedIds(); }

    public List<Video> byType(VideoType type){
        return repo.all().stream()
                .filter(v -> !v.isDeleted())
                .filter(v -> v.getType() == type)
                .collect(Collectors.toList());
    }

    public List<Video> similar(UUID id, int minCommon){
        Video origin = repo.find(id).orElseThrow(() -> new NoSuchElementException("Not found"));
        if (origin.isDeleted()) return List.of();

        Set<String> originLabels = origin.getLabels() == null
                ? Set.of()
                : origin.getLabels().stream().map(String::toLowerCase).collect(Collectors.toSet());

        return repo.all().stream()
                .filter(v -> !v.isDeleted())
                .filter(v -> !v.getId().equals(id))
                .filter(v -> v.getLabels() != null)
                .filter(v -> v.getLabels().stream()
                        .map(String::toLowerCase)
                        .filter(originLabels::contains)
                        .count() >= minCommon)
                .collect(Collectors.toList());
    }

    /**
     * - Movie : director + releaseDate présents
     * - Series: numberOfEpisodes présent
     * - Sinon : Base
     * Si les champs Movie et Series sont mélangés -> IllegalArgumentException
     */
    private void inferTypeIfMissing(Video v) {
        boolean hasMovieFields  = (v.getDirector() != null) || (v.getReleaseDate() != null);
        boolean hasSeriesField  = (v.getNumberOfEpisodes() != null);

        if (v.getType() == null) {
            if (hasMovieFields && hasSeriesField) {
                throw new IllegalArgumentException("Fields must be exclusive: movie and series attributes cannot be combined");
            } else if (hasMovieFields) {
                // exiger les deux si on prétend être un film
                if (v.getDirector() == null || v.getReleaseDate() == null) {
                    throw new IllegalArgumentException("Movie requires both director and releaseDate");
                }
                v.setType(VideoType.MOVIE);
            } else if (hasSeriesField) {
                v.setType(VideoType.SERIES);
            } else {
                v.setType(VideoType.BASE);
            }
        }
    }

    private void validate(Video v){
        if (v.getId() == null) throw new IllegalArgumentException("id required");
        if (v.getTitle() == null || v.getTitle().isBlank()) throw new IllegalArgumentException("title required");
        if (v.getLabels() == null) v.setLabels(new ArrayList<>());

        if (v.getType() == VideoType.MOVIE) {
            if (v.getDirector() == null || v.getDirector().isBlank())
                throw new IllegalArgumentException("director required for MOVIE");
            if (v.getReleaseDate() == null)
                throw new IllegalArgumentException("releaseDate required for MOVIE");
            if (v.getNumberOfEpisodes() != null)
                throw new IllegalArgumentException("numberOfEpisodes must be null for MOVIE");
        } else if (v.getType() == VideoType.SERIES) {
            if (v.getNumberOfEpisodes() == null || v.getNumberOfEpisodes() <= 0)
                throw new IllegalArgumentException("numberOfEpisodes>0 required for SERIES");
            if (v.getDirector() != null || v.getReleaseDate() != null)
                throw new IllegalArgumentException("director/releaseDate must be null for SERIES");
        } else {
            // Pour BASE, pas de champs spécifiques
            if (v.getDirector() != null || v.getReleaseDate() != null || v.getNumberOfEpisodes() != null) {
                throw new IllegalArgumentException("BASE cannot have movie/series fields");
            }
        }
    }
}
