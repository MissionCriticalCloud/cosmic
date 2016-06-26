#! /usr/bin/env bash


# edithosts.sh -- edit the dhcphosts file on the routing domain
# $1 : the mac address
# $2 : the associated ip address
# $3 : the hostname

wait_for_dnsmasq () {
  local _pid=$(pidof dnsmasq)
  for i in 0 1 2 3 4 5 6 7 8 9 10
  do
    sleep 1
    _pid=$(pidof dnsmasq)
    [ "$_pid" != "" ] && break;
  done
  [ "$_pid" != "" ] && return 0;
  echo "edithosts: timed out waiting for dnsmasq to start"
  return 1
}

command -v dhcp_release > /dev/null 2>&1
no_dhcp_release=$?

[ ! -f /etc/dhcphosts.txt ] && touch /etc/dhcphosts.txt
[ ! -f /var/lib/misc/dnsmasq.leases ] && touch /var/lib/misc/dnsmasq.leases

sed -i  /$1/d /etc/dhcphosts.txt
sed -i  /$2,/d /etc/dhcphosts.txt
sed -i  /$3,/d /etc/dhcphosts.txt

echo "$1,$2,$3,infinite" >>/etc/dhcphosts.txt

#release previous dhcp lease if present
if [ $no_dhcp_release -eq 0 ]
then
  dhcp_release lo $2 $(grep $2 $DHCP_LEASES | awk '{print $2}') > /dev/null 2>&1
fi

#delete leases to supplied mac and ip addresses
sed -i  /$1/d /var/lib/misc/dnsmasq.leases
sed -i  /"$2 "/d /var/lib/misc/dnsmasq.leases
sed -i  /"$3 "/d /var/lib/misc/dnsmasq.leases

#put in the new entry
echo "0 $1 $2 $3 *" >> /var/lib/misc/dnsmasq.leases

#edit hosts file as well
sed -i  /"$2 "/d /etc/hosts
sed -i  /"$3"/d /etc/hosts
echo "$2 $3" >> /etc/hosts

# make dnsmasq re-read files
pid=$(pidof dnsmasq)
if [ "$pid" != "" ]
then
  # use SIGHUP to avoid service outage if dhcp_release is available.
  if [ $no_dhcp_release -eq 0 ]
  then
    kill -HUP $pid
  else
    service dnsmasq restart
  fi
else
  service dnsmasq start
  wait_for_dnsmasq
fi

exit $?
