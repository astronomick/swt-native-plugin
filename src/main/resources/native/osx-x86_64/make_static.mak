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

include ../swt/library/make_common.mak

GCC = gcc
LIBTOOL = libtool
CFLAGS = -I../graalvm/include -Wall

JAVA_HOME = {{javaHome}}
SVM_DIR = {{svmDir}}
SWT_LIBRARY_DIR = {{swtLibDir}}
SWT_VERSION=$(maj_ver)$(min_ver)r$(rev)

JAVA_LIB_DIR = -L$(JAVA_HOME)/lib/svm/clibraries/darwin-amd64 -L$(JAVA_HOME)/lib/static/darwin-amd64
JAVA_LIB = -lnet -lextnet -ljava -lfdlibm -ljaas -lsunec -lzip -lnio -llibchelper -ljvm -ldarwin -lprefs -lj2pkcs11 -lffi  -ljaas
OS_LIB = -lz -ldl -lstdc++ -lobjc -Wl,-framework,Cocoa -Wl,-framework,WebKit -Wl,-framework,CoreServices -Wl,-framework,JavaScriptCore -Wl,-framework,Security -Wl,-framework,SecurityInterface -Wl,-force_load,$(SWT_LIBRARY_DIR)/libswt-$(SWT_VERSION).a

SWT_OBJECTS = $(SWT_LIBRARY_DIR)/c.o $(SWT_LIBRARY_DIR)/c_stats.o $(SWT_LIBRARY_DIR)/callback.o $(SWT_LIBRARY_DIR)/os.o $(SWT_LIBRARY_DIR)/os_custom.o $(SWT_LIBRARY_DIR)/os_stats.o $(SWT_LIBRARY_DIR)/os_structs.o $(SWT_LIBRARY_DIR)/swt.o

libswt-$(SWT_VERSION).a:
	$(LIBTOOL) -static -no_warning_for_no_symbols -o $(SWT_LIBRARY_DIR)/libswt-$(SWT_VERSION).a $(SWT_OBJECTS)

%.o: %.c
	$(GCC) -c $(CFLAGS) $<

all: libswt-$(SWT_VERSION).a launcher.o
	$(GCC) launcher.o $(SVM_DIR)/{{artifactId}}.o $(JAVA_LIB) $(OS_LIB) -o ../{{artifactId}} $(JAVA_LIB_DIR)
