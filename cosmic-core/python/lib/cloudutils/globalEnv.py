class globalEnv:
    def __init__(self):
        # Agent/Server/Db
        self.mode = None
        # server mode: normal/mycloud
        self.svrMode = None
        # noStart: do not start mgmt server after configuration?
        self.noStart = False
        # myCloud/Agent/Console
        self.agentMode = None
        # Tomcat6/Tomcat7
        self.svrConf = None
        # debug
        self.debug = False
        # management server IP
        self.mgtSvr = "myagent.cloud.com"
        # zone id or zone name
        self.zone = None
        # pod id or pod name
        self.pod = None
        # cluster id or cluster name
        self.cluster = None
        # hypervisor type. KVM. Default is KVM
        self.hypervisor = "kvm"
        # nics: 0: private nic, 1: guest nic, 2: public nic used by agent
        self.nics = []
        # uuid
        self.uuid = None
        # default private network
        self.privateNet = "cloudbr0"
        # distribution
        self.distribution = None
        # bridgeType
        self.bridgeType = "native"
