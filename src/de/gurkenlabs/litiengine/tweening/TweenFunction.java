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
 * Copyright 2020 gurkenlabs
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
 * 
 */
package de.gurkenlabs.litiengine.tweening;

/**
 * Easing equations based on Robert Penner's work: http://robertpenner.com/easing/
 *
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 * @author Various optimizations by Michael "Code Poet" Pohoreski | https://github.com/Michaelangel007/
 * @author dorkbox, llc
 * @author gurkenlabs
 */
public enum TweenFunction {

  LINEAR(time -> time),

  ///////////////////////////////////////////////////////
  // Quad based (x^2)
  ///////////////////////////////////////////////////////
  QUAD_IN(time -> (float) Math.pow(time, 2)),
  QUAD_OUT(time -> (float) (1f - Math.pow((time - 1f), 2))),
  QUAD_INOUT(time -> (float) (time * 2f < 1f ? Math.pow(time, 2) * 2f : 1f - Math.pow(time - 1f, 2) * 2f)),

  ///////////////////////////////////////////////////////
  // Cubic (x^3)
  ///////////////////////////////////////////////////////
  CUBIC_IN(time -> (float) Math.pow(time, 3)),
  CUBIC_OUT(time -> (float) (1f + Math.pow(time - 1f, 3))),
  CUBIC_INOUT(time -> (float) (time * 2f < 1f ? Math.pow(time, 3) * 4f : 1f + Math.pow(time - 1f, 3) * 4f)),

  ///////////////////////////////////////////////////////
  // Quart (x^4)
  ///////////////////////////////////////////////////////
  QUART_IN(time -> (float) Math.pow(time, 4)),
  QUART_OUT(time -> (float) (1f - Math.pow((time - 1f), 4))),
  QUART_INOUT(time -> (float) (time * 2f < 1f ? Math.pow(time, 4) * 8f : 1f - Math.pow(time - 1f, 4) * 8f)),

  ///////////////////////////////////////////////////////
  // Quint (x^5)
  ///////////////////////////////////////////////////////
  QUINT_IN(time -> (float) Math.pow(time, 5)),
  QUINT_OUT(time -> (float) (1f + Math.pow((time - 1f), 5))),
  QUINT_INOUT(time -> (float) (time * 2f < 1f ? Math.pow(time, 5) * 16f : 1f + Math.pow(time - 1f, 5) * 16f)),

  ///////////////////////////////////////////////////////
  // Sextic (x^6)
  ///////////////////////////////////////////////////////
  SEXTIC_IN(time -> (float) Math.pow(time, 6)),
  SEXTIC_OUT(time -> (float) (1f - Math.pow((time - 1f), 6))),
  SEXTIC_INOUT(time -> (float) (time * 2f < 1f ? Math.pow(time, 6) * 32f : 1f - Math.pow(time - 1f, 6) * 32f)),

  ///////////////////////////////////////////////////////
  // Septic (x^7)
  ///////////////////////////////////////////////////////
  SEPTIC_IN(time -> (float) Math.pow(time, 7)),
  SEPTIC_OUT(time -> (float) (1f + Math.pow((time - 1f), 6))),
  SEPTIC_INOUT(time -> (float) (time * 2f < 1f ? Math.pow(time, 7) * 64f : 1f + Math.pow(time - 1f, 7) * 64f)),

  ///////////////////////////////////////////////////////
  // Octic (x^8)
  ///////////////////////////////////////////////////////
  OCTIC_IN(time -> (float) Math.pow(time, 8)),
  OCTIC_OUT(time -> (float) (1f - Math.pow((time - 1f), 8))),
  OCTIC_INOUT(time -> (float) (time * 2f < 1f ? Math.pow(time, 8) * 128f : 1f - Math.pow(time - 1f, 8) * 128f)),

  ///////////////////////////////////////////////////////
  // Circle
  ///////////////////////////////////////////////////////
  CIRCLE_IN(time -> (float) (1.0F - Math.sqrt(1.0F - Math.pow(time, 2)))),
  CIRCLE_OUT(time -> (float) Math.sqrt(1.0F - Math.pow(time - 1f, 2))),
  CIRCLE_INOUT(time -> (float) (time * 2f < 1f ? (1f - Math.sqrt(1.0F - Math.pow(time * 2f, 2))) * 0.5 : (Math.sqrt(1f - 4 * Math.pow((time - 1f), 2)) + 1.0f) * 0.5f)),

  ///////////////////////////////////////////////////////
  // Sine
  ///////////////////////////////////////////////////////
  SINE_IN(time -> (float) (1.0f - Math.cos(time * Math.PI / 2f))),
  SINE_OUT(time -> (float) (1.0f - Math.sin(time * Math.PI / 2f))),
  SINE_INOUT(time -> (float) (.5f * (1f - Math.cos(time * Math.PI / 2f)))),

