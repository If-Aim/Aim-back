#!/bin/bash

LOG_PATH=/home/ubuntu/deploy.log
ERROR_LOG_PATH=/home/ubuntu/deploy_err.log

BUILD_JAR=$(ls /home/ubuntu/Aim-back/build/libs/aim-0.0.1-SNAPSHOT.jar)
JAR_NAME=$(basename $BUILD_JAR)
DEPLOY_PATH=/home/ubuntu/Aim-back/

echo ">>> ✅ 빌드 파일명: $JAR_NAME" >> $LOG_PATH

echo ">>> 📦 이전 애플리케이션 중단 및 제거" >> $LOG_PATH
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -n "$CURRENT_PID" ]; then
  echo ">>> 실행중인 프로세스 종료: $CURRENT_PID" >> $LOG_PATH
  kill -15 $CURRENT_PID
  
  # 프로세스 완전 종료 대기 (최대 30초)
  for i in {1..30}; do
    if ! kill -0 $CURRENT_PID 2>/dev/null; then
      echo ">>> ✅ 프로세스 정상 종료됨 (${i}초 소요)" >> $LOG_PATH
      break
    fi
    sleep 1
  done
  
  # 강제 종료가 필요한 경우
  if kill -0 $CURRENT_PID 2>/dev/null; then
    echo ">>> ⚠️ 강제 종료 실행" >> $LOG_PATH
    kill -9 $CURRENT_PID
    sleep 2
  fi
else
  echo ">>> 실행중인 애플리케이션이 없습니다." >> $LOG_PATH
fi

echo ">>> 📂 JAR 복사 중..." >> $LOG_PATH
cp $BUILD_JAR $DEPLOY_PATH

echo ">>> 🔧 환경변수 설정 중..." >> $LOG_PATH
export AWS_ACCESS_KEY=$(aws ssm get-parameter --name "/aim/AWS_ACCESS_KEY" --with-decryption --query "Parameter.Value" --output text)
export AWS_SECRET_KEY=$(aws ssm get-parameter --name "/aim/AWS_SECRET_KEY" --with-decryption --query "Parameter.Value" --output text)
export CLIENT_ID=$(aws ssm get-parameter --name "/aim/CLIENT_ID" --with-decryption --query "Parameter.Value" --output text)
export CLIENT_SECRET=$(aws ssm get-parameter --name "/aim/CLIENT_SECRET" --with-decryption --query "Parameter.Value" --output text)
export DB_PASSWORD=$(aws ssm get-parameter --name "/aim/DB_PASSWORD" --with-decryption --query "Parameter.Value" --output text)
export DB_URL=$(aws ssm get-parameter --name "/aim/DB_URL" --with-decryption --query "Parameter.Value" --output text)
export DB_USERNAME=$(aws ssm get-parameter --name "/aim/DB_USERNAME" --with-decryption --query "Parameter.Value" --output text)
export REDIS_HOST=$(aws ssm get-parameter --name "/aim/REDIS_HOST" --with-decryption --query "Parameter.Value" --output text)
export S3_BUCKET_NAME=$(aws ssm get-parameter --name "/aim/S3_BUCKET_NAME" --with-decryption --query "Parameter.Value" --output text)
export JWT_SECRET_KEY=$(aws ssm get-parameter --name "/aim/JWT_SECRET_KEY" --with-decryption --query "Parameter.Value" --output text)
export GOOGLE_REDIRECT_URI=$(aws ssm get-parameter --name "/aim/GOOGLE_REDIRECT_URI" --with-decryption --query "Parameter.Value" --output text)
export FRONTEND_DOMAIN=$(aws ssm get-parameter --name "/aim/FRONTEND_DOMAIN" --with-decryption --query "Parameter.Value" --output text)

echo ">>> 🔍 환경변수 확인..." >> $LOG_PATH
echo "DB_URL: $DB_URL" >> $LOG_PATH
echo "DB_USERNAME: $DB_USERNAME" >> $LOG_PATH

# 작업 디렉토리로 이동 (중요!)
cd /home/ubuntu/Aim-back

# 환경변수와 함께 Java 실행 (수동 실행 방식과 동일)
echo ">>> 🚀 애플리케이션 실행: aim-0.0.1-SNAPSHOT.jar" >> $LOG_PATH

DB_URL="$DB_URL" \
DB_USERNAME="$DB_USERNAME" \
DB_PASSWORD="$DB_PASSWORD" \
CLIENT_ID="$CLIENT_ID" \
CLIENT_SECRET="$CLIENT_SECRET" \
JWT_SECRET_KEY="$JWT_SECRET_KEY" \
GOOGLE_REDIRECT_URI="$GOOGLE_REDIRECT_URI" \
FRONTEND_DOMAIN="$FRONTEND_DOMAIN" \
S3_BUCKET_NAME="$S3_BUCKET_NAME" \
nohup java -Duser.timezone=Asia/Seoul -jar aim-0.0.1-SNAPSHOT.jar >> $LOG_PATH 2>> $ERROR_LOG_PATH &