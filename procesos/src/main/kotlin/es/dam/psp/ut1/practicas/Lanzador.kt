package es.dam.psp.ut1.practicas

import es.dam.psp.ut1.Jvm
// Necesitarás además: import java.io.File y import java.util.concurrent.TimeUnit

/**
 * Lanzador.kt · Práctica 3 · Lanzador de órdenes. Completa los TODO 3.x (letras de la práctica).
 *
 * Apuntes UT-1, apartados 5.1 (ProcessBuilder, Jvm.comandoShell), 5.3 (leer la salida),
 * 6.1 (waitFor con límite, destroy) y 6.2 (no bloquearse leyendo).
 * Cómo ejecutarlo: triángulo ▶ junto a main, sin argumentos; escribe las órdenes en la consola
 * de IntelliJ (p. ej. "echo hola", "ls noexiste", "sleep 10" o, en Windows, "ping -n 11 127.0.0.1").
 *
 * Bucle: pide una orden al usuario, la ejecuta como proceso hijo y muestra
 *   - su salida (stdout y stderr juntas), con cada línea numerada (3.a),
 *   - su código de salida y el tiempo que ha tardado en milisegundos (3.b).
 * Si la orden tarda más de TIMEOUT_S segundos, se termina el proceso y se avisa (3.c).
 * Cada ejecución se añade a historial.txt con el formato:  orden;codigo;milisegundos (3.d)
 * La orden "salir" termina el programa.
 */

import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

const val TIMEOUT_S = 5L
fun main() {
    println("Lanzador PSP · sistema: ${System.getProperty("os.name")} · escribe 'salir' para terminar")
    while (true) {
        print("> ")
        val orden = readlnOrNull()?.trim() ?: break
        if (orden == "salir") break
        if (orden.isEmpty()) continue

        val pb = ProcessBuilder(Jvm.comandoShell(orden))
        pb.redirectErrorStream(true)

        val ficheroTemporal = File.createTempFile("lanzador", ".txt")
        pb.redirectOutput(ficheroTemporal)

        var codigoSalida: Int
        var proceso: Process? = null

        val milisegundos = measureTimeMillis {
            try {
                proceso = pb.start()

                // TODO 3.c: espera como máximo TIMEOUT_S segundos
                val terminadoATiempo = proceso!!.waitFor(TIMEOUT_S, TimeUnit.SECONDS)

                if (!terminadoATiempo) {
                    println("¡AVISO: La orden ha superado el tiempo límite de $TIMEOUT_S segundos y será terminada!")

                    proceso!!.descendants().forEach { it.destroyForcibly() }

                    proceso!!.destroy()
                    if (proceso!!.isAlive) {
                        proceso!!.destroyForcibly()
                    }
                    codigoSalida = -1
                } else {
                    codigoSalida = proceso!!.exitValue()
                }
            } catch (e: Exception) {
                codigoSalida = -1
                ficheroTemporal.writeText("Error al ejecutar la orden: ${e.message}\n")
            }
        }

        if (ficheroTemporal.exists()) {
            var numeroLinea = 1
            ficheroTemporal.forEachLine { linea ->
                println("${numeroLinea++}: $linea")
            }
            ficheroTemporal.delete() // Limpieza
        }

        println("[código $codigoSalida · $milisegundos ms]")

        File("historial.txt").appendText("$orden;$codigoSalida;$milisegundos\n")
    }
    println("Hasta luego")
}

