
using Android.App;
using Android.Content;
using Android.Support.V4.App;
using Android.Graphics;
using System;

namespace WhatsAppBetaUpdater {
	[BroadcastReceiver]			
	public class AlarmReceiver : BroadcastReceiver {
		public override void OnReceive (Context context, Intent intent) {
			if (RetreiveLatestReceiver.IsAvailableVersion(context)) {
				intent.PutExtra ("title", string.Format(context.Resources.GetString(Resource.String.notification_title), "WhatsApp " + RetreiveLatestReceiver.GetLatestVersion ()));
				intent.PutExtra ("message", string.Format(context.Resources.GetString(Resource.String.notification_description), context.Resources.GetString(Resource.String.app_name)));
				var message = intent.GetStringExtra ("message");
				var title = intent.GetStringExtra ("title");

				var notIntent = new Intent (context, typeof(MainActivity));
				var contentIntent = PendingIntent.GetActivity (context, 0, notIntent, PendingIntentFlags.CancelCurrent);
				var manager = NotificationManagerCompat.From (context);

				var style = new NotificationCompat.BigTextStyle ().BigText (message);
				int resourceId = Resource.Drawable.ic_launcher;

				var wearableExtender = new NotificationCompat.WearableExtender ().SetBackground (BitmapFactory.DecodeResource (context.Resources, resourceId));

				// Generate notification (short text and small icon)
				var builder = new NotificationCompat.Builder (context);
				builder.SetContentIntent (contentIntent);
				builder.SetSmallIcon (Resource.Drawable.ic_launcher);
				builder.SetContentTitle (title);
				builder.SetContentText (message);
				builder.SetStyle (style);
				builder.SetWhen (Java.Lang.JavaSystem.CurrentTimeMillis ()); // When the AlarmManager will check changes?
				builder.SetAutoCancel (true);
				builder.Extend (wearableExtender); // Support Android Wear, yeah!

				var notification = builder.Build ();
				manager.Notify (0, notification);
			}
		}
	}
}

