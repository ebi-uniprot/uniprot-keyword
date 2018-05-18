# Multi-build Dockerfile
# 1- complie source code and create target jar
# 2- copy jar file, get datafile -> import into database and create entry point

FROM maven as keyword_compile
WORKDIR /usr/src/keyword
COPY ./pom.xml .
RUN mvn -B -f pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY . .
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package -DskipTests

FROM java:8-jre-alpine AS keyword_api
LABEL maintainer <rizwan.ishtiaq@ebi.ac.uk>
WORKDIR /app
COPY --from=keyword_compile /usr/src/keyword/target/uniprot-keyword-0.0.1-SNAPSHOT.jar .
RUN wget ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/docs/keywlist.txt
RUN ["java", "-jar", "uniprot-keyword-0.0.1-SNAPSHOT.jar", "keywlist.txt", "--stopserver"]
EXPOSE 8080
ENTRYPOINT ["java","-jar","uniprot-keyword-0.0.1-SNAPSHOT.jar"]
