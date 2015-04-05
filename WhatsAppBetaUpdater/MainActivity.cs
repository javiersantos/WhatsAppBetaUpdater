using Android.App;
using Android.Views;
using Android.OS;
using Android.Support.V7.App;
using Android.Widget;
using Toolbar = Android.Support.V7.Widget.Toolbar;
using System.Net;
using System.IO;
using System.Net.Http;
using System.Text;
using System;
using Java.Net;
using Java.IO;
using Android.Util;
using System.ComponentModel;
using Android.Content;
using Android.Support.V4.Widget;
using System.Threading.Tasks;
using Android.Gms.Ads;
using Java.Lang;

namespace WhatsAppBetaUpdater {
	[Activity (Label = "@string/app_name", MainLauncher = true, Icon = "@drawable/ic_launcher")]
	public class MainActivity : ActionBarActivity {
		private string installedVersion;
		private string latestVersion;
		private string webUrl = "http://www.whatsapp.com/android/";
		private string apkUrl = "http://www.whatsapp.com/android/current/WhatsApp.apk";
		private string filename = "/sdcard/Download/WhatsApp.apk";

		protected override void OnCreate (Bundle bundle) {
			base.OnCreate (bundle);

			// Set our view from the "main" layout resource
			SetContentView (Resource.Layout.main);

			var toolbar = FindViewById<Toolbar> (Resource.Id.toolbar);
//			var refresher = FindViewById<SwipeRefreshLayout> (Resource.Id.refresher);

			SetSupportActionBar (toolbar);
			SupportActionBar.Title = Resources.GetString (Resource.String.app_name);;
			SupportActionBar.SetIcon (Resource.Drawable.ic_launcher);

			AdView ad = new AdView (this);
			ad.AdSize = AdSize.Banner;
			ad.AdUnitId = Resources.GetString(Resource.String.admob);
			var requestBuilder = new AdRequest.Builder ();
			ad.LoadAd(requestBuilder.Build());
			var admob = FindViewById<LinearLayout> (Resource.Id.adView);
			admob.AddView (ad);

//			refresher.SetColorScheme (Resource.Color.xam_dark_blue, Resource.Color.xam_purple, Resource.Color.xam_gray, Resource.Color.xam_green);
//			refresher.Refresh += HandleRefresh;

			GetLatestVersion (webUrl);

		}

		// Retrieve WhatsApp HTML code
		public async Task GetLatestVersion (string pageUrl) {
			var getVersion = new WebClient();
			string htmlAndroid = getVersion.DownloadString (new Uri(pageUrl));

			// Get WhatsApp latest version
			string[] split = htmlAndroid.Split (new char[] { '>' });
			int i = 0;
			while (i < split.Length) {
				if (split.GetValue (i).ToString ().StartsWith ("V")) {
					split = split.GetValue (i).ToString ().Split (new char[] { ' ', '<' });
					latestVersion = split.GetValue (1).ToString ().Trim ();
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

			if (versionCompare(installedVersion, latestVersion) < 0) {
				whatsapp_button_update.Text = Resources.GetString (Resource.String.whatsapp_button_update);
				whatsapp_button_update.Click += delegate {
					var webClient = new WebClient ();
					webClient.DownloadProgressChanged += new DownloadProgressChangedEventHandler (downloadProgressChanged);
					webClient.DownloadFileCompleted += new AsyncCompletedEventHandler (downloadFileCompleted);
					webClient.DownloadFileAsync (new Uri(apkUrl), filename);
					whatsapp_button_update.Text = Resources.GetString(Resource.String.downloading) + "...";
				};
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

		async void HandleRefresh (object sender, EventArgs e) {
			await GetLatestVersion (webUrl);
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
			}
			return base.OnOptionsItemSelected (item);
		}

	}
}


