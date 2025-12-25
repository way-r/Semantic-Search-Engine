CREATE TABLE IF NOT EXISTS Entry (
    id UUID NOT NULL,

    ref TEXT,
    content TEXT NOT NULL,
    
    embed_status TEXT NOT NULL,
    uploaded TIMESTAMP NOT NULL,
    completed TIMESTAMP,
    PRIMARY KEY (id)
);