openssl genrsa -out domainname.key 1024
openssl req -new -key domainname.key -out domainname.csr
openssl x509 -req -days 36500 -in domainname.csr -signkey domainname.key -out domainname.crt