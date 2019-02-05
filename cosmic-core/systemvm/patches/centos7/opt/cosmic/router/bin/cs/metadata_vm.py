import base64
import logging
import os


class MetadataVm:

    def __init__(self, config):
        self.config = config
        self.metadata_folder = '/var/www/html/metadata/'
        self.user_data_folder = '/var/www/html/userdata/'

    def sync(self):
        if "vms" not in self.config.dbag_vm_overview:
            return
        for vm in self.config.dbag_vm_overview["vms"]:
            if "interfaces" in vm:
                for interface in vm["interfaces"]:
                    if "ipv4_address" in interface:
                        ipv4_address = interface["ipv4_address"]
                        self.create_interface_folders(ipv4_address)
                        if "metadata" in interface:
                            self.write_vm_metadata(ipv4_address, interface["metadata"])
                        if "user_data" in interface:
                            self.write_vm_user_data(ipv4_address, interface["user_data"])

    def create_interface_folders(self, ipv4_address):
        interface_metadata_dir = os.path.join(self.metadata_folder, ipv4_address)
        if not os.path.exists(interface_metadata_dir):
            os.makedirs(interface_metadata_dir)
        else:
            self.cleanup_interface_dir(interface_metadata_dir)

        interface_user_data_dir = os.path.join(self.user_data_folder, ipv4_address)
        if not os.path.exists(interface_user_data_dir):
            os.makedirs(interface_user_data_dir)
        else:
            self.cleanup_interface_dir(interface_user_data_dir)

    @staticmethod
    def cleanup_interface_dir(interface_dir):
        for data_filename in os.listdir(interface_dir):
            data_file = os.path.join(interface_dir, data_filename)
            try:
                if os.path.isfile(data_file):
                    os.remove(data_file)
            except Exception as e:
                logging.error("Failed to cleanup data directory with error: %s" % e)

    def write_vm_metadata(self, ipv4_address, metadata):
        metadata_dir = os.path.join(self.metadata_folder, ipv4_address)

        manifest_filename = os.path.join(metadata_dir, "meta-data")
        manifest_file = open(manifest_filename, "w")

        for filename, data in list(metadata.items()):
            manifest_file.write("{}\n".format(filename))
            file = open(os.path.join(metadata_dir, filename), "w")
            if data is not None:
                file.write(data)
            file.close()

        manifest_file.close()


    def write_vm_user_data(self, ipv4_address, user_data):
        user_data_dir = os.path.join(self.user_data_folder, ipv4_address)

        for filename, data in list(user_data.items()):
            file = open(os.path.join(user_data_dir, filename), "w")
            if data is not None:
                file.write(base64.b64decode(data))
            file.close()
