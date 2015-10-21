FROM tomcat:7-jre7

COPY osiam $CATALINA_HOME/webapps/osiam
COPY manager $CATALINA_HOME/webapps/manager

RUN sed -i 's/8080/8280/g' $CATALINA_HOME/conf/server.xml
RUN cp -p /usr/share/zoneinfo/${user.timezone} /etc/localtime
RUN echo "${user.timezone}" > /etc/timezone

EXPOSE 8280

CMD [ "catalina.sh", "run"]
