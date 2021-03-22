cl -O1 /I. /c /MD /D_UNICODE /DUNICODE /DWIN32 /D_WINDOWS launcher.c
rc -DASTRONOMY_TOOLS_FILE_VERSION=\"0.1.0\" -DASTRONOMY_TOOLS_COMMA_VERSION=0,1,0 -DASTRONOMY_TOOLS_ORG_FILENAME=\"astronomy-tools.exe\" -r -fo astronomy-tools.res astronomy-tools.rc

move SVM-* SVM

link ^
launcher.obj ^
SVM\astronomy-tools.obj ^
swt_library\library\swt.obj ^
swt_library\library\callback.obj ^
swt_library\library\c.obj ^
swt_library\library\c_stats.obj ^
swt_library\library\os.obj ^
swt_library\library\os_structs.obj ^
swt_library\library\os_stats.obj ^
swt_library\library\com_structs.obj ^
swt_library\library\com.obj ^
swt_library\library\com_stats.obj ^
swt_library\library\com_custom.obj ^
astronomy-tools.res ^
/out:..\astronomy-tools.exe ^
/NODEFAULTLIB:LIBCMT ^
/LIBPATH:%JRE_LIB% ^
net.lib ^
java.lib ^
zip.lib ^
nio.lib ^
%JRE_LIB%\svm\clibraries\windows-amd64\ffi.lib ^
%JRE_LIB%\svm\clibraries\windows-amd64\strictmath.lib ^
%JRE_LIB%\svm\clibraries\windows-amd64\jvm.lib ^
%JRE_LIB%\svm\clibraries\windows-amd64\libchelper.lib ^
comctl32.lib ^
shell32.lib ^
imm32.lib ^
oleacc.lib ^
usp10.lib ^
wininet.lib ^
Crypt32.lib ^
Shlwapi.lib ^
Uxtheme.lib ^
Propsys.lib ^
Urlmon.lib ^
Msimg32.lib ^
ole32.lib ^
uuid.lib ^
oleaut32.lib ^
kernel32.lib ^
ws2_32.lib ^
mswsock.lib ^
advapi32.lib ^
bufferoverflowu.lib ^
user32.lib ^
gdi32.lib ^
comdlg32.lib ^
winspool.lib ^
userenv.lib ^
iphlpapi.lib

editbin /SUBSYSTEM:WINDOWS ..\astronomy-tools.exe
