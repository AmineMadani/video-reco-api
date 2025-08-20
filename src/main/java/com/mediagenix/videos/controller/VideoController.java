package com.mediagenix.videos.controller;

import com.mediagenix.videos.model.Video;
import com.mediagenix.videos.model.VideoType;
import com.mediagenix.videos.service.VideoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    private final VideoService service;
    public VideoController(VideoService service){ this.service = service; }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Video v){
        try{
            Video created = service.add(v);
            return ResponseEntity.created(URI.create("/api/v1/videos/"+created.getId())).body(created);
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id){
        Optional<Video> v = service.get(id);
        return v.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found"));
    }

    @GetMapping("/search")
    public List<Video> search(@RequestParam String title){ return service.searchByTitleToken(title); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id){
        return service.delete(id) ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found or already deleted");
    }

    @GetMapping("/deleted")
    public List<UUID> deleted(){ return service.deletedIds(); }

    @GetMapping("/movies")
    public List<Video> movies(){ return service.byType(VideoType.MOVIE); }

    @GetMapping("/series")
    public List<Video> series(){ return service.byType(VideoType.SERIES); }

    @GetMapping("/{id}/similar")
    public ResponseEntity<?> similar(@PathVariable UUID id, @RequestParam(defaultValue = "1") int minLabels){
        if(minLabels<1) minLabels=1;
        try{
            return ResponseEntity.ok(service.similar(id, minLabels));
        } catch (NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }
}
