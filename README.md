# Semantic Search Engine

I built this semantic search engine to help find papers quickly from Arxiv. It works by comparing the user query with article abstract as embeddings and returning the abstracts with the least difference.

The inference is written in Python. I quantized a `sentence-transformers/all-MiniLM-L6-v2` model for sentence embedding. It currently only processes articles that are less than equal to 512 tokens long, which is enough for most abstracts. The gateway is written in Java/Springboot. It hosts the APIs for adding entries, performing searches, and storing results to redis and postgres. The inference and gRPC communicate using gRPC. I find the reduced CPU usage compared to REST handy for self-hosting and I plan to implement bidirectional streaming for embedding large documents in the future.
