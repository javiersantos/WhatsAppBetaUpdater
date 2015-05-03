using Android.App;
using Android.Views;
using Android.OS;
using Android.Support.V7.App;
using Android.Widget;
using Toolbar = Android.Support.V7.Widget.Toolbar;
using AlertDialog = Android.Support.V7.App.AlertDialog;
using System.Net;
using System;
using System.ComponentModel;
using Android.Content;
using System.Threading.Tasks;
using Android.Gms.Ads;
using Android.Preferences;
using Android.Content.PM;
using Connectivity.Plugin;
using com.refractored.fab;
using System.IO;

namespace WhatsAppBetaUpdater {
	[Activity (Label = "@string/app_name", MainLauncher = true, Icon = "@drawable/ic_launcher", ScreenOrientation = ScreenOrientation.Portrait)]
	public class MainActivity : AppCompatActivity {
		
		// Predefined variables
		private string installedWhatsAppVersion;
		private string latestWhatsAppVersion;
		private string installedAppVersion;
		private string latestAppVersion;
		private string appUrl = "https://github.com/javiersantos/WhatsAppBetaUpdater.Android/tags";
		private string appApk = "https://github.com/javiersantos/WhatsAppBetaUpdater.Android/releases/download/";
		private string whatsAppUrl = "http://www.whatsapp.com/android/";
		private string whatsAppApk = "http://www.whatsapp.com/android/current/WhatsApp.apk";
		private string filename = Android.OS.Environment.GetExternalStoragePublicDirectory (Android.OS.Environment.DirectoryDownloads).ToString () + "/";
		private string fullLatestWhatsAppFilename;
		private string fullLatestAppFilename = Android.OS.Environment.GetExternalStoragePublicDirectory (Android.OS.Environment.DirectoryDownloads).ToString () + "/com.javiersantos.whatsappbetaupdater.apk";

