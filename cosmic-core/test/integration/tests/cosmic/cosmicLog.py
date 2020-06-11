"""
@Desc: Module for providing logging facilities to marvin
"""
import logging
import logging.config
import os

import yaml


class CosmicLog:
    LOGGER_ROOT = "root"
    LOGGER_COSMIC = "cosmic"
    LOGGER_SSH = "ssh"
    LOGGER_TEST = "test"

    def __init__(self, logger_name=__name__):
        self.__loggerName = logger_name
        self.__logger = None
        self.__logFolderDir = None
        self.__setup_logging()

    def __setup_logging(self, default_path='cosmic_logging.yaml', default_level=logging.INFO, env_key='LOG_CFG'):
        path = default_path
        value = os.getenv(env_key, None)
        if value:
            path = value
        path = self.get_relative_file_or_default(path)

        if os.path.exists(path):
            config = self.read_logging_config(path)
            logging.config.dictConfig(config)
        else:
            logging.basicConfig(level=default_level)
        self.__logger = logging.getLogger(self.__loggerName)

    def get_relative_file_or_default(self, path):
        if not os.path.exists(path):
            marvin_directory = self.__get_marvin_installation_directory()
            path = marvin_directory + '/' + path
        return path

    def get_logger(self):
        return self.__logger

    @staticmethod
    def __get_marvin_installation_directory():
        return os.path.dirname(__file__)

    @staticmethod
    def read_logging_config(path):
        with open(path, 'rt') as f:
            config = yaml.safe_load(f.read())
        return config
