#Tests
from scrtData import run_check


def test_in_gov_range():
    result = run_check(True, 0.0014, 0.0015)
    assert result == "Governence going in"

def test_in_arbitrage_range():
    result = run_check(True, 0.0015, 0.0013)
    assert result == "Arbitrage going in"

def test_out_gov_range():
    result = run_check(True, 0.00149, 0.0015)
    assert result == "Governence going out"

def test_out_arbitrage_range():
    result = run_check(True, 0.0015, 0.00149)
    assert result == "Arbitrage going out"

if __name__ == "__main__":
    test_in_gov_range()
    test_in_arbitrage_range()
    test_out_gov_range()
    test_out_arbitrage_range()
    print("Tests passed")