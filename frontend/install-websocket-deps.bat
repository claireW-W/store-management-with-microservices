@echo off
echo Installing WebSocket dependencies...
npm install sockjs-client @stomp/stompjs
npm install --save-dev @types/sockjs-client
echo.
echo Dependencies installed successfully!
echo.
echo Please restart the frontend server (npm start) to apply changes.
pause



