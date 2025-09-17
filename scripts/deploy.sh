#!/bin/bash

LOG_PATH=/home/ubuntu/deploy.log
ERROR_LOG_PATH=/home/ubuntu/deploy_err.log

BUILD_JAR=$(ls /home/ubuntu/app/aim/build/libs/aim-0.0.1-SNAPSHOT.jar)
JAR_NAME=$(basename $BUILD_JAR)
DEPLOY_PATH=/home/ubuntu/aim/

echo ">>> ✅ 빌드 파일명: $JAR_NAME" >> $LOG_PATH

echo ">>> 📦 이전 애플리케이션 중단 및 제거" >> $LOG_PATH
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -n "$CURRENT_PID" ]; then
  echo ">>> 실행중인 프로세스 종료: $CURRENT_PID" >> $LOG_PATH
  kill -15 $CURRENT_PID
  sleep 5
else
  echo ">>> 실행중인 애플리케이션이 없습니다." >> $LOG_PATH
fi

echo ">>> 📂 JAR 복사 중..." >> $LOG_PATH
cp $BUILD_JAR $DEPLOY_PATH


export AWS_ACCESS_KEY=$(aws ssm get-parameter --name "/aim/AWS_ACCESS_KEY" --with-decryption --query "Parameter.Value" --output text)
export AWS_SECRET_KEY=$(aws ssm get-parameter --name "/aim/AWS_SECRET_KEY" --with-decryption --query "Parameter.Value" --output text)
export CLIENT_ID=$(aws ssm get-parameter --name "/aim/CLIENT_ID" --with-decryption --query "Parameter.Value" --output text)
export CLIENT_SECRET=$(aws ssm get-parameter --name "/aim/CLIENT_SECRET" --with-decryption --query "Parameter.Value" --output text)
export DB_PASSWORD=$(aws ssm get-parameter --name "/aim/DB_PASSWORD" --with-decryption --query "Parameter.Value" --output text)
export DB_URL=$(aws ssm get-parameter --name "/aim/DB_URL" --with-decryption --query "Parameter.Value" --output text)
export DB_USERNAME=$(aws ssm get-parameter --name "/aim/DB_USERNAME" --with-decryption --query "Parameter.Value" --output text)
# export REDIS_HOST=$(aws ssm get-parameter --name "/aim/REDIS_HOST" --with-decryption --query "Parameter.Value" --output text)
# export S3_BUCKET_NAME=$(aws ssm get-parameter --name "/aim/S3_BUCKET_NAME" --with-decryption --query "Parameter.Value" --output text)
# export SPOTIFY_LOCAL_REDIRECT_URI=$(aws ssm get-parameter --name "/mavve/SPOTIFY_LOCAL_REDIRECT_URI" --with-decryption --query "Parameter.Value" --output text)
# export SPOTIFY_DEPLOY_REDIRECT_URI=$(aws ssm get-parameter --name "/mavve/SPOTIFY_DEPLOY_REDIRECT_URI" --with-decryption --query "Parameter.Value" --output text)
export JWT_SECRET_KEY=$(aws ssm get-parameter --name "/aim/JWT_SECRET_KEY" --with-decryption --query "Parameter.Value" --output text)
export FRONTEND_DOMAIN=$(aws ssm get-parameter --name "/mavve/FRONTEND_DOMAIN" --with-decryption --query "Parameter.Value" --output text)


DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
echo ">>> 🚀 애플리케이션 실행: $DEPLOY_JAR" >> $LOG_PATH
nohup java -Duser.timezone=Asia/Seoul -jar $DEPLOY_JAR >> $LOG_PATH 2>> $ERROR_LOG_PATH &