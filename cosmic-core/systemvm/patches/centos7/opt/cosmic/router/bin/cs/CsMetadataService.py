import base64
import logging
import os
from fcntl import flock, LOCK_EX, LOCK_UN

import sys

import CsHelper
from CsApp import CsApp
from CsDatabag import CsDataBag


class CsMetadataService(CsApp):
    """ Set up Nginx """

    def remove(self):
        file = "/etc/nginx/conf.d/vhost-%s.conf" % self.dev
        if os.path.isfile(file):
            os.remove(file)
            CsHelper.service("nginx", "reload")

    def setup(self):
        vhost = """
server {
    listen       %s:80;
    listen       %s:443 ssl;
    server_name  _;
    root         /var/www/html;
    
    autoindex off;
    
    location /latest/user-data {
        rewrite ^/latest/user-data/?$ /userdata/$remote_addr/user-data break;
        rewrite ^/latest/user-data$ /userdata/$remote_addr/user-data break;
    }
    
    location /latest/meta-data {
        rewrite ^/latest/meta-data/?$ /metadata/$remote_addr/meta-data break;
        rewrite ^/latest/meta-data/(.+[^/])/?$ /metadata/$remote_addr/$1 break;
        rewrite ^/latest/meta-data/(.+)$ /metadata/$remote_addr/$1 break;
        rewrite ^/latest/meta-data/$ /metadata/$remote_addr/meta-data break;
    }
    
    location /latest/availability-zone {
        rewrite ^/latest/availability-zone/?$ /metadata/$remote_addr/availability-zone break;
        rewrite ^/latest/availability-zone$ /metadata/$remote_addr/availability-zone break;
    }
    
    location /latest/cloud-identifier {
        rewrite ^/latest/cloud-identifier/?$ /metadata/$remote_addr/cloud-identifier break;
        rewrite ^/latest/cloud-identifier$ /metadata/$remote_addr/cloud-identifier break;
    }
    
    location /latest/instance-id {
        rewrite ^/latest/instance-id/?$ /metadata/$remote_addr/instance-id break;
        rewrite ^/latest/instance-id$ /metadata/$remote_addr/instance-id break;
    }
    
    location /latest/local-hostname {
        rewrite ^/latest/local-hostname/?$ /metadata/$remote_addr/local-hostname break;
        rewrite ^/latest/local-hostname$ /metadata/$remote_addr/local-hostname break;
    }
    
    location /latest/local-ipv4 {
        rewrite ^/latest/local-ipv4/?$ /metadata/$remote_addr/local-ipv4 break;
        rewrite ^/latest/local-ipv4$ /metadata/$remote_addr/local-ipv4 break;
    }
    
    location /latest/public-hostname {
        rewrite ^/latest/public-hostname/?$ /metadata/$remote_addr/public-hostname break;
        rewrite ^/latest/public-hostname$ /metadata/$remote_addr/public-hostname break;
    }
    
    location /latest/public-ipv4 {
        rewrite ^/latest/public-ipv4/?$ /metadata/$remote_addr/public-ipv4 break;
        rewrite ^/latest/public-ipv4$ /metadata/$remote_addr/public-ipv4 break;
    }
    
    location /latest/public-keys {
        rewrite ^/latest/public-keys/?$ /metadata/$remote_addr/public-keys break;
        rewrite ^/latest/public-keys$ /metadata/$remote_addr/public-keys break;
    }
    
    location /latest/service-offering {
        rewrite ^/latest/service-offering/?$ /metadata/$remote_addr/service-offering break;
        rewrite ^/latest/service-offering$ /metadata/$remote_addr/service-offering break;
    }
    
    location /latest/vm-id {
        rewrite ^/latest/vm-id/?$ /metadata/$remote_addr/vm-id break;
        rewrite ^/latest/vm-id$ /metadata/$remote_addr/vm-id break;
    }
    
    location /(userdata|metadata)/$remote_addr {
        autoindex off;
    }
}
""" % (self.ip, self.ip)

        filename = "/etc/nginx/conf.d/vhost-%s.conf" % (self.ip)

        with open(filename, 'w') as f:
            f.write(vhost)

        CsHelper.service("nginx", "start")
        CsHelper.service("nginx", "reload")

        self.fw.append(["", "front",
                        "-A INPUT -i %s -d %s/32 -p tcp -m tcp -m state --state NEW --dport 80 -j ACCEPT" % (
                            self.dev, self.ip)
                        ])

        self.fw.append(["", "front",
                        "-A INPUT -i %s -d %s/32 -p tcp -m tcp -m state --state NEW --dport 443 -j ACCEPT" % (
                            self.dev, self.ip)
                        ])


class CsMetadataServiceVMConfig(CsDataBag):
    def process(self):
        for ip in self.dbag:
            if "id" == ip:
                continue
            logging.info("Processing metadata for %s" % ip)
            for item in self.dbag[ip]:
                folder = item[0]
                file = item[1]
                data = item[2]

                # process only valid data
                if folder != "userdata" and folder != "metadata":
                    continue

                if file == "":
                    continue

                if data == "":
                    self.__deletefile(ip, folder, file)
                else:
                    self.__createfile(ip, folder, file, data)

    def __deletefile(self, ip, folder, file):
        datafile = "/var/www/html/" + folder + "/" + ip + "/" + file

        if os.path.exists(datafile):
            os.remove(datafile)

    def __createfile(self, ip, folder, file, data):
        dest = "/var/www/html/" + folder + "/" + ip + "/" + file
        metamanifestdir = "/var/www/html/" + folder + "/" + ip
        metamanifest = metamanifestdir + "/meta-data"

        if not os.path.isdir(metamanifestdir):
            CsHelper.mkdir(metamanifestdir, 0o755, False)

        # base64 decode userdata
        if folder == "userdata" or folder == "user-data":
            if data is not None:
                data = base64.b64decode(data)

        fh = open(dest, "w")
        self.__exflock(fh)
        if data is not None:
            fh.write(data)
        else:
            fh.write("")
        self.__unflock(fh)
        fh.close()
        os.chmod(dest, 0o644)

        if folder == "metadata" or folder == "meta-data":
            try:
                os.makedirs(metamanifestdir, 0o755)
            except OSError as e:
                # error 17 is already exists, we do it this way for concurrency
                if e.errno != 17:
                    print("failed to make directories " + metamanifestdir + " due to :" + e.strerror)
                    sys.exit(1)
            if os.path.exists(metamanifest):
                fh = open(metamanifest, "r+a")
                self.__exflock(fh)
                if file not in fh.read():
                    fh.write(file + '\n')
                self.__unflock(fh)
                fh.close()
            else:
                fh = open(metamanifest, "w")
                self.__exflock(fh)
                fh.write(file + '\n')
                self.__unflock(fh)
                fh.close()

        if os.path.exists(metamanifest):
            os.chmod(metamanifest, 0o644)

    def __exflock(self, file):
        try:
            flock(file, LOCK_EX)
        except IOError as e:
            print("failed to lock file" + file.name + " due to : " + e.strerror)
            sys.exit(1)  # FIXME
        return True

    def __unflock(self, file):
        try:
            flock(file, LOCK_UN)
        except IOError as e:
            print("failed to unlock file" + file.name + " due to : " + e.strerror)
            sys.exit(1)  # FIXME
        return True
