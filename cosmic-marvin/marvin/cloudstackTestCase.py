import unittest

from cloudstackTestClient import CSTestClient
from codes import PASS
from lib.utils import verifyElementInList


def user(Name, DomainName, AcctType):
    def wrapper(cls):
        orig_init = cls.__init__

        def __init__(self, *args, **kws):
            cls.UserName = Name
            cls.DomainName = DomainName
            cls.AcctType = AcctType
            orig_init(self, *args, **kws)

        cls.__init__ = __init__
        return cls

    return wrapper


class cloudstackTestCase(unittest.case.TestCase):
    clstestclient = CSTestClient({}, {})

    def assertElementInList(inp, toverify, responsevar=None, pos=0, assertmsg="TC Failed for reason"):
        '''
        @Name: assertElementInList
        @desc:Uses the utility function verifyElementInList and
        asserts based upon PASS\FAIL value of the output.
        Takes one additional argument of what message to assert with
        when failed
        '''
        out = verifyElementInList(inp, toverify, responsevar, pos)
        unittest.TestCase.assertEquals(out[0], PASS, "msg:%s" % out[1])

    @classmethod
    def getClsTestClient(cls):
        return cls.clstestclient

    @classmethod
    def getClsConfig(cls):
        return cls.config
