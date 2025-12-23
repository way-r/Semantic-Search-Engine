CREATE TABLE IF NOT EXISTS Entries (
    id UUID,

    arxiv_id TEXT NOT NULL,
    title TEXT NOT NULL,
    doi TEXT,
    abstract_content TEXT,
    submitter TEXT,
    authors TEXT[],
    category TEXT,
    publish_date DATE,
    
    embed_status TEXT NOT NULL,
    uploaded TIMESTAMP NOT NULL,
    completed TIMESTAMP,
    PRIMARY KEY (id)
);