package cgeo.geocaching.files;

import cgeo.geocaching.R;
import cgeo.geocaching.activity.ActivityMixin;
import cgeo.geocaching.location.GeoItemHolder;
import cgeo.geocaching.models.Route;
import cgeo.geocaching.models.geoitem.IGeoItemSupplier;
import cgeo.geocaching.storage.ContentStorage;
import cgeo.geocaching.utils.AndroidRxUtils;
import cgeo.geocaching.utils.EnvironmentUtils;
import cgeo.geocaching.utils.Formatter;
import cgeo.geocaching.utils.GeoJsonUtils;
import cgeo.geocaching.utils.Log;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;

public class GPXTrackOrRouteImporter {

    private GPXTrackOrRouteImporter() {
    }

    public static void doImport(final Context context, final Uri uri, final String displayName, final Route.UpdateRoute callback) {
        final AtomicBoolean success = new AtomicBoolean(false);
        AndroidRxUtils.andThenOnUi(Schedulers.io(), () -> {
            try {
                final IGeoItemSupplier value = doInBackground(context, uri);
                success.set(null != value && value.hasData());
                if (success.get()) {
                    AndroidSchedulers.mainThread().createWorker().schedule(() -> {
                        try {
                            callback.updateRoute(value);
                        } catch (final Throwable t) {
                            Log.e("Error on track/route import: " + t.getMessage());
                        }
                    });
                }
            } catch (final Exception e) {
                Log.e("Exception on doImport", e);
            }
        }, () -> {
            if (!success.get()) {
                if (displayName == null) {
                    ActivityMixin.showToast(context, R.string.load_track_error);
                } else {
                    ActivityMixin.showToast(context, R.string.load_track_error_named, displayName);
                }
                callback.updateRoute(null);
            }
        });
    }

    // splitting up that method would not help improve readability
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.ExcessiveMethodLength"})
    private static IGeoItemSupplier doInBackground(final Context context, final Uri uri) {
        try {
            // default: import properly formatted routes or tracks
            Route route = parse(new GPXTrackOrRouteParser("http://www.topografix.com/GPX/1/1", "1.1"), uri);
            if (null == route) {
                route = parse(new GPXTrackOrRouteParser("http://www.topografix.com/GPX/1/0", "1.0"), uri);
            }
            // import waypoints as tracks
            if (null == route) {
                route = parse(new GPXWptAsTrackParser("http://www.topografix.com/GPX/1/1", "1.1"), uri);
            }
            if (null == route) {
                route = parse(new GPXWptAsTrackParser("http://www.topografix.com/GPX/1/0", "1.0"), uri);
            }
            // as last resort ignore missing namespace identifier
            if (null == route) {
                route = parse(new GPXTrackOrRouteParser("", "1.0"), uri);
            }
            if (null != route) {
                route.calculateNavigationRoute();
            }
            if (null == route) {
                return parseAsGeoJson(context, uri);
            }
            return route;
        } catch (IOException e) {
            Log.e("Problem accessing GPX Track file '" + uri + "'. Maybe file was removed or renamed by user?", e);
            return null;
        }
    }

    private static Route parse(@NonNull final AbstractTrackOrRouteParser.RouteParse parser, @NonNull final Uri uri) throws IOException {
        BufferedInputStream stream = null;
        try {
            stream = new BufferedInputStream(ContentStorage.get().openForRead(uri));
            if (stream == null) {
                return null;
            }
            final Route route = parser.parse(stream);
            return route.getNumSegments() < 1 ? null : route;
        } catch (ParserException e) {
            Log.e("Problem parsing GPX Track file '" + uri + "': " + e);
            return null;
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private static IGeoItemSupplier parseAsGeoJson(final Context context, final Uri uri) throws IOException {
        final ContentStorage.FileInformation fi = ContentStorage.get().getFileInfo(uri);
        final long freeMem = EnvironmentUtils.getFreeMemory(context);
        if (fi == null || freeMem < 0 || fi.size * 10 > freeMem) {
            Log.e("Won't import '" + uri + "' as json due to limited memory (filesize: " +
                    Formatter.formatBytes(fi == null ? 0 : fi.size) + ", freeMem: " + Formatter.formatBytes(freeMem));
            return null;
        }

        try (InputStream is = ContentStorage.get().openForRead(uri)) {
            if (is == null) {
                return null;
            }

            return new GeoItemHolder(GeoJsonUtils.parseGeoJson(is));
        } catch (JSONException e) {
            Log.e("Problem parsing GeoJson file '" + uri + "': " + e);
            return null;
        }
    }

}
