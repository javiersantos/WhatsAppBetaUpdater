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
using com.refractored.fab;
using System.IO;

namespace WhatsAppBetaUpdater {
	[Activity (Label = "@string/app_name", MainLauncher = true, Icon = "@drawable/ic_launcher", ScreenOrientation = ScreenOrientation.Portrait)]
	public class MainActivity : ActionBarActivity {
		
		// Predefined variables
		private string installedVersion;
		private string latestVersion;
		private string webUrl = "http://www.whatsapp.com/android/";
		private string apkUrl = "http://www.whatsapp.com/android/current/WhatsApp.apk";
		private string filename = Android.OS.Environment.GetExternalStoragePublicDirectory (Android.OS.Environment.DirectoryDownloads).ToString () + "/";
		private string fullLatestFilename;

		// Preferences variables
		private bool prefAutoDownload;
		private bool doubleBackPressed = false;

		// ProgressDialog
		private ProgressDialog progressDialog;

		// Start Beta Updates for WhatsApp
		protected override void OnCreate (Bundle bundle) {
			base.OnCreate (bundle);

			SetContentView (Resource.Layout.main);

			var toolbar = FindViewById<Toolbar> (Resource.Id.toolbar);

			SetSupportActionBar (toolbar);
			SupportActionBar.Title = Resources.GetString (Resource.String.app_name);

			GetPreferences ();

			if (CrossConnectivity.Current.IsConnected) {
				GetLatestVersion (webUrl);
				SetAdmobBanner ();
			} else {
				Finish ();
				StartActivity (typeof(ErrorActivity));
			}

		}

		protected override void OnResume () {
			base.OnResume ();
			doubleBackPressed = false;
		}

