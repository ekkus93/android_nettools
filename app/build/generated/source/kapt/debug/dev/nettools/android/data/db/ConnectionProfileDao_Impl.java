package dev.nettools.android.data.db;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import java.lang.Class;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class ConnectionProfileDao_Impl implements ConnectionProfileDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<ConnectionProfileEntity> __insertAdapterOfConnectionProfileEntity;

  public ConnectionProfileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfConnectionProfileEntity = new EntityInsertAdapter<ConnectionProfileEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `connection_profiles` (`id`,`name`,`host`,`port`,`username`,`authType`,`keyPath`,`savePassword`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final ConnectionProfileEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getName());
        }
        if (entity.getHost() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getHost());
        }
        statement.bindLong(4, entity.getPort());
        if (entity.getUsername() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getUsername());
        }
        if (entity.getAuthType() == null) {
          statement.bindNull(6);
        } else {
          statement.bindText(6, entity.getAuthType());
        }
        if (entity.getKeyPath() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getKeyPath());
        }
        final int _tmp = entity.getSavePassword() ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
  }

  @Override
  public Object upsert(final ConnectionProfileEntity entity,
      final Continuation<? super Unit> $completion) {
    if (entity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfConnectionProfileEntity.insert(_connection, entity);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<ConnectionProfileEntity>> getAll() {
    final String _sql = "SELECT * FROM connection_profiles ORDER BY name ASC";
    return FlowUtil.createFlow(__db, false, new String[] {"connection_profiles"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfHost = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "host");
        final int _columnIndexOfPort = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "port");
        final int _columnIndexOfUsername = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "username");
        final int _columnIndexOfAuthType = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "authType");
        final int _columnIndexOfKeyPath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "keyPath");
        final int _columnIndexOfSavePassword = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "savePassword");
        final List<ConnectionProfileEntity> _result = new ArrayList<ConnectionProfileEntity>();
        while (_stmt.step()) {
          final ConnectionProfileEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final String _tmpHost;
          if (_stmt.isNull(_columnIndexOfHost)) {
            _tmpHost = null;
          } else {
            _tmpHost = _stmt.getText(_columnIndexOfHost);
          }
          final int _tmpPort;
          _tmpPort = (int) (_stmt.getLong(_columnIndexOfPort));
          final String _tmpUsername;
          if (_stmt.isNull(_columnIndexOfUsername)) {
            _tmpUsername = null;
          } else {
            _tmpUsername = _stmt.getText(_columnIndexOfUsername);
          }
          final String _tmpAuthType;
          if (_stmt.isNull(_columnIndexOfAuthType)) {
            _tmpAuthType = null;
          } else {
            _tmpAuthType = _stmt.getText(_columnIndexOfAuthType);
          }
          final String _tmpKeyPath;
          if (_stmt.isNull(_columnIndexOfKeyPath)) {
            _tmpKeyPath = null;
          } else {
            _tmpKeyPath = _stmt.getText(_columnIndexOfKeyPath);
          }
          final boolean _tmpSavePassword;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfSavePassword));
          _tmpSavePassword = _tmp != 0;
          _item = new ConnectionProfileEntity(_tmpId,_tmpName,_tmpHost,_tmpPort,_tmpUsername,_tmpAuthType,_tmpKeyPath,_tmpSavePassword);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Object getById(final String id,
      final Continuation<? super ConnectionProfileEntity> $completion) {
    final String _sql = "SELECT * FROM connection_profiles WHERE id = ? LIMIT 1";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfHost = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "host");
        final int _columnIndexOfPort = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "port");
        final int _columnIndexOfUsername = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "username");
        final int _columnIndexOfAuthType = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "authType");
        final int _columnIndexOfKeyPath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "keyPath");
        final int _columnIndexOfSavePassword = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "savePassword");
        final ConnectionProfileEntity _result;
        if (_stmt.step()) {
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final String _tmpHost;
          if (_stmt.isNull(_columnIndexOfHost)) {
            _tmpHost = null;
          } else {
            _tmpHost = _stmt.getText(_columnIndexOfHost);
          }
          final int _tmpPort;
          _tmpPort = (int) (_stmt.getLong(_columnIndexOfPort));
          final String _tmpUsername;
          if (_stmt.isNull(_columnIndexOfUsername)) {
            _tmpUsername = null;
          } else {
            _tmpUsername = _stmt.getText(_columnIndexOfUsername);
          }
          final String _tmpAuthType;
          if (_stmt.isNull(_columnIndexOfAuthType)) {
            _tmpAuthType = null;
          } else {
            _tmpAuthType = _stmt.getText(_columnIndexOfAuthType);
          }
          final String _tmpKeyPath;
          if (_stmt.isNull(_columnIndexOfKeyPath)) {
            _tmpKeyPath = null;
          } else {
            _tmpKeyPath = _stmt.getText(_columnIndexOfKeyPath);
          }
          final boolean _tmpSavePassword;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfSavePassword));
          _tmpSavePassword = _tmp != 0;
          _result = new ConnectionProfileEntity(_tmpId,_tmpName,_tmpHost,_tmpPort,_tmpUsername,_tmpAuthType,_tmpKeyPath,_tmpSavePassword);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final String id, final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM connection_profiles WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
