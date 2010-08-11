package at.tugraz.ist.s2a.constructionSite.gui.adapter;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import at.tugraz.ist.s2a.R;
import at.tugraz.ist.s2a.R.layout;
import at.tugraz.ist.s2a.constructionSite.content.ContentManager;

public class FileAdapter extends BaseAdapter{
    private Context mCtx;
    public ArrayList<String> mList;
    public Dialog mDialog;
    public ContentManager mContentManager;
    
	public FileAdapter(Context context,
			ArrayList<String> data) {
		mCtx = context;
		mList = data;
	}
	
	

	public int getCount() {
		return mList.size();
	}

	public Object getItem(int arg0) {
		
		return mList.get(arg0);
	}

	public long getItemId(int position) {
			return (position);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		String file = mList.get(position);
		LinearLayout view;
		if (convertView == null){
		  LayoutInflater inflater = (LayoutInflater)mCtx.getSystemService(
	      Context.LAYOUT_INFLATER_SERVICE);
		  view = (LinearLayout) inflater.inflate(R.layout.sprites_list_element, null);
		}
		else{
		  view = (LinearLayout)convertView;
		}
		TextView text = (TextView)view.getChildAt(0);
		text.setText(file);
	    text.setTextColor(Color.WHITE);
		text.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
			 mContentManager.loadContent(((TextView)v).getText().toString());
			 mDialog.dismiss();	
			}
		});
		return view;
   }
    public void setDialog(Dialog dialog)
	{
		mDialog = dialog;
	}
	public void setContentManager(ContentManager content)
	{
		mContentManager = content;
	}

}
