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

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements DirectoryPreference.IntentLauncher
{
	private DirectoryPreference directoryPreference = null;

	@Override
	public void launch(final Intent intent, final int result)
	{
		startActivityForResult(intent, DirectoryPreference.REQUEST_FILEMANAGER_FOR_TYPEFACE);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		if(directoryPreference != null)
		{
			if(directoryPreference.onActivityResult(requestCode, resultCode, data)) return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		directoryPreference = (DirectoryPreference) findPreference("directory");
		if(directoryPreference != null) directoryPreference.setIntentLaunchListener(this);
	}
}