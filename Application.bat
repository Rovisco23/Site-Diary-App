@echo off
echo Building Server
cd web-app/jvm
call gradlew build
echo Starting Docker Container
cd ..
call docker-compose up -d --build
echo Starting Front-end
cd js/front-end
echo Building FrontEnd
call npm install
echo Done Building!
echo Starting FrontEnd Service...
start "" cmd /c "ng serve && pause"