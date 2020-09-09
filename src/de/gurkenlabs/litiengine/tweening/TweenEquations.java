/*
 * Copyright © 2001 Robert Penner
 * All rights reserved.
 *                              BSD License
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *
 *      * Neither the name of the <ORGANIZATION> nor the names of its contributors
 *        may be used to endorse or promote products derived from this software
 *        without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL <ORGANIZATION> BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * Copyright 2012 Aurelien Ribon
 * Copyright 2015 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.gurkenlabs.litiengine.tweening;

/**
 * Easing equation based on Robert Penner's work: http://robertpenner.com/easing/
 *
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 * @author Various optimizations by Michael "Code Poet" Pohoreski | https://github.com/Michaelangel007/
 * @author dorkbox, llc
 */
@SuppressWarnings({"NumericCastThatLosesPrecision", "FloatingPointEquality", "unused"})
public
enum TweenEquations {
    // Linear (just to prevent confusion, same as none)
    Linear(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            return time;
        }

        @Override
        public
        String toString() {
            return "Linear.INOUT";
        }
    }),


    ///////////////////////////////////////////////////////
    // Quad based (x^2)
    ///////////////////////////////////////////////////////
    Quad_In(new TweenEquation() {
        @Override
        public final
        float compute(final float time) {
            return time * time;
        }

        @Override
        public
        String toString() {
            return "Quad.IN";
        }
    }),
    Quad_Out(new TweenEquation() {
        @Override
        public final
        float compute(final float time) {
            final float m = time - 1.0F;
            return 1.0F - m * m;
        }

        @Override
        public
        String toString() {
            return "Quad.OUT";
        }
    }),
    Quad_InOut(new TweenEquation() {
        @Override
        public final
        float compute(float time) {
            final float t = time * 2.0F;
            if (t < 1.0F) {
                return time * t;
            }

            final float m = time - 1.0F;
            return 1.0F - m * m * 2.0F;
        }

        @Override
        public
        String toString() {
            return "Quad.INOUT";
        }
    }),


    ///////////////////////////////////////////////////////
    // Cubic (x^3)
    ///////////////////////////////////////////////////////
    Cubic_In(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            return time * time * time;
        }

        @Override
        public
        String toString() {
            return "Cubic.IN";
        }
    }),
    Cubic_Out(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            final float m = time - 1.0F;
            return 1.0F + m * m * m;
        }

        @Override
        public
        String toString() {
            return "Cubic.OUT";
        }
    }),
    Cubic_InOut(new TweenEquation() {
        @Override
        public
        float compute(float time) {
            final float t = time * 2.0F;
            if (t < 1.0F) {
                return time * t * t;
            }

            final float m = time - 1.0F;
            return 1.0F + m * m * m * 4.0F;
        }

        @Override
        public
        String toString() {
            return "Cubic.INOUT";
        }
    }),


    ///////////////////////////////////////////////////////
    // Quart (x^4)
    ///////////////////////////////////////////////////////
    Quart_In(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            final float t = time * time;
            return t * t;
        }

        @Override
        public
        String toString() {
            return "Quart.IN";
        }
    }),
    Quart_Out(new TweenEquation() {
        @Override
        public
        float compute(float time) {
            final float m = time - 1.0F;
            final float m2 = m * m;
            return 1.0F - m2 * m2;
        }

        @Override
        public
        String toString() {
            return "Quart.OUT";
        }
    }),
    Quart_InOut(new TweenEquation() {
        @Override
        public
        float compute(float time) {
            final float t = time * 2.0F;
            if (t < 1.0F) {
                return time * t * t * t;
            }

            final float m = time - 1.0F;
            final float m2 = m * m;
            return 1.0F - m2 * m2 * 8.0F;
        }

        @Override
        public
        String toString() {
            return "Quart.INOUT";
        }
    }),


    ///////////////////////////////////////////////////////
    // Quint (x^5)
    ///////////////////////////////////////////////////////
    Quint_In(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            final float t = time * time;
            return t * t * time;
        }

        @Override
        public
        String toString() {
            return "Quint.IN";
        }
    }),
    Quint_Out(new TweenEquation() {
        @Override
        public
        float compute(float time) {
            final float m = time - 1.0F;
            final float m2 = m * m;
            return 1.0F + m2 * m2 * m;
        }

        @Override
        public
        String toString() {
            return "Quint.OUT";
        }
    }),
    Quint_InOut(new TweenEquation() {
        @Override
        public
        float compute(float time) {
            final float t = time * 2.0F;
            if (t < 1.0F) {
                final float t2 = t * t;
                return time * t2 * t2;
            }

            final float m = time - 1.0F;
            final float m2 = m * m;
            return 1.0F + m2 * m2 * m * 16.0F;
        }

        @Override
        public
        String toString() {
            return "Quint.INOUT";
        }
    }),


    ///////////////////////////////////////////////////////
    // Sextic (x^6)
    ///////////////////////////////////////////////////////
    Sextic_In(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            final float t = time * time;
            return t * t * t;
        }

        @Override
        public
        String toString() {
            return "Sextic.IN";
        }
    }),
    Sextic_Out(new TweenEquation() {
        @Override
        public
        float compute(float time) {
            final float m = time - 1.0F;
            final float m2 = m * m;
            return 1.0F - m2 * m2 * m2;
        }

        @Override
        public
        String toString() {
            return "Sextic.OUT";
        }
    }),
    Sextic_InOut(new TweenEquation() {
        @Override
        public
        float compute(float time) {
            final float t = time * 2.0F;
            if (t < 1.0F) {
                final float t2 = t * t;
                return time * t2 * t2 * t;
            }

            final float m = time - 1.0F;
            final float m2 = m * m;
            return 1.0F - m2 * m2 * m2 * 32.0F;
        }

        @Override
        public
        String toString() {
            return "Sextic.INOUT";
        }
    }),


    ///////////////////////////////////////////////////////
    // Septic (x^7)
    ///////////////////////////////////////////////////////
    Septic_In(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            final float t = time * time;
            return t * t * t * time;
        }

        @Override
        public
        String toString() {
            return "Septic.IN";
        }
    }),
    Septic_Out(new TweenEquation() {
        @Override
        public
        float compute(float time) {
            final float m = time - 1.0F;
            final float m2 = m * m;
            return 1.0F + m2 * m2 * m2 * m;
        }

        @Override
        public
        String toString() {
            return "Septic.OUT";
        }
    }),
    Septic_InOut(new TweenEquation() {
        @Override
        public
        float compute(float time) {
            final float t = time * 2.0F;
            if (t < 1.0F) {
                final float t2 = t * t;
                return time * t2 * t2 * t2;
            }

            final float m = time - 1.0F;
            final float m2 = m * m;
            return 1.0F + m2 * m2 * m2 * m * 64.0F;
        }

        @Override
        public
        String toString() {
            return "Septic.INOUT";
        }
    }),


    ///////////////////////////////////////////////////////
    // Octic (x^8)
    ///////////////////////////////////////////////////////
    Octic_In(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            final float t = time * time;
            final float t2 = t * t;
            return t2 * t2;
        }

        @Override
        public
        String toString() {
            return "Octic.IN";
        }
    }),
    Octic_Out(new TweenEquation() {
        @Override
        public
        float compute(float time) {
            final float m = time - 1.0F;
            final float m2 = m * m;
            final float m4 = m2 * m2;
            return 1.0F - m4 * m4;
        }

        @Override
        public
        String toString() {
            return "Octic.OUT";
        }
    }),
    Octic_InOut(new TweenEquation() {
        @Override
        public
        float compute(float time) {
            final float t = time * 2.0F;
            if (t < 1.0F) {
                final float t2 = t * t;
                return time * t2 * t2 * t2 * t;
            }

            final float m = time - 1.0F;
            final float m2 = m * m;
            final float m4 = m2 * m2;
            return 1.0F - m4 * m4 * 128.0F;
        }

        @Override
        public
        String toString() {
            return "Octic.INOUT";
        }
    }),


    ///////////////////////////////////////////////////////
    // Circle
    ///////////////////////////////////////////////////////
    Circle_In(new TweenEquation() {
        @Override
        public final
        float compute(final float time) {
            return (float) (1.0F - Math.sqrt(1.0F - time * time));
        }

        @Override
        public
        String toString() {
            return "Circle.IN";
        }
    }),
    Circle_Out(new TweenEquation() {
        @Override
        public final
        float compute(final float time) {
            final float m = time - 1.0F;
            return (float) Math.sqrt(1.0F - m * m);
        }

        @Override
        public
        String toString() {
            return "Circle.OUT";
        }
    }),
    Circle_InOut(new TweenEquation() {
        @Override
        public final
        float compute(final float time) {
            final float m = time - 1.0F;
            final float t = time * 2.0F;

            if (t < 1.0F) {
                return (float) ((1.0F - Math.sqrt(1.0F - t * t)) * 0.5);
            }
            else {
                return (float) ((Math.sqrt(1.0F - 4 * m * m) + 1.0F) * 0.5F);
            }
        }

        @Override
        public
        String toString() {
            return "Circle.INOUT";
        }
    }),


    ///////////////////////////////////////////////////////
    // Sine
    ///////////////////////////////////////////////////////
    Sine_In(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            return (float) (1.0F - Math.cos(time * PI_DIV_2));
        }

        @Override
        public
        String toString() {
            return "Sine.IN";
        }
    }),
    Sine_Out(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            return (float) Math.sin(time * PI_DIV_2);
        }

        @Override
        public
        String toString() {
            return "Sine.OUT";
        }
    }),
    Sine_InOut(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            return (float) (0.5F * (1.0F - Math.cos(time * PI)));
        }

        @Override
        public
        String toString() {
            return "Sine.INOUT";
        }
    }),


    ///////////////////////////////////////////////////////
    // Exponential
    ///////////////////////////////////////////////////////
    Expo_In(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            return (float) Math.pow(2, 10.0F * (time - 1.0F));
        }

        @Override
        public
        String toString() {
            return "Expo.IN";
        }
    }),
    Expo_Out(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            return (float) (1.0F - Math.pow(2, -10.0F * time));
        }

        @Override
        public
        String toString() {
            return "Expo.OUT";
        }
    }),
    Expo_InOut(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            if (time < 0.5F) {
                return (float) Math.pow(2, 10.0F * (2.0F * time - 1.0F) - 1.0F);
            }

            return (float) (1.0F - Math.pow(2, -10.0F * (2.0F * time - 1.0F) - 1.0F));
        }

        @Override
        public
        String toString() {
            return "Expo.INOUT";
        }
    }),


    ///////////////////////////////////////////////////////
    // Back
    ///////////////////////////////////////////////////////
    Back_In(new TweenEquation() {

        @Override
        public final
        float compute(final float time) {
            // final float k = 1.70158F; (as constant K)
            return time * time * (time * (K + 1.0F) - K);
        }

        @Override
        public
        String toString() {
            return "Back.IN";
        }
    }),
    Back_Out(new TweenEquation() {
        @Override
        public final
        float compute(final float time) {
            final float m = time - 1.0F;
            // final float k = 1.70158F; (as constant K)

            return 1.0F + m * m * (m * (K + 1.0F) + K);
        }

        @Override
        public
        String toString() {
            return "Back.OUT";
        }
    }),
    Back_InOut(new TweenEquation() {
        @Override
        public final
        float compute(final float time) {
            final float m = time - 1.0F;
            final float t = time * 2.0F;
            // final float k = 1.70158F * 1.525F; (as constant K2)

            if (time < 0.5F) {
                return time * t * (t * (K2 + 1.0F) - K2);
            }
            else {
                return 1.0F + 2.0F * m * m * (2.0F * m * (K2 + 1.0F) + K2);
            }
        }

        @Override
        public
        String toString() {
            return "Back.INOUT";
        }
    }),


    ///////////////////////////////////////////////////////
    // Bounce
    ///////////////////////////////////////////////////////
    Bounce_In(new TweenEquation() {
        @Override
        public final
        float compute(final float time) {
            return 1.0F - Bounce_Out.equation.compute(1.0F - time);
        }

        @Override
        public
        String toString() {
            return "Bounce.IN";
        }
    }),
    Bounce_Out(new TweenEquation() {
        @Override
        public final
        float compute(final float time) {
            if (time < bounce_k1) {
                return bounce_k0 * time * time;
            }
            else if (time < bounce_k2) {
                // 48/64
                final float t = time - bounce_k3;
                return bounce_k0 * t * t + 0.75F;
            }
            else if (time < bounce_k4) {
                // 60/64
                final float t = time - bounce_k5;
                return bounce_k0 * t * t + 0.9375F;
            }
            else {
                // 63/64
                final float t = time - bounce_k6;
                return bounce_k0 * t * t + 0.984375F;
            }
        }

        @Override
        public
        String toString() {
            return "Bounce.OUT";
        }
    }),
    Bounce_InOut(new TweenEquation() {
        @Override
        public final
        float compute(final float time) {
            final float t = time * 2.0F;

            if (t < 1.0F) {
                return 0.5F - 0.5F * Bounce_Out.equation.compute(1.0F - t);
            }

            return 0.5F + 0.5F * Bounce_Out.equation.compute(t - 1.0F);
        }

        @Override
        public
        String toString() {
            return "Bounce.INOUT";
        }
    }),


    ///////////////////////////////////////////////////////
    // Elastic
    ///////////////////////////////////////////////////////
    Elastic_In(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            final float m = time - 1.0F;

            return (float) (-Math.pow(2, 10.0F * m) * Math.sin((m * 40.0F - 3.0F) * PI_DIV_6)); // Note: 40/6 = 6.666... = 2/0.3 = PI/6;
        }

        @Override
        public
        String toString() {
            return "Elastic.IN";
        }
    }),
    Elastic_Out(new TweenEquation() {
        @Override
        public
        float compute(final float time) {
            return (float) (1.0F + (Math.pow(2, 10.0F * -time) * Math.sin((-time * 40.0F - 3.0F) * PI_DIV_6)));
        }

        @Override
        public
        String toString() {
            return "Elastic.OUT";
        }
    }),
    Elastic_InOut(new TweenEquation() {
        @Override
        public
        float compute(float time) {
            time *= 2.0F; // remap: [0,0.5] -> [-1,0]
            time -= 1.0F; // and    [0.5,1] -> [0,+1]

            final float k = ((80.0F * time - 9.0F) * PI_DIV_18);

            if (time < 0.0F) {
                return (float) (-0.5F * Math.pow(2, 10.0F * time) * Math.sin(k));
            }
            return (float) (1.0F + 0.5F * Math.pow(2, -10.0F * time) * Math.sin(k));
        }

        @Override
        public
        String toString() {
            return "Elastic.INOUT";
        }
    }),

    ;

    // float values of used constants
    private static final float       PI = 3.1415926535897932384626433832795028841971693993759F;
    private static final float PI_DIV_2 = 1.5707963267948966192313216916397514420985846996875F;
    private static final float PI_DIV_6 = 0.5235987755982988730771072305465838140328615665625F; // Note: 40/6 = 6.666... = 2/0.3 = PI/6;
    private static final float PI_DIV_18= 0.1745329251994329576923690768488612713442871888541F;

    private static final float K =  1.70158F;
    private static final float K2 = 1.70158F * 1.525F;

    private static final float bounce_r = 1.0F / 2.75F;       // reciprocal
    private static final float bounce_k0 = 7.5625F;
    private static final float bounce_k1 = 1.0F * bounce_r;   // 36.36%
    private static final float bounce_k2 = 2.0F * bounce_r;   // 72.72%
    private static final float bounce_k3 = 1.5F * bounce_r;   // 54.54%
    private static final float bounce_k4 = 2.5F * bounce_r;   // 90.90%
    private static final float bounce_k5 = 2.25F * bounce_r;  // 81.81%
    private static final float bounce_k6 = 2.625F * bounce_r; // 95.45%

    /**
     * Takes an easing name and gives you the corresponding TweenEquation. You probably won't need this, but tools will love that.
     *
     * @param name
     *                 The name of an easing, like "Quad_InOut".
     *
     * @return The parsed equation, or null if there is no match.
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static
    TweenEquation parse(String name) {
        TweenEquations[] values = TweenEquations.values();

        for (int i = 0, n = values.length; i < n; i++) {
            if (name.equals(values[i].toString())) {
                return values[i].equation;
            }
        }

        return null;
    }
    private transient final TweenEquation equation;

    TweenEquations(final TweenEquation equation) {
        this.equation = equation;
    }

    public
    TweenEquation getEquation() {
        return equation;
    }

    /**
     * Computes the next value of the interpolation.
     *
     * @param time The current time, between 0 and 1.
     *
     * @return The corresponding value.
     */
    public
    float compute(float time) {
        return equation.compute(time);
    }

    @Override
    public
    String toString() {
        return equation.toString();
    }
}
