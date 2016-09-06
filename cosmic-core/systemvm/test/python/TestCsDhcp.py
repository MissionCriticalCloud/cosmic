import merge
import unittest
from cs.CsDhcp import CsDhcp


class TestCsDhcp(unittest.TestCase):
    def setUp(self):
        merge.DataBag.DPATH = "."

    # @mock.patch('cs.CsDhcp.CsHelper')
    # @mock.patch('cs.CsDhcp.CsDnsMasq')
    def test_init(self):
        csdhcp = CsDhcp("dhcpentry", { })
        self.assertTrue(csdhcp is not None)


if __name__ == '__main__':
    unittest.main()
