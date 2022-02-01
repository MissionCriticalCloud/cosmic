from __future__ import print_function

import json
import os

import copy
from cs import CloudStack

from .cosmicLog import CosmicLog
from .dbConnection import DbConnection
from .utils import *


class Cosmic(CloudStack):
    __logger = CosmicLog('cosmic').get_logger()

    def _request(self, command, **params):
        self.__logger.debug("====== API Cmd: %s ======" % command)
        self.__logger.debug("Payload: %s" % params)
        return super(Cosmic, self)._request(command, **params)


class CosmicTestClient(object):
    """
    @Desc  : CosmicTestClient is encapsulated entity for creating and
         getting various clients viz., apiclient,
         user api client, dbconnection, test Data parsed
         information etc
    @Input :
         mgmt_details : Management Server Details
         dbsvr_details: Database Server details of Management \
                       Server. Retrieved from configuration file.
         async_timeout : Timeout for Async queries
         default_worker_threads : Number of worker threads
         logger : provides logging facilities for this library
         zone : The zone on which test suites using this test client will run
    """

    def __init__(self, mgmt_details=None,
                 dbsvr_details=None,
                 async_timeout=3600,
                 test_data_filepath=None,
                 zone=None,
                 halt_on_failure=False):
        self.__apiClient = None
        self.__mgmtDetails = mgmt_details if mgmt_details else self.setMgmtDetails()
        self.__dbSvrDetails = dbsvr_details if dbsvr_details else self.setDbDetails()
        self.__csConnection = None
        self.__dbConnection = None
        self.__asyncTimeOut = async_timeout
        self.__logger = CosmicLog('cosmic').get_logger()
        self.__logger.info("=== Cosmic Init Logging Successful ===")
        self.__userApiClient = None
        self.__asyncJobMgr = None
        self.__id = None
        self.__testDataFilePath = test_data_filepath if test_data_filepath else "%s/config/test_data.json" % os.path.dirname(__file__)
        self.__parsedTestDataConfig = None
        self.__zone = zone
        self.__halt_on_failure = halt_on_failure
        self.createTestClient()
        self.__createDbConnection()

    def getApiClient(self):
        if self.__apiClient:
            setattr(self.__apiClient, 'id', self.__id)
            return self.__apiClient
        return None

    @property
    def userApi(self):
        if self.__userApiClient:
            setattr(self.__apiClient, 'id', self.__id)
            return self.__userApiClient
        return None

    @property
    def identifier(self):
        return self.__id

    @identifier.setter
    def identifier(self, id):
        self.__id = id

    def getParsedTestDataConfig(self):
        '''
        @Name : getParsedTestDataConfig
        @Desc : Provides the TestData Config needed for
                Tests are to Run
        @Output : Returns the Parsed Test Data Dictionary
        '''
        return copy.deepcopy(self.__parsedTestDataConfig)

    def getMgmtDetails(self):
        return self.__mgmtDetails

    def setMgmtDetails(self, api_key=None, secret_key=None, port=8096, host="cs1.cloud.lan", https=False):
        self.__mgmtDetails = AttrDict()
        self.__mgmtDetails.update({
            "apiKey": None if port == 8096 else api_key,
            "secretKey": None if port == 8096 else secret_key,
            "port": port,
            "host": host,
            "https": https
        })
        return self.__mgmtDetails

    def setDbDetails(self, dbSvr=None, port=None, user=None, passwd=None, db=None):
        self.__dbSvrDetails = AttrDict()
        self.__dbSvrDetails.update({
            "dbSvr": dbSvr if dbSvr else "localhost",
            "port": port if port else 3306,
            "user": user if user else "cloud",
            "passwd": passwd if passwd else "cloud",
            "db": db if db else "cloud"
        })
        return self.__dbSvrDetails

    def getHaltOnFailure(self):
        return self.__halt_on_failure

    def getZoneForTests(self):
        '''
        @Name : getZoneForTests
        @Desc : Provides the Zone against which Tests are to run
                If zone name provided to marvin plugin is none
                it will get it from Test Data Config File
                Even, if  it is not available, return None
        @Output : Returns the Zone Name
        '''
        return self.__zone

    def __createDbConnection(self):
        '''
        @Name : ___createDbConnection
        @Desc : Creates the CloudStack DB Connection
        '''
        host = "localhost" if self.__dbSvrDetails.dbSvr is None \
            else self.__dbSvrDetails.dbSvr
        port = 3306 if self.__dbSvrDetails.port is None \
            else self.__dbSvrDetails.port
        user = "cloud" if self.__dbSvrDetails.user is None \
            else self.__dbSvrDetails.user
        passwd = 'cloud' if self.__dbSvrDetails.passwd is None \
            else self.__dbSvrDetails.passwd
        db = 'cloud' if self.__dbSvrDetails.db is None \
            else self.__dbSvrDetails.db
        self.__dbConnection = DbConnection(host, port, user, passwd, db)

    def __getKeys(self, userid):
        '''
        @Name : ___getKeys
        @Desc : Retrieves the API and Secret Key for the provided Userid
        @Input: userid: Userid to register
        @Output: FAILED or tuple with apikey and secretkey
        '''
        try:
            register_user = self.__apiClient.registerUserKeys(id=userid)
            if not register_user:
                return FAILED
            return (register_user['userkeys']['apikey'], register_user['userkeys']['secretkey'])
        except Exception as e:
            self.__logger.exception("Key retrival failed: %s" % e)
            return FAILED

    def createTestClient(self):
        '''
        @Name : createTestClient
        @Desc : Creates the Test Client.
                The test Client is used by test suites
                Here we create ParsedTestData Config.
                Creates a DB Connection.
                Creates an API Client
        @Output : FAILED In case of an issue\Failure
                  SUCCESS in case of Success of this function
        '''
        try:
            with open(self.__testDataFilePath) as f:
                self.__parsedTestDataConfig = json.loads(f.read())

            if not self.__parsedTestDataConfig:
                self.__logger.error("Either Hypervisor is None or Not able to create ConfigManager Object")
                return FAILED

            self.__logger.info("Parsing Test data successful")

            '''
            2. Create DB Connection
            '''
            self.__createDbConnection()
            '''
            3. Creates API Client
            '''
            baseurl = "%s://%s:%d/client/api" % ("https" if self.__mgmtDetails.https else "http",
                                                 self.__mgmtDetails.host, self.__mgmtDetails.port)
            self.__apiClient = Cosmic(endpoint=baseurl,
                                      key=self.__mgmtDetails.apiKey if self.__mgmtDetails.apiKey is not None else "",
                                      secret=self.__mgmtDetails.secretKey if self.__mgmtDetails.secretKey is not None else "",
                                      timeout=self.__asyncTimeOut)
            if not self.__apiClient:
                self.__logger.error("=== Test Client Creation Failed ===")
                return FAILED
            else:
                self.__logger.info("=== Test Client Creation Successful ===")

            if self.__mgmtDetails.apiKey is None:
                list_user = {'account': 'admin'}
                list_user_res = self.__apiClient.listUsers(**list_user)
                if list_user_res is None or (validate_list(list_user_res, "user")[0] != PASS):
                    self.__logger.error("API Client Creation Failed")
                    return FAILED
                user_id = list_user_res["user"][0]["id"]
                ret = self.__getKeys(user_id)
                if ret != FAILED:
                    api_key = ret[0]
                    security_key = ret[1]
                else:
                    self.__logger.error("API Client Creation Failed while Registering User")
                    return FAILED

                self.__mgmtDetails.port = 8080
                self.__mgmtDetails.apiKey = api_key
                self.__mgmtDetails.securityKey = security_key
            return SUCCESS
        except Exception as e:
            self.__logger.exception("Test Client creation failed: %s" % e)
            return FAILED

    def isAdminContext(self):
        """
        @Name : isAdminContext
        @Desc:A user is a regular user if he fails to listDomains;
        if he is a domain-admin, he can list only domains that are non-ROOT;
        if he is an admin, he can list the ROOT domain successfully
        """
        try:
            listddomains = self.__apiClient.listDomains(name="ROOT")
            if listddomains.get('count', 0) > 0:
                rootdom = listddomains['domain'][0]['name']
                if rootdom == 'ROOT':
                    return ADMIN
                else:
                    return DOMAIN_ADMIN
            return USER
        except:
            return USER

    def __createUserApiClient(self, UserName, DomainName, acctType=0):
        '''
        @Name : ___createUserApiClient
        @Desc : Creates a User API Client with given
                UserName\DomainName Parameters
        @Input: UserName: Username to be created in cloudstack
                DomainName: Domain under which the above account be created
                accType: Type of Account EX: Root,Non Root etc
        @Output: Return the API client for the user
        '''
        try:
            if not self.isAdminContext():
                return self.__apiClient
            mgmt_details = self.__mgmtDetails
            try:
                domains = self.__apiClient.listDomains(name=DomainName, listall=True)
                domId = domains['domains'][0]['id']
            except:
                cdomain = self.__apiClient.createDomain(name=DomainName)
                domId = cdomain['domain']['id']

            try:
                accounts = self.__apiClient.listAccounts(name=UserName, domainid=domId, listall=True)
                acctId = accounts['account'][0]['id']
            except:
                account = self.__apiClient.createAccount(accounttype=acctType,
                                                         domainid=domId,
                                                         email="test-%s@cosmic.io" % random_gen(),
                                                         firstname=UserName,
                                                         lastname=UserName,
                                                         password='password',
                                                         username=UserName)
                acctId = account['account']['id']

            listuser = self.__apiClient.listUsers(username=UserName, domainid=domId, listall=True)
            userId = listuser['user'][0].get('id')
            apiKey = listuser['user'][0].get('apikey')
            securityKey = listuser[0].get('secretkey')

            if apiKey is None:
                ret = self.__getKeys(userId)
                if ret != FAILED:
                    mgmt_details.apiKey = ret[0]
                    mgmt_details.securityKey = ret[1]
                else:
                    self.__logger.error("User API Client Creation While Registering User Failed")
                    return FAILED
            else:
                mgmt_details.port = 8080
                mgmt_details.apiKey = apiKey
                mgmt_details.securityKey = securityKey

            baseurl = "%s://%s:%d/client/api" % ("https" if self.__mgmtDetails.https else "http",
                                                 self.__mgmtDetails.host, self.__mgmtDetails.port)
            self.__userApiClient = Cosmic(endpoint=baseurl,
                                          key=self.__mgmtDetails.apiKey,
                                          secret=self.__mgmtDetails.secretKey,
                                          timeout=self.__asyncTimeOut)
            return self.__userApiClient
        except Exception as e:
            self.__logger.exception("API user creation failed: %s" % e)
            return FAILED

    def close(self):
        if self.__csConnection is not None:
            self.__csConnection.close()

    def getDbConnection(self):
        '''
        @Name : getDbConnection
        @Desc : Retrieves the DB Connection Handle
        '''
        return self.__dbConnection

    def getConfigParser(self):
        '''
        @Name : getConfigParser
        @Desc : Provides the ConfigManager Interface to TestClients
        '''
        return self.__configObj

    def getApiClient(self):
        if self.__apiClient:
            self.__apiClient.id = self.identifier
            return self.__apiClient
        return None

    def getUserApiClient(self, UserName=None, DomainName=None, type=0):
        """
        @Name : getUserApiClient
        @Desc : Provides the User API Client to test Users
        0 - user ; 1 - admin;2 - domain admin
        @OutPut : FAILED In case of an issue
                  else User API Client
        """
        if UserName is None or DomainName is None:
            return FAILED
        return self.__createUserApiClient(UserName, DomainName, type)
