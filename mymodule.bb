DESCRIPTION = "My Kernel Module"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://mymodule.c;md5=c5ff74bd0cb860d2d1718e11955bfbc4"
SRC_URI = "file://mymodule.c file://Makefile"

S = "${WORKDIR}"

inherit module

do_compile() {
	echo "CFLAGS: ${CFLAGS}"
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
	oe_runmake KERNEL_SRC=${STAGING_KERNEL_DIR}
}

do_install() {
	install -d ${D}${base_libdir}/modules/${KERNEL_VERSION}/extra
	install -m 0644 mymodule.ko ${D}${base_libdir}/modules/${KERNEL_VERSION}/extra/
}
