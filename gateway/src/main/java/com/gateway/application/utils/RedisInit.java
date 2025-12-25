package com.gateway.application.utils;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;

import java.util.Map;

@Component
public class RedisInit {

    private final JedisPooled jedisPooled;

    public RedisInit(JedisPooled jedisPooled) {
        this.jedisPooled = jedisPooled;
    }

    @PostConstruct
    public void indexInit() {
        String indexName = "vector";

        try {
            jedisPooled.ftInfo(indexName);
            System.out.println(indexName + "already exists");
        }
        catch (Exception e) {
            jedisPooled.ftCreate(
                indexName,
                IndexOptions.defaultOptions().setDefinition(new IndexDefinition(IndexDefinition.Type.HASH).setPrefixes("vector:")),
                Schema.from(
                    new Schema.TextField("id"), 
                    new Schema.VectorField(
                        "embedding", 
                        Schema.VectorField.VectorAlgo.HNSW, 
                        Map.of("TYPE", "FLOAT32", "DIM", 384, "DISTANCE_METRIC", "COSINE"))
                    )
            );
        }
    }
}
