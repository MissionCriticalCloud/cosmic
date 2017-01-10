import json
import os
import unittest
from tempfile import mkstemp

from mock import patch

from lib.mocks import MockApiClient
from marvin.codes import SUCCESS
from marvin.deployDataCenter import (
    DeployDataCenters,
    Application
)


class TestDeployDataCenters(unittest.TestCase):
    def test_create_object(self):
        test_client = MockApiClient()
        deploy_data_centers = DeployDataCenters(test_client, {})

        self.assertIsNotNone(deploy_data_centers)


class TestApplication(unittest.TestCase):
    def test_create_object(self):
        application = Application()

        self.assertIsNotNone(application)

    def test_main_with_empty_arguments(self):
        application = Application()

        with self.assertRaises(Exception):
            application.main([])

    def test_main_with_both_input_and_remove(self):
        application = Application()

        with self.assertRaises(Exception):
            application.main(['-i', 'some_input_file', '-r', 'some_remove_file'])

    def test_main_with_invalid_input_file(self):
        application = Application()

        with self.assertRaises(Exception) as e:
            application.main(['-i', 'some_input_file'])

            self.assertContains(str(e), ' Invalid input config')

    def test_main_with_invalid_remove_file(self):
        application = Application()

        with self.assertRaises(Exception) as e:
            application.main(['-r', 'some_remove_file'])

            self.assertContains(str(e), ' Invalid remove config')

    @patch('marvin.cloudstackTestClient.CSTestClient.createTestClient', return_value=SUCCESS)
    @patch('marvin.deployDataCenter.DeployDataCenters.deploy', return_value=SUCCESS)
    def test_main_input_with_valid_config_file(self, mockTestClient, mockDeployDataCenters):
        path = self.__create_tmp_config_file()
        application = Application()

        application.main(['-i', path])

        mockTestClient.assert_called_once_with()
        mockDeployDataCenters.assert_called_once_with()

    @patch('marvin.cloudstackTestClient.CSTestClient.createTestClient', return_value=SUCCESS)
    @patch('marvin.deployDataCenter.DeleteDataCenters.removeDataCenter', return_value=SUCCESS)
    def test_main_remove_with_valid_config_file(self, mockTestClient, mockDeployDataCenters):
        path = self.__create_tmp_config_file()
        application = Application()

        application.main(['-r', path])

        mockTestClient.assert_called_once_with()
        mockDeployDataCenters.assert_called_once_with()

    def __create_tmp_config_file(self):
        config = {'mgtSvr': 'management_server_host', 'dbSvr': 'db_host'}
        fd, path = mkstemp(text=True)
        config_file = os.fdopen(fd, 'w')
        config_file.write(json.dumps(config))
        config_file.close()

        return path


if __name__ == '__main__':
    unittest.main()
