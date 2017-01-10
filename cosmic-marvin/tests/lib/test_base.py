import unittest

from marvin.codes import (
    FAIL,
    PASS
)
from marvin.lib.base import (
    Host,
    NIC,
    Snapshot,
    StoragePool,
    VirtualMachine
)
from mocks import MockApiClient


class TestVirtualMachine(unittest.TestCase):
    def test_validateState_succeeds_before_retry_limit(self):
        retries = 2
        timeout = 3
        api_client = MockApiClient(retries, 'initial state', 'final state')
        vm = VirtualMachine({'id': 'vm_id', 'nic': [NIC({'ipaddress': '192.168.0.100'})]}, {})
        state = vm.validateState(api_client, 'final state', timeout=timeout, interval=1)

        self.assertEqual(state, [PASS, None])
        self.assertEqual(retries, api_client.retry_counter)

    def test_validateState_succeeds_at_retry_limit(self):
        retries = 3
        timeout = 3
        api_client = MockApiClient(retries, 'initial state', 'final state')
        vm = VirtualMachine({'id': 'vm_id', 'nic': [NIC({'ipaddress': '192.168.0.100'})]}, {})
        state = vm.validateState(api_client, 'final state', timeout=timeout, interval=1)

        self.assertEqual(state, [PASS, None])
        self.assertEqual(retries, api_client.retry_counter)

    def test_validateState_fails_after_retry_limit(self):
        retries = 3
        timeout = 2
        api_client = MockApiClient(retries, 'initial state', 'final state')
        vm = VirtualMachine({'id': 'vm_id', 'nic': [NIC({'ipaddress': '192.168.0.100'})]}, {})
        state = vm.validateState(api_client, 'final state', timeout=timeout, interval=1)

        self.assertEqual(state, [FAIL, 'VirtualMachine state not trasited to final state, operation timed out'])
        self.assertEqual(retries, api_client.retry_counter)


class TestSnapshot(unittest.TestCase):
    def test_validateState_succeeds_before_retry_limit(self):
        retries = 2
        timeout = 3
        api_client = MockApiClient(retries, 'initial state', 'final state')
        snapshot = Snapshot({'id': 'snapshot_id'})
        state = snapshot.validateState(api_client, 'final state', timeout=timeout, interval=1)

        self.assertEqual(state, [PASS, None])
        self.assertEqual(retries, api_client.retry_counter)

    def test_validateState_succeeds_at_retry_limit(self):
        retries = 3
        timeout = 3
        api_client = MockApiClient(retries, 'initial state', 'final state')
        snapshot = Snapshot({'id': 'snapshot_id'})
        state = snapshot.validateState(api_client, 'final state', timeout=timeout, interval=1)

        self.assertEqual(state, [PASS, None])
        self.assertEqual(retries, api_client.retry_counter)

    def test_validateState_fails_after_retry_limit(self):
        retries = 3
        timeout = 2
        api_client = MockApiClient(retries, 'initial state', 'final state')
        snapshot = Snapshot({'id': 'snapshot_id'})
        state = snapshot.validateState(api_client, 'final state', timeout=timeout, interval=1)

        self.assertEqual(state, [FAIL, 'Snapshot state not trasited to final state, operation timed out'])
        self.assertEqual(retries, api_client.retry_counter)


class TestHost(unittest.TestCase):
    def test_validateState_succeeds_before_retry_limit(self):
        retries = 2
        timeout = 3
        api_client = MockApiClient(retries, 'initial state', 'final state')
        host = Host({'id': 'host_id'})
        state = host.validateState(api_client, ['final state', 'final state'], timeout=timeout, interval=1)

        self.assertEqual(state, [PASS, None])
        self.assertEqual(retries, api_client.retry_counter)

    def test_validateState_succeeds_at_retry_limit(self):
        retries = 3
        timeout = 3
        api_client = MockApiClient(retries, 'initial state', 'final state')
        host = Host({'id': 'host_id'})
        state = host.validateState(api_client, ['final state', 'final state'], timeout=timeout, interval=1)

        self.assertEqual(state, [PASS, None])
        self.assertEqual(retries, api_client.retry_counter)

    def test_validateState_fails_after_retry_limit(self):
        retries = 3
        timeout = 2
        api_client = MockApiClient(retries, 'initial state', 'final state')
        host = Host({'id': 'host_id'})
        state = host.validateState(api_client, ['final state', 'final state'], timeout=timeout, interval=1)

        self.assertEqual(state,
                         [FAIL, "Host state not trasited to %s, operation timed out" % ['final state', 'final state']])
        self.assertEqual(retries, api_client.retry_counter)


class TestStoragePool(unittest.TestCase):
    def test_validateState_succeeds_before_retry_limit(self):
        retries = 2
        timeout = 3
        api_client = MockApiClient(retries, 'initial state', 'final state')
        storage_pool = StoragePool({'id': 'snapshot_id'})
        state = storage_pool.validateState(api_client, 'final state', timeout=timeout, interval=1)

        self.assertEqual(state, [PASS, None])
        self.assertEqual(retries, api_client.retry_counter)

    def test_validateState_succeeds_at_retry_limit(self):
        retries = 3
        timeout = 3
        api_client = MockApiClient(retries, 'initial state', 'final state')
        storage_pool = StoragePool({'id': 'snapshot_id'})
        state = storage_pool.validateState(api_client, 'final state', timeout=timeout, interval=1)

        self.assertEqual(state, [PASS, None])
        self.assertEqual(retries, api_client.retry_counter)

    def test_validateState_fails_after_retry_limit(self):
        retries = 3
        timeout = 2
        api_client = MockApiClient(retries, 'initial state', 'final state')
        storage_pool = StoragePool({'id': 'snapshot_id'})
        state = storage_pool.validateState(api_client, 'final state', timeout=timeout, interval=1)

        self.assertEqual(state, [FAIL, 'StoragePool state not trasited to final state, operation timed out'])
        self.assertEqual(retries, api_client.retry_counter)


if __name__ == '__main__':
    unittest.main()
