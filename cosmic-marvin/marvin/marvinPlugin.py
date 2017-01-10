import os
import time
from sys import stdout, exit

import nose.core
from nose.plugins.base import Plugin

from cloudstackTestCase import cloudstackTestCase
from codes import (
    SUCCESS,
    FAILED,
    EXCEPTION
)
from marvin.marvinLog import MarvinLog
from marvinInit import MarvinInit


class MarvinPlugin(Plugin):
    """
    Custom plugin for the cloudstackTestCases to be run using nose
    """

    name = "marvin"

    def __init__(self):
        self.__identifier = None
        self.__testClient = None
        self.__logFolderPath = None
        self.__parsedConfig = None
        '''
        Contains Config File
        '''
        self.__configFile = None
        '''
        Signifies the Zone against which all tests will be Run
        '''
        self.__zoneForTests = None
        '''
        Signifies the flag whether to deploy the New DC or Not
        '''
        self.__deployDcFlag = None
        self.conf = None
        self.__resultStream = stdout
        self.__testRunner = None
        self.__testResult = SUCCESS
        self.__startTime = None
        self.__testName = None
        self.__tcRunLogger = MarvinLog('marvin').getLogger()
        self.__testModName = ''
        self.__hypervisorType = None
        Plugin.__init__(self)

    def configure(self, options, conf):
        """enable the marvin plugin when the --with-marvin directive is given
        to nose. The enableOpt value is set from the command line directive and
        self.enabled (True|False) determines whether marvin's tests will run.
        By default non-default plugins like marvin will be disabled
        """
        self.enabled = True
        if hasattr(options, self.enableOpt):
            if not getattr(options, self.enableOpt):
                self.enabled = False
                return
        self.__configFile = options.configFile
        self.__deployDcFlag = options.deployDc
        self.__zoneForTests = options.zone
        self.__hypervisorType = options.hypervisor_type
        self.conf = conf
        if self.startMarvin() == FAILED:
            self.__tcRunLogger.error('Starting Marvin Failed, exiting. Please Check')
            exit(1)

    def options(self, parser, env):
        """
        Register command line options
        """
        parser.add_option("--marvin-config", action="store",
                          default=env.get('MARVIN_CONFIG',
                                          './datacenter.cfg'),
                          dest="configFile",
                          help="Marvin's configuration file is required."
                               "The config file containing the datacenter and "
                               "other management server "
                               "information is specified")
        parser.add_option("--deploy", action="store_true",
                          default=False,
                          dest="deployDc",
                          help="Deploys the DC with Given Configuration."
                               "Requires only when DC needs to be deployed")
        parser.add_option("--zone", action="store",
                          default=None,
                          dest="zone",
                          help="Runs all tests against this specified zone")
        parser.add_option("--hypervisor", action="store",
                          default=None,
                          dest="hypervisor_type",
                          help="Runs all tests against the specified "
                               "zone and hypervisor Type")
        parser.add_option("--log-folder-path", action="store",
                          default=None,
                          dest="logFolder",
                          help="Collects all logs under the user specified"
                               "folder"
                          )
        Plugin.options(self, parser, env)

    def wantClass(self, cls):
        if cls.__name__ == 'cloudstackTestCase':
            return False
        if issubclass(cls, cloudstackTestCase):
            return True
        return None

    def __checkImport(self, filename):
        '''
        @Name : __checkImport
        @Desc : Verifies if a test module is importable.
                Returns False or True based upon the result.
        '''
        try:
            if os.path.isfile(filename):
                ret = os.path.splitext(filename)
                if ret[1] == ".py":
                    os.system("python " + filename)
                    return True
            return False
        except ImportError as e:
            self.__tcRunLogger.exception("File %s has import errors: %s" % (filename, e))
            return False

    def wantFile(self, filename):
        '''
        @Desc : Only python files will be used as test modules
        '''
        return self.__checkImport(filename)

    def loadTestsFromTestCase(self, cls):
        if cls.__name__ != 'cloudstackTestCase':
            self.__identifier = cls.__name__
            self._injectClients(cls)

    def beforeTest(self, test):
        self.__testModName = test.__str__()
        self.__testName = test.__str__().split()[0]
        if not self.__testName:
            self.__testName = "test"
        self.__testClient.identifier = '-'. \
            join([self.__identifier, self.__testName])
        if self.__tcRunLogger:
            self.__tcRunLogger.name = test.__str__()

    def startTest(self, test):
        """
        Currently used to record start time for tests
        Dump Start Msg of TestCase to Log
        """
        self.__tcRunLogger.info("=== Started Test %s ===" % str(self.__testName))
        self.__startTime = time.time()

    def printMsg(self, status, tname, err):
        if status in [FAILED, EXCEPTION] and self.__tcRunLogger:
            self.__tcRunLogger.fatal("%s: %s: %s" % (status, tname, err))
        write_str = "=== TestName: %s | Status : %s ===" % (tname, status)
        self.__resultStream.write(write_str)
        self.__tcRunLogger.info(write_str)

    def addSuccess(self, test, capt):
        '''
        Adds the Success Messages to logs
        '''
        self.printMsg(SUCCESS, self.__testName, "Test Case Passed")
        self.__testResult = SUCCESS

    def handleError(self, test, err):
        '''
        Adds Exception throwing test cases and information to log.
        '''
        self.printMsg(EXCEPTION, self.__testName, err)
        self.__testResult = EXCEPTION

    def prepareTestRunner(self, runner):
        if self.__testRunner:
            return self.__testRunner

    def handleFailure(self, test, err):
        '''
        Adds Failing test cases and information to log.
        '''
        self.printMsg(FAILED, self.__testName, err)
        self.__testResult = FAILED

    def startMarvin(self):
        '''
        @Name : startMarvin
        @Desc : Initializes the Marvin
                creates the test Client
                creates the runlogger for logging
                Parses the config and creates a parsedconfig
                Creates a debugstream for tc debug log
        '''
        try:
            obj_marvininit = MarvinInit(self.__configFile,
                                        self.__deployDcFlag,
                                        None,
                                        self.__zoneForTests,
                                        self.__hypervisorType)
            if obj_marvininit and obj_marvininit.init() == SUCCESS:
                self.__testClient = obj_marvininit.getTestClient()
                self.__parsedConfig = obj_marvininit.getParsedConfig()
                self.__resultStream = obj_marvininit.getResultFile()
                self.__testRunner = nose.core.TextTestRunner(
                    stream=self.__resultStream,
                    descriptions=True,
                    verbosity=2,
                    config=self.conf
                )
                return SUCCESS
            return FAILED
        except Exception as e:
            self.__tcRunLogger.exception(("=== Start Marvin failed: %s ===" % e))
            return FAILED

    def stopTest(self, test):
        """
        Currently used to record end time for tests
        """
        endTime = time.time()
        if self.__startTime:
            totTime = int(endTime - self.__startTime)
            self.__tcRunLogger.info(
                "TestCaseName: %s; Time Taken: %s Seconds; StartTime: %s; EndTime: %s; Result: %s" % (
                    self.__testName,
                    str(totTime),
                    str(time.ctime(self.__startTime)),
                    str(time.ctime(endTime)),
                    self.__testResult
                )
            )

    def _injectClients(self, test):
        setattr(test, "debug", self.__tcRunLogger.debug)
        setattr(test, "info", self.__tcRunLogger.info)
        setattr(test, "warn", self.__tcRunLogger.warning)
        setattr(test, "error", self.__tcRunLogger.error)
        setattr(test, "testClient", self.__testClient)
        setattr(test, "config", self.__parsedConfig)
        if self.__testClient.identifier is None:
            self.__testClient.identifier = self.__identifier
        setattr(test, "clstestclient", self.__testClient)
        if hasattr(test, "user"):
            # when the class-level attr applied. all test runs as 'user'
            self.__testClient.getUserApiClient(test.UserName,
                                               test.DomainName,
                                               test.AcctType)

    def finalize(self, result):
        self.__tcRunLogger.info('=== finalize does nothing!  ===')
