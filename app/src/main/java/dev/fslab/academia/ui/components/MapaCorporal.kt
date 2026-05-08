package dev.fslab.academia.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.fslab.academia.model.GrupoMuscular
import dev.fslab.academia.ui.theme.LocalAcademiaColors

@Composable
fun MapaCorporal(
    grupoSelecionado: GrupoMuscular?,
    onGrupoSelecionado: (GrupoMuscular?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAcademiaColors.current
    val context = LocalContext.current
    val bodyMap = remember { BodyMapAssets.load(context) }

    val aspect = bodyMap.viewBoxWidth / bodyMap.viewBoxHeight

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Mapa corporal",
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Frente · Costas",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                if (grupoSelecionado != null) {
                    AssistChip(
                        onClick = { onGrupoSelecionado(null) },
                        label = { Text(grupoSelecionado.display, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = colors.primary,
                            labelColor = colors.textOnPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspect)
                    .pointerInput(bodyMap) {
                        detectTapGestures { offset ->
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val sx = w / bodyMap.viewBoxWidth
                            val sy = h / bodyMap.viewBoxHeight
                            val s = minOf(sx, sy)
                            val ox = (w - bodyMap.viewBoxWidth * s) / 2f
                            val oy = (h - bodyMap.viewBoxHeight * s) / 2f
                            val vx = (offset.x - ox) / s
                            val vy = (offset.y - oy) / s
                            val grupo = bodyMap.grupoParaBounds.entries.firstOrNull { (_, listaBounds) ->
                                listaBounds.any { b -> b.contains(vx, vy) }
                            }?.key
                            if (grupo != null) {
                                onGrupoSelecionado(if (grupoSelecionado == grupo) null else grupo)
                            } else {
                                onGrupoSelecionado(null)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val sx = w / bodyMap.viewBoxWidth
                    val sy = h / bodyMap.viewBoxHeight
                    val s = minOf(sx, sy)
                    val ox = (w - bodyMap.viewBoxWidth * s) / 2f
                    val oy = (h - bodyMap.viewBoxHeight * s) / 2f

                    withTransform({
                        translate(left = ox, top = oy)
                        scale(scaleX = s, scaleY = s, pivot = androidx.compose.ui.geometry.Offset.Zero)
                    }) {
                        // Silhueta + borders
                        bodyMap.borders.forEach { path ->
                            drawPath(path, color = colors.textSecondary.copy(alpha = 0.7f), style = Stroke(width = 4f))
                        }

                        // Músculos (preenchimento sutil)
                        bodyMap.grupoParaPaths.forEach { (grupo, paths) ->
                            val ehSelecionado = grupo == grupoSelecionado
                            val corFill = if (ehSelecionado) colors.primary
                            else colors.primary.copy(alpha = 0.10f)
                            val corStroke = if (ehSelecionado) colors.primary
                            else colors.textSecondary.copy(alpha = 0.5f)
                            paths.forEach { path ->
                                drawPath(path, color = corFill)
                                drawPath(path, color = corStroke, style = Stroke(width = if (ehSelecionado) 3f else 1.5f))
                            }
                        }
                    }
                }
            }

            if (grupoSelecionado == null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Toque um grupo muscular",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
