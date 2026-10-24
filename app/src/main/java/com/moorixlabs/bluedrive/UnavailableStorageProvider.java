package com.moorixlabs.bluedrive;
import android.net.Uri;
import java.util.List;
/** Explicitly unavailable, not a mock and never reports a successful transfer. */
public final class UnavailableStorageProvider implements StorageProvider {
 private final String reason;
 public UnavailableStorageProvider(String reason) { this.reason = reason; }
 public String id() { return "unavailable"; } public boolean isConnected() { return false; }
 public void enumerate(Callback<List<CloudFile>> c) { c.failure(reason); }
 public Transfer upload(Uri u,String n,Progress p,Callback<CloudFile> c) { c.failure(reason); return () -> {}; }
 public Transfer download(CloudFile f,Uri u,Progress p,Callback<Uri> c) { c.failure(reason); return () -> {}; }
 public void delete(CloudFile f,Callback<Boolean> c) { c.failure(reason); }
 public void disconnect() { }
}