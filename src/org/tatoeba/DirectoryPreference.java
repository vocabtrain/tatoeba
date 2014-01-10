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

import java.io.File;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Environment;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DirectoryPreference extends DialogPreference implements PreferenceManager.OnActivityResultListener
{

	public interface IntentLauncher
	{
		public void launch(Intent intent, int result);
	}

	public static Intent getFileManagerIntent(final Context context, final String title, final String button_text, final File file)
	{
		if(!context.getPackageManager().queryIntentActivities(new Intent("org.openintents.action.PICK_FILE"), 0).isEmpty())
		{
			final Intent intent = new Intent("org.openintents.action.PICK_DIRECTORY");
			intent.putExtra("org.openintents.extra.TITLE", title);
			intent.putExtra("org.openintents.extra.BUTTON_TEXT", button_text);
			intent.setData(Uri.fromFile(file));
			return intent;
		}
		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		return intent;

	}

	public static boolean hasFileManager(final Context context)
	{
		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		return !context.getPackageManager().queryIntentActivities(intent, 0).isEmpty();

	}

	private IntentLauncher intentlauncher = null;

	private EditText filename;

	public final static int REQUEST_FILEMANAGER_FOR_TYPEFACE = 2;

	public DirectoryPreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		setDialogLayoutResource(R.layout.directory_preference);
		setDialogTitle(context.getString(R.string.layout_button_select_file));
	}

	@Override
	public boolean onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		if(requestCode == REQUEST_FILEMANAGER_FOR_TYPEFACE && data != null)
		{
			filename.setText(data.getData().toString().replace("file://", ""));
			return true;
		}
		return false;
	}

	@Override
	protected View onCreateDialogView()
	{
		final View v = super.onCreateDialogView();
		filename = (EditText) v.findViewById(R.id.directory_filename);
		final Button selectButton = (Button) v.findViewById(R.id.directory_selectbutton);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

		filename.setText(prefs.getString(getKey(), new File(Environment.getExternalStorageDirectory(), "tatoeba").toString()));

		if(!hasFileManager(getContext()))
		{
			selectButton.setEnabled(false);
			selectButton.setVisibility(View.GONE);
		}
		else selectButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				try
				{
					final Intent intent = getFileManagerIntent(getContext(), getContext().getString(R.string.layout_button_select_file), getContext()
							.getString(R.string.button_load), new File(filename.getText().toString()));
					if(intentlauncher != null) intentlauncher.launch(intent, REQUEST_FILEMANAGER_FOR_TYPEFACE);
				}
				catch(final ActivityNotFoundException e)
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setMessage(getContext().getString(R.string.missing_oi_filemanager)).setPositiveButton(getContext().getString(android.R.string.ok),
							new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(final DialogInterface dialog, final int id)
								{
									dialog.dismiss();
								}
							});
					final AlertDialog alert = builder.create();
					alert.show();
				}
			}

		});

		return v;
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		if(positiveResult)
		{
			final Editor edit = prefs.edit();
			edit.putString(getKey(), filename.getText().toString());
			edit.commit();
		}
	}

	public void setIntentLaunchListener(final IntentLauncher launcher)
	{
		this.intentlauncher = launcher;
	}

}
