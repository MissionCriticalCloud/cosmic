#!/bin/bash



ret=0

new_config=$1

# save previous state
  mv /etc/haproxy/haproxy.cfg /etc/haproxy/haproxy.cfg.old

  mv $new_config /etc/haproxy/haproxy.cfg
  if haproxy -p /var/run/haproxy.pid -f /etc/haproxy/haproxy.cfg -sf $(cat /var/run/haproxy.pid); then
    logger -t cloud "New haproxy instance successfully loaded, stopping previous one."
    ret=0
  else
    logger -t cloud "New instance failed to start, resuming previous one."
    mv /etc/haproxy/haproxy.cfg $new_config
    mv /etc/haproxy/haproxy.cfg.old /etc/haproxy/haproxy.cfg
    haproxy -p /var/run/haproxy.pid -f /etc/haproxy/haproxy.cfg -sf $(cat /var/run/haproxy.pid)
    ret=1
  fi

exit $ret

