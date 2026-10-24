package com.moorixlabs.bluedrive;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.*;
import androidx.core.os.LocaleListCompat;
import com.google.android.material.button.MaterialButton;
public class SettingsActivity extends AppCompatActivity {
 @Override public void onCreate(Bundle b){super.onCreate(b);
  ScrollView scroll=new ScrollView(this);LinearLayout content=new LinearLayout(this);content.setOrientation(1);int pad=(int)(24*getResources().getDisplayMetrics().density);content.setPadding(pad,pad,pad,pad);scroll.addView(content);setContentView(scroll);
  androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(scroll,(v,i)->{androidx.core.graphics.Insets bars=i.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return i;});
  add(content,R.string.bd_back,this::finish);TextView title=new TextView(this);title.setText(R.string.settings_title);title.setTextSize(28);title.setTypeface(null,1);content.addView(title);
  add(content,R.string.bd_appearance,()->{String[] modes={getString(R.string.bd_system),getString(R.string.bd_light),getString(R.string.bd_dark)};new AlertDialog.Builder(this).setTitle(R.string.bd_appearance).setSingleChoiceItems(modes,getSharedPreferences("appearance",0).getInt("theme",0),(d,w)->{getSharedPreferences("appearance",0).edit().putInt("theme",w).apply();d.dismiss();MyApplication.applyTheme(this);}).show();});
  add(content,R.string.bd_language,()->{String[] langs={"English","العربية","Français"};new AlertDialog.Builder(this).setTitle(R.string.bd_language).setItems(langs,(d,w)->{String tag=new String[]{"en","ar","fr"}[w];getSharedPreferences("appearance",0).edit().putString("locale",tag).apply();AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));}).show();});
  add(content,R.string.bd_default_view,()->new AlertDialog.Builder(this).setTitle(R.string.bd_default_view).setSingleChoiceItems(new String[]{getString(R.string.bd_list),getString(R.string.bd_grid)},getSharedPreferences("appearance",0).getBoolean("grid",false)?1:0,(d,w)->{getSharedPreferences("appearance",0).edit().putBoolean("grid",w==1).apply();d.dismiss();}).show());
  add(content,R.string.sort_files,()->{
   int[] ids={R.string.sort_name_asc,R.string.sort_name_desc,R.string.sort_newest,R.string.sort_oldest,R.string.sort_largest,R.string.sort_smallest,R.string.bd_sort_type};String[] labels=new String[7];for(int i=0;i<7;i++)labels[i]=getString(ids[i]);
   new AlertDialog.Builder(this).setTitle(R.string.sort_files).setSingleChoiceItems(labels,getSharedPreferences("appearance",0).getInt("sort",2),(d,w)->{getSharedPreferences("appearance",0).edit().putInt("sort",w).apply();d.dismiss();}).show();
  });
  add(content,R.string.bd_restore_records,()->{getSharedPreferences("library_hidden",0).edit().clear().apply();android.widget.Toast.makeText(this,R.string.bd_restored,android.widget.Toast.LENGTH_SHORT).show();});
  add(content,R.string.bd_connection,()->show(R.string.bd_connection,R.string.bd_provider_reason));
  add(content,R.string.bd_forget_credentials,()->new AlertDialog.Builder(this).setTitle(R.string.bd_forget_credentials).setMessage(R.string.bd_disconnect_help).setPositiveButton(R.string.bd_confirm,(d,w)->SettingsUtils.clear(this)).setNegativeButton(R.string.cancel,null).show());
  add(content,R.string.bd_downloads,()->show(R.string.bd_downloads,R.string.bd_download_help));
  add(content,R.string.bd_privacy,()->show(R.string.bd_privacy,R.string.bd_privacy_text));add(content,R.string.bd_about,()->show(R.string.bd_about,R.string.bd_about_text));
 }
 private void add(LinearLayout l,int id,Runnable action){MaterialButton b=new MaterialButton(this,null,com.google.android.material.R.attr.materialButtonOutlinedStyle);b.setText(id);b.setAllCaps(false);b.setMinHeight((int)(56*getResources().getDisplayMetrics().density));l.addView(b,new LinearLayout.LayoutParams(-1,-2));b.setOnClickListener(v->action.run());}
 private void show(int title,int text){new AlertDialog.Builder(this).setTitle(title).setMessage(text).setPositiveButton(R.string.ok,null).show();}
}