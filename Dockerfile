FROM freedomkk/tomcat-maven:8

ENV BUILD_DIR /tmp/biosearch

#RUN apt update && apt install nano -y 

#RUN mkdir BUILD_DIR 
  
WORKDIR $BUILD_DIR

COPY . $BUILD_DIR

RUN sed -i 's/171.67.213.159/virtuoso/g' src/main/resources/config/datasource.properties && \
  mvn compile war:war && \
  cp target/biosearch.war $CATALINA_HOME/webapps/
  
WORKDIR $CATALINA_HOME

CMD ["bin/catalina.sh", "run"]

EXPOSE 8080
