package io.github.janmalch.woroboro.data

import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.janmalch.woroboro.data.dao.ExerciseDao
import io.github.janmalch.woroboro.data.dao.ReminderDao
import io.github.janmalch.woroboro.data.dao.RoutineDao
import io.github.janmalch.woroboro.data.dao.TagDao
import io.github.janmalch.woroboro.data.model.ExerciseEntity
import io.github.janmalch.woroboro.data.model.ExerciseFtsEntity
import io.github.janmalch.woroboro.data.model.ExerciseTagCrossRefEntity
import io.github.janmalch.woroboro.data.model.MediaEntity
import io.github.janmalch.woroboro.data.model.ReminderEntity
import io.github.janmalch.woroboro.data.model.ReminderFilterTagCrossRefEntity
import io.github.janmalch.woroboro.data.model.RoutineEntity
import io.github.janmalch.woroboro.data.model.RoutineStepEntity
import io.github.janmalch.woroboro.data.model.TagEntity
import java.util.UUID

@Database(
    entities = [
        ExerciseEntity::class,
        ExerciseFtsEntity::class,
        MediaEntity::class,
        TagEntity::class,
        ExerciseTagCrossRefEntity::class,
        RoutineEntity::class,
        RoutineStepEntity::class,
        ReminderEntity::class,
        ReminderFilterTagCrossRefEntity::class,
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ]
)
@TypeConverters(StandardConverters::class, DomainConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun tagDao(): TagDao
    abstract fun routineDao(): RoutineDao
    abstract fun reminderDao(): ReminderDao

}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // CREATE TABLE from schema 3
        db.execSQL(
            """
CREATE TABLE IF NOT EXISTS `routine_step_new`
  (
     `id`           TEXT NOT NULL,
     `routine_id`   TEXT NOT NULL,
     `sort_index`   INTEGER NOT NULL,
     `exercise_id`  TEXT,
     `pause_step`   TEXT,
     `custom_sets`  INTEGER,
     `custom_reps`  INTEGER,
     `custom_hold`  TEXT,
     `custom_pause` TEXT,
     PRIMARY KEY(`routine_id`, `sort_index`),
     FOREIGN KEY(`routine_id`) REFERENCES `routine`(`id`) ON UPDATE no action ON DELETE CASCADE,
     FOREIGN KEY(`exercise_id`) REFERENCES `exercise`(`id`) ON UPDATE no action ON DELETE CASCADE
  )
        """.trimIndent()
        )
        val c = db.query("SELECT * FROM routine_step")
        val ridCol = c.getColumnIndexOrThrow("routine_id")
        val idxCol = c.getColumnIndexOrThrow("sort_index")
        val eidCol = c.getColumnIndexOrThrow("exercise_id")
        val psCol = c.getColumnIndexOrThrow("pause_step")
        val csCol = c.getColumnIndexOrThrow("custom_sets")
        val crCol = c.getColumnIndexOrThrow("custom_reps")
        val chCol = c.getColumnIndexOrThrow("custom_hold")
        val cpCol = c.getColumnIndexOrThrow("custom_pause")
        while (c.moveToNext()) {
            val rid = c.getString(ridCol)
            val idx = c.getInt(idxCol)
            val eid = c.getStringOrNull(eidCol)
            val ps = c.getStringOrNull(psCol)
            val cs = c.getIntOrNull(csCol)
            val cr = c.getIntOrNull(crCol)
            val ch = c.getStringOrNull(chCol)
            val cp = c.getStringOrNull(cpCol)
            db.execSQL(
                """
INSERT INTO routine_step_new
            (id,
             routine_id,
             sort_index,
             exercise_id,
             pause_step,
             custom_sets,
             custom_reps,
             custom_hold,
             custom_pause)
VALUES      (?,
             ?,
             ?,
             ?,
             ?,
             ?,
             ?,
             ?,
             ?) 
             """.trimMargin(),
                arrayOf(UUID.randomUUID().toString(), rid, idx, eid, ps, cs, cr, ch, cp)
            )
        }

        db.execSQL("DROP TABLE routine_step");
        db.execSQL("ALTER TABLE routine_step_new RENAME TO routine_step");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_routine_step_id` ON `routine_step` (`id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_routine_step_routine_id` ON `routine_step` (`routine_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_routine_step_sort_index` ON `routine_step` (`sort_index`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_routine_step_exercise_id` ON `routine_step` (`exercise_id`)")
    }
}
