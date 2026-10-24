package com.moorixlabs.bluedrive;
import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.*;
import java.util.function.Consumer;
public final class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.Holder> {
 private final Context context; private List<CloudFile> files; private final Consumer<CloudFile> open,details; private final boolean grid;
 LibraryAdapter(Context c,List<CloudFile> f,Consumer<CloudFile> o,Consumer<CloudFile> d,boolean g){context=c;files=f;open=o;details=d;grid=g;}
 void replace(List<CloudFile> f){files=f;notifyDataSetChanged();}
 static int dp(Context c,int n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
 static View row(Context c,CloudFile f,Runnable open,Runnable details,boolean grid){
  LinearLayout card=new LinearLayout(c);card.setOrientation(grid?1:0);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(c,12),dp(c,12),dp(c,8),dp(c,12));card.setBackgroundResource(R.drawable.bd_card);
  ImageView image=new ImageView(c);image.setImageResource(R.drawable.ic_file_general);image.setScaleType(ImageView.ScaleType.CENTER_CROP);card.addView(image,new LinearLayout.LayoutParams(grid?-1:dp(c,48),dp(c,grid?104:48)));
  if(FileIndex.category(f)<2 && f.getPath()!=null&&!f.getPath().isEmpty()) Glide.with(c).load(android.net.Uri.parse(f.getPath())).centerCrop().error(R.drawable.ic_file_general).into(image);
  LinearLayout info=new LinearLayout(c);info.setOrientation(1);info.setPadding(dp(c,12),0,dp(c,4),0);card.addView(info,grid?new LinearLayout.LayoutParams(-1,-2):new LinearLayout.LayoutParams(0,-2,1));
  TextView name=new TextView(c);name.setText(f.getName());name.setTextSize(15);name.setTextColor(c.getColor(R.color.bd_text));name.setTypeface(null,1);name.setMaxLines(2);name.setEllipsize(android.text.TextUtils.TruncateAt.END);info.addView(name);
  TextView meta=new TextView(c);meta.setText(android.text.format.Formatter.formatShortFileSize(c,f.getSize())+" · "+c.getString(f.isUploaded()?R.string.bd_indexed:R.string.bd_on_device));if(f.getDate()>0) meta.append(" · "+java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT).format(new java.util.Date(f.getDate())));meta.setMaxLines(2);meta.setTextColor(c.getColor(R.color.bd_muted));meta.setTextSize(12);meta.setPadding(0,dp(c,4),0,0);info.addView(meta);
  ImageButton more=new ImageButton(c);more.setImageResource(android.R.drawable.ic_menu_more);more.setColorFilter(c.getColor(R.color.bd_muted));more.setBackgroundColor(android.graphics.Color.TRANSPARENT);more.setContentDescription(c.getString(R.string.bd_details));card.addView(more,new LinearLayout.LayoutParams(dp(c,48),dp(c,48)));more.setOnClickListener(v->details.run());card.setOnClickListener(v->open.run());card.setFocusable(true);
  RecyclerView.LayoutParams p=new RecyclerView.LayoutParams(-1,-2);p.setMargins(dp(c,4),dp(c,4),dp(c,4),dp(c,8));card.setLayoutParams(p);return card;
 }
 static class Holder extends RecyclerView.ViewHolder{FrameLayout root;Holder(FrameLayout v){super(v);root=v;}}
 @Override public Holder onCreateViewHolder(ViewGroup p,int t){return new Holder(new FrameLayout(context));}
 @Override public void onBindViewHolder(Holder h,int pos){CloudFile f=files.get(pos);h.root.removeAllViews();View v=row(context,f,()->open.accept(f),()->details.accept(f),grid);v.setLayoutParams(new FrameLayout.LayoutParams(-1,-2));h.root.setPadding(dp(context,4),dp(context,4),dp(context,4),dp(context,4));h.root.addView(v);}
 @Override public int getItemCount(){return files.size();}
}