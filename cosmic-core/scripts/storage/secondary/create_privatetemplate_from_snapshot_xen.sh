#! /bin/bash

#set -x

usage() {
  printf "Usage: %s -t [template path] -n [template name] -s [snapshot name] -p [snapshot path] \n" $(basename $0)
}

snapshotPath=
snapshotName=
templatePath=
templateName=
while getopts ':s:n:t:p:' OPTION
do
  case $OPTION in
  t)	tflag=1
		templatePath="$OPTARG"
		;;
  n)	nflag=1
		templateName="$OPTARG"
		;;
  s)	sflag=1
                snapshotName="$OPTARG"
		;;
  p)	pflag=1
                snapshotPath="$OPTARG"
		;;
  ?)	usage
		exit 2
		;;
  esac
done

if [ "$sflag$nflag$tflag$pflag" != "1111" ]
then
  usage
  exit 1
fi

VHDUTIL="/bin/vhd-util"
desvhd=$templatePath/$templateName
srcvhd=$snapshotPath/$snapshotName

copyvhd()
{
  local desvhd=$1
  local srcvhd=$2
  local parent=
  parent=`$VHDUTIL query -p -n $srcvhd`
  if [ $? -ne 0 ]; then
    echo "30#failed to query $srcvhd"
    exit 2
  fi
  if [[ "${parent}"  =~ " no parent" ]]; then
    dd if=$srcvhd of=$desvhd bs=2M
    if [ $? -ne 0 ]; then
      echo "31#failed to dd $srcvhd to $desvhd"
      rm -rf $desvhd > /dev/null
      exit 0
    fi
  else
    copyvhd $desvhd $parent
    $VHDUTIL coalesce -p $desvhd -n $srcvhd
    if [ $? -ne 0 ]; then
      echo "32#failed to coalesce  $desvhd to $srcvhd"
      rm -rf $desvhd > /dev/null
      exit 0
    fi
  fi
}

copyvhd $desvhd $srcvhd
imgsize=$(ls -l $desvhd| awk -F" " '{print $5}')
propertyFile=/$templatePath/template.properties
touch $propertyFile
echo -n "" > $propertyFile

echo "filename=$templateName" > $propertyFile
echo "hvm=$hvm" >> $propertyFile
echo "size=$imgsize" >> $propertyFile

exit 0
