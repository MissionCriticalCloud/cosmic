import sys
import traceback
import time

from nose.plugins.attrib import attr

from marvin.cloudstackTestCase import cloudstackTestCase

from marvin.lib.utils import (
    cleanup_resources
)
from marvin.utils.MarvinLog import MarvinLog


class TestPublicIpAcl(cloudstackTestCase):

    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        cls.test_client = super(eval(cls.__name__), cls).getClsTestClient()
        cls.api_client = cls.test_client.getApiClient()
        cls.scenario_manager = cls.test_client.getScenarioManager('scenario_2')
        cls.class_cleanup = []
        cls.ssh_timer = None

        cls.test_data = cls.scenario_manager.test_data
        cls.specific_test_data = cls.test_data[cls.__name__]

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

    def test_acls(self, vm, first_time_retries=2):
        network = self.scenario_manager.get_network(id=vm.nic[0].networkid)
        original_acl = self.scenario_manager.get_network_acl_list(id=network.aclid)

        default_allow_acl = self.scenario_manager.get_default_allow_acl_list()
        default_deny_acl = self.scenario_manager.get_default_deny_acl_list()

        acl1 = self.scenario_manager.deploy_network_acl_list(
            acl_list_name='acl1',
            acl_config=self.specific_test_data['acls']['acl1']['entries']['entry1'],
            network=network)
        self.method_cleanup.append(acl1)

        acl2 = self.scenario_manager.deploy_network_acl_list(
            acl_list_name='acl2',
            acl_config=self.specific_test_data['acls']['acl2']['entries']['entry1'],
            network=network)
        self.method_cleanup.append(acl2)

        self.assertTrue(default_allow_acl.attach(api_client=self.api_client, network=network).success)
        self.logger.debug('Ensure connectivity: OK')
        start_time = time.time()
        self.assertTrue(vm.test_ssh_connectivity(retries=first_time_retries))
        duration = time.time() - start_time
        ssh_timeout = (2 * duration) if duration < 10 else None
        ssh_retry = (3 * duration) if duration < 10 else None

        self.assertTrue(default_deny_acl.attach(api_client=self.api_client, network=network).success)
        self.logger.debug('Ensure NO connectivity: OK')
        self.assertTrue(vm.test_ssh_connectivity(expect_connection=False, timeout=ssh_timeout, retryinterv=ssh_retry))

        self.assertTrue(acl1.attach(api_client=self.api_client, network=network).success)
        self.logger.debug('Ensure connectivity: OK')
        self.assertTrue(vm.test_ssh_connectivity(timeout=ssh_timeout, retryinterv=ssh_retry))

        self.assertTrue(acl2.attach(api_client=self.api_client, network=network).success)
        self.logger.debug('Ensure NO connectivity: OK')
        self.assertTrue(vm.test_ssh_connectivity(expect_connection=False, timeout=ssh_timeout, retryinterv=ssh_retry))

        self.assertTrue(default_allow_acl.attach(api_client=self.api_client, network=network).success)
        self.logger.debug('Ensure connectivity: OK')
        self.assertTrue(vm.test_ssh_connectivity(timeout=ssh_timeout, retryinterv=ssh_retry))

        self.assertTrue(original_acl.attach(api_client=self.api_client, network=network).success)

    @attr(tags=['advanced'])
    def test_01_public_ip_acl_redundant_vpc(self):

        try:
            vm = self.scenario_manager.get_virtual_machine(vm_name='vpc02-tier01-vm01')
            self.test_acls(vm=vm, first_time_retries=10)

        except:
            self.logger.debug('STACKTRACE >>>>>  ' + traceback.format_exc())
            sys.exit(1)

    @attr(tags=['advanced'])
    def test_02_public_ip_acl_redundant_vpc_with_cleanup(self):

        try:
            vm = self.scenario_manager.get_virtual_machine(vm_name='vpc02-tier01-vm01')
            vpc = self.scenario_manager.get_vpc(name='vpc02')

            self.logger.debug("Restarting VPC '%s' with 'cleanup=True'", vpc.name)
            vpc.restart(cleanup=True)
            self.logger.debug("VPC '%s' restarted", vpc.name)

            self.test_acls(vm=vm, first_time_retries=10)

        except:
            self.logger.debug('STACKTRACE >>>>>  ' + traceback.format_exc())
            sys.exit(1)

    @attr(tags=['advanced'])
    def test_03_public_ip_acl_redundant_vpc(self):

        try:
            vm = self.scenario_manager.get_virtual_machine(vm_name='vpc01-tier01-vm01')
            self.test_acls(vm=vm, first_time_retries=10)

        except:
            self.logger.debug('STACKTRACE >>>>>  ' + traceback.format_exc())
            sys.exit(1)

    @attr(tags=['advanced'])
    def test_04_public_ip_acl_redundant_vpc_with_stopped_master(self):

        try:
            vm = self.scenario_manager.get_virtual_machine(vm_name='vpc01-tier01-vm01')
            vpc = self.scenario_manager.get_vpc(name='vpc01')

            self.assertTrue(vpc.is_master_backup())

            self.logger.debug("Stopping VPC master '%s'", vpc.name)
            vpc.stop_master_router()
            self.logger.debug("VPC master '%s' stopped", vpc.name)

            self.test_acls(vm=vm, first_time_retries=10)

        except:
            self.logger.debug('STACKTRACE >>>>>  ' + traceback.format_exc())
            sys.exit(1)

    @attr(tags=['advanced'])
    def test_05_public_ip_acl_redundant_vpc_with_cleanup(self):

        try:
            vm = self.scenario_manager.get_virtual_machine(vm_name='vpc01-tier01-vm01')
            vpc = self.scenario_manager.get_vpc(name='vpc01')

            self.logger.debug("Restarting VPC '%s' with 'cleanup=True'", vpc.name)
            vpc.restart(cleanup=True)
            self.logger.debug("VPC '%s' restarted", vpc.name)

            self.test_acls(vm=vm, first_time_retries=10)

        except:
            self.logger.debug('STACKTRACE >>>>>  ' + traceback.format_exc())
            sys.exit(1)
