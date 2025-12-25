package com.gateway.application.search;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.application.embedding.EmbeddingClient;
import com.gateway.application.entry.Entry;
import com.gateway.application.entry.EntryRepository;
import com.gateway.application.utils.VectorUtils;

import embedding.proto.EmbeddingResponse;
import jakarta.validation.Valid;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final EntryRepository entryRepository;
    private final EmbeddingClient embeddingClient;
    private final JedisPooled jedisPooled;
    // private final Logger log = LoggerFactory.getLogger(SearchController.class);

    public SearchController(EntryRepository entryRepository, EmbeddingClient embeddingClient, JedisPooled jedisPooled) {
        this.entryRepository = entryRepository;
        this.embeddingClient = embeddingClient;
        this.jedisPooled = jedisPooled;
    }

    @PostMapping("")
    ResponseEntity<SearchOutcome> searchOutcome(@Valid @RequestBody Search search) {
        // embed keyword
        EmbeddingResponse response = embeddingClient.getEmbeddingResponse(search.keyword());
        if (response.getStatus() == EmbeddingResponse.Status.FAILED) {
            return ResponseEntity.internalServerError().build();
        }
        
        // query keyword embedding
        byte[] embedding = VectorUtils.floatListToByteArr(response.getEmbedding().getValuesList());
        Query query = new Query("*=>[KNN $K @embedding $BLOB AS score]")
                .addParam("K", search.k())
                .addParam("BLOB", embedding)
                .returnFields("id", "score")
                .setSortBy("score", true)
                .dialect(2);

        // process results
        SearchOutcome searchOutcome = new SearchOutcome();
        SearchResult result = this.jedisPooled.ftSearch("vector", query);
        for (Document doc : result.getDocuments()) {
            UUID id = UUID.fromString(((String) doc.get("id")).substring(7));
            Float score = Float.parseFloat((String) doc.get("score"));
            Optional<Entry> entrySearch = findEntry(id);
            Entry entry = entrySearch.orElseThrow();
            searchOutcome.addResult(entry, score);
        }
        return ResponseEntity.ok(searchOutcome);
    }

    Optional<Entry> findEntry(UUID id) {
        return this.entryRepository.findById(id);
    }
}
