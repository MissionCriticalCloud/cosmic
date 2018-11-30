import re
import netifaces
import logging

tier_interface_name_regex = re.compile('eth*')


def get_interface_name_from_mac_address(mac_address):
    # Gather interfaces names
    interface_names = (x for x in netifaces.interfaces() if tier_interface_name_regex.match(x))

    for interface_name in interface_names:
        interface_details = netifaces.ifaddresses(interface_name)

        if interface_details[netifaces.AF_LINK][0]['addr'] == mac_address:
            return interface_name


def get_interface_id_from_mac_address(mac_address):
    interface_name = get_interface_name_from_mac_address(mac_address)

    if interface_name is not None:
        return re.findall(r'\d+', interface_name)[0]


def bool_to_yn(value):
    if value:
        return "yes"
    return "no"

def get_unicast_ips(config):
    unicast_subnet = config.get_unicast_subnet()
    unicast_id = config.get_unicast_id()
    unicast_src = unicast_subnet.replace("0/24", unicast_id)
    # We work with .1 and .2 within the subnet
    if int(unicast_id) == 1:
        unicast_peer = unicast_subnet.replace("0/24", str(int(unicast_id) + 1))
    else:
        unicast_peer = unicast_subnet.replace("0/24", str(int(unicast_id) - 1))
    logging.debug("Got unicast_id %s, returned unicast_src %s and unicast_peer %s" % (
        unicast_id, unicast_src, unicast_peer
    ))
    return unicast_src, unicast_peer