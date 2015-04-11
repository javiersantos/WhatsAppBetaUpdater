
using Android.App;
using Android.OS;
using Android.Widget;
using Android.Support.V7.App;
using Toolbar = Android.Support.V7.Widget.Toolbar;
using Android.Content;
using Android.Views;
using Android.Content.PM;

namespace WhatsAppBetaUpdater
{
	[Activity (Label = "ErrorActivity", ScreenOrientation = ScreenOrientation.Portrait)]			
	public class ErrorActivity : ActionBarActivity
	{
		protected override void OnCreate (Bundle bundle)
		{
			base.OnCreate (bundle);

			SetContentView (Resource.Layout.error_noconnection);

			var toolbar = FindViewById<Toolbar> (Resource.Id.toolbar);

			SetSupportActionBar (toolbar);
			SupportActionBar.Title = Resources.GetString (Resource.String.app_name);;
			SupportActionBar.SetIcon (Resource.Drawable.ic_launcher);

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
				Intent shareIntent = new Intent (Android.Content.Intent.ActionSend);
				shareIntent.SetType ("text/plain");
				shareIntent.PutExtra (Android.Content.Intent.ExtraSubject, Resources.GetString (Resource.String.app_name));
				shareIntent.PutExtra (Android.Content.Intent.ExtraText, Resources.GetString (Resource.String.share_description) + " " + "https://play.google.com/store/apps/details?id=com.javiersantos.whatsappbetaupdater");
				StartActivity (Intent.CreateChooser(shareIntent, Resources.GetString(Resource.String.share)));
				return true;
			}
			return base.OnOptionsItemSelected (item);
		}

	}
}
