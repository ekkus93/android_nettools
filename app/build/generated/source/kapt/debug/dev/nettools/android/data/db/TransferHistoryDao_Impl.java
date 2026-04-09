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
public final class TransferHistoryDao_Impl implements TransferHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<TransferHistoryEntity> __insertAdapterOfTransferHistoryEntity;

  public TransferHistoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfTransferHistoryEntity = new EntityInsertAdapter<TransferHistoryEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `transfer_history` (`id`,`timestamp`,`direction`,`host`,`username`,`fileName`,`remoteDir`,`fileSizeBytes`,`status`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final TransferHistoryEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        statement.bindLong(2, entity.getTimestamp());
        if (entity.getDirection() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getDirection());
        }
        if (entity.getHost() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getHost());
        }
        if (entity.getUsername() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getUsername());
        }
        if (entity.getFileName() == null) {
          statement.bindNull(6);
        } else {
          statement.bindText(6, entity.getFileName());
        }
        if (entity.getRemoteDir() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getRemoteDir());
        }
        statement.bindLong(8, entity.getFileSizeBytes());
        if (entity.getStatus() == null) {
          statement.bindNull(9);
        } else {
          statement.bindText(9, entity.getStatus());
        }
      }
    };
  }

  @Override
  public Object insert(final TransferHistoryEntity entity,
      final Continuation<? super Unit> $completion) {
    if (entity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfTransferHistoryEntity.insert(_connection, entity);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<TransferHistoryEntity>> getAll() {
    final String _sql = "SELECT * FROM transfer_history ORDER BY timestamp DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"transfer_history"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfTimestamp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "timestamp");
        final int _columnIndexOfDirection = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "direction");
        final int _columnIndexOfHost = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "host");
        final int _columnIndexOfUsername = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "username");
        final int _columnIndexOfFileName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "fileName");
        final int _columnIndexOfRemoteDir = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "remoteDir");
        final int _columnIndexOfFileSizeBytes = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "fileSizeBytes");
        final int _columnIndexOfStatus = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "status");
        final List<TransferHistoryEntity> _result = new ArrayList<TransferHistoryEntity>();
        while (_stmt.step()) {
          final TransferHistoryEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final long _tmpTimestamp;
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp);
          final String _tmpDirection;
          if (_stmt.isNull(_columnIndexOfDirection)) {
            _tmpDirection = null;
          } else {
            _tmpDirection = _stmt.getText(_columnIndexOfDirection);
          }
          final String _tmpHost;
          if (_stmt.isNull(_columnIndexOfHost)) {
            _tmpHost = null;
          } else {
            _tmpHost = _stmt.getText(_columnIndexOfHost);
          }
          final String _tmpUsername;
          if (_stmt.isNull(_columnIndexOfUsername)) {
            _tmpUsername = null;
          } else {
            _tmpUsername = _stmt.getText(_columnIndexOfUsername);
          }
          final String _tmpFileName;
          if (_stmt.isNull(_columnIndexOfFileName)) {
            _tmpFileName = null;
          } else {
            _tmpFileName = _stmt.getText(_columnIndexOfFileName);
          }
          final String _tmpRemoteDir;
          if (_stmt.isNull(_columnIndexOfRemoteDir)) {
            _tmpRemoteDir = null;
          } else {
            _tmpRemoteDir = _stmt.getText(_columnIndexOfRemoteDir);
          }
          final long _tmpFileSizeBytes;
          _tmpFileSizeBytes = _stmt.getLong(_columnIndexOfFileSizeBytes);
          final String _tmpStatus;
          if (_stmt.isNull(_columnIndexOfStatus)) {
            _tmpStatus = null;
          } else {
            _tmpStatus = _stmt.getText(_columnIndexOfStatus);
          }
          _item = new TransferHistoryEntity(_tmpId,_tmpTimestamp,_tmpDirection,_tmpHost,_tmpUsername,_tmpFileName,_tmpRemoteDir,_tmpFileSizeBytes,_tmpStatus);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM transfer_history";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
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
