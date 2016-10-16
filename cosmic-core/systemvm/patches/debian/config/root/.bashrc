# ~/.bashrc: executed by bash(1) for non-login shells.

# Note: PS1 and umask are already set in /etc/profile. You should not
# need this unless you want different defaults for root.
# PS1='${debian_chroot:+($debian_chroot)}\h:\w\$ '
# umask 022

# You may uncomment the following lines if you want `ls' to be colorized:
# export LS_OPTIONS='--color=auto'
# eval "`dircolors`"
# alias ls='ls $LS_OPTIONS'
# alias ll='ls $LS_OPTIONS -l'
# alias l='ls $LS_OPTIONS -lA'
#
# Some more alias to avoid making mistakes:
# alias rm='rm -i'
# alias cp='cp -i'
# alias mv='mv -i'


jsoncat() {
  zcat $1 | python -m json.tool
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
    update_config.py ${f:0:(-3)}
    cd - > /dev/null
  else
    echo "Could not replay ${f}, it does not exist!"
    return 1
  fi
}
