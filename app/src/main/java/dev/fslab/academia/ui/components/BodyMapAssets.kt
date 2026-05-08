package dev.fslab.academia.ui.components

import android.content.Context
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.vector.PathParser
import dev.fslab.academia.model.GrupoMuscular

data class BodyMap(
    val viewBoxWidth: Float,
    val viewBoxHeight: Float,
    val borders: List<Path>,
    val grupoParaPaths: Map<GrupoMuscular, List<Path>>,
    val grupoParaBounds: Map<GrupoMuscular, List<android.graphics.RectF>>
)

object BodyMapAssets {

    private var cache: BodyMap? = null

    private val MUSCLE_TO_GROUP: Map<String, GrupoMuscular> = mapOf(
        "biceps" to GrupoMuscular.BRACOS,
        "triceps" to GrupoMuscular.BRACOS,
        "forearms" to GrupoMuscular.BRACOS,
        "side_delts" to GrupoMuscular.OMBROS,
        "front_delts" to GrupoMuscular.OMBROS,
        "rear_delts" to GrupoMuscular.OMBROS,
        "upper_pecs" to GrupoMuscular.PEITO,
        "lower_pecs" to GrupoMuscular.PEITO,
        "middle_pecs" to GrupoMuscular.PEITO,
        "upper_traps" to GrupoMuscular.COSTAS,
        "lower_traps" to GrupoMuscular.COSTAS,
        "rhomboids" to GrupoMuscular.COSTAS,
        "lats" to GrupoMuscular.COSTAS,
        "lower_back" to GrupoMuscular.COSTAS,
        "upper_abs" to GrupoMuscular.ABDOMEN,
        "lower_abs" to GrupoMuscular.ABDOMEN,
        "obliques" to GrupoMuscular.ABDOMEN,
        "quads" to GrupoMuscular.PERNAS,
        "hamstrings" to GrupoMuscular.PERNAS,
        "glutes" to GrupoMuscular.PERNAS,
        "calves" to GrupoMuscular.PERNAS,
        "hip_abductor" to GrupoMuscular.PERNAS,
        "hip_adductor" to GrupoMuscular.PERNAS,
        "neck" to GrupoMuscular.PESCOCO
    )

    private val BORDER_GROUPS = setOf("front_borders", "rear_borders", "front", "rear", "face")

    fun load(context: Context): BodyMap {
        cache?.let { return it }
        val svg = context.assets.open("body_map.svg").bufferedReader().use { it.readText() }
        return parse(svg).also { cache = it }
    }

    private fun parse(svg: String): BodyMap {
        val viewBoxRegex = Regex("""viewBox="([\d.\s\-]+)"""")
        val vb = viewBoxRegex.find(svg)?.groupValues?.get(1)?.trim()?.split(Regex("""\s+"""))
        val w = vb?.getOrNull(2)?.toFloatOrNull() ?: 3528f
        val h = vb?.getOrNull(3)?.toFloatOrNull() ?: 3203f

        val groupRegex = Regex("""<g\s+id="([^"]+)"[^>]*>([\s\S]*?)</g>""")
        val pathRegex = Regex("""<path[^>]*\sd="([^"]+)"""")

        val borders = mutableListOf<Path>()
        val grupoPaths = mutableMapOf<GrupoMuscular, MutableList<Path>>()
        val grupoBounds = mutableMapOf<GrupoMuscular, MutableList<android.graphics.RectF>>()

        groupRegex.findAll(svg).forEach { match ->
            val id = match.groupValues[1]
            val body = match.groupValues[2]
            val paths = pathRegex.findAll(body).map { it.groupValues[1] }.toList()

            val composePaths = paths.mapNotNull { d ->
                runCatching { PathParser().parsePathString(d).toPath() }.getOrNull()
            }

            when {
                id in BORDER_GROUPS -> borders.addAll(composePaths)
                MUSCLE_TO_GROUP.containsKey(id) -> {
                    val grupo = MUSCLE_TO_GROUP.getValue(id)
                    grupoPaths.getOrPut(grupo) { mutableListOf() }.addAll(composePaths)
                    val bounds = composePaths.map { p ->
                        val rect = android.graphics.RectF()
                        p.asAndroidPath().computeBounds(rect, true)
                        rect
                    }
                    grupoBounds.getOrPut(grupo) { mutableListOf() }.addAll(bounds)
                }
            }
        }

        return BodyMap(
            viewBoxWidth = w,
            viewBoxHeight = h,
            borders = borders,
            grupoParaPaths = grupoPaths,
            grupoParaBounds = grupoBounds
        )
    }
}
