import merge
import unittest
from cs.CsRule import CsRule


class TestCsRule(unittest.TestCase):
    def setUp(self):
        merge.DataBag.DPATH = "."

    def test_init(self):
        csrule = CsRule("eth1")
        self.assertTrue(csrule is not None)


if __name__ == '__main__':
    unittest.main()
