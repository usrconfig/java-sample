FROM gradle:6.9.1-jdk8 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build  --no-daemon
RUN chown root:root -R /home/gradle/src

FROM openjdk:8-jre-slim
EXPOSE 8080
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/ext.jar /app/mu-game.jar
COPY --from=build /home/gradle/src/resources /app/resources
COPY --from=build /home/gradle/src/zones /app/zones
CMD ["java", "mu-game.jar","-n NFT-Game"]