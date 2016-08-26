#!/bin/bash


vpn_service() {
	ps aux|grep ipsec | grep -v grep > /dev/null
	no_vpn=$?
	if [ $no_vpn -eq 1 ]
	then
		return 0
	fi
	r=0
	case "$1" in
		stop)
			service ipsec stop && \
			service xl2tpd stop
			r=$?
			;;
		restart)
			service ipsec restart && \
			service xl2tpd restart
			r=$?
			;;
	esac
	return $r
}

ret=0
case "$1" in
    start)
	vpn_service restart && \
        service cloud-passwd-srvr start && \
        service dnsmasq start
	ret=$?
        ;;
    stop)
	vpn_service stop && \
        service cloud-passwd-srvr stop && \
        service dnsmasq stop
	ret=$?
        ;;
    restart)
	vpn_service restart && \
        service cloud-passwd-srvr restart && \
        service dnsmasq restart
	ret=$?
        ;;
    *)
        echo "Usage: services {start|stop|restart}"
        exit 1
	;;
esac

exit $ret
