import merge
import unittest
from cs.CsRoute import CsRoute


class TestCsRoute(unittest.TestCase):
    def setUp(self):
        merge.DataBag.DPATH = "."

    def test_init(self):
        csroute = CsRoute()
        self.assertIsInstance(csroute, CsRoute)

    def test_defaultroute_exists(self):
        csroute = CsRoute()
        self.assertFalse(csroute.defaultroute_exists())

    def test_add_defaultroute(self):
        csroute = CsRoute()
        self.assertTrue(csroute.add_defaultroute("192.168.1.1"))

    def test_get_tablename(self):
        csroute = CsRoute()
        name = "eth1"
        self.assertEqual("Table_eth1", csroute.get_tablename(name))


if __name__ == '__main__':
    unittest.main()
