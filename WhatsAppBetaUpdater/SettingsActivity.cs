
using Android.App;
using Android.OS;
using Android.Views;
using Android.Widget;
using Android.Preferences;

namespace WhatsAppBetaUpdater {
	[Activity (Label = "SettingsActivity")]			
	public class SettingsActivity : PreferenceActivity {
		protected override void OnCreate (Bundle bundle) {
			base.OnCreate (bundle);
			AddPreferencesFromResource (Resource.Layout.settings);

		}

		public override bool OnOptionsItemSelected (IMenuItem item) {
			if (item.ItemId == Android.Resource.Id.Home) {
				Finish ();
			}

			return base.OnOptionsItemSelected (item);
		}
	}

}

