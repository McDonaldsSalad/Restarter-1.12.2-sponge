#!/usr/bin/env python3
"""Reconstruct Restarter's application bytecode and decompile it with CFR 0.152.

Original bundled Connector/J classes are intentionally not included.
This script only generates recovered source; it does not modify plugin behavior.
"""
from __future__ import annotations

import base64
import hashlib
import io
import lzma
import pathlib
import shutil
import subprocess
import sys
import tarfile
import urllib.request
import zipfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCE_ARCHIVE = ROOT / "recovery" / "restarter-app-classes.b85"
RECOVERED_JAR = ROOT / "build" / "recovery" / "restarter-application-bytecode.jar"
CFR_JAR = ROOT / "tools" / "cfr-0.152.jar"
CFR_URL = "https://repo.maven.apache.org/maven2/org/benf/cfr/0.152/cfr-0.152.jar"
JAVA_OUT = ROOT / "src" / "main" / "java"
RES_OUT = ROOT / "src" / "main" / "resources"
REPORT = ROOT / "docs" / "DECOMPILATION_REPORT.md"
EXPECTED_INPUT_SHA256 = "4a7b4955f4a96147fce497b1f63653c9352e6919d9f36a7f1af5ab3e76242bb0"


def reconstruct():
    encoded = "".join(SOURCE_ARCHIVE.read_text(encoding="ascii").split())
    archive_bytes = lzma.decompress(base64.b85decode(encoded))
    items = {}
    with tarfile.open(fileobj=io.BytesIO(archive_bytes), mode="r:") as archive:
        for member in archive:
            if not member.isfile():
                raise RuntimeError(f"Unexpected archive entry: {member.name}")
            path = pathlib.PurePosixPath(member.name)
            if path.is_absolute() or ".." in path.parts:
                raise RuntimeError(f"Unsafe archive member: {member.name}")
            payload = archive.extractfile(member)
            if payload is None:
                raise RuntimeError(f"Unreadable member: {member.name}")
            items[member.name] = payload.read()

    classes = [name for name in items if name.endswith(".class")]
    assert len(classes) == 51, f"Expected 51 Restarter class files, got {len(classes)}"
    assert "net/craftersland/restarter/RR.class" in items
    RECOVERED_JAR.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(RECOVERED_JAR, "w", zipfile.ZIP_DEFLATED) as output:
        for name, data in sorted(items.items()):
            output.writestr(name, data)
    print(f"Reconstructed {len(classes)} application classes into {RECOVERED_JAR}")
    return items, classes


def run_decompiler():
    if not CFR_JAR.exists():
        print(f"Fetching CFR from {CFR_URL}")
        with urllib.request.urlopen(CFR_URL, timeout=60) as response:
            payload = response.read()
        CFR_JAR.write_bytes(payload)

    if JAVA_OUT.exists():
        shutil.rmtree(JAVA_OUT)
    JAVA_OUT.mkdir(parents=True)
    command = [
        "java", "-jar", str(CFR_JAR),
        str(RECOVERED_JAR),
        "--outputdir", str(JAVA_OUT),
        "--silent", "true",
        "--clobber", "true",
    ]
    print("Running CFR 0.152...")
    subprocess.run(command, check=True)
    files = sorted(JAVA_OUT.rglob("*.java"))
    if not files:
        raise RuntimeError("CFR did not output any Java source files")
    print(f"Recovered {len(files)} Java source files")
    return files


def write_resources(items):
    RES_OUT.mkdir(parents=True, exist_ok=True)
    for name in ("plugin.yml", "assets/restarter/config.yml", "assets/restarter/data.yml"):
        data = items.get(name)
        if data is None:
            raise RuntimeError(f"Missing required resource: {name}")
        dest = RES_OUT / name
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(data)


def write_report(classes, files):
    REPORT.parent.mkdir(parents=True, exist_ok=True)
    report = f"""# Decompiled source recovery report

- Source input: `Plugin-Restarter-1.21.3-7.2-jdbc-registration(1).jar`
- Original JAR SHA-256: `{EXPECTED_INPUT_SHA256}`
- Recovered application bytecode: **{len(classes)}** `.class` files
- CFR 0.152 outputs: **{len(files)}** `.java` files
- Resources copied: `plugin.yml`, `assets/restarter/config.yml`, `assets/restarter/data.yml`
- Separate bundled MySQL Connector/J: **excluded** from source recovery
- Sponge entry point: `net.craftersland.restarter.RR`
- Sponge annotation in binary: `restarter`, version `1.21.3`
- Legacy Bukkit metadata `plugin.yml`: version `1.18.11` (mismatch)

The generated Java code is **best-effort decompiled output**, not the original project source.
No successful compilation, integration testing, or production verification is implied.

The original application's external dependencies (SpongeAPI, Forge/Minecraft,
and mod APIs) may require manual configuration before the recovered code builds.
"""
    REPORT.write_text(report, encoding="utf-8")


def main():
    items, classes = reconstruct()
    files = run_decompiler()
    write_resources(items)
    write_report(classes, files)
    print("Recovery finished.")


if __name__ == "__main__":
    try:
        main()
    except Exception as ex:
        print(f"Recovery failed: {ex}", file=sys.stderr)
        raise
