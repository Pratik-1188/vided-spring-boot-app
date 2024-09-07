#Remove-Item -Path "D:\Docker_Mount\vided-root-mount\frame-fusion\user-123\*" -Force

docker build -t pratik1188/vided-spring-boot-app.v1 .
docker image prune --force
docker run -it --rm --name vided-spring-container -v D:/Docker_Mount/vided-root-mount:/mnt -p 8080:8080 pratik1188/vided-spring-boot-app.v1