		public override void OnBackPressed () {
			Random random = new Random ();

			if (doubleBackPressed) {
				base.OnBackPressed ();
				return;
			} else {
				doubleBackPressed = true;
				Toast.MakeText(this, Resources.GetString(Resource.String.toast_tap), ToastLength.Short).Show ();
				var showAd = random.Next (5);
				if (showAd == 0) {
					SetAdmobInterstitial ();
				}
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

			fullLatestFilename = filename + "WhatsApp_" + latestVersion + ".apk";

			// Load Floating Button
			var fab = FindViewById<FloatingActionButton> (Resource.Id.fab);

			// Compare installed and latest WhatsApp version
			if (VersionCompare(installedVersion, latestVersion) < 0) { // There is a new version
				fab.SetImageDrawable(Resources.GetDrawable(Resource.Drawable.ic_download));
				// Preference: Autodownload
				if (prefAutoDownload) {
					SetDownloadDialog ();
					var webClient = new WebClient ();
					webClient.DownloadProgressChanged += new DownloadProgressChangedEventHandler (downloadProgressChanged);
					webClient.DownloadFileCompleted += new AsyncCompletedEventHandler (downloadFileCompleted);
					webClient.DownloadFileAsync (new Uri (apkUrl), fullLatestFilename);
					progressDialog.Show ();
				} else {
					fab.Click += delegate {
						SetDownloadDialog ();
						var webClient = new WebClient ();
						webClient.DownloadProgressChanged += new DownloadProgressChangedEventHandler (downloadProgressChanged);
						webClient.DownloadFileCompleted += new AsyncCompletedEventHandler (downloadFileCompleted);
						webClient.DownloadFileAsync (new Uri (apkUrl), fullLatestFilename);
						progressDialog.Show ();
					};
				}
			// There is not a new version
			} else {
				fab.SetImageDrawable(Resources.GetDrawable(Resource.Drawable.ic_menu_about));
				fab.Click += delegate {
					AlertDialog errorInstalled = new AlertDialog.Builder (this).Create ();
					errorInstalled.SetTitle (Resources.GetString(Resource.String.latest_installed));
					errorInstalled.SetMessage ("WhatsApp " + installedVersion + " " + Resources.GetString(Resource.String.latest_installed_description));
					errorInstalled.SetButton (Resources.GetString(Resource.String.ok), (object senderClose, DialogClickEventArgs eClose) => errorInstalled.Dismiss ());
					errorInstalled.Show ();
				};
			}
		}

		void downloadProgressChanged (object sender, DownloadProgressChangedEventArgs e) {
			var bytesIn = double.Parse (e.BytesReceived.ToString ());
			var totalBytes = double.Parse (e.TotalBytesToReceive.ToString ());
			var percentage = (int)(bytesIn / totalBytes * 100);

			RunOnUiThread (() => {
				progressDialog.Progress = percentage;
			});
		}

		void downloadFileCompleted (object sender, AsyncCompletedEventArgs e) {
			RunOnUiThread (() => {
				progressDialog.Dismiss ();
				var installWhatsApp = new Intent(Intent.ActionView);
				installWhatsApp.SetDataAndType(Android.Net.Uri.Parse("file://" + fullLatestFilename), "application/vnd.android.package-archive");
				installWhatsApp.SetFlags(ActivityFlags.NewTask);
				try {
					StartActivity(installWhatsApp);
					AlertDialog errorInstalled = new AlertDialog.Builder (this).Create ();
					errorInstalled.SetTitle (Resources.GetString(Resource.String.delete));
					errorInstalled.SetMessage (Resources.GetString(Resource.String.delete_description));
					errorInstalled.SetButton (Resources.GetString(Resource.String.delete_button_delete), (object senderDelete, DialogClickEventArgs eDelete) => File.Delete(fullLatestFilename));
					errorInstalled.SetButton2 (Resources.GetString(Resource.String.delete_button_cancel), (object senderCancel, DialogClickEventArgs eCancel) => errorInstalled.Dismiss ());
					errorInstalled.Show ();
				} catch (ActivityNotFoundException ex) {
					var errorInstalled = new AlertDialog.Builder (this).Create ();
					errorInstalled.SetTitle (Resources.GetString(Resource.String.download_error));
					errorInstalled.SetMessage ("WhatsApp " + latestVersion + " " + Resources.GetString(Resource.String.download_error_description));
					errorInstalled.Show ();	
				}
			});
		}

		public int VersionCompare (string oldVersion, string newVersion) {
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

		public void SetDownloadDialog () {
			progressDialog = new ProgressDialog (this);
			progressDialog.SetProgressStyle (ProgressDialogStyle.Horizontal);
			progressDialog.SetTitle (Resources.GetString(Resource.String.downloading) +  " WhatsApp " + latestVersion + "...");
			progressDialog.Progress = 0;
			progressDialog.Max = 100;
			progressDialog.SetCancelable (false);
		}

		public override bool OnCreateOptionsMenu (IMenu menu) {
			MenuInflater.Inflate (Resource.Menu.items, menu);
			return base.OnCreateOptionsMenu (menu);
		}
		public override bool OnOptionsItemSelected (IMenuItem item) {
			switch (item.ItemId) {
			case Resource.Id.menu_refresh:
				GetLatestVersion (webUrl);
				if (VersionCompare(installedVersion, latestVersion) < 0) {
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

		public void SetAdmobBanner () {
			var admob = FindViewById<LinearLayout> (Resource.Id.adView);
			var bannerAd = AdWrapper.ConstructStandardBanner(this, AdSize.Banner, Resources.GetString (Resource.String.admob_banner));
			bannerAd.CustomBuild ();
			admob.AddView (bannerAd);
		}
		public void SetAdmobInterstitial () {
			var fullAd = AdWrapper.ConstructFullPageAdd(this, Resources.GetString(Resource.String.admob_interstitial));
			var intListener = new AdEventListerner ();
			intListener.AdLoaded += () => { if (fullAd.IsLoaded)fullAd.Show (); };
			fullAd.AdListener = intListener;
			fullAd.CustomBuild ();
		}

	}
}
