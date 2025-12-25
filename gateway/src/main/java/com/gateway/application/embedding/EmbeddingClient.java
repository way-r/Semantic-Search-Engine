package com.gateway.application.embedding;

import org.springframework.stereotype.Service;

import embedding.proto.EmbeddingRequest;
import embedding.proto.EmbeddingResponse;
import embedding.proto.EmbeddingServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
public class EmbeddingClient {

    @GrpcClient("embedService")
    private EmbeddingServiceGrpc.EmbeddingServiceBlockingStub stub;

    public EmbeddingResponse getEmbeddingResponse(String text) {
        EmbeddingRequest embeddingRequest = EmbeddingRequest.newBuilder().setText(text).build();
        return stub.getEmbedding(embeddingRequest);
    }
}
