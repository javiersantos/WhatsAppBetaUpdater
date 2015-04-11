
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Android.App;
using Android.Content;
using Android.OS;
using Android.Runtime;
using Android.Views;
using Android.Widget;
using Android.Support.V7.App;
using Android.Preferences;

namespace WhatsAppBetaUpdater {
	[Activity (Label = "SettingsActivity")]			
	public class SettingsActivity : PreferenceActivity {
		protected override void OnCreate (Bundle bundle) {
			base.OnCreate (bundle);
			AddPreferencesFromResource (Resource.Layout.settings);

			TextView settings_about = FindViewById<TextView> (Resource.Id.settings_about);

		}

		public override bool OnOptionsItemSelected (IMenuItem item) {
			if (item.ItemId == Android.Resource.Id.Home) {
				Finish ();
			}

			return base.OnOptionsItemSelected (item);
		}
	}

}

