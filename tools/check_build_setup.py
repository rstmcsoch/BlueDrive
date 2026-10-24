#!/usr/bin/env python3
"""Offline build-input checks. This does not compile the Android application."""
import hashlib
from pathlib import Path
import re
import tomllib
import zipfile

ROOT = Path(__file__).resolve().parents[1]
WRAPPER_SHA256 = '81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f'


def check(root=ROOT):
    wrapper = root / 'gradle/wrapper/gradle-wrapper.jar'
    assert wrapper.is_file(), 'Missing gradle-wrapper.jar: gradlew cannot start'
    assert hashlib.sha256(wrapper.read_bytes()).hexdigest() == WRAPPER_SHA256, 'Unexpected Gradle 8.13 wrapper JAR'
    with zipfile.ZipFile(wrapper) as jar:
        assert jar.testzip() is None, 'Corrupt wrapper archive'
        assert 'org/gradle/wrapper/GradleWrapperMain.class' in jar.namelist()
    properties = (root/'gradle/wrapper/gradle-wrapper.properties').read_text()
    assert 'gradle-8.13-bin.zip' in properties
    assert 'gradle/wrapper/gradle-wrapper.jar' in (root/'gradlew').read_text()

    catalog = tomllib.loads((root/'gradle/libs.versions.toml').read_text())
    modules = [lib['module'] for lib in catalog['libraries'].values()]
    assert len(modules) == len(set(modules)), 'Duplicate dependency coordinates in catalog'
    for lib in catalog['libraries'].values():
        if isinstance(lib.get('version'), dict):
            assert lib['version']['ref'] in catalog['versions'], 'Undefined version reference'
    for script in (root/'app/build.gradle.kts', root/'build.gradle.kts'):
        text = script.read_text()
        for alias in re.findall(r'libs\.([\w.]+)', text):
            if alias.startswith('plugins.'):
                assert alias[8:].replace('.', '-') in catalog['plugins'], alias
            else:
                assert alias.replace('.', '-') in catalog['libraries'], alias
        assert not re.search(r'implementation\("com.github.bumptech.glide:', text), 'Use one catalog Glide version'
    assert catalog['libraries']['compiler']['version'] == catalog['libraries']['glide']['version'], 'Glide processor/runtime mismatch'
    workflow = (root/'.github/workflows/build-apk.yml').read_text()
    assert re.search(r'^\s+packages:\s+platform-tools\s*$', workflow, re.M), 'Override obsolete setup-android tools default'
    assert 'platforms;android-36' in workflow and 'assembleDebug' in workflow
    assert 'app/build/outputs/apk/debug/' in workflow


if __name__ == '__main__':
    check()
    print('PASS: wrapper integrity, catalog references, aligned Glide, SDK setup and APK artifact path.')
