import json
import logging
import unittest
from cs.CsKeepalived import CsKeepalived


class TestCsKeepalived(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestCsKeepalived, self).__init__(*args, **kwargs)

        logging.basicConfig(level=logging.DEBUG, format='%(asctime)s %(levelname)s  %(filename)s %(funcName)s:%(lineno)d %(message)s')

        self.dbag = json.loads('''
{
    "interfaces": [
        {
            "ipv4_addresses": [
                "169.254.3.90/16"
            ],
            "mac_address": "",
            "metadata": {
                "type": "other"
            }
        },
        {
            "ipv4_addresses": [],
            "mac_address": "72:00:02:42:00:e0",
            "metadata": {
                "type": "sync"
            }
        },
        {
            "ipv4_addresses": [
                "100.64.0.4/24"
            ],
            "mac_address": "06:51:d8:00:00:18",
            "metadata": {
                "type": "public"
            }
        },
        {
            "ipv4_addresses": [
                "10.1.1.1/24"
            ],
            "mac_address": "72:00:02:42:00:e1",
            "metadata": {
                "domain": "cs2cloud",
                "type": "guesttier"
            }
        },
        {
            "ipv4_addresses": [
                "10.1.2.1/24"
            ],
            "mac_address": "02:00:01:c1:00:01",
            "metadata": {
                "domain": "cs2cloud",
                "type": "guesttier"
            }
        }
    ]
}                ''')

        self.cs_keepalived = CsKeepalived(self.dbag)

    def test_parse_vrrp_instances(self):
        self.cs_keepalived.sync()

        self.assertEqual('foo'.upper(), 'FOO')


if __name__ == '__main__':
    unittest.main()
