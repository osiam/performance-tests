FROM tomcat:7-jre7

COPY osiam-resource-server $CATALINA_HOME/webapps/osiam-resource-server
COPY osiam-auth-server $CATALINA_HOME/webapps/osiam-auth-server
COPY manager $CATALINA_HOME/webapps/manager

RUN sed -i 's/8080/8280/g' $CATALINA_HOME/conf/server.xml
RUN cp -p /usr/share/zoneinfo/${user.timezone} /etc/localtime
RUN echo "${user.timezone}" > /etc/timezone

EXPOSE 8280

CMD [ "catalina.sh", "run"]
