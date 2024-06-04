package com.alfayedoficial.astagfirullah

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CustomProgressTask(project: Project) : Task.Backgroundable(project, "اذكر الله", false) {

    companion object {
        val arabicPhrases = listOf(
            "اللهم صل وسلم على نبينا محمد",
            "سبحان الله",
            "الحمدلله",
            "لا إله إلا الله",
            "الله أكبر",
            "أستغفر الله",
            "سبحان الله وبحمده",
            "سبحان الله العظيم",
            "لا حول ولا قوة إلا بالله",
            "اللهم صل وسلم على نبينا محمد",
            "لا إله إلا أنت سبحانك إني كنت من الظالمين"
        )
    }

    override fun run(indicator: ProgressIndicator) {
        runBlocking {
            withContext(Dispatchers.Default) {
                for (i in arabicPhrases.indices) {
                    indicator.text = arabicPhrases[i]
                    indicator.fraction = (i + 1) / arabicPhrases.size.toDouble()
                    delay(1000) // Adjust the delay as needed
                }
            }
        }
    }
}
