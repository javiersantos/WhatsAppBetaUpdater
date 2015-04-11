using Android.App;
using Android.Views;
using Android.OS;
using Android.Support.V7.App;
using Android.Widget;
using Toolbar = Android.Support.V7.Widget.Toolbar;
using System.Net;
using System;
using System.ComponentModel;
using Android.Content;
using System.Threading.Tasks;
using Android.Gms.Ads;
using Java.Lang;
using Android.Preferences;
using Android.Content.PM;
using Connectivity.Plugin;

namespace WhatsAppBetaUpdater {
	[Activity (Label = "@string/app_name", MainLauncher = true, Icon = "@drawable/ic_launcher", ScreenOrientation = ScreenOrientation.Portrait)]
	public class MainActivity : ActionBarActivity {
		
		// Predefined variables
		private string installedVersion;
		private string latestVersion;
		private string webUrl = "http://www.whatsapp.com/android/";
		private string apkUrl = "http://www.whatsapp.com/android/current/WhatsApp.apk";
		private string filename = "/sdcard/Download/WhatsApp.apk";

		// Preferences variables
		private bool prefAutoDownload;

		// Start Beta Updates for WhatsApp
		protected override void OnCreate (Bundle bundle) {
			base.OnCreate (bundle);

			SetContentView (Resource.Layout.main);

			var toolbar = FindViewById<Toolbar> (Resource.Id.toolbar);
			var admob = FindViewById<LinearLayout> (Resource.Id.adView);

			SetSupportActionBar (toolbar);
			SupportActionBar.Title = Resources.GetString (Resource.String.app_name);;
			SupportActionBar.SetIcon (Resource.Drawable.ic_launcher);


			GetPreferences ();

			if (CrossConnectivity.Current.IsConnected) {
				AdView ad = new AdView (this);
				ad.AdSize = AdSize.Banner;
				ad.AdUnitId = Resources.GetString (Resource.String.admob);
				var requestBuilder = new AdRequest.Builder ();
				ad.LoadAd (requestBuilder.Build ());
				admob.AddView (ad);

				GetLatestVersion (webUrl);
			} else {
				StartActivity (typeof(ErrorActivity));
			}

		}

		// Retrieve WhatsApp HTML code
		public async Task GetLatestVersion (string pageUrl) {
			var getVersion = new WebClient();
			string htmlAndroid = getVersion.DownloadString (new Uri(pageUrl));

			// Get WhatsApp latest version
			string[] split = htmlAndroid.Split (new char[] { '>' });
			int i = 0;
			while (i < split.Length) {
				if (split.GetValue (i).ToString ().StartsWith ("Version")) {
					split = split.GetValue (i).ToString ().Split (new char[] { ' ', '<' });
					latestVersion = split.GetValue (1).ToString ().Trim ();
					break;
				}
				i++;
			}

			// Display WhatsApp installed and latest version
			TextView whatsapp_installed_version = FindViewById<TextView> (Resource.Id.whatsapp_installed_version);
			TextView whatsapp_latest_version = FindViewById<TextView> (Resource.Id.whatsapp_latest_version);

			installedVersion = PackageManager.GetPackageInfo ("com.whatsapp", 0).VersionName.Trim ();

			whatsapp_installed_version.Text = installedVersion;
			whatsapp_latest_version.Text = latestVersion;

			// Show update or latest version button
			Button whatsapp_button_update = FindViewById<Button> (Resource.Id.whatsapp_button_update);

			// Compare installed and latest WhatsApp version
			if (versionCompare(installedVersion, latestVersion) < 0) { // There is a new version
				whatsapp_button_update.Text = Resources.GetString (Resource.String.whatsapp_button_update);
				// Preference: Autodownload
				if (prefAutoDownload) {
					var webClient = new WebClient ();
					webClient.DownloadProgressChanged += new DownloadProgressChangedEventHandler (downloadProgressChanged);
					webClient.DownloadFileCompleted += new AsyncCompletedEventHandler (downloadFileCompleted);
					webClient.DownloadFileAsync (new Uri (apkUrl), filename);
					whatsapp_button_update.Text = Resources.GetString (Resource.String.downloading) + "...";
				} else {
					whatsapp_button_update.Click += delegate {
						var webClient = new WebClient ();
						webClient.DownloadProgressChanged += new DownloadProgressChangedEventHandler (downloadProgressChanged);
						webClient.DownloadFileCompleted += new AsyncCompletedEventHandler (downloadFileCompleted);
						webClient.DownloadFileAsync (new Uri (apkUrl), filename);
						whatsapp_button_update.Text = Resources.GetString (Resource.String.downloading) + "...";
					};
				}
			// There is not a new version
			} else {
				whatsapp_button_update.Text = Resources.GetString (Resource.String.whatsapp_button_latest);
				whatsapp_button_update.Click += delegate {
					var errorInstalled = new AlertDialog.Builder (this).Create ();
					errorInstalled.SetTitle (Resources.GetString(Resource.String.latest_installed));
					errorInstalled.SetMessage ("WhatsApp " + installedVersion + " " + Resources.GetString(Resource.String.latest_installed_description));
					errorInstalled.Show ();
				};
			}
		}

