package com.student.teamsync.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.student.teamsync.models.ProjectEntity;
import com.student.teamsync.models.ProjectMember;
import com.student.teamsync.models.User;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDao_Impl implements AppDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<User> __insertionAdapterOfUser;

  private final EntityInsertionAdapter<ProjectEntity> __insertionAdapterOfProjectEntity;

  private final EntityInsertionAdapter<ProjectMember> __insertionAdapterOfProjectMember;

  public AppDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUser = new EntityInsertionAdapter<User>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `users` (`userId`,`name`,`email`,`role`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final User entity) {
        if (entity.getUserId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getUserId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getEmail());
        }
        if (entity.getRole() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getRole());
        }
      }
    };
    this.__insertionAdapterOfProjectEntity = new EntityInsertionAdapter<ProjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `projects` (`localId`,`firebaseId`,`projectName`,`courseCode`,`dueDate`,`ownerId`,`progressPercentage`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ProjectEntity entity) {
        statement.bindLong(1, entity.localId);
        if (entity.firebaseId == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.firebaseId);
        }
        if (entity.projectName == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.projectName);
        }
        if (entity.courseCode == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.courseCode);
        }
        if (entity.dueDate == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.dueDate);
        }
        if (entity.ownerId == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.ownerId);
        }
        statement.bindLong(7, entity.progressPercentage);
      }
    };
    this.__insertionAdapterOfProjectMember = new EntityInsertionAdapter<ProjectMember>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `project_members` (`projectLocalId`,`userId`) VALUES (?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ProjectMember entity) {
        statement.bindLong(1, entity.projectLocalId);
        if (entity.userId == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.userId);
        }
      }
    };
  }

  @Override
  public void insertUser(final User user) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfUser.insert(user);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public long insertProject(final ProjectEntity project) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfProjectEntity.insertAndReturnId(project);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertProjectMembers(final List<ProjectMember> members) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfProjectMember.insert(members);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public LiveData<List<ProjectEntity>> getMyProjects(final String currentUserId) {
    final String _sql = "SELECT p.* FROM projects p INNER JOIN project_members pm ON p.localId = pm.projectLocalId WHERE pm.userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (currentUserId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, currentUserId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"projects",
        "project_members"}, false, new Callable<List<ProjectEntity>>() {
      @Override
      @Nullable
      public List<ProjectEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfLocalId = CursorUtil.getColumnIndexOrThrow(_cursor, "localId");
          final int _cursorIndexOfFirebaseId = CursorUtil.getColumnIndexOrThrow(_cursor, "firebaseId");
          final int _cursorIndexOfProjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "projectName");
          final int _cursorIndexOfCourseCode = CursorUtil.getColumnIndexOrThrow(_cursor, "courseCode");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
          final int _cursorIndexOfProgressPercentage = CursorUtil.getColumnIndexOrThrow(_cursor, "progressPercentage");
          final List<ProjectEntity> _result = new ArrayList<ProjectEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProjectEntity _item;
            _item = new ProjectEntity();
            _item.localId = _cursor.getLong(_cursorIndexOfLocalId);
            if (_cursor.isNull(_cursorIndexOfFirebaseId)) {
              _item.firebaseId = null;
            } else {
              _item.firebaseId = _cursor.getString(_cursorIndexOfFirebaseId);
            }
            if (_cursor.isNull(_cursorIndexOfProjectName)) {
              _item.projectName = null;
            } else {
              _item.projectName = _cursor.getString(_cursorIndexOfProjectName);
            }
            if (_cursor.isNull(_cursorIndexOfCourseCode)) {
              _item.courseCode = null;
            } else {
              _item.courseCode = _cursor.getString(_cursorIndexOfCourseCode);
            }
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _item.dueDate = null;
            } else {
              _item.dueDate = _cursor.getString(_cursorIndexOfDueDate);
            }
            if (_cursor.isNull(_cursorIndexOfOwnerId)) {
              _item.ownerId = null;
            } else {
              _item.ownerId = _cursor.getString(_cursorIndexOfOwnerId);
            }
            _item.progressPercentage = _cursor.getInt(_cursorIndexOfProgressPercentage);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
