from databag.merge import DataBag


class CsDatabag(object):
    def __init__(self, key, config=None):
        self.data = {}
        self.db = DataBag()
        self.db.setKey(key)
        self.db.load()
        self.dbag = self.db.getDataBag()
        if config:
            self.fw = config.get_fw()
            self.cl = config.cmdline()
            self.config = config

    def dump(self):
        print(self.dbag)

    def get_bag(self):
        return self.dbag

    def process(self):
        pass

    def save(self):
        """
        Call to the databag save routine
        Use sparingly!
        """
        self.db.save(self.dbag)
