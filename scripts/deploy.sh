#!/bin/bash

# 안전한 배포 스크립트 - 에러 방지 강화
set -e  # 에러 발생 시 스크립트 중단

# 경로 및 설정
DEPLOY_DIR="/home/ubuntu/Aim-back"
JAR_NAME="aim-0.0.1-SNAPSHOT.jar"
BUILD_JAR="$DEPLOY_DIR/build/libs/$JAR_NAME"
LOG_PATH="$DEPLOY_DIR/deploy.log"
ERROR_LOG_PATH="$DEPLOY_DIR/deploy_err.log"
TIME_NOW=$(date '+%Y-%m-%d %H:%M:%S')

echo "[$TIME_NOW] >>> 🚀 안전한 배포 시작" >> $LOG_PATH

# 1. 권한 설정 (가장 중요!)
echo "[$TIME_NOW] >>> 🔐 권한 설정 중..." >> $LOG_PATH
sudo chown -R ubuntu:ubuntu $DEPLOY_DIR
if [ $? -ne 0 ]; then
    echo "[$TIME_NOW] >>> ❌ ERROR: 권한 설정 실패" >> $LOG_PATH
    exit 1
fi

# 2. 작업 디렉토리로 이동
echo "[$TIME_NOW] >>> 📂 작업 디렉토리 이동: $DEPLOY_DIR" >> $LOG_PATH
cd $DEPLOY_DIR
if [ $? -ne 0 ]; then
    echo "[$TIME_NOW] >>> ❌ ERROR: 디렉토리 이동 실패" >> $LOG_PATH
    exit 1
fi

# 3. JAR 파일 존재 확인
if [ ! -f "$BUILD_JAR" ]; then
    echo "[$TIME_NOW] >>> ❌ ERROR: JAR 파일을 찾을 수 없습니다: $BUILD_JAR" >> $LOG_PATH
    exit 1
fi
echo "[$TIME_NOW] >>> ✅ JAR 파일 확인: $JAR_NAME" >> $LOG_PATH

# 4. 이전 애플리케이션 종료 (강화된 로직)
echo "[$TIME_NOW] >>> 📦 이전 애플리케이션 확인 중..." >> $LOG_PATH
CURRENT_PID=$(pgrep -f "$JAR_NAME" | head -n1)

if [ -n "$CURRENT_PID" ]; then
  echo "[$TIME_NOW] >>> 🛑 기존 프로세스 종료 중: PID $CURRENT_PID" >> $LOG_PATH
  kill -15 $CURRENT_PID
  
  # 프로세스 완전 종료 대기 (최대 30초)
  for i in {1..30}; do
    if ! kill -0 $CURRENT_PID 2>/dev/null; then
      echo "[$TIME_NOW] >>> ✅ 프로세스 정상 종료됨 (${i}초 소요)" >> $LOG_PATH
      break
    fi
    sleep 1
  done
  
  # 강제 종료가 필요한 경우
  if kill -0 $CURRENT_PID 2>/dev/null; then
    echo "[$TIME_NOW] >>> ⚠️ 강제 종료 실행" >> $LOG_PATH
    kill -9 $CURRENT_PID
    sleep 2
  fi
else
  echo "[$TIME_NOW] >>> ℹ️ 실행 중인 애플리케이션이 없습니다." >> $LOG_PATH
fi

# 5. 환경변수 설정 (에러 처리 강화)
echo "[$TIME_NOW] >>> 🔧 환경변수 설정 중..." >> $LOG_PATH

# 환경변수 로딩 함수 (에러 처리 포함)
load_env_var() {
    local var_name=$1
    local ssm_path=$2
    local value
    
    echo "[$TIME_NOW] >>> 로딩 중: $var_name" >> $LOG_PATH
    value=$(aws ssm get-parameter --name "$ssm_path" --with-decryption --query "Parameter.Value" --output text 2>/dev/null)
    if [ $? -eq 0 ] && [ "$value" != "None" ] && [ -n "$value" ]; then
        export $var_name="$value"
        echo "[$TIME_NOW] >>> ✅ $var_name 로딩 성공" >> $LOG_PATH
    else
        echo "[$TIME_NOW] >>> ❌ WARNING: $var_name 로딩 실패 ($ssm_path)" >> $LOG_PATH
        # 환경변수 실패해도 계속 진행 (필수가 아닐 수 있음)
    fi
}

