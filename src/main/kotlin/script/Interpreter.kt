package com.shadowforgedmmo.engine.script

import com.google.common.base.CaseFormat
import com.shadowforgedmmo.engine.runtime.Runtime
import org.python.core.Py
import org.python.core.PyObject
import org.python.util.PythonInterpreter
import java.io.File

class Interpreter(scriptDir: File) {
    private val interpreter = PythonInterpreter()

    init {
        interpreter.systemState.path.add(scriptDir.path)
    }

    fun loadScriptLibrary(runtime: Runtime) {
        interpreter.systemState.modules.__setitem__(SCRIPT_LIBRARY_MODULE_NAME, scriptLibraryModule(runtime))
    }

    fun exec(s: String) = interpreter.exec(s)

    fun eval(s: String): PyObject = interpreter.eval(s)

    inline fun <reified T> instantiate(script: Script, vararg args: Any): T {
        exec("import ${script.id}")
        val className = idToPythonClassName(script.id)
        val scriptClass = eval("${script.id}.${className}")
        return scriptClass
            .__call__(args.map(Py::java2py).toTypedArray())
            .__tojava__(T::class.java) as T
    }

    fun idToPythonClassName(id: String): String = CaseFormat.LOWER_UNDERSCORE.to(
        CaseFormat.UPPER_CAMEL,
        id.split('.').last()
    )

    fun close() {
        interpreter.close()
    }
}
