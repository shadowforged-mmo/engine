package com.shadowforgedmmo.engine.script

import com.google.common.base.CaseFormat
import com.shadowforgedmmo.engine.resource.parseId
import org.python.core.Py
import org.python.core.PyObject
import org.python.util.PythonInterpreter

fun parseScriptId(id: String) = parseId(id, "scripts")

fun idToPythonClassName(id: String): String = CaseFormat.LOWER_UNDERSCORE.to(
    CaseFormat.UPPER_CAMEL,
    id.split('.').last()
)

fun getScriptClass(scriptId: String, interpreter: PythonInterpreter): PyObject {
    interpreter.exec("import $scriptId")
    val className = idToPythonClassName(scriptId)
    return interpreter.eval("${scriptId}.${className}")
}

inline fun <reified T> instantiateScriptClass(
    scriptId: String,
    interpreter: PythonInterpreter,
    vararg args: Any
) = getScriptClass(
    scriptId,
    interpreter
).__call__(args.map { Py.java2py(it) }.toTypedArray()).__tojava__(T::class.java) as T
