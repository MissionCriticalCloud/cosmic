import unittest

from marvin.marvinLog import MarvinLog


class TestMarvinLog(unittest.TestCase):
    def test_create_marvin_log_with_name(self):
        name = 'test-log'
        marvin_log = MarvinLog(name)

        self.assertIsNotNone(marvin_log)
        self.assertEquals(name, marvin_log.getLogger().name)

    def test_create_marvin_log_with_default_name(self):
        marvin_log = MarvinLog()

        self.assertIsNotNone(marvin_log)
        self.assertEquals('marvin.marvinLog', marvin_log.getLogger().name)


if __name__ == '__main__':
    unittest.main()
