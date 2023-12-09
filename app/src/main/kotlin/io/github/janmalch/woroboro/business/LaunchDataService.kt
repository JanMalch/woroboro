package io.github.janmalch.woroboro.business

import android.util.Log
import io.github.janmalch.woroboro.models.RoutineFilter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LaunchDataService @Inject constructor() {

    private var routineFilter: RoutineFilter? = null

    fun setRoutineFilter(routineFilter: RoutineFilter?) {
        if (routineFilter != null) {
            Log.w("LaunchDataRepository", "A routine filter is already set. Overwriting it.")
        }
        this.routineFilter = routineFilter
    }

    fun consumeRoutineFilter(): RoutineFilter? {
        val filter = routineFilter
        routineFilter = null
        return filter
    }
}
