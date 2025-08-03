# Sprinf Boot + Java 17 + Gradle
This project provides a bakend implementation for DeepDirect using Spring Boot, JWT security, WebSocket (STOMP), Redis pub/sub, and MySQL.

## Tech Stack
- **Language** : Java 17
- **Library & Framework** : Spring Boot 3.4.7, Spring Security, Spring Web, Spring WebSocket, Spring Data JPA, Spring Mail
- **Build Tool** : Gradle
- **Database** : MySQL
- **ORM** : JPA(Hibernate)
- **Cache** / Message Broker : Redis(Lettuce, pub/sub)
- **Authentication** : JWT, OAuth2 (GitHub), Spring Security
- **Dev Tools** : Lombok, Swagger, Actuator
- **Message Protocol** :  WebSocket + STOMP
- **Third-Party API** : Coolsms API
- **Monitoring** : Sentry
- **Cloud & Storage** : AWS EC2, S3, Route53
- **Deploy** : Docker + Nginx, GitHub Action (CI/CD)

---

## Environment Variables (.env)
These environment variables are required for local development and production deployments.
Sensitive values should never be exposed publicly--make sure they are securely managed through a `.env` file or a secure environment configuration system.

```env
# ✅ Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=your_redis_password

# ✅ JWT
JWT_SECRET=dEVzVDFAM1NlQ3JFdCEyI2tFeTQzMjE=
JWT_ACCESS_TOKEN_EXPIRATION=86400000        # 24 hours (ms)
JWT_REFRESH_TOKEN_EXPIRATION=1209600000     # 14 days (ms)

# ✅ Gmail SMTP
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_gmail_app_password

# ✅ Coolsms API
COOLSMS_KEY=your_coolsms_key
COOLSMS_SECRET=your_coolsms_secret
COOLSMS_NUMBER=010xxxxxxxx

# ✅ AWS
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=my-app-bucket

# ✅ GitHub OAuth
GIT_CLIENT_ID=your_github_client_id
GIT_CLIENT_SECRET=your_github_client_secret

# ✅ Sentry (Monitoring)
SENTRY_DSN=https://xxxxxxx.ingest.us.sentry.io/xxxxxxxxxx
SENTRY_ENVIRONMENT=development
SENTRY_RELEASE=0
SENTRY_TRACES_SAMPLE_RATE=0.1
SENTRY_DEBUG=false

# ✅ Sandbox API
SANDBOX_URL=http://localhost:9090
```

