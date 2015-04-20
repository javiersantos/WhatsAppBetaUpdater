
using System;
using Android.Content;
using System.Net;

namespace WhatsAppBetaUpdater {		
	public static class RetreiveLatestReceiver {
		
		public static bool IsAvailableVersion (Context context) {
			if (CompareVersionReceiver.VersionCompare (GetInstalledVersion (context), GetLatestVersion ()) < 0) {
				return true;
			} else {
				return false;
			}
		}

		public static string GetLatestVersion () {
			string latestVersion = "0.0.0";

			var getVersion = new WebClient();
			string htmlAndroid = getVersion.DownloadString (new Uri("http://www.whatsapp.com/android/"));

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

			return latestVersion;
		}

		public static string GetInstalledVersion (Context context) {
			return context.PackageManager.GetPackageInfo ("com.whatsapp", 0).VersionName.Trim ();
		}
	}
}
