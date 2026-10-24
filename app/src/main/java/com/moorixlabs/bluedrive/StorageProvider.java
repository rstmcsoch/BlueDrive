package com.moorixlabs.bluedrive;
import android.net.Uri;
import java.util.List;
/** Provider callbacks may occur off the UI thread. No quota unless actually reported. */
public interface StorageProvider {
 interface Callback<T> { void success(T result); void failure(String error); }
 interface Transfer { void cancel(); }
 interface Progress { void update(long bytes, long total); }
 String id(); boolean isConnected();
 void enumerate(Callback<List<CloudFile>> callback);
 Transfer upload(Uri source, String name, Progress progress, Callback<CloudFile> callback);
 Transfer download(CloudFile file, Uri destination, Progress progress, Callback<Uri> callback);
 void delete(CloudFile file, Callback<Boolean> callback);
 void disconnect();
}