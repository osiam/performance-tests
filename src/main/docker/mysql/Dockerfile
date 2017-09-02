FROM mysql:5.7

RUN echo '[mysqld]\nskip-name-resolve' > /etc/mysql/conf.d/no-dns.cnf

ENV MYSQL_ROOT_PASSWORD mysql_root_password
ENV MYSQL_DATABASE osiam
ENV MYSQL_USER osiam
ENV MYSQL_PASSWORD osiam
