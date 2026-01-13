package gambi.zerone.camscanner.app

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPrefs(context: Context) {
	private val prefs: SharedPreferences =
		context.getSharedPreferences("App_prefs", Context.MODE_PRIVATE)

	companion object {
		private const val KEY_CAMERA_DENIED_COUNT = "camera_denied_count"
		private const val KEY_EFFECT_SELECTED = "effect_selected"
	}

	var cameraDeniedCount: Int
		get() = prefs.getInt(KEY_CAMERA_DENIED_COUNT, 0)
		set(value) = prefs.edit { putInt(KEY_CAMERA_DENIED_COUNT, value) }

	fun incrementCameraDeniedCount() {
		cameraDeniedCount += 1
	}

	var effectSelected: Int
		get() = prefs.getInt(KEY_EFFECT_SELECTED, 2)
		set(value) = prefs.edit { putInt(KEY_EFFECT_SELECTED, value) }
}