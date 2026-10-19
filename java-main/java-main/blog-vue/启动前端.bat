@echo off
chcp 936 >nul
cd /d "%~dp0"
title 喵喵博客-前端启动器
setlocal EnableExtensions

set "LOG=%~dp0..\启动日志.txt"

REM ---------- 1. 已在运行则退出 ----------
netstat -ano | findstr ":5173 " | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [提示] 前端已在运行，无需重复启动
    if not "%1"=="auto" pause
    exit /b 0
)

REM ---------- 2. 定位 npm ----------
where npm >nul 2>&1
if not errorlevel 1 goto node_ok
if exist "D:\Program Files\nodejs\npm.cmd" (
    set "PATH=D:\Program Files\nodejs;%PATH%"
    goto node_ok
)
if exist "%USERPROFILE%\.workbuddy\binaries\node\versions\22.22.2-2\npm.cmd" (
    set "PATH=%USERPROFILE%\.workbuddy\binaries\node\versions\22.22.2-2;%PATH%"
    goto node_ok
)
echo [错误] 未找到 Node.js，请先安装 Node.js
>> "%LOG%" echo [失败] 前端：未找到 Node.js
if not "%1"=="auto" pause
exit /b 1
:node_ok

REM ---------- 3. 每次都重新安装依赖（node_modules 会被重置删除，绝不使用旧内容） ----------
echo [步骤1] 正在安装前端依赖（国内镜像加速，约10秒-2分钟）...
call npm install --registry=https://registry.npmmirror.com --no-audit --no-fund
if exist "node_modules\.bin\vite.cmd" goto inst_ok
echo [失败] 依赖安装失败，请检查网络后重试
>> "%LOG%" echo [失败] 前端：依赖安装失败
if not "%1"=="auto" pause
exit /b 1
:inst_ok
echo [OK] 依赖已就绪

REM ---------- 4. 启动并等待就绪 ----------
echo [步骤2] 正在启动前端开发服务器...
start "喵喵博客-前端(关闭此窗口即停止)" cmd /k "npm run dev 1>>npm.log 2>&1"
echo 等待前端就绪（最长120秒）...
set /a m=0
:wait_fe
netstat -ano | findstr ":5173 " | findstr "LISTENING" >nul
if not errorlevel 1 goto fe_ok
set /a m+=1
if %m% GEQ 120 goto fe_fail
timeout /t 1 >nul
goto wait_fe
:fe_fail
echo [失败] 前端 120 秒内未就绪，请查看 blog-vue\npm.log
>> "%LOG%" echo [失败] 前端未启动
if not "%1"=="auto" pause
exit /b 1
:fe_ok
echo [OK] 前端已就绪   http://localhost:5173
>> "%LOG%" echo [OK] 前端已就绪
if not "%1"=="auto" pause
exit /b 0
