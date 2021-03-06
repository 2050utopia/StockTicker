package com.github.premnirmal.ticker

import android.support.multidex.MultiDexApplication
import com.github.premnirmal.tickerwidget.R
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

/**
 * Created by premnirmal on 2/26/16.
 */
abstract class BaseApp : MultiDexApplication() {

  override fun onCreate() {
    super.onCreate()
    instance = this
    CalligraphyConfig.initDefault(
        CalligraphyConfig.Builder().setDefaultFontPath("fonts/Ubuntu-Regular.ttf").setFontAttrId(
            R.attr.fontPath).build())
    Injector.init(this)
  }

  companion object {
    lateinit var instance: BaseApp
      private set
  }
}