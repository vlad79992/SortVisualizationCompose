package com.moshk.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter

class MyProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MyProcessor(environment.codeGenerator, environment.logger)
    }
}

class MyProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()

        val annotationName = "com.moshk.sortviz.core.annotations.SortingAlgorithm"
        logger.warn("=== KSP PROCESSOR STARTED ===")

        val symbols = resolver.getSymbolsWithAnnotation(annotationName)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }
            .toList()

        logger.warn("Found ${symbols.size} symbols")

        if (symbols.isEmpty()) return emptyList()

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = "com.moshk.generated",
            fileName = "AlgorithmRegistry"
        )

        OutputStreamWriter(file).use { w ->
            w.write("package com.moshk.generated\n\n")

            // Импортируем базовый класс
            w.write("import com.moshk.sortviz.core.SortingAlgorithm\n")

            // Импортируем все классы
            symbols.forEach { cls ->
                val fqcn = cls.qualifiedName?.asString() ?: return@forEach
                w.write("import $fqcn\n")
            }
            w.write("\n")

            // Генерируем список фабрик с типом SortingAlgorithm<*>
            w.write("/**\n * Список фабрик для создания экземпляров алгоритмов сортировки\n */\n")
            w.write("val algorithmFactories: List<() -> SortingAlgorithm<*>> = listOf(\n")
            symbols.forEachIndexed { i, cls ->
                val fqcn = cls.qualifiedName?.asString() ?: return@forEachIndexed
                val shortName = fqcn.substringAfterLast(".")
                w.write("    { $shortName() }")
                if (i < symbols.lastIndex) w.write(",")
                w.write("\n")
            }
            w.write(")\n")
        }

        invoked = true
        return emptyList()
    }
}