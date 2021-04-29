docker build -t amz-test .
docker run -p 8080:8080 --name amz-test amz-test