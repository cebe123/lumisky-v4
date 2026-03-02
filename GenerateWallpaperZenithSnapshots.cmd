@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "SCRIPT_DIR=%~dp0"
set "HOST_OUTPUT_DIR=%SCRIPT_DIR%snapshot-output"
set "HOST_RAW_SUBDIR=%HOST_OUTPUT_DIR%\zenith-snapshots"
set "HOST_PNG_SUBDIR=%HOST_OUTPUT_DIR%\zenith-png"
set "APP_WEBP_ASSET_DIR=%SCRIPT_DIR%app\src\main\assets\previews\zenith"
set "APK_FILE=%SCRIPT_DIR%app\build\outputs\apk\debug\app-debug.apk"
set "DEVICE_OUTPUT_DIR=/sdcard/Android/data/com.example.lumisky/files/Pictures/zenith-snapshots"
set "DEVICE_MARKER_FILE=%DEVICE_OUTPUT_DIR%/completed.txt"
set "TARGET_ACTIVITY=com.example.lumisky/.snapshot.ZenithSnapshotActivity"
set "MAX_WAIT_SECONDS=180"
set "ADB_EXE="
set "GIT_BASH_EXE="

if exist "C:\platform-tools\adb.exe" (
	set "ADB_EXE=C:\platform-tools\adb.exe"
)

if exist "C:\Program Files\Git\bin\bash.exe" (
	set "GIT_BASH_EXE=C:\Program Files\Git\bin\bash.exe"
)

if not defined GIT_BASH_EXE (
	echo Git Bash bulunamadi. Bu script gradle ve WebP donusumu icin Git Bash gerektirir.
	pause
	exit /b 1
)

if not defined ADB_EXE (
	where adb >nul 2>nul
	if errorlevel 1 (
		echo adb bulunamadi. Android platform-tools klasorunu PATH'e ekleyin.
		pause
		exit /b 1
	)
	set "ADB_EXE=adb"
)

pushd "%SCRIPT_DIR%"
echo Guncel debug build hazirlaniyor...
"%GIT_BASH_EXE%" -lc "./gradlew :app:assembleDebug --console=plain"
set "BUILD_EXIT=%ERRORLEVEL%"
popd
if not "!BUILD_EXIT!"=="0" (
	echo Gradle build basarisiz.
	pause
	exit /b 1
)

if not exist "%APK_FILE%" (
	echo Debug APK bulunamadi:
	echo %APK_FILE%
	pause
	exit /b 1
)

"%ADB_EXE%" start-server >nul 2>nul

set "ADB_STATE="
for /f %%S in ('"%ADB_EXE%" get-state 2^>nul') do set "ADB_STATE=%%S"
if /i not "%ADB_STATE%"=="device" (
	echo Bagli ve hazir bir Android cihaz bulunamadi.
	echo USB debugging acik oldugundan emin olun.
	pause
	exit /b 1
)

if exist "%HOST_RAW_SUBDIR%" (
	rmdir /s /q "%HOST_RAW_SUBDIR%" >nul 2>nul
)

if exist "%HOST_PNG_SUBDIR%" (
	rmdir /s /q "%HOST_PNG_SUBDIR%" >nul 2>nul
)

if not exist "%HOST_OUTPUT_DIR%" (
	mkdir "%HOST_OUTPUT_DIR%"
)

echo Guncel debug APK cihaza yukleniyor...
"%ADB_EXE%" install -r "%APK_FILE%" >nul
if errorlevel 1 (
	echo APK cihaza yuklenemedi.
	pause
	exit /b 1
)

"%ADB_EXE%" shell input keyevent KEYCODE_WAKEUP >nul 2>nul
"%ADB_EXE%" shell rm -rf %DEVICE_OUTPUT_DIR% >nul 2>nul
"%ADB_EXE%" shell am start -S -W -n %TARGET_ACTIVITY% >nul
if errorlevel 1 (
	echo Snapshot activity baslatilamadi.
	pause
	exit /b 1
)

set /a ELAPSED_SECONDS=0
:wait_for_output
"%ADB_EXE%" shell ls %DEVICE_MARKER_FILE% >nul 2>nul
if not errorlevel 1 goto pull_output

if %ELAPSED_SECONDS% GEQ %MAX_WAIT_SECONDS% goto wait_timeout

set /a ELAPSED_SECONDS+=1
timeout /t 1 /nobreak >nul
goto wait_for_output

:pull_output
"%ADB_EXE%" pull "%DEVICE_OUTPUT_DIR%" "%HOST_OUTPUT_DIR%" >nul
if errorlevel 1 (
	echo Snapshot klasoru cihazdan bilgisayara kopyalanamadi.
	pause
	exit /b 1
)

if not exist "%HOST_RAW_SUBDIR%" (
	echo Snapshot klasoru kopyalandi ama beklenen klasor bulunamadi.
	pause
	exit /b 1
)

pushd "%SCRIPT_DIR%"
echo PNG snapshotlar local WebP assetlerine donusturuluyor...
"%GIT_BASH_EXE%" -lc "./gradlew :app:syncZenithPreviewAssets --console=plain"
set "SYNC_EXIT=%ERRORLEVEL%"
popd
if not "!SYNC_EXIT!"=="0" (
	echo Snapshot WebP senkronizasyonu basarisiz.
	pause
	exit /b 1
)

if exist "%HOST_RAW_SUBDIR%" (
	rmdir /s /q "%HOST_RAW_SUBDIR%" >nul 2>nul
)

dir /b "%APP_WEBP_ASSET_DIR%\*.webp" >nul 2>nul
if errorlevel 1 (
	echo WebP snapshot assetleri olusturulamadi.
	pause
	exit /b 1
)

echo Local PNG snapshotlar:
echo %HOST_PNG_SUBDIR%
echo WebP assetleri:
echo %APP_WEBP_ASSET_DIR%
pause
exit /b 0

:wait_timeout
echo Snapshotlar %MAX_WAIT_SECONDS% saniye icinde tamamlanmadi.
echo Cihaz ekraninin acik kaldigindan emin olun.
pause
exit /b 1
