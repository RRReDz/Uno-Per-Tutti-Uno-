start /b cmd /c call cleanandbuild.bat >nul 2>&1
timeout 3
start /b cmd /c call webserver.bat >nul 2>&1
start /b cmd /c call clientgui.bat >nul 2>&1
exit