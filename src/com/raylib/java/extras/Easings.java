package com.raylib.java.extras;

import static com.raylib.java.raymath.Raymath.PI;

public class Easings{

    // Linear Easing functions
    public static float EaseLinearNone(float t, float b, float c, float d){
        return (c * t / d + b);
    }

    public static float EaseLinearIn(float t, float b, float c, float d){
        return (c * t / d + b);
    }

    public static float EaseLinearOut(float t, float b, float c, float d){
        return (c * t / d + b);
    }

    public static float EaseLinearInOut(float t, float b, float c, float d){
        return (c * t / d + b);
    }

    // Sine Easing functions
    public static float EaseSineIn(float t, float b, float c, float d){
        return (float) (-c * Math.cos(t / d * (PI / 2.0f)) + c + b);
    }

    public static float EaseSineOut(float t, float b, float c, float d){
        return (float) (c * Math.sin(t / d * (PI / 2.0f)) + b);
    }

    public static float EaseSineInOut(float t, float b, float c, float d){
        return (float) (-c / 2.0f * (Math.cos(PI * t / d) - 1.0f) + b);
    }

    // Circular Easing functions
    public static float EaseCircIn(float t, float b, float c, float d){
        t /= d;
        return (float) (-c * (Math.sqrt(1.0f - t * t) - 1.0f) + b);
    }

    public static float EaseCircOut(float t, float b, float c, float d){
        t = t / d - 1.0f;
        return (float) (c * Math.sqrt(1.0f - t * t) + b);
    }

    public static float EaseCircInOut(float t, float b, float c, float d){
        if ((t /= d / 2.0f) < 1.0f) return (float) (-c / 2.0f * (Math.sqrt(1.0f - t * t) - 1.0f) + b);
        t -= 2.0f;
        return (float) (c / 2.0f * (Math.sqrt(1.0f - t * t) + 1.0f) + b);
    }

    // Cubic Easing functions
    public static float EaseCubicIn(float t, float b, float c, float d){
        t /= d;
        return (c * t * t * t + b);
    }

    public static float EaseCubicOut(float t, float b, float c, float d){
        t = t / d - 1.0f;
        return (c * (t * t * t + 1.0f) + b);
    }

    public static float EaseCubicInOut(float t, float b, float c, float d){
        if ((t /= d / 2.0f) < 1.0f) return (c / 2.0f * t * t * t + b);
        t -= 2.0f;
        return (c / 2.0f * (t * t * t + 2.0f) + b);
    }

    // Quadratic Easing functions
    public static float EaseQuadIn(float t, float b, float c, float d){
        t /= d;
        return (c * t * t + b);
    }

    public static float EaseQuadOut(float t, float b, float c, float d){
        t /= d;
        return (-c * t * (t - 2.0f) + b);
    }

    public static float EaseQuadInOut(float t, float b, float c, float d){
        if ((t /= d / 2) < 1) return (((c / 2) * (t * t)) + b);
        return (-c / 2.0f * (((t - 1.0f) * (t - 3.0f)) - 1.0f) + b);
    }

    // Exponential Easing functions
    public static float EaseExpoIn(float t, float b, float c, float d){
        return (t == 0.0f) ? b : (float) (c * Math.pow(2.0f, 10.0f * (t / d - 1.0f)) + b);
    }

    public static float EaseExpoOut(float t, float b, float c, float d){
        return (t == d) ? (b + c) : (float) (c * (-Math.pow(2.0f, -10.0f * t / d) + 1.0f) + b);
    }

    public static float EaseExpoInOut(float t, float b, float c, float d){
        if (t == 0.0f) return b;
        if (t == d) return (b + c);
        if ((t /= d / 2.0f) < 1.0f) return (float) (c / 2.0f * Math.pow(2.0f, 10.0f * (t - 1.0f)) + b);

        return (float) (c / 2.0f * (-Math.pow(2.0f, -10.0f * (t - 1.0f)) + 2.0f) + b);
    }

