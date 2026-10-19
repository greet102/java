@echo off
chcp 936 >nul
cd /d "%~dp0"
title 喵喵博客-停止后端

set "PID="
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8081 " ^| findstr "LISTENING"') do set PID=%%a
if "%PID%"=="" (
    echo 后端未在运行
) else (
    taskkill /PID %PID% /F >nul 2>&1
    echo 已停止后端 (PID=%PID%)
)
if not "%1"=="auto" pause
