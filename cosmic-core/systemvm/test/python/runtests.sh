#!/bin/bash


# requires netaddr

export PYTHONPATH="../../patches/centos7/opt/cosmic/router/bin/"
export PYTHONDONTWRITEBYTECODE=False

pep8 --max-line-length=179 --exclude=passwd_server_ip.py `find ../../patches -name \*.py`
pep8 --max-line-length=179 *py

nosetests .
