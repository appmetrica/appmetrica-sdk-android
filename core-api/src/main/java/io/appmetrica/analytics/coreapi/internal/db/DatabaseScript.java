package io.appmetrica.analytics.coreapi.internal.db;

import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import java.sql.SQLException;
import org.json.JSONException;

public abstract class DatabaseScript {

    public abstract void runScript(@NonNull SQLiteDatabase database) throws SQLException, JSONException;
}
