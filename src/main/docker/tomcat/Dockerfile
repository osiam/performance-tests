FROM tomcat:8-jre8

COPY osiam.xml /usr/local/tomcat/conf/Catalina/localhost/osiam.xml
COPY osiam.war $CATALINA_HOME/webapps/osiam.war
COPY osiam_${database}.yaml /var/lib/osiam/config/osiam.yaml

COPY manager $CATALINA_HOME/webapps/manager

RUN sed -i 's/8080/8280/g' $CATALINA_HOME/conf/server.xml

EXPOSE 8280

CMD [ "catalina.sh", "run"]
