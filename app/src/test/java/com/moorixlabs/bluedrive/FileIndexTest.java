package com.moorixlabs.bluedrive;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.Assert.*;

public class FileIndexTest {
    @Test public void readsSixFieldLegacyWithoutLosingPath() {
        CloudFile f=FileIndex.parse("id|été.jpg|17|123|content://provider/document/42|false");
        assertNotNull(f); assertEquals("été.jpg",f.getName());
        assertEquals("content://provider/document/42",f.getPath());
        assertEquals("",f.getFileId()); assertFalse(f.isUploaded());
    }
    @Test public void preservesRemoteIdentifiersAndEmptyTrailingFields() {
        CloudFile f=FileIndex.parse("x|a.pdf|12|0|/a.pdf|true|remote|message");
        assertEquals("remote",f.getFileId()); assertEquals("message",f.getMessageId());
        assertNotNull(FileIndex.parse("x|a|0|0||false||"));
    }
    @Test public void rejectsMalformedAndAmbiguousRecords() {
        assertNull(FileIndex.parse("x|a|oops|0||true"));
        assertNull(FileIndex.parse("x|a|-1|0||true"));
        assertNull(FileIndex.parse("x|a|1|0||maybe"));
        assertNull(FileIndex.parse("x|name|pipe|1|0|path|false"));
    }
    @Test public void deduplicatesWithoutOverwritingFirstRecord() throws Exception {
        String data="x|a|1|0||false\nx|b|2|0||true\nbroken\n";
        List<CloudFile> files=FileIndex.read(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
        assertEquals(1,files.size()); assertEquals("a",files.get(0).getName());
    }
    @Test public void categoriesAndSortAreDeterministic() {
        CloudFile audio=new CloudFile("1","Z.MP3",3,1,"",false);
        CloudFile pdf=new CloudFile("2","a.pdf",7,2,"",false);
        assertEquals(3,FileIndex.category(audio)); assertEquals(2,FileIndex.category(pdf));
        assertEquals(4,FileIndex.category(new CloudFile("3","a.zip",0,0,"",false)));
        List<CloudFile> files=new ArrayList<>(Arrays.asList(audio,pdf));
        FileIndex.sort(files,0); assertEquals(pdf,files.get(0));
        FileIndex.sort(files,5); assertEquals(audio,files.get(0));
        FileIndex.sort(files,2); assertEquals(pdf,files.get(0));
    }
    @Test public void productionRemoteGateCannotBeEnabledByCredentials() {
        assertTrue(StoragePolicy.remoteBlocked());
    }
}
