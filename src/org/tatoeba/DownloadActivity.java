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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadActivity extends FragmentActivity
{

	class Task extends AsyncTask<Void, Integer, Void>
	{
		private boolean extracting = false;

		private String current_extracting_filename;
		private int archive_filesize = 0;
		private int current_size_downloaded = 0;
		private String error_message = null;

		@Override
		protected Void doInBackground(final Void... params)
		{
			final String zipfile = directory + "/" + ARCHIVE_NAME;

			try
			{
				final byte buffer[] = new byte[1024];

				{
					final String alternative_url = download_path.getText().toString();
					final URL url = new URL((alternative_url == null || alternative_url.length() == 0) ? ARCHIVE_URL : alternative_url);

					final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET"); 
					connection.connect();
					
					archive_filesize = connection.getContentLength();

					final InputStream input = new BufferedInputStream(connection.getInputStream());
					final OutputStream output = new FileOutputStream(zipfile);

					publishProgress(0);
					int count = 0;
					while((count = input.read(buffer)) != -1)
					{
						current_size_downloaded += count;
						publishProgress((int) ((current_size_downloaded * 100.0) / archive_filesize));
						output.write(buffer, 0, count);
						if(isCancelled()) 
						{
							output.close();
							input.close();
							return null;
						}
					}

					output.flush();
					output.close();
					input.close();
				}
				extracting = true;
				publishProgress(0);

				final ZipInputStream zipInput = new ZipInputStream(new FileInputStream(zipfile));
				ZipEntry entry;
				int filenumber = 0;
				while((entry = zipInput.getNextEntry()) != null)
				{
					if(isCancelled()) 
					{
						zipInput.close();
						return null;
					}
					current_extracting_filename = directory + '/' + entry.getName();
					publishProgress(++filenumber);
					if(entry.isDirectory())
					{
						final File newDirectory = new File(current_extracting_filename);
						if(!newDirectory.isDirectory() && !newDirectory.mkdirs())
						{
							zipInput.close();
							throw new IOException(getString(R.string.error_directory_not_createable, newDirectory));
						}
					}
					else
					{
						final FileOutputStream fileOutput = new FileOutputStream(current_extracting_filename);
						int count;
						while((count = zipInput.read(buffer)) != -1)
						{
							fileOutput.write(buffer, 0, count);
							if(isCancelled())
							{
								fileOutput.close();
								zipInput.close();
								return null;
							}
						}
						fileOutput.close();
					}
				}
				zipInput.close();

			}
			catch(final Exception e)
			{
				error_message = e.toString() + ": " + e.getMessage();
			}
			finally
			{
				final File f = new File(zipfile);
				if(f.exists()) f.delete();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Void result)
		{
			if(isFinishing()) return;
			extract_progress.setVisibility(View.INVISIBLE);
			if(error_message != null)
			{

				final AlertDialog.Builder builder = new AlertDialog.Builder(DownloadActivity.this);
				builder.setMessage(error_message)
						.setTitle(getString(R.string.error_download))
						.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(final DialogInterface dialog, final int id)
							{
								finish();
							}
						});
				final AlertDialog alert = builder.create();
				alert.show();

			}
			else
			{
				final Toast toast = Toast.makeText(DownloadActivity.this, getString(R.string.download_finished), Toast.LENGTH_LONG);
				toast.show();
			}
			button_cancel.setText(getText(android.R.string.ok));
			button_cancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v)
				{
					finish();
				}
			});
		}

		@Override
		protected void onPreExecute()
		{
		}

		@Override
		protected void onProgressUpdate(final Integer... progress)
		{
			if(!extracting)
			{
				download_progress.setProgress(progress[0]);
				download_countlabel.setText("" + current_size_downloaded + '/' + archive_filesize);
			}
			else
			{
				if(extract_progress.getVisibility() == View.INVISIBLE)
				{
					extract_label.setVisibility(View.VISIBLE);
					extract_progress.setVisibility(View.VISIBLE);
				}
				extract_filelabel.setText(getString(R.string.download_filelabel, current_extracting_filename, progress[0]));
			}
		}
	}

	private final static String ARCHIVE_URL = TatoebaApplication.API_SERVER + "static/bin/gen/tatoeba.zip";
	private final static String ARCHIVE_NAME = "tatoeba.zip";


	private EditText download_path;

	private TextView extract_filelabel;
	private ProgressBar download_progress;
	private TextView extract_label;
	private TextView download_countlabel;
	private ProgressBar extract_progress;
	private String directory;

	private Button button_cancel;
	private Button button_start;

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		directory = prefs.getString("directory", null);
		if(directory == null)
		{
			finish();
			return;
		}
		final File dir = new File(directory);
		if(!dir.isDirectory() && !dir.mkdirs())
		{
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.error_directory_not_createable, directory))
					.setTitle(getString(R.string.error_directory_not_createable_title))
					.setPositiveButton(getString(R.string.button_done), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(final DialogInterface dialog, final int id)
						{
							finish();
						}
					});
			final AlertDialog alert = builder.create();
			alert.show();
			return;
		}

		setContentView(R.layout.download_activity);
		download_path = (EditText) this.findViewById(R.id.download_path);
		download_path.setText(ARCHIVE_URL);
		final TextView download_label = (TextView) this.findViewById(R.id.download_download_label);

		download_countlabel = (TextView) this.findViewById(R.id.download_download_countlabel);
		extract_filelabel = (TextView) this.findViewById(R.id.download_extract_filelabel);
		extract_label = (TextView) this.findViewById(R.id.download_extract_label);
		download_progress = (ProgressBar) this.findViewById(R.id.download_download_progress);
		extract_progress = (ProgressBar) this.findViewById(R.id.download_extract_progress);
		button_cancel = (Button) this.findViewById(R.id.download_button_cancel);
		button_start = (Button) this.findViewById(R.id.download_button_start);
		download_label.setVisibility(View.INVISIBLE);
		download_progress.setVisibility(View.INVISIBLE);
		extract_label.setVisibility(View.INVISIBLE);
		extract_progress.setVisibility(View.INVISIBLE);
		button_cancel.setEnabled(false);

		final Task task = new Task();
		button_start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View arg0)
			{
				download_label.setVisibility(View.VISIBLE);
				download_progress.setVisibility(View.VISIBLE);
				button_start.setEnabled(false);
				button_cancel.setEnabled(true);
				task.execute();
			}

		});

		button_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View arg0)
			{
				task.cancel(true);
				finish();
			}

		});
	}
}