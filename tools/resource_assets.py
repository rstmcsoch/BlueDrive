"""Validate app-owned file resource references without resolving Android libraries."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

# Restrict this check to file assets. Styles/attrs may come from Material/AndroidX.
ASSET_TYPES = {'drawable', 'mipmap', 'layout', 'xml', 'raw', 'font', 'menu', 'anim', 'animator'}
REFERENCE = re.compile(r'@(?P<type>' + '|'.join(sorted(ASSET_TYPES)) + r')/(?P<name>[\w.]+)')


def missing_asset_references(res: Path, manifest: Path = None):
    available = {kind: set() for kind in ASSET_TYPES}
    for folder in res.iterdir():
        kind = folder.name.split('-')[0]
        if folder.is_dir() and kind in available:
            for path in folder.iterdir():
                if path.is_file():
                    available[kind].add(path.name.split('.')[0])
    xml_files = list(res.rglob('*.xml'))
    # File resources can also be declared as aliases in values resources.
    for path in xml_files:
        tree = ET.parse(path)
        if tree.getroot().tag == 'resources':
            for item in tree.getroot():
                kind = item.attrib.get('type', item.tag)
                if kind in available and 'name' in item.attrib:
                    available[kind].add(item.attrib['name'])
    if manifest is not None:
        xml_files.append(manifest)
    errors = []
    for path in xml_files:
        for match in REFERENCE.finditer(path.read_text()):
            kind, name = match.group('type', 'name')
            if name not in available[kind]:
                errors.append(f'{path}: missing {kind}/{name}')
    return errors
