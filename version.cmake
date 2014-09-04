string(TIMESTAMP VERSION %Y%m%d%H%M%S UTC)
file(WRITE ${CMAKE_SOURCE_DIR}/resources/us/ihmc/codecs/Version.java "package us.ihmc.codecs;\n\npublic class Version\n{\n\tpublic static final String VERSION = \"${VERSION}\";\n}")

