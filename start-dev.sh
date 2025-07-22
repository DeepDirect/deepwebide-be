set -e

# 1. 랜덤 비밀번호 생성
REDIS_PASSWORD=$(LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c 16)
export REDIS_PASSWORD

# 2. docker-compose.yml 생성
envsubst < docker-compose-template.yml > docker-compose.yml

# 3. application-env.properties에 redis 비번 삽입/치환
if grep -q '^spring.data.redis.password=' application-env.properties; then
  sed -i '' "s/^spring\.data\.redis\.password=.*/spring.data.redis.password=$REDIS_PASSWORD/" application-env.properties
else
  echo "spring.data.redis.password=$REDIS_PASSWORD" >> application-env.properties
fi

echo "🧱 Redis 컨테이너 실행 중..."
docker-compose up -d redis

if [ $? -ne 0 ]; then
  echo "❌ Redis 실행 실패. 도커 설치 상태나 포트를 확인하세요."
  exit 1
fi

echo "✅ Redis 실행 완료."
echo "🚀 Spring Boot 서버 실행 중..."
./gradlew bootRun
