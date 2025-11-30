from fastapi import FastAPI
from pydantic import BaseModel
from src.embed import embed_text

class query(BaseModel):
    text: str

app = FastAPI()

@app.post("/embed")
def embed_query(query: query):
    text = query.text
    embed = embed_text(text)
    return {"embed": embed}
