package com.mediagenix.videos.repo;

import com.mediagenix.videos.model.Video;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRepo {
    private final Map<UUID, Video> store = new ConcurrentHashMap<>();
    private final LinkedHashSet<UUID> deleted = new LinkedHashSet<>();

    public Optional<Video> find(UUID id){ return Optional.ofNullable(store.get(id)); }
    public void save(Video v){ store.put(v.getId(), v); }
    public List<Video> all(){ return new ArrayList<>(store.values()); }
    public boolean softDelete(UUID id){
        Video v = store.get(id);
        if(v == null || v.isDeleted()) return false;
        v.setDeleted(true);
        deleted.add(id);
        return true;
    }
    public List<UUID> deletedIds(){ return new ArrayList<>(deleted); }
}
