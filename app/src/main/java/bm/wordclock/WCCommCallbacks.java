package bm.wordclock;

/**
 * Created by mrks on 05.02.17.
 */

public interface WCCommCallbacks extends WCCallbacks {
    enum STATE {
        CONNECTED,
        DISCONNECTED,
        COULD_NOT_CONNECT,
        HOST_UNKNOWN
    }

    void setState(STATE state);
}
