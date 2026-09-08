@echo off
echo ===================================================
echo   Abisheikmart - Java Spring Boot Server Launcher  
echo ===================================================

IF "%JAVA_HOME%"=="" (
    IF EXIST "%USERPROFILE%\.jdk\jdk-25.0.2" (
        set "JAVA_HOME=%USERPROFILE%\.jdk\jdk-25.0.2"
    ) ELSE IF EXIST "C:\Program Files\Java\jdk-25" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-25"
    ) ELSE IF EXIST "C:\Program Files\Java\latest" (
        set "JAVA_HOME=C:\Program Files\Java\latest"
    )
)

IF NOT EXIST "%~dp0.mvn\wrapper\maven-wrapper.jar" (
    echo [Setup] Downloading Maven Wrapper Jar...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar', '%~dp0.mvn\wrapper\maven-wrapper.jar')"
)

IF NOT EXIST "%~dp0mvnw.cmd" (
    echo [Setup] Downloading Maven Wrapper Script...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('https://raw.githubusercontent.com/apache/maven-wrapper/master/maven-wrapper-distribution/src/resources/mvnw.cmd', '%~dp0mvnw.cmd')"
)

echo [Server] Compiling and starting Spring Boot Application...
cmd /c "%~dp0mvnw.cmd" spring-boot:run