		// Preferences variables
		private ISharedPreferences prefs;
		private bool prefAutoDownload;
		private bool prefEnableNotifications;
		private int prefHoursNotifications;
		private bool prefShowAppUpdates;
		private long millisecondsNotifications;
		private bool doubleBackPressed = false;
		private bool downloadedOK;

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
				if (prefShowAppUpdates) {
					GetLatestAppVersion (appUrl);
				}
				GetLatestWhatsAppVersion (whatsAppUrl);
//				SetAdmobBanner ();
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
//					SetAdmobInterstitial ();
				}
			}
		}

		public override bool OnCreateOptionsMenu (IMenu menu) {
			MenuInflater.Inflate (Resource.Menu.items, menu);
			return base.OnCreateOptionsMenu (menu);
		}

		public override bool OnOptionsItemSelected (IMenuItem item) {
			switch (item.ItemId) {
			case Resource.Id.menu_refresh:
				Finish ();
				StartActivity (typeof(MainActivity));
				OverridePendingTransition (Resource.Animation.abc_fade_in, Resource.Animation.abc_fade_out);
				if (CompareVersionReceiver.VersionCompare(installedWhatsAppVersion, latestWhatsAppVersion) < 0) {
					Toast.MakeText (this, string.Format(Resources.GetString (Resource.String.available), "WhatsApp " + latestWhatsAppVersion), ToastLength.Long).Show ();
				} else {
					Toast.MakeText (this, string.Format(Resources.GetString (Resource.String.latest_installed_description), "WhatsApp " + installedWhatsAppVersion), ToastLength.Long).Show ();
				}
				return true;
			case Resource.Id.menu_settings:
				Finish ();
				StartActivity (typeof(SettingsActivity));
				return true;
			case Resource.Id.menu_share:
				Intent shareIntent = new Intent (Intent.ActionSend);
				shareIntent.SetType ("text/plain");
				shareIntent.PutExtra (Intent.ExtraText, string.Format(Resources.GetString (Resource.String.share_description), Resources.GetString (Resource.String.app_name)) + ": WhatsApp " + latestWhatsAppVersion + " " + "http://bit.ly/BetaUpdaterAndroid");
				StartActivity (Intent.CreateChooser(shareIntent, Resources.GetString(Resource.String.share)));
				return true;
			}
			return base.OnOptionsItemSelected (item);
		}

		// Retrieve latest version of WhatsApp
		public async Task GetLatestWhatsAppVersion (string pageUrl) {
			var getVersion = new WebClient();
			string htmlAndroid = getVersion.DownloadString (new Uri(pageUrl));

			// Get WhatsApp latest version
			string[] split = htmlAndroid.Split (new char[] { '>' });
			int i = 0;
			while (i < split.Length) {
				if (split.GetValue (i).ToString ().StartsWith ("Version")) {
					split = split.GetValue (i).ToString ().Split (new char[] { ' ', '<' });
					latestWhatsAppVersion = split.GetValue (1).ToString ().Trim ();
					break;
				}
				i++;
			}

			// Display WhatsApp installed and latest version
			TextView whatsapp_installed_version = FindViewById<TextView> (Resource.Id.whatsapp_installed_version);
			TextView whatsapp_latest_version = FindViewById<TextView> (Resource.Id.whatsapp_latest_version);

			installedWhatsAppVersion = PackageManager.GetPackageInfo ("com.whatsapp", 0).VersionName.Trim ();

			whatsapp_installed_version.Text = installedWhatsAppVersion;
			whatsapp_latest_version.Text = latestWhatsAppVersion;

			fullLatestWhatsAppFilename = filename + "WhatsApp_" + latestWhatsAppVersion + ".apk";

			// Load Floating Button
			var fab = FindViewById<FloatingActionButton> (Resource.Id.fab);

			// Compare installed and latest WhatsApp version
			if (CompareVersionReceiver.VersionCompare(installedWhatsAppVersion, latestWhatsAppVersion) < 0) { // There is a new version
				fab.SetImageDrawable(Resources.GetDrawable(Resource.Drawable.ic_download));
				// Preference: Autodownload
				if (prefAutoDownload) {
					SetDownloadDialog ();
					var webClient = new WebClient ();
					webClient.DownloadProgressChanged += new DownloadProgressChangedEventHandler (downloadProgressChanged);
					webClient.DownloadFileCompleted += new AsyncCompletedEventHandler (downloadWhatsAppFileCompleted);
					webClient.DownloadFileAsync (new Uri (whatsAppApk), fullLatestWhatsAppFilename);
					progressDialog.SetTitle (string.Format(Resources.GetString(Resource.String.downloading), "WhatsApp " + latestWhatsAppVersion + "..."));
					progressDialog.SetButton (Resources.GetString(Resource.String.cancel_button), (object senderCancel, DialogClickEventArgs eCancel) => {webClient.CancelAsync (); progressDialog.Dismiss ();});
					progressDialog.Show ();
				} else {
					fab.Click += delegate {
						SetDownloadDialog ();
						var webClient = new WebClient ();
						webClient.DownloadProgressChanged += new DownloadProgressChangedEventHandler (downloadProgressChanged);
						webClient.DownloadFileCompleted += new AsyncCompletedEventHandler (downloadWhatsAppFileCompleted);
						webClient.DownloadFileAsync (new Uri (whatsAppApk), fullLatestWhatsAppFilename);
						progressDialog.SetTitle (string.Format(Resources.GetString(Resource.String.downloading), "WhatsApp " + latestWhatsAppVersion + "..."));
						progressDialog.SetButton (Resources.GetString(Resource.String.cancel_button), (object senderCancel, DialogClickEventArgs eCancel) => {webClient.CancelAsync (); progressDialog.Dismiss ();});
						progressDialog.Show ();
					};
				}
			// There is not a new version
			} else {
				fab.SetImageDrawable(Resources.GetDrawable(Resource.Drawable.ic_menu_about));
				fab.Click += delegate {
					AlertDialog errorInstalled = new AlertDialog.Builder (this).Create ();
					errorInstalled.SetTitle (Resources.GetString(Resource.String.latest_installed));
					errorInstalled.SetMessage (string.Format(Resources.GetString(Resource.String.latest_installed_description), "WhatsApp " + installedWhatsAppVersion));
					errorInstalled.SetButton ((int)DialogButtonType.Positive, Resources.GetString(Resource.String.ok), (object senderClose, DialogClickEventArgs eClose) => errorInstalled.Dismiss ());
					errorInstalled.Show ();
				};
			}
		}

		// Retrieve latest version of Beta Updater
		public async Task GetLatestAppVersion (string pageUrl) {
			var getVersion = new WebClient();
			string htmlAndroid = getVersion.DownloadString (new Uri(pageUrl));

			// Get WhatsApp latest version
			string[] split = htmlAndroid.Split (new char[] { '>' });
			int i = 0;
			while (i < split.Length) {
				if (split.GetValue (i).ToString ().StartsWith ("v")) {
					split = split.GetValue (i).ToString ().Split (new char[] { 'v', '<' });
					latestAppVersion = split.GetValue (1).ToString ().Trim ();
					break;
				}
				i++;
			}

			installedAppVersion = PackageManager.GetPackageInfo ("com.javiersantos.whatsappbetaupdater", 0).VersionName.Trim ();

			if (CompareVersionReceiver.VersionCompare (installedAppVersion, latestAppVersion) < 0) {
				appApk = appApk + "v" + latestAppVersion + "/com.javiersantos.whatsappbetaupdater.apk";

				AlertDialog appUpdateDialog = new AlertDialog.Builder (this).Create ();
				appUpdateDialog.SetTitle (string.Format(Resources.GetString(Resource.String.app_update), latestAppVersion));
				appUpdateDialog.SetMessage (string.Format(Resources.GetString(Resource.String.app_update_description), Resources.GetString(Resource.String.app_name)));
				appUpdateDialog.SetButton ((int)DialogButtonType.Positive, Resources.GetString (Resource.String.update_button), (object senderUpdateAppOK, DialogClickEventArgs eUpdateAppOK) => {
					SetDownloadDialog ();
					var webClient = new WebClient ();
					webClient.DownloadProgressChanged += new DownloadProgressChangedEventHandler (downloadProgressChanged);
					webClient.DownloadFileCompleted += new AsyncCompletedEventHandler (downloadAppFileCompleted);
					webClient.DownloadFileAsync (new Uri (appApk), fullLatestAppFilename);
					progressDialog.SetTitle (string.Format(Resources.GetString(Resource.String.downloading), Resources.GetString(Resource.String.app_name) + " " + latestAppVersion + "..."));
					progressDialog.SetButton (Resources.GetString(Resource.String.cancel_button), (object senderCancel, DialogClickEventArgs eCancel) => {webClient.CancelAsync (); progressDialog.Dismiss ();});
					progressDialog.Show ();
				});
				appUpdateDialog.SetButton ((int)DialogButtonType.Negative, Resources.GetString(Resource.String.cancel_button), (object senderUpdateAppCancel, DialogClickEventArgs eUpdateAppCancel) => appUpdateDialog.Dismiss ());
				appUpdateDialog.SetButton ((int)DialogButtonType.Neutral, Resources.GetString(Resource.String.never_button), (object senderUpdateAppNever, DialogClickEventArgs eUpdateAppNever) => {prefs.Edit().PutBoolean("prefShowAppUpdates", false).Commit(); appUpdateDialog.Dismiss (); });
				appUpdateDialog.SetCancelable (false);
				appUpdateDialog.Show ();
			}
		}

		void downloadProgressChanged (object sender, DownloadProgressChangedEventArgs e) {
			var bytesIn = double.Parse (e.BytesReceived.ToString ());
			var totalBytes = double.Parse (e.TotalBytesToReceive.ToString ());
			var percentage = (int)(bytesIn / totalBytes * 100);
			if (percentage == 100) {
				downloadedOK = true;
			}

			RunOnUiThread (() => {
				progressDialog.Progress = percentage;
			});
		}

		void downloadWhatsAppFileCompleted (object sender, AsyncCompletedEventArgs e) {
			RunOnUiThread (() => {
				progressDialog.Dismiss ();
				if (downloadedOK) {
					var installApk = new Intent(Intent.ActionView);
					installApk.SetDataAndType(Android.Net.Uri.Parse("file://" + fullLatestWhatsAppFilename), "application/vnd.android.package-archive");
					installApk.SetFlags(ActivityFlags.NewTask);
					try {
						StartActivity(installApk);
						AlertDialog deleteWhatsApp = new AlertDialog.Builder (this).Create ();
						deleteWhatsApp.SetTitle (Resources.GetString(Resource.String.delete));
						deleteWhatsApp.SetMessage (Resources.GetString(Resource.String.delete_description));
						deleteWhatsApp.SetButton ((int)DialogButtonType.Positive, Resources.GetString(Resource.String.delete_button_delete), (object senderDelete, DialogClickEventArgs eDelete) => File.Delete(fullLatestWhatsAppFilename));
						deleteWhatsApp.SetButton ((int)DialogButtonType.Negative, Resources.GetString(Resource.String.delete_button_cancel), (object senderCancel, DialogClickEventArgs eCancel) => deleteWhatsApp.Dismiss ());
						deleteWhatsApp.SetCancelable (false);
						deleteWhatsApp.Show ();
					} catch (ActivityNotFoundException ex) {
						var errorInstalled = new AlertDialog.Builder (this).Create ();
						errorInstalled.SetTitle (Resources.GetString(Resource.String.download_error));
						errorInstalled.SetMessage (string.Format(Resources.GetString(Resource.String.download_error_description), "WhatsApp " + latestWhatsAppVersion));
						errorInstalled.Show ();	
					}
					downloadedOK = false;
				} else {
					File.Delete(fullLatestWhatsAppFilename);
				}
			});
		}

		void downloadAppFileCompleted (object sender, AsyncCompletedEventArgs e) {
			RunOnUiThread (() => {
				progressDialog.Dismiss ();
				if (downloadedOK) {
					var installApk = new Intent(Intent.ActionView);
					installApk.SetDataAndType(Android.Net.Uri.Parse("file://" + fullLatestAppFilename), "application/vnd.android.package-archive");
					installApk.SetFlags(ActivityFlags.NewTask);
					try {
						StartActivity(installApk);
					} catch (ActivityNotFoundException ex) {
						var errorInstalled = new AlertDialog.Builder (this).Create ();
						errorInstalled.SetTitle (Resources.GetString(Resource.String.download_error));
						errorInstalled.SetMessage (string.Format(Resources.GetString(Resource.String.download_error_description), Resources.GetString(Resource.String.app_name) + " " + latestAppVersion));
						errorInstalled.Show ();	
					}
					downloadedOK = false;
				} else {
					File.Delete(fullLatestAppFilename);
				}
			});
		}

		public void SetDownloadDialog () {
			progressDialog = new ProgressDialog (this);
			progressDialog.SetProgressStyle (ProgressDialogStyle.Horizontal);
			progressDialog.Progress = 0;
			progressDialog.Max = 100;
			progressDialog.SetCancelable (false);
			downloadedOK = false;
		}

		public void GetPreferences() {
			prefs = PreferenceManager.GetDefaultSharedPreferences(this);
			prefAutoDownload = prefs.GetBoolean ("prefAutoDownload", false);
			prefEnableNotifications = prefs.GetBoolean ("prefEnableNotifications", true);
			prefShowAppUpdates = prefs.GetBoolean ("prefShowAppUpdates", true);
			prefHoursNotifications = int.Parse(prefs.GetString ("prefHoursNotifications", "4"))-1;
			if (prefHoursNotifications == 1) {
				millisecondsNotifications = 3 * 60 * 60 * 1000;
			} else if (prefHoursNotifications == 2) {
				millisecondsNotifications = 6 * 60 * 60 * 1000;
			} else if (prefHoursNotifications == 3) {
				millisecondsNotifications = 9 * 60 * 60 * 1000;
			} else if (prefHoursNotifications == 4) {
				millisecondsNotifications = 12 * 60 * 60 * 1000;
			} else if (prefHoursNotifications == 5) {
				millisecondsNotifications = 24 * 60 * 60 * 1000;
			} else {
				millisecondsNotifications = 12 * 60 * 60 * 1000;
			}

			SetNotification ();
		}

		public void SetAdmobBanner () {
			var admob = FindViewById<LinearLayout> (Resource.Id.adView);
			var bannerAd = AdWrapper.ConstructStandardBanner(this, AdSize.Banner, Resources.GetString (Resource.String.admob_banner));
			bannerAd.CustomBuild ();
			admob.AddView (bannerAd);
		}

		public void SetAdmobInterstitial () {
			var fullAd = AdWrapper.ConstructFullPageAdd(this, Resources.GetString(Resource.String.admob_interstitial));
			var intListener = new AdEventListener ();
			intListener.AdLoaded += () => { if (fullAd.IsLoaded)fullAd.Show (); };
			fullAd.AdListener = intListener;
			fullAd.CustomBuild ();
		}

		public void SetNotification () {
			Intent alarmIntent = new Intent (this, typeof(AlarmReceiver));

			PendingIntent pendingIntent = PendingIntent.GetBroadcast (this, 0, alarmIntent, PendingIntentFlags.UpdateCurrent);
			AlarmManager alarmManager = (AlarmManager)this.GetSystemService (Context.AlarmService);

			if (prefEnableNotifications) {
				alarmManager.SetRepeating (AlarmType.ElapsedRealtimeWakeup, SystemClock.ElapsedRealtime (), millisecondsNotifications, pendingIntent);
			} else {
				alarmManager.Cancel (pendingIntent);
			}
		}

	}
}
