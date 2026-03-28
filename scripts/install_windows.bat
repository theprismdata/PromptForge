@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem Usage:
rem   install_windows.bat [ECLIPSE_HOME] [PACKAGE_DIR]
rem
rem Defaults:
rem   ECLIPSE_HOME = C:\eclipse
rem   PACKAGE_DIR  = script parent directory (expected extracted bundle root)

set "SCRIPT_DIR=%~dp0"
set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"
for %%I in ("%SCRIPT_DIR%\..") do set "DEFAULT_PACKAGE_DIR=%%~fI"

if "%~1"=="" (
  if defined ECLIPSE_HOME (
    set "ECLIPSE_HOME=%ECLIPSE_HOME%"
  ) else (
    set "ECLIPSE_HOME=C:\eclipse"
  )
) else (
  set "ECLIPSE_HOME=%~1"
)

if "%~2"=="" (
  if defined PACKAGE_DIR (
    set "PACKAGE_DIR=%PACKAGE_DIR%"
  ) else (
    set "PACKAGE_DIR=%DEFAULT_PACKAGE_DIR%"
  )
) else (
  set "PACKAGE_DIR=%~2"
)

if not exist "%ECLIPSE_HOME%\plugins" (
  echo ERROR: Eclipse directory not found: %ECLIPSE_HOME%
  echo Set ECLIPSE_HOME or pass it as first argument.
  exit /b 1
)

set "ASSISTAI_SRC=%PACKAGE_DIR%\payload\assistai\plugins\com.github.gradusnikov.eclipse.plugin.assistai.main_1.0.7.202603202120.jar"
set "ECLIPSE_UI_SRC=%PACKAGE_DIR%\payload\llm-patch\plugins\org.eclipse.ui.ide_3.23.100.v20260327-1616.jar"

if not exist "%ASSISTAI_SRC%" (
  echo ERROR: Missing payload file: %ASSISTAI_SRC%
  exit /b 1
)
if not exist "%ECLIPSE_UI_SRC%" (
  echo ERROR: Missing payload file: %ECLIPSE_UI_SRC%
  exit /b 1
)

if defined USERPROFILE (
  set "BACKUP_ROOT=%USERPROFILE%\.eclipse_llm_patch_backups"
) else (
  set "BACKUP_ROOT=%CD%\.eclipse_llm_patch_backups"
)

for /f %%I in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set "STAMP=%%I"
if not defined STAMP (
  set "STAMP=backup"
)
set "BACKUP_DIR=%BACKUP_ROOT%\%STAMP%"

call :install_one "%ASSISTAI_SRC%" "dropins\assistai\eclipse\plugins\com.github.gradusnikov.eclipse.plugin.assistai.main_1.0.7.202603202120.jar"
if errorlevel 1 exit /b 1

call :install_one "%ECLIPSE_UI_SRC%" "dropins\llm-patch\eclipse\plugins\org.eclipse.ui.ide_3.23.100.v20260327-1616.jar"
if errorlevel 1 exit /b 1

(
  echo Rollback command:
  echo   rollback_windows.bat "%BACKUP_DIR%"
) > "%BACKUP_DIR%\restore_hint.txt"

echo.
echo Install complete.
echo Backup saved to: %BACKUP_DIR%
echo Start Eclipse once with -clean.
exit /b 0

:install_one
set "SRC=%~1"
set "REL_DST=%~2"
set "DST=%ECLIPSE_HOME%\%REL_DST%"
set "DST_DIR=%DST%"
for %%I in ("%DST_DIR%") do set "DST_DIR=%%~dpI"
set "BACKUP_TARGET=%BACKUP_DIR%\%REL_DST%"
set "BACKUP_DIR_FOR_FILE=%BACKUP_TARGET%"
for %%I in ("%BACKUP_DIR_FOR_FILE%") do set "BACKUP_DIR_FOR_FILE=%%~dpI"

if not exist "%SRC%" (
  echo ERROR: Missing payload file: %SRC%
  exit /b 1
)

if not exist "%DST_DIR%" mkdir "%DST_DIR%"
if errorlevel 1 (
  echo ERROR: Failed to create destination directory: %DST_DIR%
  exit /b 1
)

if not exist "%BACKUP_DIR_FOR_FILE%" mkdir "%BACKUP_DIR_FOR_FILE%"
if errorlevel 1 (
  echo ERROR: Failed to create backup directory: %BACKUP_DIR_FOR_FILE%
  exit /b 1
)

if exist "%DST%" (
  copy /Y "%DST%" "%BACKUP_TARGET%" >nul
  if errorlevel 1 (
    echo ERROR: Failed to backup existing file: %DST%
    exit /b 1
  )
  echo Backed up: %DST%
)

copy /Y "%SRC%" "%DST%" >nul
if errorlevel 1 (
  echo ERROR: Failed to install file to: %DST%
  exit /b 1
)
echo Installed: %DST%
exit /b 0
