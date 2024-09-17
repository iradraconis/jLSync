#!/bin/bash

# Step 1: Build the jar (with dependencies)

# Step 2:
# Install Packager tools for Linux (RHEL)
# sudo dnf install fedora-packager fedora-review

# Linux: signing - erstelle oder importiere einen GPG-Schlüssel
# Ggfs. einen neuen GPG-Schlüssel erstellen: (nur RSA num Signieren)

# gpg --full-generate-key
# gpg --list-keys


# Step 3: Build the package
# MacOs 
# check if os is windows, linux, mac

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "Linux"
    # native package for linux corresponding to the current OS (Debian oder RPM) based.
    jpackage --input ./target/ --main-jar jLSync-0.1.jar --main-class com.iradraconis.jlsync.JLSync --icon ./src/main/resources/com/iradraconis/jlsync/resources/icon.png
elif [[ "$OSTYPE" == "darwin"* ]]; then
    echo "MacOS"
    jpackage --input ./target/ --main-jar jLSync-0.1.jar --main-class com.iradraconis.jlsync.JLSync --icon ./src/main/resources/com/iradraconis/jlsync/resources/icon.icns
else
    echo "Windows"
    # MSI Package
    jpackage --input ./target/ --main-jar jLSync-0.1.jar --main-class com.iradraconis.jlsync.JLSync --icon ./src/main/resources/com/iradraconis/jlsync/resources/icon.ico --type msi
fi

# Step 4: Install the package
# Fedora
# sudo dnf install ./jlsync-1.0-1.x86_64.rpm

# Step 5: Run the application

# Step 6: Uninstall the package
# dnf list installed | grep jlsync

# Deinstallieren - Fedora:
# sudo dnf remove jlsync