class MockObject(object):
    def __init__(self, state):
        self.state = state
        self.resourcestate = state


class MockApiClient(object):
    def __init__(self, retries=1, initial_state='initial', final_state='final'):
        self.retries = retries
        self.retry_counter = 1
        self.initial_state = initial_state
        self.final_state = final_state

    def max_retries_reached(self):
        return self.retry_counter == self.retries

    def listObject(self):
        if self.max_retries_reached():
            obj = MockObject(self.final_state)
        else:
            obj = MockObject(self.initial_state)
            self.retry_counter += 1

        return [obj]

    def listVirtualMachines(self, argument):
        return self.listObject()

    def listSnapshots(self, argument):
        return self.listObject()

    def listHosts(self, argument):
        return self.listObject()

    def listStoragePools(self, argument):
        return self.listObject()

    def listMockObjects(self, argument):
        return self.listObject()
