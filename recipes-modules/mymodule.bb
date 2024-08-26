DESCRIPTION = "My Kernel Module"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://mymodule.c;md5=ae2757b109b624bd24c872216d2c37ed"
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
