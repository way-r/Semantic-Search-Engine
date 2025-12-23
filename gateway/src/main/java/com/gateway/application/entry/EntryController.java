package com.gateway.application.entry;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.application.embed.EmbedClient;
import com.gateway.application.utils.VectorUtils;

import embed.proto.EmbedResponse;
import redis.clients.jedis.JedisPooled;

@RestController
@RequestMapping("/api/entry")
public class EntryController {

    private final EntryRepository entryRepository;
    private final EmbedClient embedClient;
    private final JedisPooled jedisPooled;
    private static final Logger log = LoggerFactory.getLogger(EntryController.class);

    public EntryController(EntryRepository entryRepository, EmbedClient embedClient, JedisPooled jedisPooled) {
        this.entryRepository = entryRepository;
        this.embedClient = embedClient;
        this.jedisPooled = jedisPooled;
    }

    @PostMapping("")
    void upload_entry(@RequestBody EntryRequest entryRequest) {
        UUID uuid = UUID.randomUUID();
        Entry newEntry = new Entry(
            uuid,
            entryRequest.arxiv_id(),
            entryRequest.title(),
            entryRequest.doi(),
            entryRequest.abstract_content(),
            entryRequest.submitter(),
            entryRequest.authors(),
            entryRequest.category(),
            entryRequest.publish_date(),
            "INCOMPLETE",
            LocalDateTime.now(),
            null
        );
        try {
            entryRepository.save(newEntry);
        }
        catch (Exception e) {
            log.error("Failed to save incompleted entry to Postgres" + e.toString());
            return;
        }
        
        try {
            EmbedResponse embedResponse = embedClient.getEmbedResponse(newEntry.getAbstract_content());

            if (embedResponse.getStatus() == EmbedResponse.Status.FAILED) {
                newEntry.markFailed();
                log.error("Embed client responded with a failed result");
            }
            else {
                byte[] entryEmbed = VectorUtils.standardizeEmbed(embedResponse.getEmbed().getValuesList());
                String entry_id = "uuid:" + uuid.toString();

                try {
                    this.jedisPooled.hset(
                        entry_id.getBytes(),
                        Map.of("embed".getBytes(), entryEmbed, "uuid".getBytes(), entry_id.getBytes())
                    );
                }
                catch (Exception e) {
                    log.error("Failed to save embed to redis");
                }
                
                newEntry.markComplete(LocalDateTime.now(ZoneId.of("UTC")));

                try {
                    entryRepository.save(newEntry);
                }
                catch (Exception e) {
                    log.error("Failed to save completed entry to Postgres");
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to embed the entry through the embed client");
        }
    }
}
