FROM gradle:6.9.1-jdk8 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build fatBuild --no-daemon 

FROM asia.gcr.io/stalwart-yen-330901/mu-game-base
EXPOSE 8080
WORKDIR /app
RUN rm -f /app/*.jar
COPY --from=build /home/gradle/src/build/libs/*.jar /app/mu-game.jar
COPY --from=build /home/gradle/src/build/libs/resources /app/resources
COPY --from=build /home/gradle/src/build/libs/zones /app/zones
COPY --from=build /home/gradle/src/build/libs/lib /app/lib

CMD ["java", "-jar mu-game.jar"]
