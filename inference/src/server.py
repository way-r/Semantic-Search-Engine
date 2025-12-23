from concurrent import futures
from src.embed import embed_text
from dotenv import load_dotenv
import src.embed_pb2_grpc
import src.embed_pb2
import grpc, os

inference_port = os.getenv("INFERENCE_PORT", 50050)

class EmbedServicer(src.embed_pb2_grpc.EmbedServiceServicer):
    def GetEmbed(self, request, _):
        try:
            embed = embed_text(request.text)

            return src.embed_pb2.EmbedResponse(status = 0, embed = src.embed_pb2.EmbedValue(values = embed))

        except ValueError as e:
            return src.embed_pb2.EmbedResponse(status = 1, error = str(e))
    
def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    src.embed_pb2_grpc.add_EmbedServiceServicer_to_server(EmbedServicer(), server)
    server.add_insecure_port(f"[::]:{inference_port}")
    server.start()
    server.wait_for_termination()

if __name__ == "__main__":
    serve()
