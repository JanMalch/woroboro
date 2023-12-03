package io.github.janmalch.woroboro.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until


fun MacrobenchmarkScope.goToExercisesAndWait() {
    device.findObject(By.res("bottom_bar_button_exercises"))
        .clickAndWait(Until.newWindow(), 3000)
}

fun MacrobenchmarkScope.goToRoutinesAndWait() {
    device.findObject(By.res("bottom_bar_button_routines"))
        .clickAndWait(Until.newWindow(), 3000)
}

fun MacrobenchmarkScope.goToExerciseEditorAndWait() {
    device.findObject(By.res("button_new_exercise"))
        .clickAndWait(Until.newWindow(), 3000)
}

fun MacrobenchmarkScope.createDummyExercise() {
    device.findObject(By.res("input_exercise_name")).text = "User Journey Exercise"
    device.findObject(By.res("input_reps")).text = "10"
    device.waitForIdle()

    device.findObject(By.res("button_save")).click()
    device.waitForIdle()
}

fun MacrobenchmarkScope.goToRoutineEditorAndWait() {
    device.wait(
        Until.hasObject(By.pkg("button_new_routine")),
        3_000
    )
    device.findObject(By.res("button_new_routine"))
        .clickAndWait(Until.newWindow(), 3000)
}

fun MacrobenchmarkScope.createDummyRoutine() {
    device.findObject(By.res("input_routine_name")).text = "User Journey Routine"
    device.findObject(By.res("button_new_step")).click()
    device.wait(Until.hasObject(By.res("input_exercise_filter")), 5000)

    device.findObject(By.res("input_exercise_filter")).text = "Exercise"
    device.wait(Until.hasObject(By.text("User Journey Exercise")), 5000)
    device.findObject(By.text("User Journey Exercise")).click()
    device.waitForIdle()

    device.findObject(By.res("button_save_step")).click()
    device.waitForIdle()

    device.findObject(By.res("button_save")).click()
    device.waitForIdle()
}
