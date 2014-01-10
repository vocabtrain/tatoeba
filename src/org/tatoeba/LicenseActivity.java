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
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class LicenseActivity extends Activity
{

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.license_activity);

		final CheckBox check = (CheckBox) this.findViewById(R.id.license_check);
		final Button cancel = (Button) this.findViewById(R.id.license_decline);
		final Button ok = (Button) this.findViewById(R.id.license_ok);
		final TextView text = (TextView) this.findViewById(R.id.license_text);
		final Button view = (Button) this.findViewById(R.id.license_view);

		try
		{
			final StringBuilder sb = new StringBuilder();
			final BufferedReader bf = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.license_short)));
			while(true)
			{
				final String s = bf.readLine();
				if(s == null) break;
				sb.append(s);
				sb.append("\n");
			}
			bf.close();
			text.setText(sb.toString());
		}
		catch(final IOException io)
		{
		}

		ok.setEnabled(false);

		check.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked)
			{
				ok.setEnabled(isChecked);
			}
		});

		ok.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(final View v)
			{
				if(check.isChecked())
				{
					final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LicenseActivity.this);
					final Editor edit = prefs.edit();
					edit.putBoolean("license_accepted", true);
					edit.commit();
					setResult(RESULT_OK);
					finish();
				}
			}
		});
		cancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		view.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.gnu.org/copyleft/gpl.html"));
				startActivity(intent);
			}
		});

	}

}
