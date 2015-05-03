
using Android.App;
using Android.OS;
using Android.Views;
using Android.Preferences;
using Android.Content;

namespace WhatsAppBetaUpdater {
	[Activity (Label = "@string/settings")]			
	public class SettingsActivity : PreferenceActivity, ISharedPreferencesOnSharedPreferenceChangeListener {
		private Preference prefVersion;
		private Preference prefHoursNotifications;
		private ISharedPreferences prefs;

		protected override void OnCreate (Bundle bundle) {
			base.OnCreate (bundle);
			AddPreferencesFromResource (Resource.Layout.settings_prefs);

			prefs = PreferenceManager.GetDefaultSharedPreferences(this);
			prefs.RegisterOnSharedPreferenceChangeListener(this);

			prefVersion = FindPreference ("prefVersion");
			prefVersion.Title = Resources.GetString(Resource.String.app_name) + " v" + ApplicationContext.PackageManager.GetPackageInfo (ApplicationContext.PackageName, 0).VersionName + " (" + ApplicationContext.PackageManager.GetPackageInfo (ApplicationContext.PackageName, 0).VersionCode + ")";

			prefHoursNotifications = FindPreference ("prefHoursNotifications");
			prefHoursNotifications.Summary = string.Format (Resources.GetString (Resource.String.settings_interval_description), Resources.GetStringArray(Resource.Array.hours).GetValue(int.Parse(prefs.GetString ("prefHoursNotifications", "4"))-1));

		}

		public void OnSharedPreferenceChanged (ISharedPreferences sharedPreferences, string key) {
			Preference pref = FindPreference (key);

			if (pref == prefHoursNotifications) {
				prefHoursNotifications.Summary = string.Format (Resources.GetString (Resource.String.settings_interval_description), Resources.GetStringArray(Resource.Array.hours).GetValue(int.Parse(prefs.GetString ("prefHoursNotifications", "4"))-1));
			}
		}

		public override bool OnOptionsItemSelected (IMenuItem item) {
			if (item.ItemId == Android.Resource.Id.Home) {
				Finish ();
				StartActivity (typeof(MainActivity));
			}

			return base.OnOptionsItemSelected (item);
		}

		public override void OnBackPressed () {
			Finish ();
			StartActivity (typeof(MainActivity));
		}

	}

}

