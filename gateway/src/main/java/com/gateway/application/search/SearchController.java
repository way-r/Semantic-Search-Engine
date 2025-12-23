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

import com.gateway.application.embed.EmbedClient;
import com.gateway.application.entry.Entry;
import com.gateway.application.entry.EntryRepository;
import com.gateway.application.utils.VectorUtils;

import embed.proto.EmbedResponse;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final EmbedClient embedClient;
    private final JedisPooled jedisPooled;
    private final EntryRepository entryRepository;
    private final Logger log = LoggerFactory.getLogger(SearchController.class);

    public SearchController(EmbedClient embedClient, JedisPooled jedisPooled, EntryRepository entryRepository) {
        this.embedClient = embedClient;
        this.jedisPooled = jedisPooled;
        this.entryRepository = entryRepository;
    }

    @PostMapping("")
    ResponseEntity<SearchOutcome> searchOutcome(@RequestBody Search search) {
        SearchOutcome searchOutcome = new SearchOutcome();

        try {
            EmbedResponse embedResponse = embedClient.getEmbedResponse(search.keyword());
            
            if (embedResponse.getStatus() == EmbedResponse.Status.FAILED) {
                log.error("Embed client responded with a failed result");
            }
            else {
                byte[] searchEmbed = VectorUtils.standardizeEmbed(embedResponse.getEmbed().getValuesList());

                Query query = new Query("*=>[KNN $K @embed $BLOB AS score]")
                        .addParam("K", search.k())
                        .addParam("BLOB", searchEmbed)
                        .returnFields("uuid", "score")
                        .setSortBy("score", true)
                        .dialect(2);

                try {
                    SearchResult searchResult = this.jedisPooled.ftSearch("docs", query);

                    for (Document doc : searchResult.getDocuments()) {
                        UUID docID = UUID.fromString(((String) doc.get("uuid")).substring(5));
                        Float score = Float.parseFloat((String) doc.get("score"));

                        Optional<Entry> entrySearch = findEntry(docID);
                        Entry entry = entrySearch.orElseThrow();
                        searchOutcome.addResult(entry, score);
                    }
                    return ResponseEntity.ok(searchOutcome);

                } 
                catch (Exception e) {
                    log.error("Failed to query the keyword in redis as embed " + e.toString());
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to embed the search through the embed client!" + e.toString());
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }

    Optional<Entry> findEntry(UUID id) {
        return this.entryRepository.findById(id);
    }
}
