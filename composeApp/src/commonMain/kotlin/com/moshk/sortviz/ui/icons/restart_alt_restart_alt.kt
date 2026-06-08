package com.moshk.sortviz.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
val restart_alt: ImageVector
  get() {
    if (_restart_alt != null) {
      return _restart_alt!!
    }
    _restart_alt =
      ImageVector.Builder(
          name = "restart_alt",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.NonZero,
          ) {
            moveTo(11f, 20.95f)
            quadTo(7.98f, 20.58f, 5.99f, 18.31f)
            reflectiveQuadTo(4f, 13f)
            quadTo(4f, 11.35f, 4.65f, 9.84f)
            reflectiveQuadTo(6.5f, 7.2f)
            lineTo(7.93f, 8.63f)
            quadTo(6.98f, 9.48f, 6.49f, 10.6f)
            reflectiveQuadTo(6f, 13f)
            quadToRelative(0f, 2.2f, 1.4f, 3.89f)
            reflectiveQuadTo(11f, 18.95f)
            verticalLineToRelative(2f)
            close()
            moveToRelative(2f, 0f)
            verticalLineToRelative(-2f)
            quadToRelative(2.18f, -0.4f, 3.59f, -2.07f)
            reflectiveQuadTo(18f, 13f)
            quadTo(18f, 10.5f, 16.25f, 8.75f)
            reflectiveQuadTo(12f, 7f)
            horizontalLineTo(11.93f)
            lineToRelative(1.1f, 1.1f)
            lineToRelative(-1.4f, 1.4f)
            lineTo(8.13f, 6f)
            lineToRelative(3.5f, -3.5f)
            lineToRelative(1.4f, 1.4f)
            lineTo(11.93f, 5f)
            horizontalLineTo(12f)
            quadToRelative(3.35f, 0f, 5.68f, 2.32f)
            reflectiveQuadTo(20f, 13f)
            quadToRelative(0f, 3.02f, -1.99f, 5.29f)
            quadTo(16.03f, 20.55f, 13f, 20.95f)
            close()
          }
        }
        .build()
    return _restart_alt!!
  }

@Suppress("ObjectPropertyName")
private var _restart_alt: ImageVector? = null
