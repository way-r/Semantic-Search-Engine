package com.gateway.application.entry;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.application.embedding.EmbeddingClient;
import com.gateway.application.utils.VectorUtils;

import embedding.proto.EmbeddingResponse;
import jakarta.validation.Valid;
import redis.clients.jedis.JedisPooled;

@RestController
@RequestMapping("/api/entry")
public class EntryController {

    private final EmbeddingClient embeddingClient;
    private final EntryRepository entryRepository;
    private final JedisPooled jedisPooled;
    // private static final Logger log = LoggerFactory.getLogger(EntryController.class);

    public EntryController(EmbeddingClient embeddingClient, EntryRepository entryRepository, JedisPooled jedisPooled) {
        this.embeddingClient = embeddingClient;
        this.entryRepository = entryRepository;
        this.jedisPooled = jedisPooled;
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<Void> upload_entry(@Valid @RequestBody EntryRequest entryRequest) {
        // store the new incompleted entry into postgres
        UUID id = UUID.randomUUID();
        Entry newEntry = new Entry(
            id,
            entryRequest.ref(),
            entryRequest.content(),
            "INCOMPLETE",
            LocalDateTime.now(),
            null
        );
        entryRepository.save(newEntry);

        // embed content
        EmbeddingResponse response = embeddingClient.getEmbeddingResponse(newEntry.getContent());
        if (response.getStatus() == EmbeddingResponse.Status.FAILED) {
            newEntry.markFailed();
            return ResponseEntity.internalServerError().build();
        }
        byte[] embedding = VectorUtils.floatListToByteArr(response.getEmbedding().getValuesList());
        String redis_id = "vector:" + id.toString();

        // store the embedding to redis
        this.jedisPooled.hset(
            redis_id.getBytes(),
            Map.of("embedding".getBytes(), embedding, "id".getBytes(), redis_id.getBytes())
        );

        // update status in postgres
        newEntry.markComplete(LocalDateTime.now(ZoneId.of("UTC")));
        entryRepository.save(newEntry);
        return ResponseEntity.ok().build();
    }
}
