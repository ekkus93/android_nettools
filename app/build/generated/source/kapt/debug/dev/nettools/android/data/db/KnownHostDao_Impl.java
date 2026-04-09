package dev.nettools.android.data.db;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import java.lang.Class;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class KnownHostDao_Impl implements KnownHostDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<KnownHostEntity> __insertAdapterOfKnownHostEntity;

  public KnownHostDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfKnownHostEntity = new EntityInsertAdapter<KnownHostEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `known_hosts` (`id`,`host`,`port`,`fingerprint`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final KnownHostEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getHost() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getHost());
        }
        statement.bindLong(3, entity.getPort());
        if (entity.getFingerprint() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getFingerprint());
        }
      }
    };
  }

  @Override
  public Object upsert(final KnownHostEntity entity, final Continuation<? super Unit> $completion) {
    if (entity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfKnownHostEntity.insert(_connection, entity);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object getByHostAndPort(final String host, final int port,
      final Continuation<? super KnownHostEntity> $completion) {
    final String _sql = "SELECT * FROM known_hosts WHERE host = ? AND port = ? LIMIT 1";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (host == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, host);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, port);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfHost = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "host");
        final int _columnIndexOfPort = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "port");
        final int _columnIndexOfFingerprint = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "fingerprint");
        final KnownHostEntity _result;
        if (_stmt.step()) {
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpHost;
          if (_stmt.isNull(_columnIndexOfHost)) {
            _tmpHost = null;
          } else {
            _tmpHost = _stmt.getText(_columnIndexOfHost);
          }
          final int _tmpPort;
          _tmpPort = (int) (_stmt.getLong(_columnIndexOfPort));
          final String _tmpFingerprint;
          if (_stmt.isNull(_columnIndexOfFingerprint)) {
            _tmpFingerprint = null;
          } else {
            _tmpFingerprint = _stmt.getText(_columnIndexOfFingerprint);
          }
          _result = new KnownHostEntity(_tmpId,_tmpHost,_tmpPort,_tmpFingerprint);
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
  public Object deleteByHostAndPort(final String host, final int port,
      final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM known_hosts WHERE host = ? AND port = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (host == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, host);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, port);
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
