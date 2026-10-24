"""Regression checks for the startup failures reported from GitHub Actions."""
from pathlib import Path
import shutil
import tempfile
import unittest
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from check_build_setup import ROOT, check


class BuildSetupTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        for relative in ('gradle/wrapper/gradle-wrapper.jar',
                         'gradle/wrapper/gradle-wrapper.properties',
                         'gradle/libs.versions.toml', 'gradlew',
                         'app/build.gradle.kts', 'build.gradle.kts',
                         '.github/workflows/build-apk.yml'):
            destination = self.root/relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(ROOT/relative, destination)

    def test_current_build_inputs(self):
        check(self.root)

    def test_missing_wrapper_fails_early(self):
        (self.root/'gradle/wrapper/gradle-wrapper.jar').unlink()
        with self.assertRaisesRegex(AssertionError, 'Missing gradle-wrapper.jar'):
            check(self.root)

    def test_corrupted_wrapper_fails_early(self):
        (self.root/'gradle/wrapper/gradle-wrapper.jar').write_bytes(b'not a JAR')
        with self.assertRaisesRegex(AssertionError, 'Unexpected Gradle'):
            check(self.root)

    def test_obsolete_sdk_package_is_rejected(self):
        p = self.root/'.github/workflows/build-apk.yml'
        p.write_text(p.read_text().replace('packages: platform-tools', 'packages: tools platform-tools'))
        with self.assertRaisesRegex(AssertionError, 'obsolete'):
            check(self.root)

    def test_glide_runtime_and_processor_must_match(self):
        p = self.root/'gradle/libs.versions.toml'
        p.write_text(p.read_text().replace('module = "com.github.bumptech.glide:compiler", version.ref = "glide"',
                                         'module = "com.github.bumptech.glide:compiler", version = "5.0.5"'))
        with self.assertRaisesRegex(AssertionError, 'Glide processor/runtime mismatch'):
            check(self.root)


if __name__ == '__main__':
    unittest.main()
