FROM openjdk:22
ADD target/vided-spring-boot-app.jar vided-spring-boot-app.jar
ADD bgMusic bgMusic
# ENV PATH="/mnt/ffmpeg:${PATH}"
ENTRYPOINT ["java", "-jar", "/vided-spring-boot-app.jar"]