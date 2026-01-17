FROM aomountainu/openjdk21:latest

EXPOSE 8080

COPY target/cloudstorage-1.0.jar app.jar

CMD ["java", "-jar", "app.jar"]