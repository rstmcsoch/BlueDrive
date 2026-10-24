package com.moorixlabs.bluedrive;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** Pure legacy codec. Ambiguous pipe-containing records are skipped, never repaired destructively. */
public final class FileIndex {
 public static CloudFile parse(String line) {
  String[] p=line.split("\\|",-1);
  if(p.length!=6 && p.length!=8) return null;
  try {
   long size=Long.parseLong(p[2]), date=Long.parseLong(p[3]);
   if(p[0].isEmpty() || size<0 || !(p[5].equals("true")||p[5].equals("false"))) return null;
   return new CloudFile(p[0],p[1],size,date,p[4],Boolean.parseBoolean(p[5]),p.length==8?p[6]:"",p.length==8?p[7]:"");
  } catch(RuntimeException e) { return null; }
 }
 public static List<CloudFile> read(InputStream in) throws IOException {
  Map<String,CloudFile> files=new LinkedHashMap<>();
  try(BufferedReader reader=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))) {
   String line; while((line=reader.readLine())!=null) { CloudFile f=parse(line); if(f!=null) files.putIfAbsent(f.getId(),f); }
  } return new ArrayList<>(files.values());
 }
 public static int category(CloudFile f) {
  String e=f.getFileExtension().toLowerCase(Locale.ROOT);
  if(Arrays.asList("jpg","jpeg","png","gif","webp","bmp","heic").contains(e)) return 0;
  if(Arrays.asList("mp4","mkv","webm","mov","avi","3gp").contains(e)) return 1;
  if(Arrays.asList("pdf","doc","docx","txt","odt","xls","xlsx","ppt","pptx","csv").contains(e)) return 2;
  if(Arrays.asList("mp3","wav","ogg","flac","aac","m4a").contains(e)) return 3;
  if(Arrays.asList("zip","rar","7z","tar","gz").contains(e)) return 4;
  return 5;
 }
 public static void sort(List<CloudFile> f,int mode) {
  Comparator<CloudFile> c;
  switch(mode) {
   case 0: c=Comparator.comparing(CloudFile::getName,String.CASE_INSENSITIVE_ORDER); break;
   case 1: c=Comparator.comparing(CloudFile::getName,String.CASE_INSENSITIVE_ORDER).reversed(); break;
   case 3: c=Comparator.comparingLong(CloudFile::getDate); break;
   case 4: c=Comparator.comparingLong(CloudFile::getSize).reversed(); break;
   case 5: c=Comparator.comparingLong(CloudFile::getSize); break;
   case 6: c=Comparator.comparing(CloudFile::getFileExtension); break;
   default: c=Comparator.comparingLong(CloudFile::getDate).reversed();
  } f.sort(c.thenComparing(CloudFile::getId));
 }
}