
using Java.Lang;

namespace WhatsAppBetaUpdater {		
	public static class CompareVersionReceiver {
		public static int VersionCompare (string oldVersion, string newVersion) {
			string[] splitOld = oldVersion.Split (new char[] { '.' });
			string[] splitNew = newVersion.Split (new char[] { '.' });
			int i = 0;
			while (i < splitOld.Length && i < splitNew.Length && splitOld[i].Equals(splitNew[i])) {
				i++;
			}
			if (i < splitOld.Length && i < splitNew.Length) {
				int diff = Integer.ValueOf (splitOld [i]).CompareTo (Integer.ValueOf (splitNew [i]));
				return Integer.Signum (diff);
			} else {
				return Integer.Signum (splitOld.Length - splitNew.Length);
			}
		}
	}
}
	