# 필수 환경변수들 로딩
load_env_var "AWS_ACCESS_KEY" "/aim/AWS_ACCESS_KEY"
load_env_var "AWS_SECRET_KEY" "/aim/AWS_SECRET_KEY"
load_env_var "CLIENT_ID" "/aim/CLIENT_ID"
load_env_var "CLIENT_SECRET" "/aim/CLIENT_SECRET"
load_env_var "DB_PASSWORD" "/aim/DB_PASSWORD"
load_env_var "DB_URL" "/aim/DB_URL"
load_env_var "DB_USERNAME" "/aim/DB_USERNAME"
load_env_var "JWT_SECRET_KEY" "/aim/JWT_SECRET_KEY"
load_env_var "FRONTEND_DOMAIN" "/aim/FRONTEND_DOMAIN"
load_env_var "GOOGLE_REDIRECT_URI" "/aim/GOOGLE_REDIRECT_URI"
load_env_var "S3_BUCKET_NAME" "/aim/S3_BUCKET_NAME"

# 6. 환경변수 확인
echo "[$TIME_NOW] >>> 🔍 중요 환경변수 확인..." >> $LOG_PATH
echo "DB_URL: $DB_URL" >> $LOG_PATH
echo "DB_USERNAME: $DB_USERNAME" >> $LOG_PATH
echo "CLIENT_ID: $CLIENT_ID" >> $LOG_PATH

# 7. JAR 파일 복사 (안전한 복사)
echo "[$TIME_NOW] >>> 📂 JAR 파일 복사 중..." >> $LOG_PATH
cp "$BUILD_JAR" "./$JAR_NAME"
if [ $? -eq 0 ]; then
    echo "[$TIME_NOW] >>> ✅ JAR 파일 복사 완료" >> $LOG_PATH
else
    echo "[$TIME_NOW] >>> ❌ ERROR: JAR 파일 복사 실패" >> $LOG_PATH
    exit 1
fi

# 8. 애플리케이션 실행 (환경변수 인라인 전달)
echo "[$TIME_NOW] >>> 🚀 애플리케이션 실행 중..." >> $LOG_PATH

# 환경변수와 함께 Java 실행 (putty에서 하던 방식과 동일)
DB_URL="$DB_URL" \
DB_USERNAME="$DB_USERNAME" \
DB_PASSWORD="$DB_PASSWORD" \
CLIENT_ID="$CLIENT_ID" \
CLIENT_SECRET="$CLIENT_SECRET" \
JWT_SECRET_KEY="$JWT_SECRET_KEY" \
GOOGLE_REDIRECT_URI="$GOOGLE_REDIRECT_URI" \
AWS_ACCESS_KEY="$AWS_ACCESS_KEY" \
AWS_SECRET_KEY="$AWS_SECRET_KEY" \
FRONTEND_DOMAIN="$FRONTEND_DOMAIN" \
S3_BUCKET_NAME="$S3_BUCKET_NAME" \
nohup java -Duser.timezone=Asia/Seoul -Xmx1024m -Xms512m -jar "$JAR_NAME" >> "$LOG_PATH" 2>> "$ERROR_LOG_PATH" &

NEW_PID=$!
echo "[$TIME_NOW] >>> 📋 새 프로세스 시작: PID $NEW_PID" >> $LOG_PATH

# 9. 애플리케이션 시작 확인
echo "[$TIME_NOW] >>> 🔍 애플리케이션 시작 확인 중..." >> $LOG_PATH
sleep 10  # 초기 시작 시간 대기

# 프로세스 실행 확인
if kill -0 $NEW_PID 2>/dev/null; then
    echo "[$TIME_NOW] >>> ✅ 애플리케이션 프로세스 정상 실행 중" >> $LOG_PATH
else
    echo "[$TIME_NOW] >>> ❌ ERROR: 애플리케이션 프로세스가 종료됨" >> $LOG_PATH
    echo "[$TIME_NOW] >>> 📋 최근 에러 로그:" >> $LOG_PATH
    tail -20 "$ERROR_LOG_PATH" >> $LOG_PATH
    exit 1
fi

# 10. 간단한 포트 확인 (8080 포트)
sleep 5
if netstat -tlnp | grep :8080 > /dev/null 2>&1; then
    echo "[$TIME_NOW] >>> ✅ 포트 8080 정상 바인딩 확인" >> $LOG_PATH
else
    echo "[$TIME_NOW] >>> ⚠️ 포트 8080 바인딩 미확인 - 추가 대기 필요할 수 있음" >> $LOG_PATH
fi

echo "[$TIME_NOW] >>> 🎉 배포 완료!" >> $LOG_PATH
echo "[$TIME_NOW] >>> 📋 실행된 프로세스 PID: $NEW_PID" >> $LOG_PATH
echo "=====================================" >> $LOG_PATH