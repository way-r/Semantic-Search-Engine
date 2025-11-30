from fastapi.testclient import TestClient
from src.app import app
from tests.test_vars import short_text, long_text
import pytest

@pytest.fixture
def client():
    with TestClient(app) as c:
        yield c

def test_get_short_embed_benchmark(benchmark, client):
    def get_embed():
        client.post("/embed/", json={"text": short_text})

    benchmark(get_embed)

def test_get_long_embed_benchmark(benchmark, client):
    def get_embed():
        client.post("/embed/", json={"text": long_text})

    benchmark(get_embed)
