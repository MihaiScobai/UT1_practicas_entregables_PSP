package main.kotlin.es.dam.psp.ut1.practicas

import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Error: Por favor, introduce una cadena para buscar procesos.")
        println("Uso: java -jar BuscarProcesos.jar <texto_a_buscar>")
        return
    }
    val textoBuscar = args[0]
    val yo = ProcessHandle.current()
    println("Soy el PID ${yo.pid()}, mi padre es ${yo.parent().map { it.pid() }.orElse(-1)}")
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

    println("%-8s %-8s %-15s %-10s %-12s %s".format("PID", "PPID", "USUARIO", "H. INICIO", "TIEMPO CPU", "COMANDO"))

    ProcessHandle.allProcesses()
        .filter { it.info().command().isPresent }
        .filter { it.info().command().get().contains(textoBuscar, ignoreCase = true) }
        .forEach { p ->
            val info = p.info()
            val horaInicio = info.startInstant()
                .map { formatter.format(it) }
                .orElse("?")
            val tiempoCpu = info.totalCpuDuration()
                .map { "${it.toSeconds()}s" }
                .orElse("?")
            println(
                "%-8d %-8s %-15s %-10s %-12s %s".format(
                    p.pid(),
                    p.parent().map { it.pid().toString() }.orElse("-"),
                    info.user().orElse("?").takeLast(15),
                    horaInicio,
                    tiempoCpu,
                    info.command().get().takeLast(60)
                )
            )
        }
}
