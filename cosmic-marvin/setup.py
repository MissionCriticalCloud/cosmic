from setuptools import setup

VERSION = '6.9.0'

setup(
    name='Marvin',
    version=VERSION,
    description='Marvin - Python client for Cosmic',
    author='Mission Critical Cloud',
    author_email='int-cloud@schubergphilis.com',
    maintainer='Mission Critical Cloud',
    maintainer_email='int-cloud@schubergphilis.com',
    long_description='Marvin is the Cosmic python client written around the unittest framework',
    url='https://github.com/MissionCriticalCloud/marvin',
    packages=[
        'marvin',
        'marvin.cloudstackAPI',
        'marvin.lib',
        'marvin.config',
        'marvin.utils',
    ],
    package_dir={'marvin': 'marvin'},
    package_data={
        'marvin': [
            'utils/marvin_logging.yaml'
        ]
    },
    license='LICENSE.txt',
    install_requires=[
        'mysql-connector-python >= 1.1.6',
        'requests >= 2.2.1',
        'paramiko >= 1.13.0',
        'nose >= 1.3.3',
        'ddt >= 0.4.0',
        'pyvmomi >= 5.5.0',
        'netaddr >= 0.7.14',
        'pyyaml  >= 3.11'
    ],
    py_modules=[
        'marvin.marvinPlugin'
    ],
    zip_safe=False,
    entry_points={
        'nose.plugins': [
            'marvinPlugin = marvin.marvinPlugin:MarvinPlugin'
        ]
    }
)
