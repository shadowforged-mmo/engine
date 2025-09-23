package com.shadowforgedmmo.engine.script

import com.shadowforgedmmo.engine.runtime.Runtime
import org.python.core.Py
import org.python.core.PyObject
import org.python.util.PythonInterpreter
import java.io.File

class Interpreter(val scriptDir: File) {
    private val interpreter = PythonInterpreter()

    init {
        interpreter.systemState.path.add(scriptDir.path)
    }

    fun loadScriptLibrary(runtime: Runtime) {
        interpreter.systemState.modules.__setitem__(SCRIPT_LIBRARY_MODULE_NAME, scriptLibraryModule(runtime))
    }

    fun exec(s: String) = interpreter.exec(s)

    fun eval(s: String): PyObject = interpreter.eval(s)

    inline fun <reified T> instantiate(scriptId: String, vararg args: Any): T {
        exec("import $scriptId")
        val className = idToPythonClassName(scriptId)
        val scriptClass = eval("${scriptId}.${className}")
        return scriptClass.__call__(args.map { Py.java2py(it) }.toTypedArray()).__tojava__(T::class.java) as T
    }
}
