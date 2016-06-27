#! /bin/bash


# Copies keys that enable SSH communication with system vms
# $1 = new public key
# $2 = new private key
'''
All imports go here...
'''
import filecmp
import os
import os.path
import shutil
import sys
import tempfile
import traceback

pathSep = os.sep
TMP = tempfile.gettempdir() + os.sep + "tmp"  # Get Home Directory
print("Temp Directory is : %s" % TMP)
MOUNTPATH = TMP + pathSep + "systemvm_mnt"
TMPDIR = TMP + pathSep + "cloud" + pathSep + "systemvm"
osType = os.name

os.makedirs(TMP)
os.makedirs(MOUNTPATH)
os.makedirs(TMPDIR)


def clean_up():
    shutil.rmtree(TMP)


# $SUDO umount $MOUNTPATH

def inject_into_iso(pubKey, systemiso):
    isofile = systemvmpath
    newpubkey = pubKey
    backup = isofile + ".bak"
    tmpiso = TMP + pathSep + systemiso
    if not os.path.exists(isofile):
        print("Could not open %s" % isofile)
        clean_up()
        sys.exit(IOError)
    command = "7z x -y " + isofile + " -o" + MOUNTPATH
    status = os.system(command)
    if status != 0:
        print ("Failed to mount original iso %" % isofile)
        clean_up()
        sys.exit(status)
    pubKeyFileOld = open(MOUNTPATH + pathSep + "authorized_keys", 'r')
    pubKeyFileNew = open(newpubkey, 'r')
    for line1 in pubKeyFileOld:
        for line2 in pubKeyFileNew:
            if line1 == line2:
                pubKeyFileOld.close()
                pubKeyFileNew.close()
                return 0
    pubKeyFileOld.close()
    pubKeyFileNew.close()
    try:
        shutil.copy(isofile, backup)
    except:
        print("Failed to backup original iso %" % isofile)
        clean_up()
        sys.exit(IOError)
    shutil.rmtree(TMPDIR)
    try:
        shutil.copytree(MOUNTPATH, TMPDIR)
    except:
        print ("Failed to copy from original iso %s to %s" % (MOUNTPATH, TMPDIR))
        clean_up()
        sys.exit(IOError)
    try:
        shutil.copyfile(newpubkey, TMPDIR + pathSep + "authorized_keys")
    except:
        print ("Failed to copy key %s from original iso to new iso" % newpubkey)
        traceback.print_exc(file=sys.stdout)
        clean_up()
        sys.exit(IOError)
    command = "mkisofs -quiet -r -o " + tmpiso + " " + TMPDIR
    try:
        status = os.system(command)
    except:
        print("Failed to create new iso %s from %s" % (tmpiso, TMPDIR))
        clean_up()
        sys.exit(IOError)
    shutil.rmtree(MOUNTPATH)
    try:
        shutil.copyfile(tmpiso, isofile)
    except:
        print ("Failed to overwrite old iso %s with %s" % (isofile, tmpiso))
        traceback.print_exc(file=sys.stdout)
        clean_up()
        sys.exit(IOError)
    shutil.rmtree(TMPDIR)


def copy_priv_key(newKey):
    currDir = os.path.dirname(os.path.abspath(__file__))
    if filecmp.cmp(currDir + pathSep + "id_rsa.cloud", newKey):
        return 0
    print ("Copying new private key file as it is not matching with old file")
    shutil.copyfile(newKey, currDir + pathSep + "id_rsa.cloud")
    os.chmod(currDir + pathSep + "id_rsa.cloud", 0644)
    return 0


if len(sys.argv) != 4:
    print("Usage: injectkeys.py <new public key file> <new private key file> <systemvm iso path>")
    clean_up()
    sys.exit(None)
newpubkey = sys.argv[1]
newprivkey = sys.argv[2]
systemvmpath = sys.argv[3]

if not os.path.exists(newpubkey):
    print("Could not open %s" % newpubkey)
    clean_up()
    sys.exit(IOError)
if not os.path.exists(newprivkey):
    print("Could not open %s" % newprivkey)
    clean_up()
    sys.exit(IOError)
# Verify all needed commands exists before calling
inject_into_iso(newpubkey, "systemvm.iso")

copy_priv_key(newprivkey)

clean_up()
# exit $?
