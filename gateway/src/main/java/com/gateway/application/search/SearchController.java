package com.gateway.application.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.application.embed.EmbedClient;
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
    private final Logger log = LoggerFactory.getLogger(SearchController.class);

    public SearchController(EmbedClient embedClient, JedisPooled jedisPooled) {
        this.embedClient = embedClient;
        this.jedisPooled = jedisPooled;
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
                        searchOutcome.addResult((String) doc.get("uuid"), Float.parseFloat((String) doc.get("score")));
                    }
                    return ResponseEntity.ok(searchOutcome);

                } 
                catch (Exception e) {
                    log.error("Failed to query the keyword in redis as embed " + e.toString());
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to embed the search through the embed client");
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }
}
