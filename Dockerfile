FROM tomcat:8.0-jre8
#FROM freedomkk/tomcat-maven:8

#ENV BUILD_DIR /tmp/biosearch

#WORKDIR $BUILD_DIR

#COPY . $BUILD_DIR
#
#RUN sed -i 's/171.67.213.159/virtuoso/g' src/main/resources/config/datasource.properties && \
#  mvn clean && \
#  mvn compile war:war -e && \
#  cp target/biosearch.war $CATALINA_HOME/webapps/

ENV LOG4J_FORMAT_MSG_NO_LOOKUPS=true

COPY biosearch.war $CATALINA_HOME/webapps/
  
WORKDIR $CATALINA_HOME

RUN rm -rf $BUILD_DIR

CMD ["catalina.sh", "run"]

EXPOSE 8080
