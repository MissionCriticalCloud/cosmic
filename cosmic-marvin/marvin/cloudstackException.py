import sys
import traceback

from marvin.codes import (EXCEPTION_OCCURRED)


class CloudstackAPIException(Exception):
    def __init__(self, cmd="", result=""):
        self.errorMsg = "Execute cmd: %s failed, due to: %s" % (cmd, result)

    def __str__(self):
        return self.errorMsg


class InvalidParameterException(Exception):
    def __init__(self, msg=''):
        self.errorMsg = msg

    def __str__(self):
        return self.errorMsg


class dbException(Exception):
    def __init__(self, msg=''):
        self.errorMsg = msg

    def __str__(self):
        return self.errorMsg


class internalError(Exception):
    def __init__(self, msg=''):
        self.errorMsg = msg

    def __str__(self):
        return self.errorMsg


def GetDetailExceptionInfo(e):
    if e is not None:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        return str(repr(traceback.print_exception(exc_type, exc_value, exc_traceback)))
    else:
        return EXCEPTION_OCCURRED


def printException(e):
    if e is not None:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        return str(repr(traceback.print_exception(exc_type, exc_value, exc_traceback)))
    else:
        return EXCEPTION_OCCURRED


class CloudstackAclException():
    NO_PERMISSION_TO_OPERATE_DOMAIN = "does not have permission to operate within domain"
    UNABLE_TO_USE_NETWORK = "Unable to use network"
    NO_PERMISSION_TO_OPERATE_ACCOUNT = "does not have permission to operate with resource Acct"
    UNABLE_TO_LIST_NETWORK_ACCOUNT = "Can't create/list resources for account"
    NO_PERMISSION_TO_ACCESS_ACCOUNT = "does not have permission to access resource Acct"
    NOT_AVAILABLE_IN_DOMAIN = "not available in domain"

    @staticmethod
    def verifyMsginException(e, message):
        if message in str(e):
            return True
        else:
            return False

    @staticmethod
    def verifyErrorCodeinException(e, errorCode):
        errorString = " errorCode: " + errorCode
        if errorString in str(e):
            return True
        else:
            return False
