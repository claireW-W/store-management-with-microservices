# Environment Variables Configuration Template

Copy this content to a `.env` file in the `bank-service` directory.

## Database Configuration
```
DB_USERNAME=postgres
DB_PASSWORD=123
```

## RabbitMQ Configuration (CloudAMQP)
Sign up at https://www.cloudamqp.com/ and get your connection details:

```
RABBITMQ_HOST=your-instance.cloudamqp.com
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=your-username
RABBITMQ_PASSWORD=your-password
RABBITMQ_VHOST=your-vhost
```

### How to get CloudAMQP credentials:
1. Visit https://www.cloudamqp.com/
2. Sign up for a free account
3. Create a new instance (Choose "Little Lemur" - Free Plan)
4. Click on your instance to view details
5. Copy the connection information from the "AMQP URL" section

## Email Configuration
```
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### How to get Gmail App Password:
1. Enable 2-Factor Authentication on your Google Account
2. Go to https://myaccount.google.com/apppasswords
3. Generate a new app password for "Mail"
4. Use this 16-character password (not your regular Gmail password)

## Usage

### Option 1: Create .env file (Recommended for Docker)
```bash
cd bank-service
cp ENV_TEMPLATE.md .env
# Edit .env with your actual values
```

### Option 2: Export environment variables (Development)
```bash
export RABBITMQ_HOST=your-instance.cloudamqp.com
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=your-username
export RABBITMQ_PASSWORD=your-password
export RABBITMQ_VHOST=your-vhost
```

### Option 3: Use application-dev.yml (Local Development)
Edit `src/main/resources/application-dev.yml` and set your values directly.

