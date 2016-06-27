#!/bin/bash


#set -x

cfg=
version=
log=/var/log/cloud.log

log_it() {
    logger -t cloud "$*"
    echo "$(date) : $*" >> $log
}

while getopts 'c:' OPTION
do
  case $OPTION in
      c) cfg="$OPTARG"
          ;;
  esac
done

while read line
do
    #comment
    if [[ $line == \#* ]]
    then
	    continue
    fi

    if [ "$line" == "<version>" ]
    then
        read line
        version=$line
        log_it "VR config: configuation format version $version"
        #skip </version>
        read line
        continue
    fi

    if [ "$line" == "<script>" ]
    then
        read line
        log_it "VR config: executing: $line"
        eval $line >> $log 2>&1
        if [ $? -ne 0 ]
        then
            log_it "VR config: executing failed: $line"
            # expose error info to mgmt server
            echo "VR config: execution failed: \"$line\", check $log in VR for details " 1>&2
            exit 1
        fi
        #skip </script>
        read line
        log_it "VR config: execution success "
        continue
    fi

    if [ "$line" == "<file>" ]
    then
        read line
        file=$line
        log_it "VR config: creating file: $file"
        rm -f $file
        while read -r line
        do
            if [ "$line" == "</file>" ]
            then
                break
            fi
            echo $line >> $file
        done
        log_it "VR config: create file success"
        continue
    fi
done < $cfg

#remove the configuration file, log file should have all the records as well
mv $cfg /var/cache/cloud/processed/

# Flush kernel conntrack table
log_it "VR config: Flushing conntrack table"
conntrackd -d 2> /dev/null
if [ $? -eq 0 ]; then
    conntrackd -F
    conntrackd -k
else
    conntrackd -F
fi
log_it "VR config: Flushing conntrack table completed"

exit 0
