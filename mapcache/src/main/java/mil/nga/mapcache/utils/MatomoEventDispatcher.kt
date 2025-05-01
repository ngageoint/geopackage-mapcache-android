package mil.nga.mapcache.utils

import mil.nga.mapcache.MapCacheApplication
import org.matomo.sdk.extra.TrackHelper

class MatomoEventDispatcher {

    enum class EventTypes {
        BUTTON_CLICK,
        APPLICATION
    }

    companion object{
        private val tracker = MapCacheApplication.matomoTracker

        //button click tracking
        fun submitButtonClickEvent(buttonName: String) {
            TrackHelper.track().event(EventTypes.BUTTON_CLICK.toString(), buttonName).with(tracker)
            tracker.dispatch()
        }

        //screen tracking
        fun submitScreenEvent(screenName: String, title: String) {
            TrackHelper.track().screen(screenName).title(title).with(tracker)
            tracker.dispatch()
        }

        //heart beat tracking
        fun submitHeartBeat() {
            TrackHelper.track().event(EventTypes.APPLICATION.toString(), "HeartBeat").with(tracker)
            tracker.dispatch()
        }
    }
}