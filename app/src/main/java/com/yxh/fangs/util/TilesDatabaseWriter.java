package com.yxh.fangs.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TilesDatabaseWriter {

  private final static String TABLE_TILES = "tiles";
  private final static String COL_TILES_ZOOM_LEVEL = "zoom_level";// z
  private final static String COL_TILES_TILE_COLUMN = "tile_column";// x
  private final static String COL_TILES_TILE_ROW = "tile_row";// y
  private final static String COL_TILES_TILE_DATA = "tile_data";

  private final static String TABLE_METADATA = "metadata";
  private final static String COL_METADATA_NAME = "name";
  private final static String COL_METADATA_VALUE = "value";

  private final static String TAG = "TilesDatabaseWriter";
  private static final String primaryKey = COL_TILES_ZOOM_LEVEL + "=? and " + COL_TILES_TILE_COLUMN
        + "=? and " + COL_TILES_TILE_ROW + "=?";
  private static final String[] expireQueryColumn = {COL_TILES_TILE_DATA};
  private File db_file;
  private SQLiteDatabase db;

  public TilesDatabaseWriter(String path) {
    db_file = new File(path);

    try {
      db = SQLiteDatabase.openOrCreateDatabase(db_file, null);

      db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TILES + " ( "
            + COL_TILES_ZOOM_LEVEL + " INTEGER , "
            + COL_TILES_TILE_COLUMN + " INTEGER , "
            + COL_TILES_TILE_ROW + " INTEGER , "
            + COL_TILES_TILE_DATA + " BLOB);");

      db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_METADATA + " ( "
            + COL_METADATA_NAME + " TEXT , "
            + COL_METADATA_VALUE + " TEXT);");

      insertMetadata("name", db_file.getName());
      insertMetadata("type", "overlay");
      insertMetadata("version", "1.0");
      insertMetadata("description", "BIGEMAP MBTiles");
      insertMetadata("format", "jpg");
      insertMetadata("minzoom", "9");
      insertMetadata("maxzoom", "10");
      insertMetadata("bounds", "");
    } catch (Throwable ex) {
      Log.e(TAG, "Unable to start the sqlite tile writer. Check external storage availability.", ex);
    }
  }

  private void insertMetadata(String name, String value) {
    ContentValues cv = new ContentValues();
    cv.put(COL_METADATA_NAME, name);
    cv.put(COL_METADATA_VALUE, value);
    db.insert(TABLE_METADATA, null, cv);
  }

  public boolean saveFile(final int column, final int row, final int zoom, final InputStream pStream) {
    if (db == null || !db.isOpen()) {
      Log.d(TAG, "Unable to store cached tile from column = " + column + ", row = " +
            row + ", zoom = " + zoom + ", database not available.");
      return false;
    }
    ByteArrayOutputStream bos = null;
    try {
      ContentValues cv = new ContentValues();
      cv.put(COL_TILES_TILE_COLUMN, column);
      cv.put(COL_TILES_TILE_ROW, row);
      cv.put(COL_TILES_ZOOM_LEVEL, zoom);

      byte[] buffer = new byte[512];
      int l;
      bos = new ByteArrayOutputStream();
      while ((l = pStream.read(buffer)) != -1)
        bos.write(buffer, 0, l);
      byte[] bits = bos.toByteArray(); // if a variable is required at all
      cv.put(COL_TILES_TILE_DATA, bits);

      db.delete(TABLE_TILES, primaryKey, getPrimaryKeyParameters(column, row, zoom));
      db.insert(TABLE_TILES, null, cv);

    } catch (Throwable ex) {
      Log.e(TAG, "Unable to store cached tile from column = " + column + ", row = "
            + row + ", zoom = " + zoom + ", db is " + (db == null ? "null" : "not null"), ex);
    } finally {
      try {
        assert bos != null;
        bos.close();
      } catch (IOException e) {
        Log.e(TAG, "Unable to store cached tile from column = " + column + ", row = "
              + row + ", zoom = " + zoom + ", db is " + (db == null ? "null" : "not null"), e);
      }
    }
    return false;
  }

  private String[] getPrimaryKeyParameters(final int column, final int row, final int zoom) {
    return new String[]{String.valueOf(zoom), String.valueOf(column), String.valueOf(row)};
  }

  public boolean exists(final int column, final int row, final int zoom) {
    if (db == null || !db.isOpen()) {
      Log.d(TAG, "Unable to test for tile exists cached tile from column = " + column
            + ", row = " + row + ", zoom = " + zoom + ", database not available.");
      return false;
    }
    boolean returnValue = false;
    Cursor cur = null;
    try {
      cur = getTileCursor(getPrimaryKeyParameters(column, row, zoom), expireQueryColumn);
      returnValue = (cur.moveToNext());
    } catch (Throwable ex) {
      Log.e(TAG, "Unable to store cached tile from column = " + column + ", row = " + row + ", zoom = " + zoom, ex);
    } finally {
      if (cur != null)
        try {
          cur.close();
        } catch (Throwable t) {
          //ignore
        }
    }
    return returnValue;
  }

  public void onDetach() {
    if (db != null && db.isOpen()) {
      try {
        db.close();
        Log.i(TAG, "Database detached");
      } catch (Exception ex) {
        Log.e(TAG, "Database detach failed", ex);
      }
    }
    db = null;
    db_file = null;
  }

  public boolean purgeCache() {
    if (db != null && db.isOpen()) {
      try {
        db.delete(TABLE_TILES, null, null);
        return true;
      } catch (final Throwable e) {
        Log.w(TAG, "Error purging the db", e);
      }
    }
    return false;
  }

  public boolean remove(final int column, final int row, final int zoom) {
    if (db == null) {
      Log.d(TAG, "Unable to delete cached tile from column = " + column + ", row = "
            + row + ", zoom = " + zoom + ", database not available.");
      return false;
    }
    try {
      db.delete(TABLE_TILES, primaryKey, getPrimaryKeyParameters(column, row, zoom));
      return true;
    } catch (Throwable ex) {
      Log.e(TAG, "Unable to delete cached tile from column = " + column + ", row = "
            + row + ", zoom = " + zoom + ", db is " + (db == null ? "null" : "not null"), ex);
    }
    return false;
  }

  public Cursor getTileCursor(final String[] pPrimaryKeyParameters, final String[] pColumns) {
    return db.query(TABLE_TILES, pColumns, primaryKey, pPrimaryKeyParameters, null, null, null);
  }
}