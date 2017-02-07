package bm.wordclock;

import java.util.Collection;

/**
 * Created by mrks on 04.02.17.
 */

public interface WCCallbacks {

    void setPlugins(Collection<Plugin> plugins);

    void setActivePlugin(int index);

}
