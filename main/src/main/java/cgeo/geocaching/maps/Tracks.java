package cgeo.geocaching.maps;

import cgeo.geocaching.models.geoitem.IGeoItemSupplier;
import cgeo.geocaching.storage.extension.Trackfiles;
import cgeo.geocaching.utils.AndroidRxUtils;
import cgeo.geocaching.utils.MapLineUtils;
import cgeo.geocaching.utils.functions.Action2;
import cgeo.geocaching.utils.functions.Action4;

import android.app.Activity;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class Tracks {

    private final ArrayList<Track> data = new ArrayList<>();

    public static class Track {
        private final Trackfiles trackfile;
        private IGeoItemSupplier route;

        Track(final Trackfiles trackfile) {
            this.trackfile = trackfile;
            this.route = null;
        }

        public IGeoItemSupplier getRoute() {
            return route;
        }

        public Trackfiles getTrackfile() {
            return trackfile;
        }
    }

    public interface UpdateTrack {
        void updateRoute(String key, IGeoItemSupplier route, int color, int width);
    }

    public Tracks() {
        getListOfTrackfilesInBackground(null);
    }

    public Tracks(final RouteTrackUtils routeTrackUtils, final UpdateTrack updateTrack) {
        getListOfTrackfilesInBackground(() -> {
            for (Track track : data) {
                routeTrackUtils.reloadTrack(track.trackfile, updateTrack);
            }
            routeTrackUtils.setTracks(this);
        });
    }

    private void getListOfTrackfilesInBackground(@Nullable final Runnable runAfterLoad) {
        AndroidRxUtils.computationScheduler.scheduleDirect(() -> {
            for (Trackfiles trackfile : Trackfiles.getTrackfiles()) {
                data.add(new Track(trackfile));
            }
            if (runAfterLoad != null) {
                runAfterLoad.run();
            }
        });
    }

    public void setRoute(@NonNull final String key, final IGeoItemSupplier route) {
        for (Track track : data) {
            if (track.trackfile.getKey().equals(key)) {
                track.route = route;
            }
        }
    }

    public void resumeAllTracks(final Action2<String, Boolean> resumeTrack) {
        for (Track track : data) {
            resumeTrack.call(track.trackfile.getKey(), false);
        }
    }

    public void traverse(final Action4<String, IGeoItemSupplier, Integer, Integer> action) {
        for (Track track : data) {
            if (track.route != null) {
                action.call(track.trackfile.getKey(), track.route, track.trackfile.getColor(), track.trackfile.getWidth());
            }
        }
    }

    public void traverse(final Action2<String, IGeoItemSupplier> action) {
        for (Track track : data) {
            if (track.route != null) {
                action.call(track.trackfile.getKey(), track.route);
            }
        }
    }

    public void find(final IGeoItemSupplier route, final Action2<String, IGeoItemSupplier> action) {
        for (Track track : data) {
            if (track.route != null && track.route.equals(route)) {
                action.call(track.trackfile.getKey(), track.route);
            }
        }
    }

    public String add(final Activity activity, final Uri uri, final UpdateTrack updateTrack) {
        final String key = Trackfiles.createTrackfile(activity, uri);
        for (Trackfiles trackfile : Trackfiles.getTrackfiles()) {
            if (trackfile.getKey().equals(key)) {
                data.add(new Track(trackfile));
                return key;
            }
        }
        return null;
    }

    public void remove(@NonNull final String key) {
        for (Track track : data) {
            if (track.trackfile.getKey().equals(key)) {
                Trackfiles.removeTrackfile(track.trackfile.getKey());
                data.remove(track);
                break;
            }
        }
    }

    public Track getTrack(@NonNull final String key) {
        for (Track track : data) {
            if (track.trackfile.getKey().equals(key)) {
                return track;
            }
        }
        return null;
    }

    public String getDisplayname(@NonNull final String key) {
        for (Track track : data) {
            if (track.trackfile.getKey().equals(key)) {
                return track.trackfile.getDisplayname();
            }
        }
        return null;
    }

    public void setDisplayname(@NonNull final String key, @NonNull final String newName) {
        for (Track track : data) {
            if (track.trackfile.getKey().equals(key)) {
                track.trackfile.setDisplayname(newName);
            }
        }
    }

    public int getColor(@NonNull final String key) {
        for (Track track : data) {
            if (StringUtils.equals(track.trackfile.getKey(), key)) {
                return track.trackfile.getColor();
            }
        }
        return MapLineUtils.getTrackColor();
    }

    public void setColor(@NonNull final String key, final int newColor) {
        for (Track track : data) {
            if (track.trackfile.getKey().equals(key)) {
                track.trackfile.setColor(newColor);
            }
        }
    }

    public int getWidth(@NonNull final String key) {
        for (Track track : data) {
            if (StringUtils.equals(track.trackfile.getKey(), key)) {
                return track.trackfile.getWidth();
            }
        }
        return MapLineUtils.getRawTrackLineWidth();
    }

    public void setWidth(@NonNull final String key, final int newWidth) {
        for (Track track : data) {
            if (track.trackfile.getKey().equals(key)) {
                track.trackfile.setWidth(newWidth);
            }
        }
    }

    public IGeoItemSupplier getRoute(@NonNull final String key) {
        for (Track track : data) {
            if (track.trackfile.getKey().equals(key)) {
                return track.route;
            }
        }
        return null;
    }

    public void hide(@NonNull final String key, final boolean hide) {
        for (Track track : data) {
            if (track.trackfile.getKey().equals(key)) {
                track.trackfile.setHidden(hide);
            }
        }
    }

    @Nullable
    public Trackfiles getTrackfile(@NonNull final String key) {
        for (Track track : data) {
            if (track.trackfile.getKey().equals(key)) {
                return track.trackfile;
            }
        }
        return null;
    }
}
