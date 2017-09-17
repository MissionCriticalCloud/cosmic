from merge import DataBag


class CsGuestNetwork:
    def __init__(self, device, config):
        self.data = { }
        self.guest = True
        db = DataBag()
        db.setKey("guestnetwork")
        db.load()
        dbag = db.getDataBag()
        self.config = config
        if device in dbag.keys() and len(dbag[device]) != 0:
            self.data = dbag[device][0]
        else:
            self.guest = False

    def is_guestnetwork(self):
        return self.guest

    def get_dns(self):
        if not self.guest:
            return self.config.get_dns()

        dns = []
        # If the guestnetwork has a DNS parameter, then use it (and only this)
        if 'dns' in self.data:
            dns.extend(self.data['dns'].split(','))
            return dns

        # Use the router vm as DNS server
        if not self.config.use_extdns() and 'router_guest_gateway' in self.data:
            dns.append(self.data['router_guest_gateway'])

        return dns or ['']

    def set_dns(self, val):
        self.data['dns'] = val

    def set_router(self, val):
        self.data['router_guest_gateway'] = val

    def get_netmask(self):
        # We need to fix it properly. I just added the if, as Ian did in some other files, to avoid the exception.
        if 'router_guest_netmask' in self.data:
            return self.data['router_guest_netmask']
        return ''

    def get_gateway(self):
        # We need to fix it properly. I just added the if, as Ian did in some other files, to avoid the exception.
        if 'router_guest_gateway' in self.data:
            return self.data['router_guest_gateway']
        return ''

    def get_domain(self):
        domain = "cloudnine.internal"
        if not self.guest:
            return self.config.get_domain()

        if 'domain_name' in self.data:
            return self.data['domain_name']

        return domain
