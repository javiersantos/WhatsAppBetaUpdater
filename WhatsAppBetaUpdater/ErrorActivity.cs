
using Android.App;
using Android.OS;
using Android.Support.V7.App;
using Toolbar = Android.Support.V7.Widget.Toolbar;
using Android.Content;
using Android.Views;
using Android.Content.PM;

namespace WhatsAppBetaUpdater {
	[Activity (Label = "ErrorActivity", ScreenOrientation = ScreenOrientation.Portrait, NoHistory = true)]			
	public class ErrorActivity : AppCompatActivity {
		protected override void OnCreate (Bundle bundle) {
			base.OnCreate (bundle);

			SetContentView (Resource.Layout.error_noconnection);

			var toolbar = FindViewById<Toolbar> (Resource.Id.toolbar);

			SetSupportActionBar (toolbar);
			SupportActionBar.Title = Resources.GetString (Resource.String.app_name);

		}

		public override bool OnCreateOptionsMenu (IMenu menu) {
			MenuInflater.Inflate (Resource.Menu.items, menu);
			return base.OnCreateOptionsMenu (menu);
		}
		public override bool OnOptionsItemSelected (IMenuItem item) {
			switch (item.ItemId) {
			case Resource.Id.menu_refresh:
				StartActivity (typeof(MainActivity));
				return true;
			case Resource.Id.menu_settings:
				StartActivity (typeof(SettingsActivity));
				return true;
			case Resource.Id.menu_share:
				Intent shareIntent = new Intent (Intent.ActionSend);
				shareIntent.SetType ("text/plain");
				shareIntent.PutExtra (Intent.ExtraText, string.Format(Resources.GetString (Resource.String.share_description), Resources.GetString (Resource.String.app_name)) + "http://bit.ly/BetaUpdaterAndroid");
				StartActivity (Intent.CreateChooser(shareIntent, Resources.GetString(Resource.String.share)));
				return true;
			}
			return base.OnOptionsItemSelected (item);
		}

	}
}
