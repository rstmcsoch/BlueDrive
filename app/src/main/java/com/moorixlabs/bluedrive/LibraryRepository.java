package com.moorixlabs.bluedrive;
import android.content.*;
import android.net.Uri;
import org.json.*;
import java.io.*;
import java.util.*;
/** Private JSON additions do not rewrite or migrate the user's external text index. */
public final class LibraryRepository {
 private final Context context;
 public LibraryRepository(Context c) { context=c.getApplicationContext(); }
 public List<CloudFile> load() {
  List<CloudFile> files=loadRaw();
  java.util.Set<String> hidden=context.getSharedPreferences("library_hidden",0).getStringSet("ids",java.util.Collections.emptySet());
  files.removeIf(f->hidden.contains(f.getId())); return files;
 }
 private List<CloudFile> loadRaw() {
  List<CloudFile> files=new ArrayList<>();
  File legacy=new File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS),"BlueDrive/bluedrive_files.txt");
  if(legacy.canRead()) try { files.addAll(FileIndex.read(new FileInputStream(legacy))); } catch(IOException ignored) {}
  try { JSONArray a=new JSONArray(readText(new File(context.getFilesDir(),"library.json")));
   for(int i=0;i<a.length();i++) { JSONObject o=a.getJSONObject(i); files.add(new CloudFile(o.getString("id"),o.getString("name"),o.getLong("size"),o.getLong("date"),o.getString("path"),o.getBoolean("uploaded"),o.optString("fileId"),o.optString("messageId"))); }
  } catch(Exception ignored) {}
  Map<String,CloudFile> unique=new LinkedHashMap<>(); for(CloudFile f:files) unique.putIfAbsent(f.getId(),f);
  return new ArrayList<>(unique.values());
 }
 private String readText(File f) throws IOException { try(InputStream in=new android.util.AtomicFile(f).openRead(); ByteArrayOutputStream out=new ByteArrayOutputStream()) { byte[] b=new byte[8192]; int n; while((n=in.read(b))!=-1) out.write(b,0,n); return out.toString("UTF-8"); } }
 public void hide(String id) {
  android.content.SharedPreferences prefs=context.getSharedPreferences("library_hidden",0);
  java.util.Set<String> ids=new java.util.HashSet<>(prefs.getStringSet("ids",java.util.Collections.emptySet()));
  ids.add(id); prefs.edit().putStringSet("ids",ids).apply();
 }
 public synchronized void merge(List<CloudFile> additions) throws Exception {
  // A damaged private index must never be silently replaced by a partial read.
  File existing=new File(context.getFilesDir(),"library.json");
  if(existing.exists()) {
   JSONArray check=new JSONArray(readText(existing));
   for(int i=0;i<check.length();i++) { JSONObject o=check.getJSONObject(i);o.getString("id");o.getString("name");o.getLong("size");o.getLong("date");o.getString("path");o.getBoolean("uploaded"); }
  }
  Map<String,CloudFile> map=new LinkedHashMap<>(); for(CloudFile f:loadRaw()) map.put(f.getId(),f); for(CloudFile f:additions) map.putIfAbsent(f.getId(),f);
  JSONArray a=new JSONArray(); for(CloudFile f:map.values()) { JSONObject o=new JSONObject(); o.put("id",f.getId()).put("name",f.getName()).put("size",f.getSize()).put("date",f.getDate()).put("path",f.getPath()).put("uploaded",f.isUploaded()).put("fileId",f.getFileId()).put("messageId",f.getMessageId()); a.put(o); }
  android.util.AtomicFile atomic=new android.util.AtomicFile(new File(context.getFilesDir(),"library.json"));
  FileOutputStream stream=atomic.startWrite(); try { stream.write(a.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)); atomic.finishWrite(stream); } catch(Exception e) { atomic.failWrite(stream); throw e; }
 }
}