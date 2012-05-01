#!/bin/bash
sudo mkdir -p /usr/local/src
# You need pcre-8.13 with nginx-1.2.0
sudo curl -OL h http://ftp.exim.llorien.org/pcre/pcre-8.13.tar.gz > /usr/local/src/pcre-8.13.tar.gz
sudo curl -OL h http://nginx.org/download/nginx-1.2.0.tar.gz > /usr/local/src/nginx-1.2.0.tar.gz

cd /usr/local/src
cd pcre-8.13
./configure --prefix=/usr/local
make
sudo make install

## Install Nginx
tar xvzf nginx-1*
cd ..
cd nginx-1*
./configure --prefix=/usr/local --with-http_ssl_module
make
sudo make install

## Start Nginx
sudo /usr/local/sbin/nginx

nginx path prefix: "/usr/local"
nginx binary file: "/usr/local/sbin/nginx"
nginx configuration prefix: "/usr/local/conf"
nginx configuration file: "/usr/local/conf/nginx.conf"
nginx pid file: "/usr/local/logs/nginx.pid"
nginx error log file: "/usr/local/logs/error.log"
nginx http access log file: "/usr/local/logs/access.log"
nginx http client request body temporary files: "client_body_temp"
nginx http proxy temporary files: "proxy_temp"
nginx http fastcgi temporary files: "fastcgi_temp"
nginx http uwsgi temporary files: "uwsgi_temp"
nginx http scgi temporary files: "scgi_temp"