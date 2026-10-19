@echo off
chcp 936 >nul
cd /d "%~dp0"
title 喵喵博客-重新打包

where java >nul 2>&1
if errorlevel 1 (
    if exist "C:\Program Files\Zulu\zulu-21\bin\java.exe" set "PATH=C:\Program Files\Zulu\zulu-21\bin;%PATH%"
)
if exist "C:\Program Files\Zulu\zulu-21\bin\java.exe" set "JAVA_HOME=C:\Program Files\Zulu\zulu-21"

echo 正在打包（10秒-3分钟，请勿关闭窗口）...
where mvn >nul 2>&1
if errorlevel 1 goto use_idea_mvn
call mvn package -DskipTests
goto pkg_check
:use_idea_mvn
if not exist "D:\IntelliJ IDEA 2024.3.2.2\plugins\maven\lib\maven3\bin\mvn.cmd" goto no_mvn
call "D:\IntelliJ IDEA 2024.3.2.2\plugins\maven\lib\maven3\bin\mvn.cmd" package -DskipTests
goto pkg_check
:no_mvn
echo [错误] 未找到 Maven，请安装 Maven 或 IntelliJ IDEA
if not "%1"=="auto" pause
exit /b 1
:pkg_check
if exist "target\springBootTest-0.0.1-SNAPSHOT.jar" goto pkg_ok
echo [失败] 打包出错，请向上翻查看 Maven 报错
if not "%1"=="auto" pause
exit /b 1
:pkg_ok
echo [成功] 打包完成: target\springBootTest-0.0.1-SNAPSHOT.jar
if not "%1"=="auto" pause
exit /b 0
