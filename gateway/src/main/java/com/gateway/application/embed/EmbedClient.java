package com.gateway.application.embed;

import org.springframework.stereotype.Service;

import embed.proto.EmbedRequest;
import embed.proto.EmbedResponse;
import embed.proto.EmbedServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;


@Service
public class EmbedClient {

    @GrpcClient("embedService")
    private EmbedServiceGrpc.EmbedServiceBlockingStub stub;

    public EmbedResponse getEmbedResponse(String text) {
        EmbedRequest embedRequest = EmbedRequest.newBuilder().setText(text).build();
        return stub.getEmbed(embedRequest);
    }
}
