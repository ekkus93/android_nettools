package dev.nettools.android.data.db;

import androidx.annotation.NonNull;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenDelegate;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.SQLite;
import androidx.sqlite.SQLiteConnection;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ConnectionProfileDao _connectionProfileDao;

  private volatile TransferHistoryDao _transferHistoryDao;

  private volatile KnownHostDao _knownHostDao;

  @Override
  @NonNull
  protected RoomOpenDelegate createOpenDelegate() {
    final RoomOpenDelegate _openDelegate = new RoomOpenDelegate(1, "21e06cee638973f38b26d179dbd4d2ad", "6661b8743e918d04770a1e773dacae8e") {
      @Override
      public void createAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `connection_profiles` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `host` TEXT NOT NULL, `port` INTEGER NOT NULL, `username` TEXT NOT NULL, `authType` TEXT NOT NULL, `keyPath` TEXT, `savePassword` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `transfer_history` (`id` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `direction` TEXT NOT NULL, `host` TEXT NOT NULL, `username` TEXT NOT NULL, `fileName` TEXT NOT NULL, `remoteDir` TEXT NOT NULL, `fileSizeBytes` INTEGER NOT NULL, `status` TEXT NOT NULL, PRIMARY KEY(`id`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `known_hosts` (`id` TEXT NOT NULL, `host` TEXT NOT NULL, `port` INTEGER NOT NULL, `fingerprint` TEXT NOT NULL, PRIMARY KEY(`id`))");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        SQLite.execSQL(connection, "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '21e06cee638973f38b26d179dbd4d2ad')");
      }

      @Override
      public void dropAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `connection_profiles`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `transfer_history`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `known_hosts`");
      }

      @Override
      public void onCreate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      public void onOpen(@NonNull final SQLiteConnection connection) {
        internalInitInvalidationTracker(connection);
      }

      @Override
      public void onPreMigrate(@NonNull final SQLiteConnection connection) {
        DBUtil.dropFtsSyncTriggers(connection);
      }

      @Override
      public void onPostMigrate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      @NonNull
      public RoomOpenDelegate.ValidationResult onValidateSchema(
          @NonNull final SQLiteConnection connection) {
        final Map<String, TableInfo.Column> _columnsConnectionProfiles = new HashMap<String, TableInfo.Column>(8);
        _columnsConnectionProfiles.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConnectionProfiles.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConnectionProfiles.put("host", new TableInfo.Column("host", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConnectionProfiles.put("port", new TableInfo.Column("port", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConnectionProfiles.put("username", new TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConnectionProfiles.put("authType", new TableInfo.Column("authType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConnectionProfiles.put("keyPath", new TableInfo.Column("keyPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConnectionProfiles.put("savePassword", new TableInfo.Column("savePassword", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysConnectionProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesConnectionProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoConnectionProfiles = new TableInfo("connection_profiles", _columnsConnectionProfiles, _foreignKeysConnectionProfiles, _indicesConnectionProfiles);
        final TableInfo _existingConnectionProfiles = TableInfo.read(connection, "connection_profiles");
        if (!_infoConnectionProfiles.equals(_existingConnectionProfiles)) {
          return new RoomOpenDelegate.ValidationResult(false, "connection_profiles(dev.nettools.android.data.db.ConnectionProfileEntity).\n"
                  + " Expected:\n" + _infoConnectionProfiles + "\n"
                  + " Found:\n" + _existingConnectionProfiles);
        }
        final Map<String, TableInfo.Column> _columnsTransferHistory = new HashMap<String, TableInfo.Column>(9);
        _columnsTransferHistory.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransferHistory.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransferHistory.put("direction", new TableInfo.Column("direction", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransferHistory.put("host", new TableInfo.Column("host", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransferHistory.put("username", new TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransferHistory.put("fileName", new TableInfo.Column("fileName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransferHistory.put("remoteDir", new TableInfo.Column("remoteDir", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransferHistory.put("fileSizeBytes", new TableInfo.Column("fileSizeBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransferHistory.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysTransferHistory = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesTransferHistory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTransferHistory = new TableInfo("transfer_history", _columnsTransferHistory, _foreignKeysTransferHistory, _indicesTransferHistory);
        final TableInfo _existingTransferHistory = TableInfo.read(connection, "transfer_history");
        if (!_infoTransferHistory.equals(_existingTransferHistory)) {
          return new RoomOpenDelegate.ValidationResult(false, "transfer_history(dev.nettools.android.data.db.TransferHistoryEntity).\n"
                  + " Expected:\n" + _infoTransferHistory + "\n"
                  + " Found:\n" + _existingTransferHistory);
        }
        final Map<String, TableInfo.Column> _columnsKnownHosts = new HashMap<String, TableInfo.Column>(4);
        _columnsKnownHosts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKnownHosts.put("host", new TableInfo.Column("host", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKnownHosts.put("port", new TableInfo.Column("port", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKnownHosts.put("fingerprint", new TableInfo.Column("fingerprint", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysKnownHosts = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesKnownHosts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoKnownHosts = new TableInfo("known_hosts", _columnsKnownHosts, _foreignKeysKnownHosts, _indicesKnownHosts);
        final TableInfo _existingKnownHosts = TableInfo.read(connection, "known_hosts");
        if (!_infoKnownHosts.equals(_existingKnownHosts)) {
          return new RoomOpenDelegate.ValidationResult(false, "known_hosts(dev.nettools.android.data.db.KnownHostEntity).\n"
                  + " Expected:\n" + _infoKnownHosts + "\n"
                  + " Found:\n" + _existingKnownHosts);
        }
        return new RoomOpenDelegate.ValidationResult(true, null);
      }
    };
    return _openDelegate;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final Map<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final Map<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "connection_profiles", "transfer_history", "known_hosts");
  }

  @Override
  public void clearAllTables() {
    super.performClear(false, "connection_profiles", "transfer_history", "known_hosts");
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final Map<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ConnectionProfileDao.class, ConnectionProfileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TransferHistoryDao.class, TransferHistoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(KnownHostDao.class, KnownHostDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final Set<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ConnectionProfileDao connectionProfileDao() {
    if (_connectionProfileDao != null) {
      return _connectionProfileDao;
    } else {
      synchronized(this) {
        if(_connectionProfileDao == null) {
          _connectionProfileDao = new ConnectionProfileDao_Impl(this);
        }
        return _connectionProfileDao;
      }
    }
  }

  @Override
  public TransferHistoryDao transferHistoryDao() {
    if (_transferHistoryDao != null) {
      return _transferHistoryDao;
    } else {
      synchronized(this) {
        if(_transferHistoryDao == null) {
          _transferHistoryDao = new TransferHistoryDao_Impl(this);
        }
        return _transferHistoryDao;
      }
    }
  }

  @Override
  public KnownHostDao knownHostDao() {
    if (_knownHostDao != null) {
      return _knownHostDao;
    } else {
      synchronized(this) {
        if(_knownHostDao == null) {
          _knownHostDao = new KnownHostDao_Impl(this);
        }
        return _knownHostDao;
      }
    }
  }
}
