package com.moorixlabs.bluedrive;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.*;
import androidx.recyclerview.widget.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import java.util.*;
import java.util.concurrent.*;

/** Shared native destination host: no network operation is initiated by a view. */
public class MainActivity extends AppCompatActivity {
 protected int destination=0;
 private int category=-1;
 private String query="";
 private FrameLayout host;
 private MaterialToolbar toolbar;
 private List<CloudFile> files=new ArrayList<>();
 private final ExecutorService io=Executors.newSingleThreadExecutor();
 private LibraryRepository repository;
 private StorageProvider provider;
 private LibraryAdapter adapter;
 private TextView empty;
 private ActivityResultLauncher<Intent> picker, importPicker;
 private final int[] categories={R.string.bd_images,R.string.bd_videos,R.string.bd_documents,R.string.bd_audio,R.string.bd_archives,R.string.bd_other};
 @Override public void onCreate(Bundle state) {
  super.onCreate(state); setContentView(R.layout.dashboard);
  repository=new LibraryRepository(this); provider=new UnavailableStorageProvider(getString(R.string.bd_provider_reason));
  if(state!=null) { destination=state.getInt("destination"); category=state.getInt("category",-1); query=state.getString("query",""); }
  else if(getIntent().getBooleanExtra("library",false)) { destination=1; category=getIntent().getIntExtra("category",-1); }
  host=findViewById(R.id.screenHost); toolbar=findViewById(R.id.toolbar);
  ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootHost),(v,insets)->{
   androidx.core.graphics.Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
   v.setPadding(bars.left,bars.top,bars.right,Math.max(bars.bottom,insets.getInsets(WindowInsetsCompat.Type.ime()).bottom)); return insets;
  });
  picker=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result->{
   if(result.getResultCode()==RESULT_OK && result.getData()!=null) {
    Uri u=result.getData().getData(); if(u==null) return;
    try { getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch(SecurityException ignored) {}
    io.execute(()->{ try {
     String name=u.getLastPathSegment(); long size=0;
     try(android.database.Cursor c=getContentResolver().query(u,null,null,null,null)) { if(c!=null && c.moveToFirst()) {
      int n=c.getColumnIndex(OpenableColumns.DISPLAY_NAME), s=c.getColumnIndex(OpenableColumns.SIZE);
      if(n>=0) name=c.getString(n); if(s>=0 && !c.isNull(s)) size=c.getLong(s);
     }}
     CloudFile f=new CloudFile("local:"+u,name==null?getString(R.string.bd_file):name,Math.max(0,size),System.currentTimeMillis(),u.toString(),false);
     repository.merge(Collections.singletonList(f)); refresh();
    } catch(Exception e) { runOnUiThread(()->message(R.string.bd_operation_failed)); } });
   }
  });
  importPicker=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result->{
   if(result.getResultCode()==RESULT_OK && result.getData()!=null && result.getData().getData()!=null) {
    Uri uri=result.getData().getData(); io.execute(()->{ try {
     List<CloudFile> imported=FileIndex.read(getContentResolver().openInputStream(uri));
     if(imported.isEmpty()) { runOnUiThread(()->message(R.string.bd_invalid_index)); return; }
     // Keep a recoverable byte-for-byte copy before committing the private representation.
     try(java.io.InputStream in=getContentResolver().openInputStream(uri); java.io.OutputStream out=new java.io.FileOutputStream(new java.io.File(getFilesDir(),"legacy-"+System.currentTimeMillis()+".txt"))) {
      byte[] b=new byte[8192]; int n; while((n=in.read(b))!=-1) out.write(b,0,n);
     }
     repository.merge(imported); refresh();
    } catch(Exception e) { runOnUiThread(()->message(R.string.bd_operation_failed)); } });
   }
  });
  BottomNavigationView nav=findViewById(R.id.navigation);
  int[] titles={R.string.bd_home,R.string.bd_files,R.string.bd_transfers,R.string.bd_more};
  int[] icons={android.R.drawable.ic_menu_view,android.R.drawable.ic_menu_agenda,android.R.drawable.stat_sys_upload,android.R.drawable.ic_menu_more};
  for(int i=0;i<4;i++) nav.getMenu().add(0,i+100,i,titles[i]).setIcon(icons[i]);
  nav.setSelectedItemId(destination+100);
  nav.setOnItemSelectedListener(item->{destination=item.getItemId()-100; category=-1; query=""; render(); return true;});
  getOnBackPressedDispatcher().addCallback(this,new androidx.activity.OnBackPressedCallback(true) {
   @Override public void handleOnBackPressed() { if(destination!=0) { nav.setSelectedItemId(100); } else { setEnabled(false); getOnBackPressedDispatcher().onBackPressed(); } }
  });
  render(); refresh();
 }
 @Override protected void onResume() { super.onResume(); if(repository!=null) refresh(); }
 @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); out.putInt("destination",destination); out.putInt("category",category); out.putString("query",query); }
 @Override protected void onDestroy() { io.shutdown(); super.onDestroy(); }
 private void refresh() { io.execute(()->{List<CloudFile> loaded=repository.load(); runOnUiThread(()->{if(!isDestroyed()) { files=loaded; render(); }});}); }
 int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
 private TextView text(int id,int size,boolean bold) { return label(getString(id),size,bold); }
 private TextView label(String s,int size,boolean bold) {
  TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(getColor(bold?R.color.bd_text:R.color.bd_muted));
  if(bold) t.setTypeface(null,android.graphics.Typeface.BOLD); t.setPadding(0,dp(8),0,dp(8)); return t;
 }
 private LinearLayout column() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(20),dp(8),dp(20),dp(24)); return l; }
 private LinearLayout scroll() { ScrollView s=new ScrollView(this); s.setFillViewport(true); LinearLayout l=column(); s.addView(l); host.addView(s); return l; }
 private LinearLayout card(LinearLayout parent) {
  LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(20),dp(16),dp(20),dp(16)); l.setBackgroundResource(R.drawable.bd_card);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(16); parent.addView(l,p); return l;
 }
 private void button(LinearLayout parent,int title,Runnable action) {
  MaterialButton b=new MaterialButton(this); b.setText(title); b.setAllCaps(false); b.setMinHeight(dp(48)); b.setOnClickListener(v->action.run()); parent.addView(b,new LinearLayout.LayoutParams(-1,-2));
 }
 private void render() {
  host.removeAllViews(); toolbar.getMenu().clear(); toolbar.setTitle(destination==0?getString(R.string.app_name):getString(new int[]{R.string.bd_home,R.string.bd_files,R.string.bd_transfers,R.string.bd_more}[Math.min(destination,3)]));
  toolbar.setSubtitle(destination==0?getString(R.string.bd_local_library):null);
  toolbar.getMenu().add(R.string.bd_search).setIcon(android.R.drawable.ic_menu_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
  toolbar.setOnMenuItemClickListener(item->{ ((BottomNavigationView)findViewById(R.id.navigation)).setSelectedItemId(101); return true; });
  if(destination==0) home(); else if(destination==1) library(); else if(destination==2) transfers(); else more();
 }
 private long size(List<CloudFile> list) { long n=0; for(CloudFile f:list) n+=f.getSize(); return n; }
 private String bytes(long n) { return android.text.format.Formatter.formatShortFileSize(this,n); }
 private void home() {
  LinearLayout l=scroll();
  LinearLayout summary=card(l); summary.addView(text(R.string.bd_tracked,14,false)); summary.addView(label(bytes(size(files)),36,true)); summary.addView(label(getString(R.string.bd_file_count,files.size()),14,false));
  button(summary,R.string.bd_insights,this::insights);
  LinearLayout connection=card(l); connection.addView(text(R.string.bd_local_first,18,true)); connection.addView(text(R.string.bd_connection_hint,14,false)); button(connection,R.string.bd_connection,this::connection);
  l.addView(text(R.string.bd_quick_access,20,true));
  int columns=getResources().getConfiguration().screenWidthDp>=600?3:2;
  for(int row=0;row<(6+columns-1)/columns;row++) {
   LinearLayout line=new LinearLayout(this); l.addView(line,new LinearLayout.LayoutParams(-1,-2));
   for(int c=0;c<columns;c++) { int cat=row*columns+c; if(cat>=6) break;
    long count=files.stream().filter(f->FileIndex.category(f)==cat).count();
    MaterialButton b=new MaterialButton(this,null,com.google.android.material.R.attr.materialButtonOutlinedStyle); b.setAllCaps(false); b.setText(getString(categories[cat])+"\n"+count); b.setMinHeight(dp(88)); b.setCornerRadius(dp(20));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1); p.setMargins(dp(4),dp(4),dp(4),dp(4)); line.addView(b,p);
    b.setOnClickListener(v->{ ((BottomNavigationView)findViewById(R.id.navigation)).setSelectedItemId(101); category=cat; render(); });
   }
  }
  LinearLayout title=new LinearLayout(this); TextView recent=text(R.string.bd_recent,20,true); title.addView(recent,new LinearLayout.LayoutParams(0,-2,1));
  MaterialButton all=new MaterialButton(this,null,com.google.android.material.R.attr.materialButtonTextButtonStyle); all.setText(R.string.bd_view_all); all.setOnClickListener(v->((BottomNavigationView)findViewById(R.id.navigation)).setSelectedItemId(101)); title.addView(all); l.addView(title);
  List<CloudFile> sorted=new ArrayList<>(files); FileIndex.sort(sorted,2);
  if(sorted.isEmpty()) { l.addView(text(R.string.bd_no_files,20,true)); l.addView(text(R.string.bd_empty_help,14,false)); }
  for(CloudFile f:sorted.subList(0,Math.min(5,sorted.size()))) l.addView(LibraryAdapter.row(this,f,()->open(f),()->details(f),false));
  button(l,R.string.bd_add_local,this::pickLocal); button(l,R.string.bd_upload,this::upload);
 }
 private void library() {
  LinearLayout l=column(); l.setPadding(dp(16),0,dp(16),0); host.addView(l);
  EditText search=new EditText(this); search.setSingleLine(true); search.setHint(R.string.bd_search); search.setInputType(android.text.InputType.TYPE_CLASS_TEXT); search.setText(query); search.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search,0,android.R.drawable.ic_menu_close_clear_cancel,0); search.setOnTouchListener((v,event)->{if(event.getAction()==android.view.MotionEvent.ACTION_UP && event.getX()>search.getWidth()-dp(48)){ search.setText(""); return true;}return false;}); search.setMinHeight(dp(48)); l.addView(search,new LinearLayout.LayoutParams(-1,-2));
  LinearLayout controls=new LinearLayout(this);
  MaterialButton clear=new MaterialButton(this,null,com.google.android.material.R.attr.materialButtonTextButtonStyle);clear.setText(R.string.bd_clear_search);clear.setOnClickListener(v->search.setText(""));controls.addView(clear);
  MaterialButton filter=new MaterialButton(this,null,com.google.android.material.R.attr.materialButtonTextButtonStyle); filter.setText(category<0?getString(R.string.bd_all):getString(categories[category])); filter.setOnClickListener(v->{String[] labels=new String[7]; labels[0]=getString(R.string.bd_all); for(int i=0;i<6;i++) labels[i+1]=getString(categories[i]); new AlertDialog.Builder(this).setTitle(R.string.bd_filter).setSingleChoiceItems(labels,category+1,(d,w)->{category=w-1; d.dismiss(); filter.setText(labels[w]); updateResults();}).show();}); controls.addView(filter,new LinearLayout.LayoutParams(0,-2,1));
  MaterialButton sort=new MaterialButton(this,null,com.google.android.material.R.attr.materialButtonTextButtonStyle); sort.setText(R.string.sort_files); sort.setOnClickListener(v->sortDialog()); controls.addView(sort);
  MaterialButton grid=new MaterialButton(this,null,com.google.android.material.R.attr.materialButtonTextButtonStyle); grid.setText(R.string.bd_view); grid.setOnClickListener(v->{prefs().edit().putBoolean("grid",!prefs().getBoolean("grid",false)).apply(); libraryRefresh();}); controls.addView(grid); HorizontalScrollView controlScroll=new HorizontalScrollView(this); controlScroll.setHorizontalScrollBarEnabled(false); controlScroll.addView(controls); l.addView(controlScroll);
  empty=text(R.string.bd_no_results,16,false); l.addView(empty);
  RecyclerView rv=new RecyclerView(this); boolean isGrid=prefs().getBoolean("grid",false); rv.setLayoutManager(isGrid?new GridLayoutManager(this,Math.max(2,getResources().getConfiguration().screenWidthDp/180)):new LinearLayoutManager(this));
  adapter=new LibraryAdapter(this,new ArrayList<>(),this::open,this::details,isGrid); rv.setAdapter(adapter); l.addView(rv,new LinearLayout.LayoutParams(-1,0,1));
  search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){} public void onTextChanged(CharSequence s,int st,int before,int count){query=s.toString();updateResults();} public void afterTextChanged(android.text.Editable e){} });
  updateResults();
 }
 private void libraryRefresh(){host.removeAllViews();library();}
 private void updateResults() { if(adapter==null)return; List<CloudFile> result=new ArrayList<>(); for(CloudFile f:files) if((category<0||FileIndex.category(f)==category)&&(f.getName()+" "+f.getFileExtension()).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) result.add(f); FileIndex.sort(result,prefs().getInt("sort",2)); adapter.replace(result); empty.setVisibility(result.isEmpty()?View.VISIBLE:View.GONE); }
 android.content.SharedPreferences prefs(){return getSharedPreferences("appearance",MODE_PRIVATE);}
 private void sortDialog(){int[] ids={R.string.sort_name_asc,R.string.sort_name_desc,R.string.sort_newest,R.string.sort_oldest,R.string.sort_largest,R.string.sort_smallest,R.string.bd_sort_type};String[] options=new String[7];for(int i=0;i<7;i++)options[i]=getString(ids[i]);new AlertDialog.Builder(this).setTitle(R.string.sort_files).setSingleChoiceItems(options,prefs().getInt("sort",2),(d,w)->{prefs().edit().putInt("sort",w).apply();d.dismiss();updateResults();}).show();}
 private void transfers(){LinearLayout l=scroll();LinearLayout c=card(l);c.addView(text(R.string.bd_no_transfers,22,true));c.addView(text(R.string.bd_transfer_help,15,false));button(c,R.string.bd_connection,this::connection);}
 private void more(){LinearLayout l=scroll();l.addView(text(R.string.bd_tools,24,true));button(l,R.string.bd_insights,this::insights);button(l,R.string.settings_title,()->startActivity(new Intent(this,SettingsActivity.class)));button(l,R.string.bd_connection,this::connection);button(l,R.string.bd_import,this::importIndex);button(l,R.string.bd_help,()->dialog(R.string.bd_help,R.string.bd_help_text));button(l,R.string.bd_privacy,()->dialog(R.string.bd_privacy,R.string.bd_privacy_text));button(l,R.string.bd_about,()->dialog(R.string.bd_about,R.string.bd_about_text));}
 private void insights(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(24),dp(8),dp(24),dp(8));l.addView(text(R.string.bd_tracked,14,false));l.addView(label(bytes(size(files))+" · "+getString(R.string.bd_file_count,files.size()),22,true));
  for(int i=0;i<6;i++){final int cat=i; List<CloudFile> group=new ArrayList<>();for(CloudFile f:files)if(FileIndex.category(f)==cat)group.add(f);l.addView(label(getString(categories[i])+" · "+group.size()+" · "+bytes(size(group)),14,true));ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(1000);bar.setProgress(size(files)==0?0:(int)(1000d*size(group)/size(files)));l.addView(bar,new LinearLayout.LayoutParams(-1,dp(8)));}
  l.addView(text(R.string.bd_largest,18,true));List<CloudFile> largest=new ArrayList<>(files);FileIndex.sort(largest,4);for(CloudFile f:largest.subList(0,Math.min(3,largest.size())))l.addView(label(f.getName()+" · "+bytes(f.getSize()),14,false));ScrollView scroll=new ScrollView(this);scroll.addView(l);new AlertDialog.Builder(this).setTitle(R.string.bd_insights).setView(scroll).setPositiveButton(R.string.ok,null).show();}
 private void connection(){dialog(R.string.bd_connection,R.string.bd_provider_reason);}
 private void upload(){new AlertDialog.Builder(this).setTitle(R.string.bd_upload).setMessage(R.string.bd_provider_reason).setPositiveButton(R.string.bd_add_local,(d,w)->pickLocal()).setNegativeButton(R.string.cancel,null).show();}
 private void dialog(int title,int body){new AlertDialog.Builder(this).setTitle(title).setMessage(body).setPositiveButton(R.string.ok,null).show();}
 private void pickLocal(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);picker.launch(i);}
 private void importIndex(){new AlertDialog.Builder(this).setTitle(R.string.bd_import).setMessage(R.string.bd_import_help).setPositiveButton(R.string.bd_choose,(d,w)->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");importPicker.launch(i);}).setNegativeButton(R.string.cancel,null).show();}
 private Uri localUri(CloudFile f){String path=f.getPath();if(path==null||path.isEmpty())return null;Uri uri=Uri.parse(path);if("content".equals(uri.getScheme()))return uri;java.io.File disk=new java.io.File("file".equals(uri.getScheme())?uri.getPath():path);if(disk.isFile()&&disk.canRead())try{return androidx.core.content.FileProvider.getUriForFile(this,getPackageName()+".fileprovider",disk);}catch(Exception ignored){}return null;}
 private void open(CloudFile f){Uri u=localUri(f);if(u==null){details(f);return;}try{if(FileIndex.category(f)==0){startActivity(new Intent(this,ImageViewerActivity.class).putExtra("local_uri",u.toString()).putExtra(ImageViewerActivity.EXTRA_FILE_NAME,f.getName()));}else{startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(u,FileUtils.getMimeType(f.getName())).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));}}catch(Exception e){message(R.string.error_no_app_to_open);}}
 private void details(CloudFile f){
  new AlertDialog.Builder(this).setTitle(f.getName()).setItems(new String[]{getString(R.string.bd_details),getString(R.string.bd_remove_record)},(d,w)->{
   if(w==0) showDetails(f); else new AlertDialog.Builder(this).setTitle(R.string.bd_remove_record).setMessage(R.string.bd_remove_help).setPositiveButton(R.string.bd_confirm,(dialog,which)->{repository.hide(f.getId());refresh();}).setNegativeButton(R.string.cancel,null).show();
  }).show();
 }
 private void showDetails(CloudFile f){String date=f.getDate()>0?java.text.DateFormat.getDateTimeInstance().format(new Date(f.getDate())):getString(R.string.bd_unknown_date);String info=f.getName()+"\n\n"+bytes(f.getSize())+" · "+f.getFileExtension()+"\n"+date+"\n\n"+getString(f.isUploaded()?R.string.bd_legacy_record:R.string.bd_local_only);new AlertDialog.Builder(this).setTitle(R.string.bd_details).setMessage(info).setPositiveButton(R.string.open_file,(d,w)->{if(localUri(f)!=null)open(f);else connection();}).setNeutralButton(R.string.menu_share_file,(d,w)->{Uri u=localUri(f);if(u==null){message(R.string.file_not_available);return;}try{startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType(FileUtils.getMimeType(f.getName())).putExtra(Intent.EXTRA_STREAM,u).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),getString(R.string.menu_share_file)));}catch(Exception e){message(R.string.bd_operation_failed);}}).setNegativeButton(R.string.ok,null).show();}
 private void message(int id){Toast.makeText(this,id,Toast.LENGTH_LONG).show();}
}