  ///////////////////////////////////////////////////////
  // Exponential
  ///////////////////////////////////////////////////////
  EXPO_IN(time -> (float) Math.pow(2, 10f * (time - 1f))),
  EXPO_OUT(time -> (float) (1f - Math.pow(2, -10f * time))),
  EXPO_INOUT(time -> (float) (time < .5f ? Math.pow(2, 10f * (2f * time - 1f) - 1f) : 1f - Math.pow(2, -10f * (2f * time - 1f) - 1f))),

  ///////////////////////////////////////////////////////
  // Back
  ///////////////////////////////////////////////////////
  BACK_IN(time -> (float) Math.pow(time, 2) * (time * (1.70158f + 1f) - 1.70158f)),
  BACK_OUT(time -> {
    final float k = 1.70158F;
    return (float) (1f + Math.pow(time - 1f, 2) * ((time - 1f) * (k + 1f) + k));
  }),
  BACK_INOUT(time -> {
    final float k2 = 1.70158F * 1.525F;
    return (float) (time < .5f ? Math.pow(time, 2) * 2.0F * (time * 2.0F * (k2 + 1.0F) - k2) : 1.0F + 2.0F * Math.pow(time - 1f, 2) * (2.0F * (time - 1.0F) * (k2 + 1.0F) + k2));
  }),

  ///////////////////////////////////////////////////////
  // Bounce
  ///////////////////////////////////////////////////////
  BOUNCE_OUT(time -> {
    final float BOUNCE_R = 1.0F / 2.75F; // reciprocal
    final float BOUNCE_K0 = 7.5625F;
    final float BOUNCE_K1 = 1.0F * BOUNCE_R; // 36.36%
    final float BOUNCE_K2 = 2.0F * BOUNCE_R; // 72.72%
    final float BOUNCE_K3 = 1.5F * BOUNCE_R; // 54.54%
    final float BOUNCE_K4 = 2.5F * BOUNCE_R; // 90.90%
    final float BOUNCE_K5 = 2.25F * BOUNCE_R; // 81.81%
    final float BOUNCE_K6 = 2.625F * BOUNCE_R; // 95.45%

    if (time < BOUNCE_K1) {
      return BOUNCE_K0 * time * time;
    } else if (time < BOUNCE_K2) {
      // 48/64
      final float t = time - BOUNCE_K3;
      return BOUNCE_K0 * t * t + 0.75F;
    } else if (time < BOUNCE_K4) {
      // 60/64
      final float t = time - BOUNCE_K5;
      return BOUNCE_K0 * t * t + 0.9375F;
    } else {
      // 63/64
      final float t = time - BOUNCE_K6;
      return BOUNCE_K0 * t * t + 0.984375F;
    }
  }),

  BOUNCE_IN(time -> 1f - BOUNCE_OUT.equation.compute(1f - time)),
  BOUNCE_INOUT(time -> (float) (time * 2f < 1f ? 0.5F - 0.5F * BOUNCE_OUT.equation.compute(1.0F - time * 2) : 0.5F + 0.5F * BOUNCE_OUT.equation.compute(time * 2 - 1.0F))),

  ///////////////////////////////////////////////////////
  // Elastic
  ///////////////////////////////////////////////////////
  ELASTIC_IN(time -> (float) (-Math.pow(2, 10f * (time - 1f)) * Math.sin(((time - 1f) * 40f - 3f) * Math.PI / 6f))),
  ELASTIC_OUT(time -> (float) (1f + (Math.pow(2, 10f * -time) * Math.sin((-time * 40f - 3f) * Math.PI / 6f)))),
  ELASTIC_INOUT(time -> {
    time *= 2.0F; // remap: [0,0.5] -> [-1,0]
    time -= 1.0F; // and    [0.5,1] -> [0,+1]

    final float k = (float) ((80f * time - 9f) * Math.PI / 18f);

    if (time < 0.0F) {
      return (float) (-.5f * Math.pow(2, 10f * time) * Math.sin(k));
    }
    return (float) (1f + .5F * Math.pow(2, -10f * time) * Math.sin(k));
  });

  private final transient TweenEquation equation;

  /**
   * Instantiates a new tween function with a given mathematical equation.
   *
   * @param equation
   *          the equation
   */
  TweenFunction(final TweenEquation equation) {
    this.equation = equation;
  }

  /**
   * Gets the mathematical equation.
   *
   * @return the equation
   */
  public TweenEquation getEquation() {
    return equation;
  }

  /**
   * Computes the next value of the interpolation.
   *
   * @param time
   *          The current progress of the tween duration, between 0 and 1.
   *
   * @return The next interpolated value.
   */
  public float compute(float time) {
    return equation.compute(time);
  }
}
