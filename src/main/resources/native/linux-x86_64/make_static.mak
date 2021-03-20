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
AR = ar
CFLAGS = -I../graalvm/include -Wall

JAVA_HOME = {{javaHome}}
SVM_DIR = {{svmDir}}
SWT_LIBRARY_DIR = {{swtLibDir}}
SWT_VERSION=$(maj_ver)$(min_ver)r$(rev)

SWT_DEPS_LIB = $(shell pkg-config --libs-only-l gtk+-3.0 gthread-2.0 atk gio-2.0 webkit2gtk-web-extension-4.0 cairo gl glu)
JAVA_LIB = -Wl,-Bstatic $(JAVA_HOME)/lib/static/linux-amd64/glibc/libnet.a $(JAVA_HOME)/lib/static/linux-amd64/glibc/libextnet.a $(JAVA_HOME)/lib/static/linux-amd64/glibc/libjava.a $(JAVA_HOME)/lib/static/linux-amd64/glibc/libfdlibm.a $(JAVA_HOME)/lib/static/linux-amd64/glibc/libjaas.a $(JAVA_HOME)/lib/static/linux-amd64/glibc/libsunec.a $(JAVA_HOME)/lib/static/linux-amd64/glibc/libzip.a $(JAVA_HOME)/lib/static/linux-amd64/glibc/libnio.a $(JAVA_HOME)/lib/svm/clibraries/linux-amd64/liblibchelper.a $(JAVA_HOME)/lib/svm/clibraries/linux-amd64/libjvm.a
OS_LIB = -rdynamic -Wl,--whole-archive $(SWT_LIBRARY_DIR)/libswt-$(SWT_VERSION).a -Wl,-Bdynamic -lX11 -lstdc++ -lglib-2.0 -lz -ldl -pthread -Wl,--no-whole-archive $(SWT_DEPS_LIB)

SWT_OBJECTS = $(shell find $(SWT_LIBRARY_DIR) -type f -name "*.o" -and -not -name "*awt*" -and -not -name "*webkitgtk_structs*" -printf '%p ')

libswt-$(SWT_VERSION).a:
	$(AR) rcs $(SWT_LIBRARY_DIR)/libswt-$(SWT_VERSION).a $(SWT_OBJECTS)

%.o: %.c
	$(GCC) -c $< $(CFLAGS)

all: libswt-$(SWT_VERSION).a launcher.o
	$(GCC) -o ../{{artifactId}} launcher.o $(SVM_DIR)/{{artifactId}}.o $(JAVA_LIB) $(OS_LIB)
