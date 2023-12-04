package io.github.janmalch.woroboro.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class CriticalUserJourneyBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun cujCompilationNone() =
        benchmark(CompilationMode.None())

    @Test
    fun cujCompilationBaselineProfiles() =
        benchmark(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun benchmark(compilationMode: CompilationMode) {
        rule.measureRepeated(
            packageName = "io.github.janmalch.woroboro",
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = 5,
            setupBlock = {
                pressHome()
            },
            measureBlock = {
                startActivityAndWait()

                goToExercisesAndWait()
                goToExerciseEditorAndWait()
                createDummyExercise()

                goToRoutinesAndWait()
                goToRoutineEditorAndWait()
                createDummyRoutine()
            }
        )
    }
}