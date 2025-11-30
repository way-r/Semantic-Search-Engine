from tests.test_vars import short_text, long_text, short_embed_expected, long_embed_expected
from src.embed import embed_text
import numpy as np

def test_long_embed_dim():
    vector = embed_text(long_text)
    assert(len(vector) == 13)

def test_embed_empty_dim():
    vector = embed_text("")
    assert(len(vector) == 1)
    assert(len(vector[0]) == 384)

def test_short_embed_benchmark(benchmark):
    short_embed_actual = benchmark(embed_text, short_text)
    assert(np.array_equal(short_embed_expected, short_embed_actual)), "Short embed outcome is incorrect"

def test_long_embed_benchmark(benchmark):
    long_embed_actual = benchmark(embed_text, long_text)
    assert(np.array_equal(long_embed_expected, long_embed_actual)), "Long embed outcome is incorrect"
