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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.tatoeba.providers.ProviderInterface;
import org.tatoeba.providers.ProviderInterface.LinkTable;
import org.tatoeba.providers.StringUtils;
import org.tatoeba.providers.TranslationProvider;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
	private class SelectAdapter extends SimpleCursorTreeAdapter
	{
		private String target_language;

		public SelectAdapter(final Cursor cursor, final int collapsedGroupLayout, final int expandedGroupLayout, final String[] groupFrom, final int[] groupTo,
				final int childLayout, final int lastChildLayout, final String[] childFrom, final int[] childTo)
		{
			super(MainActivity.this, cursor, collapsedGroupLayout, expandedGroupLayout, groupFrom, groupTo, childLayout, lastChildLayout, childFrom, childTo);
			target_language = getSelectedLanguage();
		}

		@Override
		protected Cursor getChildrenCursor(final Cursor groupCursor)
		{
			if(groupCursor == null) return null; 
			final Cursor cursor = getContentResolver().query(
					ContentUris.withAppendedId(ProviderInterface.LinkTable.CONTENT_URI, groupCursor.getLong(groupCursor.getColumnIndex(LinkTable._ID))),
					new String[] { ProviderInterface.LinkTable._ID, ProviderInterface.LinkTable.TEXT, ProviderInterface.LinkTable.LANGUAGE }, 
					(filtered_languages == null) ? null : (ProviderInterface.LinkTable.LANGUAGE + StringUtils.generateQuestionTokens(filtered_languages.length)),
					(filtered_languages == null) ? null : filtered_languages,
					null);
			return cursor;
		}
		
		private void setTypeface(TextView text, String iso3language)
		{
			if(iso3language.equals("hye"))
				text.setTypeface(Typeface.createFromAsset(getAssets(),"DejaVuSans.ttf"));
			else if(iso3language.equals("uig"))
				text.setTypeface(Typeface.createFromAsset(getAssets(),"DejaVuSans.ttf"));
			else if(iso3language.equals("kat"))
				text.setTypeface(Typeface.createFromAsset(getAssets(),"DejaVuSans.ttf"));
			else if(iso3language.equals("geo"))
				text.setTypeface(Typeface.createFromAsset(getAssets(),"DejaVuSans.ttf"));
			
			else if(iso3language.equals("heb"))
				text.setTypeface(Typeface.createFromAsset(getAssets(),"ezra_sil.ttf"));
			
			else if(iso3language.equals("hin"))
				text.setTypeface(Typeface.createFromAsset(getAssets(),"annaputra_sil.ttf"));
			else if(iso3language.equals("san"))
				text.setTypeface(Typeface.createFromAsset(getAssets(),"annaputra_sil.ttf"));

			else if(iso3language.equals("arz"))
				text.setTypeface(Typeface.createFromAsset(getAssets(),"ScheherazadeRegOT.ttf"));
			else if(iso3language.equals("ara"))
				text.setTypeface(Typeface.createFromAsset(getAssets(),"ScheherazadeRegOT.ttf"));
			else if(iso3language.equals("pes"))
				text.setTypeface(Typeface.createFromAsset(getAssets(),"ScheherazadeRegOT.ttf"));

			else if(iso3language.equals("tha"))
				text.setTypeface(Typeface.createFromAsset(getAssets(),"Garuda.ttf")); 
			else if(iso3language.equals("mal"))
				text.setTypeface(Typeface.createFromAsset(getAssets(),"Meera.ttf"));//
			else if(iso3language.equals("ben"))
				text.setTypeface(Typeface.createFromAsset(getAssets(),"AkaashNormal.ttf"));

			else
				text.setTypeface(Typeface.DEFAULT);
		}

		@Override
		public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild, final View convertView, final ViewGroup parent)
		{
			final View view = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
			final TextView text = (TextView) view.findViewById(R.id.child_text);
			final TextView language = (TextView) view.findViewById(R.id.child_language);
			final ImageView flag = (ImageView) view.findViewById(R.id.child_flag);
			String iso3language = language.getText().toString();
			setTypeface(text, iso3language);

			
			if(show_language) language.setVisibility(View.VISIBLE);

			final int pos = Arrays.binarySearch(languages_iso3_codes, iso3language);
			if(pos < 0)
			{
				flag.setImageDrawable(getResources().getDrawable(R.drawable.unknown));
				if(show_full_language) language.setText(getString(R.string.unknown_language));
				view.setClickable(false);
				return view;
			}
			flag.setImageDrawable(languages_flags.getDrawable(pos));
			if(show_full_language) language.setText(languages_names[pos]);
			if(languages_iso3_codes[pos].equals("jpn"))
			{
				view.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(final View v)
					{
						final Cursor groupCursor = getCursor();
						Cursor childCursor = null;
						try
						{
							groupCursor.moveToPosition(groupPosition);
							childCursor = getChildrenCursor(groupCursor);
							childCursor.moveToPosition(childPosition);
							final long sentence_id = childCursor.getLong(childCursor.getColumnIndex(ProviderInterface.LinkTable._ID));
							final Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(ProviderInterface.RubyTable.CONTENT_URI, sentence_id),
									new String[] { ProviderInterface.RubyTable.TEXT }, null, null, null);
							if(cursor != null)
							{
								if(cursor.moveToFirst())
								{
									final String html = cursor.getString(cursor.getColumnIndex(ProviderInterface.RubyTable.TEXT));
									final RubyDialog dialog = new RubyDialog(MainActivity.this, html);
									dialog.show();
									cursor.close();
								}
							}
							cursor.close();
						}
						catch(CursorIndexOutOfBoundsException e)
						{}
						finally
						{
							if(childCursor != null) childCursor.close();
						}
					}

				});
			}
			else view.setClickable(false);
			return view;

		}
		
		@Override
		public void changeCursor(Cursor cursor)
		{
			super.changeCursor(cursor);
			target_language = getSelectedLanguage();
		}

		@Override
		public View getGroupView(final int groupPosition, final boolean isExpanded, final View convertView, final ViewGroup parent)
		{
			final View view = super.getGroupView(groupPosition, isExpanded, convertView, parent);
			final TextView text = (TextView) view.findViewById(R.id.group_text);
			setTypeface(text, target_language);
			if(target_language.equals("jpn"))
			{
				text.setOnLongClickListener(new OnLongClickListener()
				{

					@Override
					public boolean onLongClick(final View v)
					{
						final Cursor groupCursor = getCursor();
						groupCursor.moveToPosition(groupPosition);
						final long sentence_id = groupCursor.getLong(groupCursor.getColumnIndex(ProviderInterface.LinkTable._ID));
						final Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(ProviderInterface.RubyTable.CONTENT_URI, sentence_id),
								new String[] { ProviderInterface.RubyTable.TEXT }, null, null, null);
						if(cursor.moveToFirst())
						{
							final String html = cursor.getString(cursor.getColumnIndex(ProviderInterface.RubyTable.TEXT));
							final RubyDialog dialog = new RubyDialog(MainActivity.this, html);
							dialog.show();
							return true;
						}
						cursor.close();
						return false;
					}

				});
			}
			else text.setClickable(false);
			return view;
		}

	}

	private final static int REQUEST_LICENSE_ACCEPTING = 1;

	private ExpandableListView listview;

	private EditText edit;
	private Spinner languages;
	private SelectAdapter adapter;
	private ProgressBar progress;

	private String[] languages_iso3_codes;
	private String[] languages_names;

	private TypedArray languages_flags;

	private boolean show_language;
	private boolean show_full_language;

	private String sentence_limit = null;

	private final static int REQUEST_PREFERENCE = 2;

	@SuppressWarnings("unchecked")
	private String getSelectedLanguage()
	{
		final HashMap<String, Object> map = (HashMap<String, Object>) languages.getSelectedItem();
		return (String) map.get("id");
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{

		switch(requestCode)
		{
			case REQUEST_LICENSE_ACCEPTING:
				if(resultCode != RESULT_OK) finish();
				break;
			case REQUEST_PREFERENCE:
				// OnRefresh();

				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}
	private String[] filtered_languages;
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		languages_names = getResources().getStringArray(R.array.languages);
		languages_iso3_codes = getResources().getStringArray(R.array.languages_values);
		languages_flags = getResources().obtainTypedArray(R.array.languages_drawables);

		setContentView(R.layout.main);
		listview = (ExpandableListView) this.findViewById(R.id.list);
		edit = (EditText) this.findViewById(R.id.edittext);
		languages = (Spinner) this.findViewById(R.id.languages);
		progress = (ProgressBar) this.findViewById(R.id.progress);
		progress.setVisibility(View.GONE);
		edit.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event)
			{
				getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
				return true;
			}

		});

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String value = prefs.getString("filterlanguage", null);
		filtered_languages = (value == null || value.length() == 0) ? null : value.split(",");
		if(filtered_languages != null && filtered_languages.length == 0) filtered_languages = null;
		
		
		final ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		{

			for(int i = 0; i < languages_names.length; ++i)
			{
				if(filtered_languages != null && Arrays.binarySearch(filtered_languages,  languages_iso3_codes[i] ) < 0) continue;
				final HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("flag", languages_flags.getResourceId(i, 0));
				map.put("language", languages_names[i]);
				map.put("id", languages_iso3_codes[i]);
				data.add(map);
			}

		}
		languages.setAdapter(new SimpleAdapter(this, data, R.layout.language_child, new String[] { "flag", "language" }, new int[] { R.id.language_child_flag,
				R.id.language_child_text }));
		
		if(edit.getText().length() == 0)
		{
			
			edit.setText(getString(R.string.example_string));
			int pos = Arrays.binarySearch(filtered_languages == null ? languages_iso3_codes : filtered_languages, getString(R.string.example_language));
			if(pos >= 0 && pos < languages.getAdapter().getCount())
				languages.setSelection(pos);
		}
		
		OnRefresh();
		getSupportLoaderManager().initLoader(0, null, MainActivity.this);

	}

	@Override
	public Loader<Cursor> onCreateLoader(final int arg0, final Bundle arg1)
	{
		if(languages == null) return null;
		progress.setVisibility(View.VISIBLE);

		final String[] selection = new String[] { edit.getText().toString(), getSelectedLanguage() };
		return new CursorLoader(this, ProviderInterface.SearchTable.CONTENT_URI, new String[] { ProviderInterface.LinkTable._ID,
				ProviderInterface.LinkTable.TEXT }, null, selection, sentence_limit);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader)
	{
		// adapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		progress.setVisibility(View.GONE);
		if(data == null) return;
		if(adapter == null)
		{
			final String[] childFrom = new String[] { LinkTable._ID, LinkTable.TEXT, LinkTable.LANGUAGE };
			final int[] childTo = new int[] { R.id.child_id, R.id.child_text, R.id.child_language };
			final String[] groupFrom = new String[] { LinkTable._ID, LinkTable.TEXT }; 
			final int[] groupTo = new int[] { R.id.group_id, R.id.group_text };
			adapter = new SelectAdapter(data, R.layout.group, R.layout.group, groupFrom, groupTo, R.layout.child, R.layout.child, childFrom, childTo);
			listview.setAdapter(adapter);
		}
		else adapter.changeCursor(data);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.menu_help:
				try
				{
					final BufferedReader bf = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.license_short)));
					final StringBuilder sb = new StringBuilder();
					while(true)
					{
						final String s = bf.readLine();
						if(s == null) break;
						sb.append(s);
						sb.append("\n");
					}
					bf.close();

					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage(sb.toString()).setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(final DialogInterface dialog, final int id)
						{
							dialog.dismiss();
						}
					}).setNeutralButton(getString(R.string.button_donate), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(final DialogInterface dialog, final int id)
						{
							final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=RCRXP4DYESAZC"));
							startActivity(intent);
						}
					});
					final AlertDialog alert = builder.create();
					alert.show();
				}
				catch(final IOException io)
				{

				}
				return true;
			case R.id.menu_preferences:
			{
				final Intent intent = new Intent(this, SettingsActivity.class);
				startActivityForResult(intent, REQUEST_PREFERENCE);
			}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}
	private void OnRefresh()
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		show_language = prefs.getBoolean("show_language", false);
		show_full_language = prefs.getBoolean("show_full_language", false);
		sentence_limit = prefs.getString("sencence_limit", null);

		if(prefs.getBoolean("offline_dict", false))
		{
			String directory = prefs.getString("directory", null);
			if(directory == null)
			{
				directory = Environment.getExternalStorageDirectory().toString() + "/tatoeba";
				final Editor edit = prefs.edit();
				edit.putString("directory", directory);
				edit.commit();
			}

			if(!new File(directory).isDirectory() || !new File(directory, TranslationProvider.DATABASE_FILENAME).isFile())
			{
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getString(R.string.error_directory_not_exist, directory))
						.setTitle(getString(R.string.error_directory_not_exist_title))
						.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(final DialogInterface dialog, final int id)
							{
								final Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
								startActivity(intent);
								finish();
							}
						})
						.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(final DialogInterface dialog, final int id)
							{
							}
						});
				final AlertDialog alert = builder.create();
				alert.show();
				return;
			}
		}
		else
		{
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						URLConnection connection = new URL(TatoebaApplication.API_SERVER).openConnection();
						connection.connect();
					} catch (final IOException e) {
						if(isFinishing()) return;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if(isFinishing()) return;
								final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
								builder.setMessage(getString(R.string.error_no_internet, e.getMessage()))
										.setTitle(getString(R.string.error_no_internet_title));
								final AlertDialog alert = builder.create();
								alert.show();
							}
							
						});

					}
				}
				
			}).start();
			
			
		}	
		

	}

	@Override
	public void onStart()
	{
		super.onStart();
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(!prefs.getBoolean("license_accepted", false))
		{
			final Intent intent = new Intent(MainActivity.this, LicenseActivity.class);
			startActivityForResult(intent, REQUEST_LICENSE_ACCEPTING);
		}
	}

}