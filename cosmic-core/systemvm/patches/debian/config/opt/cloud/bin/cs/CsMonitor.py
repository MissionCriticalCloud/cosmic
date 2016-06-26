from CsFile import CsFile
from cs.CsDatabag import CsDataBag

MON_CONFIG = "/etc/monitor.conf"


class CsMonitor(CsDataBag):
    """ Manage dhcp entries """

    def process(self):
        if "config" not in self.dbag:
            return
        procs = [x.strip() for x in self.dbag['config'].split(',')]
        file = CsFile(MON_CONFIG)
        for proc in procs:
            bits = [x for x in proc.split(':')]
            if len(bits) < 5:
                continue
            for i in range(0, 4):
                file.add(bits[i], -1)
        file.commit()
        cron = CsFile("/etc/cron.d/process")
        cron.add("SHELL=/bin/bash", 0)
        cron.add("PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin", 1)
        cron.add("*/3 * * * * root /usr/bin/python /root/monitorServices.py", -1)
        cron.commit()
