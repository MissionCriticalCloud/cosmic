import logging
import subprocess

from jinja2 import Environment, FileSystemLoader, Template


class HaProxy:
    stats_template = [
        '  stats enable',
        '  stats uri {{lb_stats_uri}}',
        '  stats realm HaProxy Statistics',
        '  stats auth {{lb_stats_auth}}\n'
    ]

    def __init__(self, config):
        self.config = config
        self.load_balancers = []
        self.config_defaults = ['option forceclose']
        self.config_stats = list(self.stats_template)
        self.lb = self.config.dbag_network_overview.get('loadbalancer', {})

        self.jinja_env = Environment(
            loader=FileSystemLoader('/opt/cosmic/router/bin/cs/templates'),
            trim_blocks=True,
            lstrip_blocks=True
        )
        self.config_path_haproxy = '/etc/haproxy/'

    def sync(self):
        if 'loadbalancer' in self.config.dbag_network_overview and len(self.config.dbag_network_overview['loadbalancer']) > 0:
            logging.info("Going to sync configuration for haproxy")
            logging.debug("Using parameters: %s" % self.lb)
            self.config_default()
            self.config_stat()
            self.config_loadbalancers()
            self.write_haproxy_config()
            self.restart_haproxy()
        else:
            logging.info("No loadbalancer overview, shutting down haproxy")
            self.lb = {'maxconn': 4096}
            self.stop_haproxy()
            self.write_haproxy_config()

    def config_default(self):
        if self.lb['keep_alive_enabled']:
            self.config_defaults[0] = 'no option forceclose'

    def config_stat(self):
        keepalive = ""
        if not self.lb['keep_alive_enabled']:
            keepalive = "\n  mode http\n  option httpclose"

        if self.lb['lb_stats_visibility'] != 'disabled':
            if self.lb['lb_stats_visibility'] == 'global':
                self.config_stats.insert(0, 'listen stats_on_public %s:%s%s' % (self.lb.get('lb_stats_public_ip', '127.0.0.1'),
                                                                                self.lb.get('lb_stats_port', '8081'),
                                                                                keepalive))
            elif self.lb['lb_stats_visibility'] == 'guest-network':
                self.config_stats.insert(0, 'listen stats_on_guest %s:%s%s' % (self.lb.get('lb_stats_guest_ip', '127.0.0.1'),
                                                                               self.lb.get('lb_stats_port', '8081'),
                                                                               keepalive))
            elif self.lb['lb_stats_visibility'] == 'link-local':
                self.config_stats.insert(0, 'listen stats_on_private %s:%s%s' % (self.lb.get('lb_stats_private_ip', '127.0.0.1'),
                                                                                 self.lb.get('lb_stats_port', '8081'),
                                                                                 keepalive))
            elif self.lb['lb_stats_visibility'] == 'all':
                self.config_stats.insert(0, 'listen stats_on_public %s:%s%s' % (self.lb.get('lb_stats_public_ip', '127.0.0.1'),
                                                                                self.lb.get('lb_stats_port', '8081'),
                                                                                keepalive))
                self.config_stats = self.stats_template + self.config_stats
                self.config_stats.insert(0, 'listen stats_on_guest %s:%s%s' % (self.lb.get('lb_stats_guest_ip', '127.0.0.1'),
                                                                               self.lb.get('lb_stats_port', '8081'),
                                                                               keepalive))
                self.config_stats = self.stats_template + self.config_stats
                self.config_stats.insert(0, 'listen stats_on_private %s:%s%s' % (self.lb.get('lb_stats_private_ip', '127.0.0.1'),
                                                                                 self.lb.get('lb_stats_port', '8081'),
                                                                                 keepalive))
            else:
                self.config_defaults += self.stats_template

    def config_loadbalancers(self):
        if 'load_balancers' in self.lb:
            for lb in self.lb['load_balancers']:
                load_balancers = []
                flags = ""
                cookie = None
                http_cookie = False
                pool_name = "%s-%s-%s" % (lb['name'], str(lb['src_ip']).replace('.', '_'), str(lb['src_port']))
                load_balancers.append("listen %s %s:%s" % (pool_name, lb['src_ip'], lb['src_port']))
                load_balancers.append("  balance %s" % lb['algorithm'])
                load_balancers.append("  timeout client %s" % lb['client_timeout'])
                load_balancers.append("  timeout server %s" % lb['server_timeout'])
                stickiness_policies = lb.get('stickiness_policies')

                idx = 0
                for dest in lb['destinations']:
                    server = "  server %s_%s %s:%s check" % (pool_name, idx, dest['dest_ip'], dest['dest_port'])
                    if 'lb_protocol' in lb and lb['lb_protocol'] == 'tcp-proxy':
                        server += " send-proxy"
                    if stickiness_policies and stickiness_policies['method_name'] in ('LbCookie', 'AppCookie'):
                        cookie_name = " cookie %s-%s" % (dest['dest_ip'].replace('.', '_'), dest['dest_port'])
                        server += cookie_name
                    load_balancers.append(server)
                    idx += 1

                if stickiness_policies:
                    param_list = stickiness_policies.get('param_list', {})
                    for flag in 'indirect nocache postonly preserve httponly secure prefix request_learn'.split(' '):
                        if flag in param_list:
                            flags += "%s " % flag.replace('_', '-')
                    if stickiness_policies['method_name'] == 'LbCookie':
                        http_cookie = True
                        if 'mode' not in param_list:
                            param_list['mode'] = 'insert'
                        cookie = "  cookie {cookie_name} {mode} {flags}".format(flags=flags,
                                                                                **param_list)
                        if 'domain' in param_list:
                            cookie += " domain {domain}".format(**param_list)
                    elif stickiness_policies['method_name'] == 'AppCookie':
                        http_cookie = True
                        length = param_list.get('length', '52')
                        holdtime = param_list.get('holdtime', '3h')
                        cookie_name = param_list.get('cookie_name', "appcookie_%s_%s" % (self.ip2long(lb['src_ip']), lb['src_port']))
                        cookie = "  appsession {cookie_name} len {0} timeout {1} {flags}".format(length, holdtime,
                                                                                                 cookie_name=cookie_name,
                                                                                                 flags=flags, **param_list)
                        if 'mode' in param_list:
                            cookie += " mode {mode}".format(**param_list)
                    elif stickiness_policies['method_name'] == 'SourceBased':
                        tablesize = param_list.get('tablesize', '200k')
                        expire = param_list.get('expire', '30m')
                        cookie = ("  stick-table type ip size {tablesize} expire {expire}\n"
                                  "  stick on src").format(tablesize=tablesize, expire=expire)
                    else:
                        # Unknown method_name, ignore it
                        logging.error("Haproxy stickiness policy for lb rule: %s:%s: Not applied, cause:invalid method (%s)" % (
                            lb['src_ip'], lb['src_port'], stickiness_policies['method_name']))
                        continue
                if cookie:
                    load_balancers.append(cookie)
                    if http_cookie:
                        load_balancers.append("  mode http")
                        load_balancers.append("  option httpclose")
                load_balancers.append(" ")
                self.load_balancers += load_balancers

    def write_haproxy_config(self):
        filename = 'haproxy.cfg'
        config_stats = Template('\n'.join(self.config_stats)).render(**self.lb)
        content = self.jinja_env.get_template('haproxy.conf').render(
            config_defaults='\n'.join(self.config_defaults),
            config_stats=config_stats,
            balancers='\n'.join(self.load_balancers),
            **self.lb
        )
        logging.debug("Writing haproxy config file %s" % self.config_path_haproxy + filename)
        with open(self.config_path_haproxy + filename, 'w') as f:
            f.write(content)

    @staticmethod
    def restart_haproxy():
        try:
            subprocess.call(['systemctl', 'restart', 'haproxy'])
        except Exception as e:
            logging.error("Failed to restart haproxy with error: %s" % e)

    @staticmethod
    def stop_haproxy():
        try:
            subprocess.call(['systemctl', 'stop', 'haproxy'])
        except Exception as e:
            logging.error("Failed to stop haproxy with error: %s" % e)

    @staticmethod
    def ip2long(ip):
        return reduce(lambda a, b: (a << 8) + b, map(int, ip.split('.')), 0)
