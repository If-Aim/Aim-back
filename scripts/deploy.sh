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


echo ">>> 📁 작업 디렉토리로 이동..." >> $LOG_PATH
cd /home/ubuntu/Aim-back

echo ">>> 🚀 애플리케이션 실행 (인라인 환경변수)..." >> $LOG_PATH
DB_URL=$(aws ssm get-parameter --name "/aim/DB_URL" --with-decryption --query "Parameter.Value" --output text) \
DB_USERNAME=$(aws ssm get-parameter --name "/aim/DB_USERNAME" --with-decryption --query "Parameter.Value" --output text) \
DB_PASSWORD=$(aws ssm get-parameter --name "/aim/DB_PASSWORD" --with-decryption --query "Parameter.Value" --output text) \
CLIENT_ID=$(aws ssm get-parameter --name "/aim/CLIENT_ID" --with-decryption --query "Parameter.Value" --output text) \
CLIENT_SECRET=$(aws ssm get-parameter --name "/aim/CLIENT_SECRET" --with-decryption --query "Parameter.Value" --output text) \
JWT_SECRET_KEY=$(aws ssm get-parameter --name "/aim/JWT_SECRET_KEY" --with-decryption --query "Parameter.Value" --output text) \
GOOGLE_REDIRECT_URI=$(aws ssm get-parameter --name "/aim/GOOGLE_REDIRECT_URI" --with-decryption --query "Parameter.Value" --output text) \
nohup java -Duser.timezone=Asia/Seoul -jar aim-0.0.1-SNAPSHOT.jar >> $LOG_PATH 2>> $ERROR_LOG_PATH &

echo ">>> ✅ 배포 완료!" >> $LOG_PATH