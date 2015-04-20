
using Android.Content;
using Android.Gms.Ads;

namespace WhatsAppBetaUpdater {	
	public static class AdWrapper {
		public static InterstitialAd ConstructFullPageAdd(Context context, string UnitID)
		{
			var ad = new InterstitialAd(context);
			ad.AdUnitId = UnitID;
			return ad;
		}

		public static AdView ConstructStandardBanner(Context context, AdSize adSize, string UnitID)
		{
			var ad = new AdView(context);
			ad.AdSize = adSize;
			ad.AdUnitId = UnitID;
			return ad;
		}

		public static InterstitialAd CustomBuild(this InterstitialAd ad)
		{
			var requestbuilder = new AdRequest.Builder();
			ad.LoadAd(requestbuilder.Build());
			return ad;
		}

		public static AdView CustomBuild(this AdView ad)
		{
			var requestbuilder = new AdRequest.Builder();
			ad.LoadAd(requestbuilder.Build());
			return ad;
		}
	}
}
