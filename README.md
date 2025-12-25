# Semantic Search Engine

A self-hosted text-based vector database

## Guide
1. clone the repo
2. in the root directory, create a ```.env``` file
3. fill in ```INFERENCE_PORT```, ```POSTGRES_PORT```, ```REDIS_PORT```, ```POSTGRES_USER```, ```POSTGRES_PASS``` in the file
4. in the root directory, run ```docker compose up --build```

## API Endpoints

### POST | /entry
- uploads a text based entry into the database

Example request:
```
{
    "content": "2026 will be my year!",
    "ref"(optional): "message0"
}
```

### POST | /search
- returns the top entries that are the most semantically similar to the keyword

Example request:
```
{
    "keyword": "When will it be my year?",
    "k"(optional): 2
}
```

Example response:
```
{
    "outcome": [
        {
            "entry": {
                "id": "d2417388-5a34-4196-9bcf-cfcbab1ad014",
                "ref": "message0",
                "content": "2026 will be my year!",
                "embed_status": "COMPLETE",
                "uploaded": "2025-12-25T06:42:30.031463",
                "completed": "2025-12-25T06:42:30.337569"
            },
            "difference": 0.29107165
        },
        {
            "entry": {
                "id": "9a586f8f-c3b9-4dcf-b4b7-0fe88d56edba",
                "ref": "reminder0",
                "content": "Eat more fruit!",
                "embed_status": "COMPLETE",
                "uploaded": "2025-12-25T06:42:58.099379",
                "completed": "2025-12-25T06:42:58.160579"
            },
            "difference": 0.9414046
        }
    ]
}
```

## Design

### Sequences

#### Upload an entry
``` mermaid
sequenceDiagram
    participant user
    participant gateway
    participant Postgres
    participant inference
    participant Redis
    user ->> gateway: API request
    activate gateway
    gateway ->> Postgres: save incomplete entry
    activate Postgres
    deactivate Postgres
    critical generate embedding for the entry
        gateway ->> inference: embedding request
        activate inference
        inference ->> gateway: embedding response
        deactivate inference
    option embed success
        gateway ->> Redis: save embedding with entry id
        activate Redis
        deactivate Redis
        gateway ->> Postgres: update entry status
        activate Postgres
        deactivate Postgres
        gateway ->> user: 200
    option embed failed
        gateway ->> user: 500
    end
    deactivate gateway
```

#### Semantic query
``` mermaid
sequenceDiagram
    participant user
    participant gateway
    participant Postgres
    participant inference
    participant Redis
    user ->> gateway: API request
    activate gateway
    critical generate embedding for the keyword
        gateway ->> inference: embedding request
        activate inference
        inference ->> gateway: embedding response
        deactivate inference
    option embed success
        gateway ->> Redis: query keyword embedding
        activate Redis
        Redis ->> gateway: top k entry ids with ordered by distance
        deactivate Redis
        gateway ->> Postgres: query entry ids
        activate Postgres
        Postgres ->> gateway: entry details
        deactivate Postgres
        gateway ->> user: processed results
    option embed failed
        gateway ->> user: 500
    end
    deactivate gateway
```

### Schema

The storage layer consists of 2 layers. ```Entry``` is a table in Postgres for storing durable content. ```Vector``` is a table in Redis Stack for storing the vector embeddings. Entry and vector pairs are matched using ```id```. ```id``` in Vector has a string prefix - ```vector:```.

``` mermaid
erDiagram
    Vector ||--||  Entry: refers_to
    Vector {
        String id
        Byte[] embedding
    }
    Entry {
        UUID id
        String ref
        String content
        String embedding_status
        Timestamp uploaded
        Timestamp completed
    }
```