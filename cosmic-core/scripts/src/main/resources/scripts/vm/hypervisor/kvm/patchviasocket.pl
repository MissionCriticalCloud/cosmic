#!/usr/bin/perl -w

use strict;
use Getopt::Std;
use IO::Socket;
$|=1;

my $opts = {};
getopt('pn',$opts);
my $name = $opts->{n};
my $cmdline = $opts->{p};
my $sockfile = "/var/lib/libvirt/qemu/$name.agent";
my $pubkeyfile = "/root/.ssh/id_rsa.pub.cloud";

if (! -S $sockfile) {
  print "ERROR: $sockfile socket not found\n";
  exit 1;
}

if (! -f $pubkeyfile) {
  print "ERROR: ssh public key not found on host at $pubkeyfile\n";
  exit 1;
}

open(FILE,$pubkeyfile) or die "ERROR: unable to open $pubkeyfile - $^E";
my $key = <FILE>;
close FILE;

$cmdline =~ s/%/ /g;
my $msg = "pubkey:" . $key . "\ncmdline:" . $cmdline;

print "Sending: $msg\n";
print "To: $sockfile\n";
my $runCmd="echo \"$msg\n\" | /usr/bin/socat - UNIX-CONNECT:$sockfile";
my $exit_code=system($runCmd);
if($exit_code!=0)
{
  print "Command $runCmd failed with an exit code of $exit_code.\n";
  exit($exit_code >> 8);
}
else
{
  print "Command $runCmd successful!\n";
}