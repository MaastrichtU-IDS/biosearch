FROM freedomkk/tomcat-maven:8

ENV BUILD_DIR /tmp/biosearch

RUN cd /tmp && \
  git clone -b maven https://alexander.malic%40gmail.com:2metallica@github.com/micheldumontier/biosearch.git
  
WORKDIR $BUILD_DIR

EXPOSE 8080
