package com.rasalexman.flaircore.animation

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

/**
 * Created by a.minkin on 24.11.2017.
 * @param from
 * From what mediator animation played
 *
 * @param to
 * to what mediator animation played
 *
 * @param isShow
 * directional of animation show or hide
 *
 * @param duration
 * duration of animation
 *
 * @param popLast
 * Need to remove last mediator
 *
 */
open class LinearAnimator : BaseAnimator() {

    override var isShow: Boolean = true
    override var duration: Long = 500
    override var popLast: Boolean = false

    /**
     * Get current animation
     */
    override fun getAnimator(): Animator {
        val animatorSet = AnimatorSet()
        val fromView = from?.viewComponent
        val toView = to?.viewComponent

        if (isShow) {
            fromView?.let {
                animatorSet.play(ObjectAnimator.ofFloat<View>(it, View.TRANSLATION_X, -it.width.toFloat()))
            }
            toView?.let {
                animatorSet.play(ObjectAnimator.ofFloat<View>(it, View.TRANSLATION_X, it.width.toFloat(), 0f))
            }
        } else {
            fromView?.let {
                animatorSet.play(ObjectAnimator.ofFloat<View>(it, View.TRANSLATION_X, it.width.toFloat()))
            }
            toView?.let {
                val fromLeft = fromView?.translationX ?: 0f
                animatorSet.play(ObjectAnimator.ofFloat<View>(it, View.TRANSLATION_X, fromLeft - it.width.toFloat(), 0f))
            }
        }

        return animatorSet
    }
}