		void downloadProgressChanged (object sender, DownloadProgressChangedEventArgs e) {
			var bytesIn = double.Parse (e.BytesReceived.ToString ());
			var totalBytes = double.Parse (e.TotalBytesToReceive.ToString ());
			var percentage = (int)(bytesIn / totalBytes * 100);

			RunOnUiThread (() => {
				Button whatsapp_button_update = FindViewById<Button> (Resource.Id.whatsapp_button_update);
				whatsapp_button_update.Text = Resources.GetString(Resource.String.downloading) + ": " + percentage + " %";
				whatsapp_button_update.Enabled = false;
			});
		}

		void downloadFileCompleted (object sender, AsyncCompletedEventArgs e) {
			RunOnUiThread (() => {
				Button whatsapp_button_update = FindViewById<Button> (Resource.Id.whatsapp_button_update);
				whatsapp_button_update.Text = Resources.GetString(Resource.String.download_again);
				var installWhatsApp = new Intent(Intent.ActionView);
				installWhatsApp.SetDataAndType(Android.Net.Uri.Parse("file://" + filename), "application/vnd.android.package-archive");
				installWhatsApp.SetFlags(ActivityFlags.NewTask);
				try {
					StartActivity(installWhatsApp);
				} catch (ActivityNotFoundException ex) {
					var errorInstalled = new AlertDialog.Builder (this).Create ();
					errorInstalled.SetTitle (Resources.GetString(Resource.String.download_error));
					errorInstalled.SetMessage ("WhatsApp " + latestVersion + " " + Resources.GetString(Resource.String.download_error_description));
					errorInstalled.Show ();	
				}
				whatsapp_button_update.Enabled = true;
			});
		}

		public int versionCompare (string oldVersion, string newVersion) {
			string[] splitOld = oldVersion.Split (new char[] { '.' });
			string[] splitNew = newVersion.Split (new char[] { '.' });
			int i = 0;
			while (i < splitOld.Length && i < splitNew.Length && splitOld[i].Equals(splitNew[i])) {
				i++;
			}
			if (i < splitOld.Length && i < splitNew.Length) {
				int diff = Integer.ValueOf (splitOld [i]).CompareTo (Integer.ValueOf (splitNew [i]));
				return Integer.Signum (diff);
			} else {
				return Integer.Signum (splitOld.Length - splitNew.Length);
			}
		}

		public override bool OnCreateOptionsMenu (IMenu menu) {
			MenuInflater.Inflate (Resource.Menu.items, menu);
			return base.OnCreateOptionsMenu (menu);
		}
		public override bool OnOptionsItemSelected (IMenuItem item) {
			switch (item.ItemId) {
			case Resource.Id.menu_refresh:
				GetLatestVersion (webUrl);
				if (versionCompare(installedVersion, latestVersion) < 0) {
					Toast.MakeText (this, "WhatsApp " + latestVersion + " " + Resources.GetString (Resource.String.available), ToastLength.Short).Show ();
				} else {
					Toast.MakeText (this, "WhatsApp " + installedVersion + " " + Resources.GetString (Resource.String.latest_installed_description), ToastLength.Short).Show ();
				}
				return true;
			case Resource.Id.menu_settings:
				StartActivity (typeof(SettingsActivity));
				return true;
			case Resource.Id.menu_share:
				Intent shareIntent = new Intent (Android.Content.Intent.ActionSend);
				shareIntent.SetType ("text/plain");
				shareIntent.PutExtra (Android.Content.Intent.ExtraSubject, Resources.GetString (Resource.String.app_name));
				shareIntent.PutExtra (Android.Content.Intent.ExtraText, Resources.GetString (Resource.String.share_description) + ": WhatsApp " + latestVersion + " " + "https://play.google.com/store/apps/details?id=com.javiersantos.whatsappbetaupdater");
				StartActivity (Intent.CreateChooser(shareIntent, Resources.GetString(Resource.String.share)));
				return true;
			}
			return base.OnOptionsItemSelected (item);
		}

		public void GetPreferences() {
			ISharedPreferences prefs = PreferenceManager.GetDefaultSharedPreferences(this);
			prefAutoDownload = prefs.GetBoolean ("prefAutoDownload", false);
		}

	}
}
