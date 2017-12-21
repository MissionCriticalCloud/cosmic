import re
import netifaces
import os
import sys

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
