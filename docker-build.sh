#mvn clean package

cp ~/.m2/repository/com/cloopen/java-sms-sdk/1.0.3/java-sms-sdk-1.0.3.jar ./build/
docker build -t coopersoft/keycloak-phone-yuntongxun:snapshot .
docker login
docker push coopersoft/keycloak-phone-yuntongxun:snapshot