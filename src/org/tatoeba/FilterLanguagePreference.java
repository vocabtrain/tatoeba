/*
Tatoeba - Collection of example sentences for android 
Copyright (C) 2012 Dominik Köppl

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tatoeba;

import java.util.Arrays;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;

public class FilterLanguagePreference extends DialogPreference
{
	
	private TypedArray languages_flags;

	
	class LanguageAdapter extends BaseAdapter {
		
		private final boolean[] check_states;
		private final String[] languages_iso3_codes;
		private final String[] languages_names;
		
		public LanguageAdapter() 
		{
			languages_iso3_codes = getContext().getResources().getStringArray(R.array.languages_values);
			languages_names = getContext().getResources().getStringArray(R.array.languages);
			languages_flags = getContext().getResources().obtainTypedArray(R.array.languages_drawables);
			check_states = new boolean[languages_iso3_codes.length];
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

			String value = prefs.getString(getKey(), null);
			if(value == null) return;
			String[] langs = value.split(",");
			

			for(int i = 0; i < langs.length; ++i)
			{
				int pos = Arrays.binarySearch(languages_iso3_codes, langs[i]);
				if(pos < 0) continue;
				check_states[pos] = true;
			}
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if(view == null)
			{
				LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = vi.inflate(R.layout.filterlanguage_child, parent, false);
			}

			final CheckedTextView text = (CheckedTextView) view.findViewById(R.id.filterlanguage_child_text);
			text.setText(languages_names[position]);
			text.setChecked(check_states[position]);
			
			final ImageView flag = (ImageView) view.findViewById(R.id.filterlanguage_child_flag);
			flag.setImageResource(languages_flags.getResourceId(position, 0));
			
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					check_states[position] = !check_states[position];
					text.setChecked(check_states[position]);
				}
				
			});
			return view;
		}
		@Override
		public int getCount() {
			return check_states.length;
		}
		@Override
		public Object getItem(int position) {
			return check_states[position];
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		
	}
	
	
	private ListView listview;
	public FilterLanguagePreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		setDialogLayoutResource(R.layout.filterlanguage_preference);
		setDialogTitle(context.getString(R.string.pref_filter_language));
	}

	@Override
	protected View onCreateDialogView()
	{
		final View v = super.onCreateDialogView();
		listview = (ListView) v.findViewById(R.id.filterlanguage_listview);
		adapter = new LanguageAdapter();
		listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listview.setAdapter(adapter);
		return v;
	}
	private LanguageAdapter adapter;
	@Override
	protected void onDialogClosed(final boolean positiveResult)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		if(positiveResult)
		{
			final Editor edit = prefs.edit();
			StringBuilder value = new StringBuilder();
			String[] languages_iso3_codes = getContext().getResources().getStringArray(R.array.languages_values);
			for(int i = 0; i < adapter.check_states.length; ++i)
			{
				if(adapter.check_states[i] == false) continue;
				value.append(languages_iso3_codes[i]);
				value.append(",");
			}
			if(value.length() > 0)
				value.deleteCharAt(value.length()-1);
			edit.putString(getKey(), value.toString());
			edit.commit();
		}
	}
	public void toggleChecked(View v)
	{
		((CheckedTextView) v).toggle();
	}

}
