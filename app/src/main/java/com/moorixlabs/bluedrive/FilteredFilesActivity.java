package com.moorixlabs.bluedrive;
import android.os.Bundle;
/** Category deep links use the same library host and operations as Home. */
public class FilteredFilesActivity extends MainActivity {
 public static final String EXTRA_FILE_TYPE="file_type", TYPE_IMAGE="IMAGE", TYPE_VIDEO="VIDEO", TYPE_PDF="PDF", TYPE_OTHER="OTHER";
 @Override public void onCreate(Bundle state) { getIntent().putExtra("library",true); String t=getIntent().getStringExtra(EXTRA_FILE_TYPE); int c=TYPE_IMAGE.equals(t)?0:TYPE_VIDEO.equals(t)?1:TYPE_PDF.equals(t)?2:TYPE_OTHER.equals(t)?5:-1; getIntent().putExtra("category",c); super.onCreate(state); }
}