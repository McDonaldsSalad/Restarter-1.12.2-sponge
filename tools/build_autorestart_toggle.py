#!/usr/bin/env python3
"""Build the Sponge 1.12.2 Restarter restart-toggle JAR from archived app bytecode.

Restarter's CFR-recovered source is for editing and reference; some original
classes do not decompile into compilable Java. To preserve other plugin behavior
this builder patches only three Java 8 application classes and bundles the
original-version MySQL 5.1.40 JDBC driver.

Uses JDK 21 only as a *build tool* for its internal ASM package. All patched
application classes remain Java 8 classfile version 52.
"""
from __future__ import annotations

import argparse
import base64
import hashlib
import io
import lzma
from pathlib import Path, PurePosixPath
import shutil
import subprocess
import tarfile
from urllib.request import urlopen
import zipfile

ROOT = Path(__file__).resolve().parent.parent
INPUT = ROOT / 'recovery' / 'restarter-app-classes.b85'
CONFIG = ROOT / 'src/main/resources/assets/restarter/config.yml'
OUTPUT = ROOT / 'build' / 'libs' / 'Restarter-1.21.3-SpongeForge-1.12.2-AutoRestartToggle.jar'
DRIVER_URL = 'https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/5.1.40/mysql-connector-java-5.1.40.jar'
APP_PACKAGE = 'net/craftersland/restarter/'
CHANGED_CLASSES = {'net/craftersland/restarter/ConfigHandler.class',
                   'net/craftersland/restarter/Engine$2.class',
                   'net/craftersland/restarter/Engine$3.class'}
ASM_EXPORTS = [
    '--add-exports=java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED',
    '--add-exports=java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED',
]


def app_entries():
    payload = lzma.decompress(base64.b85decode(''.join(INPUT.read_text('ascii').split())))
    entries = {}
    with tarfile.open(fileobj=io.BytesIO(payload), mode='r:') as tf:
        for member in tf:
            path = PurePosixPath(member.name)
            if not member.isfile() or path.is_absolute() or '..' in path.parts:
                raise ValueError('Unsafe recovery member: ' + member.name)
            stream = tf.extractfile(member)
            if stream is None or member.name in entries:
                raise ValueError('Unreadable or duplicate recovery member: ' + member.name)
            entries[member.name] = stream.read()
    classes = {name for name in entries if name.endswith('.class')}
    if len(classes) != 51 or not CHANGED_CLASSES.issubset(classes):
        raise RuntimeError('Expected 51 original Restarter application class files')
    for resource in ('plugin.yml','assets/restarter/config.yml','assets/restarter/data.yml'):
        if resource not in entries:
            raise RuntimeError('Missing resource: ' + resource)
    return entries


def mysql_entries(original_jar: str | None):
    if original_jar:
        with zipfile.ZipFile(original_jar) as z:
            data = Path(original_jar).read_bytes()
            print('Using original user-supplied JAR to source Connector/J', hashlib.sha256(data).hexdigest())
    else:
        print('Fetching bundled driver 5.1.40 from Maven Central')
        with urlopen(DRIVER_URL, timeout=90) as response:
            data = response.read()
        print('MySQL 5.1.40 SHA-256:', hashlib.sha256(data).hexdigest())
    result = {}
    with zipfile.ZipFile(io.BytesIO(data)) as z:
        for name in z.namelist():
            if name.endswith('/'):
                continue
            if (name.startswith('com/mysql/') or name.startswith('org/gjt/') or
                    name == 'META-INF/services/java.sql.Driver'):
                result[name] = z.read(name)
    if not all(name in result for name in ('com/mysql/jdbc/Driver.class',
                                            'com/mysql/jdbc/NonRegisteringDriver.class',
                                            'META-INF/services/java.sql.Driver')):
        raise RuntimeError('MySQL 5.1.40 driver incomplete')
    return result


def patch(entries):
    temp = ROOT / 'build' / 'bytecode'
    if temp.exists():
        shutil.rmtree(temp)
    for name, data in entries.items():
        if not name.endswith('.class'):
            continue
        target = temp / name
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    patch_classes = ROOT / 'build' / 'patcher'
    patch_classes.mkdir(parents=True, exist_ok=True)
    subprocess.run(['javac', *ASM_EXPORTS, '-d', str(patch_classes),
                    str(ROOT / 'tools' / 'PatchAutoRestart.java')], check=True)
    subprocess.run(['java', *ASM_EXPORTS, '-cp', str(patch_classes),
                    'PatchAutoRestart', str(temp)], check=True)
    changed = set()
    for name, old in list(entries.items()):
        if name.endswith('.class'):
            new = (temp / name).read_bytes()
            if new != old:
                changed.add(name)
                if new[6:8] != bytes([0, 52]):
                    raise RuntimeError('Patched class is not Java 8: ' + name)
                entries[name] = new
    if changed != CHANGED_CLASSES:
        raise RuntimeError('Unexpected class changes: ' + repr(changed))
    print('Only patched application classes:', ', '.join(sorted(changed)))


def build(original_jar=None):
    entries = app_entries()
    patch(entries)
    if 'AutoRestartEnabled: true' not in CONFIG.read_text('utf-8'):
        raise RuntimeError('Bundled source config does not include the default toggle')
    entries['assets/restarter/config.yml'] = CONFIG.read_bytes()
    extras = mysql_entries(original_jar)
    if entries.keys() & extras.keys():
        raise RuntimeError('Driver and plugin payload overlap')
    entries.update(extras)
    entries['META-INF/MANIFEST.MF'] = b'Manifest-Version: 1.0\r\n\r\n'
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(OUTPUT, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as out:
        for name in sorted(entries):
            out.writestr(name, entries[name])
    with zipfile.ZipFile(OUTPUT) as result:
        if result.testzip() is not None:
            raise RuntimeError('Invalid resulting plugin JAR')
        if b'AutoRestartEnabled: true' not in result.read('assets/restarter/config.yml'):
            raise RuntimeError('Missing config toggle in final JAR')
        for name in CHANGED_CLASSES:
            if b'isAutoRestartEnabled' not in result.read(name):
                raise RuntimeError('Toggle is missing in ' + name)
    print('Built:', OUTPUT)
    print('JAR bytes:', OUTPUT.stat().st_size)
    print('JAR SHA-256:', hashlib.sha256(OUTPUT.read_bytes()).hexdigest())


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--original-jar', help='Optional local source for the exact original bundled JDBC driver')
    build(parser.parse_args().original_jar)