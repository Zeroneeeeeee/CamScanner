package gambi.zerone.camscanner.app

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PermissionPrefs(context: Context) {
	private val prefs: SharedPreferences =
		context.getSharedPreferences("permission_prefs", Context.MODE_PRIVATE)

	companion object {
		private const val KEY_CAMERA_DENIED_COUNT = "camera_denied_count"
	}

	var cameraDeniedCount: Int
		get() = prefs.getInt(KEY_CAMERA_DENIED_COUNT, 0)
		set(value) = prefs.edit { putInt(KEY_CAMERA_DENIED_COUNT, value) }

	fun incrementCameraDeniedCount() {
		cameraDeniedCount += 1
	}
}