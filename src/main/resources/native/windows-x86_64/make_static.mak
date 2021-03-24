#
# Copyright © 2021, Michael Barbeaux
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#       notice, this list of conditions and the following disclaimer in the
#       documentation and/or other materials provided with the distribution.
#     * Neither the name of the <organization> nor the
#       names of its contributors may be used to endorse or promote products
#       derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

!include <..\swt\library\make_common.mak>

CL = cl
RC = rc
LINK = link
CFLAGS = -O1 /I..\graalvm\include /c /MD /D_UNICODE /DUNICODE /DWIN32 /D_WINDOWS

JAVA_HOME = {{javaHome}}
SVM_DIR = {{svmDir}}
SWT_LIBRARY_DIR = {{swtLibDir}}
SWT_VERSION=$(maj_ver)$(min_ver)r$(rev)

JAVA_LIB = "/LIBPATH:$(JAVA_HOME)\lib\static\windows-amd64" "/LIBPATH:$(JAVA_HOME)\lib\svm\clibraries\windows-amd64" net.lib java.lib zip.lib nio.lib ffi.lib jvm.lib libchelper.lib
OS_LIB = /NODEFAULTLIB:LIBCMT Advapi32.lib Comctl32.lib Comdlg32.lib Crypt32.lib Gdi32.lib Imm32.lib Iphlpapi.lib Msimg32.lib Ole32.lib OleAut32.lib Propsys.lib Shell32.lib Shlwapi.lib Urlmon.lib Userenv.lib Usp10.lib UxTheme.lib Version.lib Wininet.lib Winspool.lib Ws2_32.lib

SWT_OBJECTS = $(SWT_LIBRARY_DIR)\swt.obj $(SWT_LIBRARY_DIR)\callback.obj $(SWT_LIBRARY_DIR)\c.obj $(SWT_LIBRARY_DIR)\c_stats.obj $(SWT_LIBRARY_DIR)\os.obj $(SWT_LIBRARY_DIR)\os_structs.obj $(SWT_LIBRARY_DIR)\os_stats.obj $(SWT_LIBRARY_DIR)\com_structs.obj $(SWT_LIBRARY_DIR)\com.obj $(SWT_LIBRARY_DIR)\com_stats.obj $(SWT_LIBRARY_DIR)\com_custom.obj

.c.obj:
	cl $(CFLAGS) $*.c

{{artifactId}}.res:
    $(RC) -r -fo {{artifactId}}.res {{artifactId}}.rc

{{artifactId}}.exe: launcher.obj {{artifactId}}.res
    $(LINK) /out:..\{{artifactId}}.exe launcher.obj "$(SVM_DIR)\{{artifactId}}.obj" $(SWT_OBJECTS) {{artifactId}}.res $(JAVA_LIB) $(OS_LIB)

all: {{artifactId}}.exe
    editbin /SUBSYSTEM:WINDOWS ..\{{artifactId}}.exe






#cl -O1 /I. /c /MD /D_UNICODE /DUNICODE /DWIN32 /D_WINDOWS launcher.c
#rc -DASTRONOMY_TOOLS_FILE_VERSION=\"0.1.0\" -DASTRONOMY_TOOLS_COMMA_VERSION=0,1,0 -DASTRONOMY_TOOLS_ORG_FILENAME=\"astronomy-tools.exe\" -r -fo astronomy-tools.res astronomy-tools.rc

#move SVM-* SVM

#link ^
#launcher.obj ^
#SVM\astronomy-tools.obj ^
#swt_library\library\swt.obj ^
#swt_library\library\callback.obj ^
#swt_library\library\c.obj ^
#swt_library\library\c_stats.obj ^
#swt_library\library\os.obj ^
#swt_library\library\os_structs.obj ^
#swt_library\library\os_stats.obj ^
#swt_library\library\com_structs.obj ^
#swt_library\library\com.obj ^
#swt_library\library\com_stats.obj ^
#swt_library\library\com_custom.obj ^
#astronomy-tools.res ^
#/out:..\astronomy-tools.exe ^
#/NODEFAULTLIB:LIBCMT ^
#/LIBPATH:%JRE_LIB% ^
#net.lib ^
#java.lib ^
#zip.lib ^
#nio.lib ^
#%JRE_LIB%\svm\clibraries\windows-amd64\ffi.lib ^
#%JRE_LIB%\svm\clibraries\windows-amd64\strictmath.lib ^
#%JRE_LIB%\svm\clibraries\windows-amd64\jvm.lib ^
#%JRE_LIB%\svm\clibraries\windows-amd64\libchelper.lib ^
#comctl32.lib ^
#shell32.lib ^
#imm32.lib ^
#oleacc.lib ^
#usp10.lib ^
#wininet.lib ^
#Crypt32.lib ^
#Shlwapi.lib ^
#Uxtheme.lib ^
#Propsys.lib ^
#Urlmon.lib ^
#Msimg32.lib ^
#ole32.lib ^
#uuid.lib ^
#oleaut32.lib ^
#kernel32.lib ^
#ws2_32.lib ^
#mswsock.lib ^
#advapi32.lib ^
#bufferoverflowu.lib ^
#user32.lib ^
#gdi32.lib ^
#comdlg32.lib ^
#winspool.lib ^
#userenv.lib ^
#iphlpapi.lib

#editbin /SUBSYSTEM:WINDOWS ..\astronomy-tools.exe
