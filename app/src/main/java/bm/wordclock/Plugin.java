package bm.wordclock;

import android.support.annotation.NonNull;

/**
 * Created by mrks on 04.02.17.
 */

public class Plugin {
    private final String name;
    private final String description;

    public Plugin(@NonNull String name) {
        this(name, null);
    }

    public Plugin(@NonNull String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasDescription() {
        return description != null;
    }
}
