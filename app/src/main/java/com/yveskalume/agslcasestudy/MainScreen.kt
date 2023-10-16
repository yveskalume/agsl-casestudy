package com.yveskalume.agslcasestudy

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import org.intellij.lang.annotations.Language

@Language("AGSL")
private const val COLOR_SHADER_SRC =
    """uniform float2 iResolution;
   half4 main(float2 fragCoord) {
   float2 scaled = fragCoord/iResolution.xy;
   return half4(scaled, 0, 1);
}"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MainScreen() {
    val colorShader = RuntimeShader(COLOR_SHADER_SRC)
    Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
        colorShader.setFloatUniform(
            "iResolution",
            size.width, size.height
        )
        drawCircle(brush = ShaderBrush(colorShader))
    })
}