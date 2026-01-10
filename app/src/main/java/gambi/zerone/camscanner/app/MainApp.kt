package gambi.zerone.camscanner.app

import android.app.Application
import org.opencv.android.OpenCVLoader

class MainApp : Application() {
	override fun onCreate() {
		super.onCreate()
		OpenCVLoader.initLocal()
	}
}