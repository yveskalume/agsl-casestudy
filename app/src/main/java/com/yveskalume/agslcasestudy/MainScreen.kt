package com.yveskalume.agslcasestudy

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.pointerInput


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
val LightScatteringShader = RuntimeShader(
    """
        uniform float2 resolution;
        uniform float pointerX;
        uniform float pointerY;
        
        const float fov = tan(radians(30.0));
        const float cameraheight = 5e1; //50.
        const float Gamma = 2.2;
        const float Rayleigh = 1.;
        const float Mie = 1.;
        const float RayleighAtt = 1.;
        const float MieAtt = 1.2;

        float g = -0.9;
        
        vec3 _betaR = vec3(1.95e-2, 1.1e-1, 2.94e-1); 
        vec3 _betaM = vec3(4e-2, 4e-2, 4e-2);
        
        const float ts= (cameraheight / 2.5e5);
        
        vec3 Ds = normalize(vec3(0., 0., -1.)); //sun 
        
        vec3 ACESFilm( vec3 x )
        {
            float tA = 2.51;
            float tB = 0.03;
            float tC = 2.43;
            float tD = 0.59;
            float tE = 0.14;
            return clamp((x*(tA*x+tB))/(x*(tC*x+tD)+tE),0.0,1.0);
        }
        
        vec4 main(in float2 fragCoord ) {
        
            float AR = resolution.x/resolution.y;
            float M = 1.0;
            
            float uvMouseX = (pointerX / resolution.x) * AR;
            float uvMouseY = (pointerY / resolution.y) * AR;
            
            vec2 uv0 = (fragCoord.xy / resolution.xy);
            uv0 *= M;
            uv0.x *= AR;
            
            vec2 uv = uv0 * (2.0*M) - (1.0*M);
            uv.x *=AR;
            
            Ds = normalize(vec3(uvMouseX-((0.5*AR)), uvMouseY-0.5, (fov/-2.0)));
            
            vec3 O = vec3(0., cameraheight, 0.);
            vec3 D = normalize(vec3(uv, -(fov*M)));
        
            vec3 color = vec3(0.);

        
              float cosine = clamp(dot(D,Ds),0.0,1.0);
              vec3 extinction = exp(-(_betaR + _betaM));
        
              float g2 = g * g;
              float fcos2 = cosine * cosine;
              float miePhase = Mie * pow(1. + g2 + 2. * g * cosine, -1.5) * (1. - g2) / (2. + g2);

              float rayleighPhase = Rayleigh;
        
              vec3 inScatter = (1. + fcos2) * vec3(rayleighPhase + _betaM / _betaR * miePhase);
        
              color = inScatter*(1.0-extinction);
        
                // sun
              color += 0.47*vec3(1.6,1.4,1.0)*pow( cosine, 350.0 ) * extinction;
              // sun haze
              color += 0.4*vec3(0.8,0.9,1.0)*pow( cosine, 2.0 )* extinction;
            
              color = ACESFilm(color);
            
              color = pow(color, vec3(Gamma));
            
              return vec4(color, 1.);
        }
    """
)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MainScreen() {
    val brush = remember { ShaderBrush(LightScatteringShader) }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        var dragOffset by remember {
            mutableStateOf(Offset(0f, 0f))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        dragOffset += dragAmount
                    }
                }
                .drawWithCache {
                    LightScatteringShader.setFloatUniform(
                        "resolution",
                        this.size.width, this.size.height
                    )
                    LightScatteringShader.setFloatUniform("pointerX", dragOffset.x)
                    LightScatteringShader.setFloatUniform("pointerY", dragOffset.y)
                    onDrawBehind {
                        drawRect(brush)
                    }
                }
        )
    }
}