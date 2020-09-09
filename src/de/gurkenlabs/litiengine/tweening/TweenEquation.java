/*
 * Copyright 2012 Aurelien Ribon
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
 * Base class for every easing equation. You can create your own equations
 * and directly use them in the Tween engine by inheriting from this class.
 *
 * @see Tween
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public abstract
class TweenEquation {

    /**
     * Computes the next value of the interpolation.
     *
     * @param time The current time, between 0 and 1.
     *
     * @return The corresponding value.
     */
    public abstract
    float compute(float time);

    /**
     * @return true if the given string is the name of this equation (the name
     * is returned in the toString() method, don't forget to override it).
     * </p>
     * This method is usually used to save/load a tween to/from a text file.
     */
    public
    boolean isValueOf(String string) {
        return string.equals(toString());
    }
}
