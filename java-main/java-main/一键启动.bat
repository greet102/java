@echo off
chcp 936 >nul
cd /d "%~dp0"
title 喵喵博客-一键启动
setlocal EnableExtensions

set "LOG=%~dp0启动日志.txt"
>> "%LOG%" echo.
>> "%LOG%" echo ==================== %date% %time% 一键启动 ====================

echo ============================================
echo   喵喵博客 一键启动
echo   本机每次重置都会删除 target 和 node_modules,
echo   本脚本每次都会重新打包后端、重新安装前端依赖,
echo   全程约需 1-5 分钟, 窗口持续滚动输出属正常,
echo   请耐心等待, 不要中途关闭本窗口!
echo ============================================

echo.
echo ===== 步骤 1/2 启动后端 =====
call "%~dp0springBootTest\启动后端.bat" auto

echo.
echo ===== 步骤 2/2 启动前端 =====
call "%~dp0blog-vue\启动前端.bat" auto

echo.
echo ============================================
netstat -ano | findstr ":8081 " | findstr "LISTENING" >nul
if errorlevel 1 goto be_down
echo   [OK] 后端运行中   http://localhost:8081
goto fe_check
:be_down
echo   [失败] 后端未运行，请查看 springBootTest\run.log
:fe_check
netstat -ano | findstr ":5173 " | findstr "LISTENING" >nul
if errorlevel 1 goto fe_down
echo   [OK] 前端运行中   http://localhost:5173
goto summary
:fe_down
echo   [失败] 前端未运行，请查看 blog-vue\npm.log
:summary
echo ============================================
netstat -ano | findstr ":8081 " | findstr "LISTENING" >nul
if errorlevel 1 goto not_all
netstat -ano | findstr ":5173 " | findstr "LISTENING" >nul
if errorlevel 1 goto not_all
>> "%LOG%" echo [结果] 启动成功
echo   启动成功！浏览器访问  http://localhost:5173
echo   演示账号：喵站长 / 123456
goto end
:not_all
>> "%LOG%" echo [结果] 启动未完成
echo   启动未完成！请把本窗口内容或 启动日志.txt 截图反馈
:end
echo ============================================
pause
