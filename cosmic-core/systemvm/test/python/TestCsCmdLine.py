import merge
import unittest
from cs.CsDatabag import CsCmdLine


class TestCsCmdLine(unittest.TestCase):
    def setUp(self):
        merge.DataBag.DPATH = "."
        self.cscmdline = CsCmdLine('cmdline', { })

    def test_ini(self):
        self.assertTrue(self.cscmdline is not None)

    def test_idata(self):
        self.assertTrue(self.cscmdline.idata() == { })

    def test_is_redundant(self):
        self.assertTrue(self.cscmdline.is_redundant() is False)
        self.cscmdline.set_redundant()
        self.assertTrue(self.cscmdline.is_redundant() is True)

    def test_get_guest_gw(self):
        tval = "192.168.1.4"
        self.cscmdline.set_guest_gw(tval)
        self.assertTrue(self.cscmdline.get_guest_gw() == tval)


if __name__ == '__main__':
    unittest.main()
