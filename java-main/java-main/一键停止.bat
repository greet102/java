@echo off
chcp 936 >nul
cd /d "%~dp0"
title ß÷ß÷²©¿Í-Ò»¼üÍ£Ö¹

echo ===== Í£Ö¹ºó¶Ë =====
call "springBootTest\Í£Ö¹ºó¶Ë.bat" auto

echo ===== Í£Ö¹Ç°¶Ë =====
call "blog-vue\Í£Ö¹Ç°¶Ë.bat" auto

echo ===== ÒÑÍ£Ö¹È«²¿·þÎñ =====
timeout /t 3 >nul
