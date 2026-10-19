@echo off
chcp 936 >nul
cd /d "%~dp0"
title 喵喵博客-后端启动器
setlocal EnableExtensions

set "LOG=%~dp0..\启动日志.txt"

REM ---------- 1. 检查 MySQL ----------
netstat -ano | findstr ":3306 " | findstr "LISTENING" >nul
if errorlevel 1 echo [警告] 未检测到 MySQL(3306)，后端将无法连接数据库

REM ---------- 2. 已在运行则退出 ----------
netstat -ano | findstr ":8081 " | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [提示] 后端已在运行，无需重复启动
    if not "%1"=="auto" pause
    exit /b 0
)

REM ---------- 3. 定位 Java 并设置 JAVA_HOME ----------
where java >nul 2>&1
if errorlevel 1 (
    if exist "C:\Program Files\Zulu\zulu-21\bin\java.exe" set "PATH=C:\Program Files\Zulu\zulu-21\bin;%PATH%"
)
where java >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Java，请安装 JDK 17 以上版本
    if not "%1"=="auto" pause
    exit /b 1
)
if exist "C:\Program Files\Zulu\zulu-21\bin\java.exe" set "JAVA_HOME=C:\Program Files\Zulu\zulu-21"

REM ---------- 4. 清除可能覆盖端口的环境变量 ----------
set "SERVER__PORT="
set "SERVER__HOST="

REM ---------- 5. 每次都重新打包（本机重置会删除 target，绝不使用旧产物） ----------
echo [步骤1] 正在重新打包后端 jar，需要 10秒-3分钟，请勿关闭窗口...
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
>> "%LOG%" echo [失败] 后端：未找到 Maven
if not "%1"=="auto" pause
exit /b 1
:pkg_check
if exist "target\springBootTest-0.0.1-SNAPSHOT.jar" goto db_check
echo [失败] 打包未生成 jar，请向上翻查看 Maven 报错
>> "%LOG%" echo [失败] 后端：打包失败
if not "%1"=="auto" pause
exit /b 1

REM ---------- 6. 数据库自检：blog_db 缺失则自动导入 ----------
:db_check
set "MYSQL=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if not exist "%MYSQL%" goto start_be
"%MYSQL%" -uroot -p123456 -N -B -e "USE blog_db;" >nul 2>&1
if errorlevel 1 goto import_db
echo [OK] 数据库 blog_db 正常
goto start_be
:import_db
echo [步骤2] 数据库 blog_db 不存在，正在导入 sql\blog.sql ...
"%MYSQL%" -uroot -p123456 --default-character-set=utf8mb4 -e "source sql/blog.sql" >nul 2>&1
if errorlevel 1 goto import_fail
echo [OK] 数据库已导入
goto start_be
:import_fail
echo [警告] 数据库导入失败，后端可能无法读取数据
>> "%LOG%" echo [警告] 后端：数据库 blog_db 导入失败

REM ---------- 7. 启动并等待就绪 ----------
:start_be
echo [步骤3] 正在启动后端服务...
start "喵喵博客-后端(关闭此窗口即停止)" cmd /k "java -XX:TieredStopAtLevel=1 -jar target\springBootTest-0.0.1-SNAPSHOT.jar 1>>run.log 2>&1"
echo 等待后端就绪（最长180秒）...
set /a n=0
:wait_be
netstat -ano | findstr ":8081 " | findstr "LISTENING" >nul
if not errorlevel 1 goto be_ok
set /a n+=1
if %n% GEQ 180 goto be_fail
timeout /t 1 >nul
goto wait_be
:be_fail
echo [失败] 后端 180 秒内未就绪，请查看 springBootTest\run.log
>> "%LOG%" echo [失败] 后端未启动
if not "%1"=="auto" pause
exit /b 1
:be_ok
echo [OK] 后端已就绪   http://localhost:8081
>> "%LOG%" echo [OK] 后端已就绪
if not "%1"=="auto" pause
exit /b 0
