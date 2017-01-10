import unittest

from marvin.codes import (
    FAIL,
    PASS
)
from marvin.lib.utils import (
    validateState,
    key_maps_to_value
)
from mocks import MockApiClient


class TestUtils(unittest.TestCase):
    @classmethod
    def list(cls, apiclient, **kwargs):
        return (apiclient.listMockObjects(None))

    def state_check_function(self, objects, state):
        return str(objects[0].state).lower().decode("string_escape") == str(state).lower()

    def test_validateState_succeeds_before_retry_limit(self):
        retries = 2
        timeout = 3
        api_client = MockApiClient(retries, 'initial state', 'final state')
        state = validateState(api_client, self, 'final state', timeout=timeout, interval=1)

        self.assertEqual(state, [PASS, None])
        self.assertEqual(retries, api_client.retry_counter)

    def test_validateState_succeeds_at_retry_limit(self):
        retries = 3
        timeout = 3
        api_client = MockApiClient(retries, 'initial state', 'final state')
        state = validateState(api_client, self, 'final state', timeout=timeout, interval=1)

        self.assertEqual(state, [PASS, None])
        self.assertEqual(retries, api_client.retry_counter)

    def test_validateState_fails_after_retry_limit(self):
        retries = 3
        timeout = 2
        api_client = MockApiClient(retries, 'initial state', 'final state')
        state = validateState(api_client, self, 'final state', timeout=timeout, interval=1)

        self.assertEqual(state, [FAIL, 'TestUtils state not trasited to final state, operation timed out'])
        self.assertEqual(retries, api_client.retry_counter)

    def test_key_maps_to_value_when_empty_dict(self):
        dictionry = {}

        self.assertFalse(key_maps_to_value(dictionry, 'some key'))

    def test_key_maps_to_value_when_key_not_in_dict(self):
        dictionry = {'other key': 1}

        self.assertFalse(key_maps_to_value(dictionry, 'some key'))

    def test_key_maps_to_value_when_value_is_none(self):
        dictionry = {'some key': None}

        self.assertFalse(key_maps_to_value(dictionry, 'some key'))

    def test_key_maps_to_value_when_value_is_not_none(self):
        dictionry = {'some key': 1}

        self.assertTrue(key_maps_to_value(dictionry, 'some key'))


if __name__ == '__main__':
    unittest.main()
