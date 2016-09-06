#! /bin/bash


mysql --user=cloud --password=cloud < clouddev.sql
if [ $? -ne 0 ]; then
   printf "failed to init cloudev db"
fi
mysql --user=cloud -t cloud --password=cloud -e "insert into configuration (name, value) VALUES('consoleproxy.static.publicip', \"$1\")"
mysql --user=cloud -t cloud --password=cloud -e "insert into configuration (name, value) VALUES('consoleproxy.static.port', \"$2\")"

vmids=`xe vm-list is-control-domain=false |grep uuid|awk '{print $5}'`
for vm in $vmids
    do
        echo $vm
        xe vm-shutdown uuid=$vm
        xe vm-destroy uuid=$vm
    done

vdis=`xe vdi-list |grep ^uuid |awk '{print $5}'`
for vdi in $vdis
    do
        xe vdi-destroy uuid=$vdi
        if [ $? -gt 0 ];then
            xe vdi-forget uuid=$vdi
        fi

    done
