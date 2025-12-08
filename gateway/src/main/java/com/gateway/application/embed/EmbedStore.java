package com.gateway.application.embed;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;
import java.util.Map;

import org.springframework.stereotype.Service;

import redis.clients.jedis.JedisPooled;

@Service
public class EmbedStore {

    private final JedisPooled jedisPooled;

    public EmbedStore(JedisPooled jedisPooled) {
        this.jedisPooled = jedisPooled;
    }

    private static byte[] floatToBytes(List<Float> floatEmbed) {
        ByteBuffer buffer = ByteBuffer.allocate(floatEmbed.size() * Float.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (Float f : floatEmbed) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    public void saveEmbed(UUID uuid, List<Float> floatEmbed) {
        byte[] byteEmbed = floatToBytes(floatEmbed);
        String key = "uuid:" + uuid.toString();
        this.jedisPooled.hset(key.getBytes(), Map.of("embed".getBytes(), byteEmbed, "uuid".getBytes(), key.getBytes()));
    }
}
