"""Regression coverage for the launcher mipmap/drawable linking failure."""
from pathlib import Path
import sys
import tempfile
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from resource_assets import missing_asset_references


class ResourceAssetsTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.res = Path(self.temp.name)
        (self.res/'drawable').mkdir()
        (self.res/'drawable/ic_launcher_foreground.xml').write_text('<vector/>')
        (self.res/'mipmap-anydpi-v26').mkdir()
        self.icon = self.res/'mipmap-anydpi-v26/ic_launcher.xml'

    def test_original_wrong_namespace_is_caught(self):
        self.icon.write_text('<adaptive-icon><foreground value="@mipmap/ic_launcher_foreground"/></adaptive-icon>')
        errors = missing_asset_references(self.res)
        self.assertEqual(1, len(errors))
        self.assertIn('missing mipmap/ic_launcher_foreground', errors[0])

    def test_correct_drawable_reference_resolves(self):
        self.icon.write_text('<adaptive-icon><foreground value="@drawable/ic_launcher_foreground"/></adaptive-icon>')
        self.assertEqual([], missing_asset_references(self.res))

    def test_qualified_asset_and_system_reference(self):
        self.icon.write_text('<icon value="@android:drawable/ic_menu_view"/>')
        (self.res/'layout').mkdir()
        (self.res/'layout/example.xml').write_text('<view icon="@mipmap/ic_launcher"/>')
        self.assertEqual([], missing_asset_references(self.res))

    def test_repository_assets_resolve(self):
        main = Path(__file__).resolve().parents[2]/'app/src/main'
        self.assertEqual([], missing_asset_references(main/'res', main/'AndroidManifest.xml'))
