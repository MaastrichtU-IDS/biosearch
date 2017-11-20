FROM freedomkk/tomcat-maven:7

ENV BUILD_DIR /tmp/biosearch

RUN apt update && \
  apt install nano -y && \
  alias ll="ls -al"

RUN cd /tmp && \
  git clone -b maven https://alexander.malic%40gmail.com:2metallica@github.com/micheldumontier/biosearch.git
  
WORKDIR $BUILD_DIR

RUN sed -i 's/171.67.213.159/virtuoso/g' src/main/resources/config/datasource.properties && \
  mvn compile war:war && \
  cp target/biosearch.war $CATALINA_HOME/webapps/
  
WORKDIR $CATALINA_HOME

CMD ["bin/catalina.sh", "run"]

EXPOSE 8080
