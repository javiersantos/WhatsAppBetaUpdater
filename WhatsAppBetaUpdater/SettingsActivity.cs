
using Android.App;
using Android.OS;
using Android.Views;
using Android.Preferences;

namespace WhatsAppBetaUpdater {
	[Activity (Label = "SettingsActivity")]			
	public class SettingsActivity : PreferenceActivity {
		protected override void OnCreate (Bundle bundle) {
			base.OnCreate (bundle);
			AddPreferencesFromResource (Resource.Layout.settings_prefs);

			Preference prefVersion = FindPreference ("prefVersion");
			prefVersion.Title = Resources.GetString(Resource.String.app_name) + " v" + ApplicationContext.PackageManager.GetPackageInfo (ApplicationContext.PackageName, 0).VersionName + " (" + ApplicationContext.PackageManager.GetPackageInfo (ApplicationContext.PackageName, 0).VersionCode + ")";

		}

		public override bool OnOptionsItemSelected (IMenuItem item) {
			if (item.ItemId == Android.Resource.Id.Home) {
				Finish ();
			}

			return base.OnOptionsItemSelected (item);
		}
	}

}

