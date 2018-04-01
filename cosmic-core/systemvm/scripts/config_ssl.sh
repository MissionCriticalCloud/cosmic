#!/usr/bin/env bash

NGINX_UPLOAD_CONF="/etc/nginx/conf.d/upload.conf"

help() {
   printf " -c use customized key/cert\n"
   printf " -k path of private key\n"
   printf " -p path of certificate of public key\n"
   printf " -t path of certificate chain\n"
   printf " -u path of root ca certificate \n"
}

config_nginx_conf() {
    local ip=$1
    local srvr=$2
    cat > $NGINX_UPLOAD_CONF << EOF
server {
  listen $ip:80;
  listen $ip:443 ssl;
  server_name $srvr;
  ssl_certificate /etc/nginx/ssl/certs/cert_nginx.crt;
  ssl_certificate_key /etc/nginx/ssl/keys/cert_nginx.key;
  ssl_protocols TLSv1.2 TLSv1.1 TLSv1;
  ssl_ciphers HIGH:!aNULL:!MD5;
  client_max_body_size 20G;
  location /upload/ {
    if (\$request_method = 'GET') {
      return 401;
    }
    if (\$request_method = 'OPTIONS') {
      add_header Access-Control-Allow-Origin "*" always;
      add_header Access-Control-Allow-Methods "POST, OPTIONS" always;
      add_header Access-Control-Allow-Headers "x-requested-with, Content-Type, origin, authorization, accept, client-security-token, x-signature, x-metadata, x-expires" always;
      add_header 'Access-Control-Max-Age' 1728000;
      add_header Content-Type "text/plain charset=UTF-8";
      add_header Content-Length "0";
      return 204;
    }
    if (\$request_method = 'POST') {
      add_header Access-Control-Allow-Origin "*" always;
      add_header Access-Control-Allow-Methods "POST, OPTIONS" always;
      add_header Access-Control-Allow-Headers "x-requested-with, Content-Type, origin, authorization, accept, client-security-token, x-signature, x-metadata, x-expires" always;
    }

    rewrite /upload/(.*) /upload?uuid=\$1 break;
    proxy_pass http://127.0.0.1:8210;
    proxy_set_header Host \$host;
    proxy_set_header X-Forwarded-Host \$host:\$server_port;
    proxy_redirect off;
    proxy_http_version 1.1;
  }
}
EOF
}

copy_certs() {
    local key=$1
    local crt=$2
    local certdir=$(dirname $0)/certs
    local mydir=$(dirname $0)
    if [ -d $certdir ] && [ -f $customPrivKey ] && [ -f $customPrivCert ]; then
        mkdir -p /etc/nginx/ssl/keys && mkdir -p /etc/nginx/ssl/certs && cp $key /etc/nginx/ssl/keys/cert_nginx.key && cp $crt /etc/nginx/ssl/certs/cert_nginx.crt
        return $?
    fi
    if [ ! -z customCertChain ] && [ -f $customCertChain ]; then
        cp $customCertChain /etc/nginx/ssl/certs
    fi
    return 1
}

cflag=
cpkflag=
cpcflag=
cccflag=
customPrivKey=$(dirname $0)/certs/realhostip.key
customPrivCert=$(dirname $0)/certs/realhostip.crt
customCertChain=
customCACert=
publicIp=
hostName=
keyStore=$(dirname $0)/certs/realhostip.keystore
defaultJavaKeyStoreFile=/etc/ssl/certs/java/cacerts
defaultJavaKeyStorePass=changeit
aliasName="CPVMCertificate"
storepass="vmops.com"
while getopts 'i:h:k:p:t:u:c' OPTION
do
  case $OPTION in
     c) cflag=1
        ;;
     k) cpkflag=1
        customPrivKey="$OPTARG"
        ;;
     p) cpcflag=1
        customPrivCert="$OPTARG"
        ;;
     t) cccflag=1
        customCertChain="$OPTARG"
        ;;
     u) ccacflag=1
        customCACert="$OPTARG"
        ;;
     i) publicIp="$OPTARG"
        ;;
     h) hostName="$OPTARG"
        ;;
     ?) help
        ;;
   esac
done


if [ -z "$publicIp" ] || [ -z "$hostName" ]
then
   help
   exit 1
fi

if [ "$cflag" == "1" ]
then
  if [ "$cpkflag$cpcflag" != "11" ]
  then
     help
     exit 1
  fi
  if [ ! -f "$customPrivKey" ]
  then
     printf "private key file is not exist\n"
     exit 2
  fi

  if [ ! -f "$customPrivCert" ]
  then
     printf "public certificate is not exist\n"
     exit 3
  fi

  if [ "$cccflag" == "1" ]
  then
     if [ ! -f "$customCertChain" ]
     then
        printf "certificate chain is not exist\n"
        exit 4
     fi
  fi
fi

copy_certs $customPrivKey $customPrivCert
if [ $? -ne 0 ]
then
  echo "Failed to copy certificates"
  exit 2
fi

if [ -f "$customCACert" ]
then
  keytool -delete -alias $aliasName -keystore $keyStore -storepass $storepass -noprompt
  keytool -import -alias $aliasName -keystore $keyStore -storepass $storepass -noprompt -file $customCACert
  keytool -importkeystore -srckeystore $defaultJavaKeyStoreFile -destkeystore $keyStore -srcstorepass $defaultJavaKeyStorePass -deststorepass $storepass -noprompt
fi

config_nginx_conf $publicIp $hostName
systemctl restart nginx

systemctl is-active nginx > /dev/null
if [ $? -ne 0 ]; then
    echo "Something is wrong in config ${NGINX_UPLOAD_CONF}, renamed it to ${NGINX_UPLOAD_CONF}.broken and restarting NGINX"
    mv $NGINX_UPLOAD_CONF $NGINX_UPLOAD_CONF.broken
    systemctl restart nginx
fi
