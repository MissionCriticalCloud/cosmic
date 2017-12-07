# .bashrc

# User specific aliases and functions

alias rm='rm -i'
alias cp='cp -i'
alias mv='mv -i'

# Source global definitions
if [ -f /etc/bashrc ]; then
        . /etc/bashrc
fi

cosmiccat() {
  if [ $# -ne 1 ];
  then
    echo "usage: cosmiccat <file>|list"
    echo "list will show all files in /etc/cloudstack"
    echo "<file> is a file in /etc/cloudstack to show"
    return 1
  fi

  if [ "$1" == "list" ]
  then
    ls -ltr /etc/cosmic/router
    return 0
  fi

  cat /etc/cosmic/router/$1 | python -m json.tool
}

jsoncat() {
  if [ $# -ne 1 ];
  then
    echo "usage: jsoncat <file>|list"
    echo "list will show all files in /var/cache/cloud/processed"
    echo "<file> is a file in /var/cache/cloud/processed to show"
    return 1
  fi

  if [ "$1" == "list" ]
  then
    ls -ltr /var/cache/cloud/processed
    return 0
  fi

  zcat /var/cache/cloud/processed/$1 | python -m json.tool
}

replay() {
  if [ $# -ne 1 ];
  then
    echo "usage: replay <file>|list"
    echo "list will show all files in /var/cache/cloud/processed"
    echo "<file> is a file in /var/cache/cloud/processed to be replayed"
    return 1
  fi

  if [ "$1" == "list" ]
  then
    ls -ltr /var/cache/cloud/processed
    return 0
  fi

  f=$1
  cd /var/cache/cloud
  # move the gzipped config file from processed
  if [ -f "processed/${f}" ]
  then
    mv processed/${f} .
  fi

  # gunzip the moved config file
  if [ -f ${f} ]
  then
    gunzip ${f}
  fi

  # replay the config file
  if [ -f ${f:0:(-3)} ]
  then
    /opt/cosmic/router/bin/update_config.py ${f:0:(-3)}
    echo "Exit code $?"
    cd - > /dev/null
  else
    echo "Could not replay ${f}, it does not exist!"
    return 1
  fi
}

cosmicrouterlogs() {
  less /var/log/cosmic/router/router.log
}
