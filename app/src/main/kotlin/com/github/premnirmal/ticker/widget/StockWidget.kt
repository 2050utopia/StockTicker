package com.github.premnirmal.ticker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.github.premnirmal.ticker.Injector
import com.github.premnirmal.ticker.ParanormalActivity
import com.github.premnirmal.ticker.Tools
import com.github.premnirmal.ticker.WidgetClickReceiver
import com.github.premnirmal.ticker.model.AlarmScheduler
import com.github.premnirmal.ticker.model.IStocksProvider
import com.github.premnirmal.tickerwidget.BuildConfig
import com.github.premnirmal.tickerwidget.R
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject
import com.github.premnirmal.ticker.Analytics
import org.joda.time.format.ISODateTimeFormat

/**
 * Created by premnirmal on 2/27/16.
 */
class StockWidget : AppWidgetProvider() {

  @Inject
  lateinit internal var stocksProvider: IStocksProvider

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    Analytics.trackWidgetUpdate("onReceive")
    if (intent.action == ACTION_NAME) {
      context.startActivity(Intent(context, ParanormalActivity::class.java))
    }
  }

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager,
      appWidgetIds: IntArray) {
    Injector.getAppComponent().inject(this)
    Analytics.trackWidgetUpdate("onUpdate")
    for (widgetId in appWidgetIds) {
      val min_width: Int
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        min_width = getMinWidgetWidth(options)
      } else {
        min_width = appWidgetManager.getAppWidgetInfo(widgetId).minWidth
      }
      val remoteViews: RemoteViews = createRemoteViews(context, min_width)
      Log.d(TAG, "widget size update: " + "${min_width}px")
      updateWidget(context, appWidgetManager, widgetId, remoteViews)
      appWidgetManager.updateAppWidget(ComponentName(context, StockWidget::class.java), remoteViews)
    }
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }

  private fun createRemoteViews(context: Context, min_width: Int): RemoteViews {
    val remoteViews: RemoteViews
    if (min_width > 750) {
      remoteViews = RemoteViews(context.packageName, R.layout.widget_4x1)
    } else if (min_width > 500) {
      remoteViews = RemoteViews(context.packageName, R.layout.widget_3x1)
    } else if (min_width > 250) {
      remoteViews = RemoteViews(context.packageName, R.layout.widget_2x1)
    } else {
      remoteViews = RemoteViews(context.packageName, R.layout.widget_1x1)
    }
    return remoteViews
  }

  private fun getMinWidgetWidth(options: Bundle?): Int {
    if (options == null || !options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
      return 0 // 2x1
    } else {
      return options.get(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) as Int
    }
  }

  override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager,
      appWidgetId: Int, newOptions: Bundle) {
    Injector.getAppComponent().inject(this)
    val min_width = getMinWidgetWidth(newOptions)
    val remoteViews: RemoteViews = createRemoteViews(context, min_width)
    Analytics.trackWidgetSizeUpdate("${min_width}px")
    Log.d(TAG, "widget size update: " + "${min_width}px")
    updateWidget(context, appWidgetManager, appWidgetId, remoteViews)
    appWidgetManager.updateAppWidget(ComponentName(context, StockWidget::class.java), remoteViews)
  }

  private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int,
      remoteViews: RemoteViews) {
    remoteViews.setRemoteAdapter(R.id.list, Intent(context, RemoteStockProviderService::class.java))
    val intent = Intent(context, WidgetClickReceiver::class.java)
    intent.action = WidgetClickReceiver.CLICK_BCAST_INTENTFILTER
    val flipIntent = PendingIntent.getBroadcast(context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT)
    remoteViews.setPendingIntentTemplate(R.id.list, flipIntent)
    val lastFetched: String = stocksProvider.lastFetched()
    val nextUpdate: String = stocksProvider.nextFetch()
    val nextUpdateText: String = "Next fetch: $nextUpdate"
    val lastUpdatedText = "Last fetch: $lastFetched"
    remoteViews.setTextViewText(R.id.last_updated, lastUpdatedText)
    remoteViews.setTextViewText(R.id.next_update, nextUpdateText)
    appWidgetManager.updateAppWidget(ComponentName(context, StockWidget::class.java), remoteViews)
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list)
    remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", Tools.getBackgroundResource(context))
  }

  companion object {
    val ACTION_NAME = "OPEN_APP"
    val TAG = StockWidget::class.java.simpleName
  }
}