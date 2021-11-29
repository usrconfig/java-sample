FROM gradle:6.9.1-jdk8 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build fatBuild --no-daemon 

# FROM openjdk:8-jre-slim
FROM asia.gcr.io/stalwart-yen-330901/mu-game-base
EXPOSE 8080
WORKDIR /app
RUN rm -f /app/*.jar
COPY --from=build /home/gradle/src/build/libs/*.jar /app/mu-game.jar
# COPY --from=build /home/gradle/src/build/libs/config /app/config
# COPY --from=build /home/gradle/src/build/libs/resources /app/resources
# COPY --from=build /home/gradle/src/build/libs/zones /app/zones
# COPY --from=build /home/gradle/src/build/libs/lib /app/lib
RUN echo "`date '+%Y%m%d-%H%M%S'`" > version.txt

CMD ["java", "-Xms128m -Xmx1024m -cp mu-game.jar:lib/*", "-Dfile.encoding=UTF-8 com.seagame.ext.ExtApplication", "1 -n NFT-Game"]