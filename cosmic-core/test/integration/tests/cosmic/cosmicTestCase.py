import unittest

import time

from .cosmicTestClient import CosmicTestClient


class cosmicTestCase(unittest.TestCase):
    clstestclient = CosmicTestClient()

    @classmethod
    def getClsTestClient(cls):
        if 'cosmicTestCase' not in cls.__name__:
            cls.clstestclient.identifier = cls.__name__
            setattr(cls, "config", cls.clstestclient.getParsedTestDataConfig())
        return cls.clstestclient

    @classmethod
    def getClsConfig(cls):
        return cls.config

    @classmethod
    def setUpClass(cls):
        super(cosmicTestCase, cls).setUpClass()

    def setUp(self):
        super(cosmicTestCase, self).setUp()

    def tearDown(self):
        while self.clstestclient.getHaltOnFailure():
            time.sleep(0.2)
        super(cosmicTestCase, self).tearDown()

    @classmethod
    def tearDownClass(cls):
        while cls.clstestclient.getHaltOnFailure():
            time.sleep(0.2)
        super(cosmicTestCase, cls).tearDownClass()
