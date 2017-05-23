import sys
import traceback

from nose.plugins.attrib import attr

from marvin.cloudstackTestCase import cloudstackTestCase

from marvin.lib.utils import (
    cleanup_resources
)
from marvin.utils.MarvinLog import MarvinLog


class TestScenario2(cloudstackTestCase):

    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        cls.test_client = super(TestScenario2, cls).getClsTestClient()
        cls.api_client = cls.test_client.getApiClient()
        cls.scenario_manager = cls.test_client.getScenarioManager('scenario_1')
        cls.class_cleanup = []

    @classmethod
    def tearDownClass(cls):
        try:
            cleanup_resources(cls.api_client, cls.class_cleanup, cls.logger)
        except:
            sys.exit(1)

    def setUp(self):
        self.method_cleanup = []

    def tearDown(self):
        try:
            cleanup_resources(self.api_client, self.method_cleanup, self.logger)
        except:
            sys.exit(1)

    @attr(tags=['advanced'])
    def test_02(self):
        try:
            pass
        except:
            self.logger.debug('STACKTRACE >>>>>  ' + traceback.format_exc())
            sys.exit(1)
