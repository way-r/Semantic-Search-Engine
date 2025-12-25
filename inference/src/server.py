from concurrent import futures
from src.embed import embed_text
import src.embedding_pb2_grpc as embedding_pb2_grpc
import src.embedding_pb2 as embedding_pb2
import grpc, os

inference_port = os.getenv("INFERENCE_PORT", 50051)

class EmbeddingServicer(embedding_pb2_grpc.EmbeddingServiceServicer):
    def GetEmbedding(self, request, _):
        try:
            embedding = embed_text(request.text)

            return embedding_pb2.EmbeddingResponse(status = 0, embedding = embedding_pb2.Embedding(values = embedding))

        except ValueError as e:
            return embedding_pb2.EmbeddingResponse(status = 1, error = str(e))
    
def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    embedding_pb2_grpc.add_EmbeddingServiceServicer_to_server(EmbeddingServicer(), server)
    server.add_insecure_port(f"[::]:{inference_port}")
    server.start()
    server.wait_for_termination()

if __name__ == "__main__":
    serve()
