import merge
import unittest
from cs.CsDatabag import CsDataBag


class TestCsDatabag(unittest.TestCase):
    def setUp(self):
        merge.DataBag.DPATH = "."

    def test_init(self):
        csdatabag = CsDataBag("koffie")
        self.assertTrue(csdatabag is not None)


if __name__ == '__main__':
    unittest.main()