    // Back Easing functions
    public static float EaseBackIn(float t, float b, float c, float d){
        float s = 1.70158f;
        float postFix = t /= d;
        return (c * (postFix) * t * ((s + 1.0f) * t - s) + b);
    }

    public static float EaseBackOut(float t, float b, float c, float d){
        float s = 1.70158f;
        t = t / d - 1.0f;
        return (c * (t * t * ((s + 1.0f) * t + s) + 1.0f) + b);
    }

    public static float EaseBackInOut(float t, float b, float c, float d){
        float s = 1.70158f;
        if ((t /= d / 2.0f) < 1.0f){
            s *= 1.525f;
            return (c / 2.0f * (t * t * ((s + 1.0f) * t - s)) + b);
        }

        float postFix = t -= 2.0f;
        s *= 1.525f;
        return (c / 2.0f * ((postFix) * t * ((s + 1.0f) * t + s) + 2.0f) + b);
    }

    // Bounce Easing functions
    public static float EaseBounceOut(float t, float b, float c, float d){
        if ((t /= d) < (1.0f / 2.75f)){
            return (c * (7.5625f * t * t) + b);
        }
        else if (t < (2.0f / 2.75f)){
            float postFix = t -= (1.5f / 2.75f);
            return (c * (7.5625f * (postFix) * t + 0.75f) + b);
        }
        else if (t < (2.5 / 2.75)){
            float postFix = t -= (2.25f / 2.75f);
            return (c * (7.5625f * (postFix) * t + 0.9375f) + b);
        }
        else{
            float postFix = t -= (2.625f / 2.75f);
            return (c * (7.5625f * (postFix) * t + 0.984375f) + b);
        }
    }

    public static float EaseBounceIn(float t, float b, float c, float d){
        return (c - EaseBounceOut(d - t, 0.0f, c, d) + b);
    }

    public static float EaseBounceInOut(float t, float b, float c, float d){
        if (t < d / 2.0f){
            return (EaseBounceIn(t * 2.0f, 0.0f, c, d) * 0.5f + b);
        }
        else{
            return (EaseBounceOut(t * 2.0f - d, 0.0f, c, d) * 0.5f + c * 0.5f + b);
        }
    }

    // Elastic Easing functions
    public static float EaseElasticIn(float t, float b, float c, float d){
        if (t == 0.0f) return b;
        if ((t /= d) == 1.0f) return (b + c);

        float p = d * 0.3f;
        float a = c;
        float s = p / 4.0f;
        float postFix = (float) (a * Math.pow(2.0f, 10.0f * (t -= 1.0f)));

        return (float) (-(postFix * Math.sin((t * d - s) * (2.0f * PI) / p)) + b);
    }

    public static float EaseElasticOut(float t, float b, float c, float d){
        if (t == 0.0f) return b;
        if ((t /= d) == 1.0f) return (b + c);

        float p = d * 0.3f;
        float a = c;
        float s = p / 4.0f;

        return (float) (a * Math.pow(2.0f, -10.0f * t) * Math.sin((t * d - s) * (2.0f * PI) / p) + c + b);
    }

    public static float EaseElasticInOut(float t, float b, float c, float d){
        if (t == 0.0f) return b;
        if ((t /= d / 2.0f) == 2.0f) return (b + c);

        float p = d * (0.3f * 1.5f);
        float s = p / 4.0f;

        if (t < 1.0f){
            float postFix = (float) (c * Math.pow(2.0f, 10.0f * (t -= 1.0f)));
            return (float) (-0.5f * (postFix * Math.sin((t * d - s) * (2.0f * PI) / p)) + b);
        }

        float postFix = (float) (c * Math.pow(2.0f, -10.0f * (t -= 1.0f)));

        return (float) (postFix * Math.sin((t * d - s) * (2.0f * PI) / p) * 0.5f + c + b);
    }
}
