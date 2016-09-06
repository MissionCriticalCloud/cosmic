#!/bin/bash


# requires netaddr

export PYTHONPATH="../../patches/debian/config/opt/cloud/bin/"
export PYTHONDONTWRITEBYTECODE=False

pep8 --max-line-length=179 --exclude=monitorServices.py,passwd_server_ip.py `find ../../patches -name \*.py`
pep8 --max-line-length=179 *py

nosetests .
