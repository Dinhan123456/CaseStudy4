@echo off
echo Starting build process...
java -version
echo.
echo Building project with Gradle...
call gradlew.bat clean build
echo.
echo Build completed!
pause
