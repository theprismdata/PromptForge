@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem Usage:
rem   rollback_windows.bat [BACKUP_DIR] [ECLIPSE_HOME]
rem
rem Defaults:
rem   BACKUP_DIR   = latest under %USERPROFILE%\.eclipse_llm_patch_backups
rem   ECLIPSE_HOME = C:\eclipse (or env ECLIPSE_HOME)

if "%~2"=="" (
  if defined ECLIPSE_HOME (
    set "ECLIPSE_HOME=%ECLIPSE_HOME%"
  ) else (
    set "ECLIPSE_HOME=C:\eclipse"
  )
) else (
  set "ECLIPSE_HOME=%~2"
)

if not exist "%ECLIPSE_HOME%\plugins" (
  echo ERROR: Eclipse directory not found: %ECLIPSE_HOME%
  echo Set ECLIPSE_HOME or pass it as second argument.
  exit /b 1
)

if defined USERPROFILE (
  set "BACKUP_ROOT=%USERPROFILE%\.eclipse_llm_patch_backups"
) else (
  set "BACKUP_ROOT=%CD%\.eclipse_llm_patch_backups"
)

if not "%~1"=="" (
  set "BACKUP_DIR=%~1"
) else (
  for /f "usebackq delims=" %%I in (`powershell -NoProfile -Command "$root='%BACKUP_ROOT%'; if (Test-Path $root) {Get-ChildItem -Path $root -Directory ^| Sort-Object LastWriteTime -Descending ^| Select-Object -First 1 -ExpandProperty FullName}"`) do set "BACKUP_DIR=%%I"
)

if not defined BACKUP_DIR (
  echo ERROR: Backup directory not found.
  echo Usage: rollback_windows.bat ^<backup_dir^>
  exit /b 1
)

if not exist "%BACKUP_DIR%" (
  echo ERROR: Backup directory not found: %BACKUP_DIR%
  exit /b 1
)

call :restore_one "dropins\assistai\eclipse\plugins\com.github.gradusnikov.eclipse.plugin.assistai.main_1.0.7.202603202120.jar"
if errorlevel 1 exit /b 1

call :restore_one "dropins\llm-patch\eclipse\plugins\org.eclipse.ui.ide_3.23.100.v20260327-1616.jar"
if errorlevel 1 exit /b 1

echo.
echo Rollback complete from: %BACKUP_DIR%
echo Start Eclipse once with -clean.
exit /b 0

:restore_one
set "REL_DST=%~1"
set "BACKUP_FILE=%BACKUP_DIR%\%REL_DST%"
set "DST=%ECLIPSE_HOME%\%REL_DST%"
set "DST_DIR=%DST%"
for %%I in ("%DST_DIR%") do set "DST_DIR=%%~dpI"

if not exist "%BACKUP_FILE%" (
  echo Skip (no backup): %BACKUP_FILE%
  exit /b 0
)

if not exist "%DST_DIR%" mkdir "%DST_DIR%"
if errorlevel 1 (
  echo ERROR: Failed to create destination directory: %DST_DIR%
  exit /b 1
)

copy /Y "%BACKUP_FILE%" "%DST%" >nul
if errorlevel 1 (
  echo ERROR: Failed to restore file to: %DST%
  exit /b 1
)
echo Restored: %DST%
exit /b 0
