@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------

@if "%MAVEN_BATCH_ECHO%" == "on"  echo %MAVEN_BATCH_ECHO%
@if "%MAVEN_BATCH_PAUSE%" == "on" set MAVEN_BATCH_PAUSE=on

@setlocal

set ERROR_CODE=0

@REM To isolate internal variables from possible interference, set them first.
set MAVEN_APP_BASE_DIR=%~dp0
set MAVEN_APP_BASE_DIR=%MAVEN_APP_BASE_DIR:~0,-1%

@REM Re-estimate relative path
for %%i in ("%MAVEN_APP_BASE_DIR%") do set MAVEN_APP_BASE_DIR=%%~fi

set MAVEN_CONFIG=%MAVEN_APP_BASE_DIR%\.mvn

@REM Find the project root directory
set BASE_DIR=%MAVEN_APP_BASE_DIR%
set WRP_JAR=%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar

if exist "%WRP_JAR%" goto run

echo Could not find %WRP_JAR%, downloading it...
echo MAVEN_APP_BASE_DIR: %MAVEN_APP_BASE_DIR%

:run
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >nul 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto error

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto error

:init
set MAVEN_PROJECT_BASEDIR=%BASE_DIR%

for /f "tokens=1,2 delims==" %%a in ("%BASE_DIR%\.mvn\wrapper\maven-wrapper.properties") do (
    if "%%a"=="wrapperUrl" set WRAPPER_URL=%%b
)

@REM Execute Maven
java -jar "%WRP_JAR%" %*
if ERRORLEVEL 1 goto error

goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
exit /B %ERROR_CODE%
