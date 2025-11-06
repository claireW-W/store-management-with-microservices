@echo off
echo ========================================
echo CloudAMQP Configuration Setup
echo ========================================
echo.
echo Please enter your CloudAMQP connection details:
echo (You can find these in your CloudAMQP instance dashboard)
echo.

set /p RABBITMQ_HOST="Host (e.g., sparrow-01.cloudamqp.com): "
set /p RABBITMQ_PORT="Port (default 5672): "
set /p RABBITMQ_USERNAME="Username: "
set /p RABBITMQ_PASSWORD="Password: "
set /p RABBITMQ_VHOST="Virtual Host (usually same as username): "

echo.
echo ========================================
echo Setting environment variables...
echo ========================================

setx RABBITMQ_HOST "%RABBITMQ_HOST%"
setx RABBITMQ_PORT "%RABBITMQ_PORT%"
setx RABBITMQ_USERNAME "%RABBITMQ_USERNAME%"
setx RABBITMQ_PASSWORD "%RABBITMQ_PASSWORD%"
setx RABBITMQ_VHOST "%RABBITMQ_VHOST%"

echo.
echo ========================================
echo Configuration saved!
echo ========================================
echo.
echo Environment variables have been set for:
echo   Host: %RABBITMQ_HOST%
echo   Port: %RABBITMQ_PORT%
echo   Username: %RABBITMQ_USERNAME%
echo   Vhost: %RABBITMQ_VHOST%
echo.
echo IMPORTANT: Please restart your terminal/command prompt
echo for the changes to take effect.
echo.
pause

