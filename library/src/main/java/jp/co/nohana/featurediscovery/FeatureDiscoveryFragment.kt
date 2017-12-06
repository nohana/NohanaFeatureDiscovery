package jp.co.nohana.featurediscovery

import android.animation.Animator
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver

open class FeatureDiscoveryFragment : Fragment() {

    companion object {
        val TAG: String = FeatureDiscoveryFragment::class.java.simpleName

        val ARGS_CENTER_X = StringBuilder().append(FeatureDiscoveryFragment::class.java.canonicalName).append(".").append("ARGS_CENTER_X").toString()
        val ARGS_CENTER_Y = StringBuilder().append(FeatureDiscoveryFragment::class.java.canonicalName).append(".").append("ARGS_CENTER_Y").toString()
        val ARGS_ICON = StringBuilder().append(FeatureDiscoveryFragment::class.java.canonicalName).append(".").append("ARGS_ICON").toString()
        val ARGS_TITLE = StringBuilder().append(FeatureDiscoveryFragment::class.java.canonicalName).append(".").append("ARGS_TITLE").toString()
        val ARGS_MESSAGE = StringBuilder().append(FeatureDiscoveryFragment::class.java.canonicalName).append(".").append("ARGS_MESSAGE").toString()

        fun newInstance(centerX: Int, centerY: Int, @DrawableRes icon: Int, @StringRes title: Int, @StringRes message: Int): FeatureDiscoveryFragment {
            val fragment = FeatureDiscoveryFragment()
            val bundle = Bundle().apply {
                putInt(ARGS_CENTER_X, centerX)
                putInt(ARGS_CENTER_Y, centerY)
                putInt(ARGS_ICON, icon)
                putInt(ARGS_TITLE, title)
                putInt(ARGS_MESSAGE, message)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feature_dicovery, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val centerX = arguments.getInt(ARGS_CENTER_X)
        val centerY = arguments.getInt(ARGS_CENTER_Y)
        val iconRes = arguments.getInt(ARGS_ICON)
        val title = arguments.getInt(ARGS_TITLE)
        val message = arguments.getInt(ARGS_MESSAGE)

        val v = view as FeatureDiscoveryView?
        v!!.setIcon(iconRes)
        v.setTapListener(object : FeatureDiscoveryView.TapListener {
            override fun onTapTarget() {
                dismissByInteraction()
            }

            override fun onTapOutSide() {
                dismiss()
            }
        })
        v.setTitle(title)
        v.setMessage(message)

        show(v, centerX, centerY)
    }

    private fun show(v: FeatureDiscoveryView, centerX: Int, centerY: Int) {
        v.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                v.viewTreeObserver.removeOnGlobalLayoutListener(this)
                v.visibility = View.VISIBLE
                v.pivotX = centerX.toFloat()
                v.pivotY = centerY.toFloat()
                v.startShowAnimation()
            }
        })
    }

    fun dismissByInteraction() {
        val v = view as FeatureDiscoveryView?
        v!!.startInteractionAnimation(object : SimpleAnimatorListener() {
            override fun onAnimationEnd(animation: Animator) {
                removeFromManager()
            }
        })
    }

    fun dismiss() {
        val v = view as FeatureDiscoveryView?
        v!!.startDismissAnimation(object : SimpleAnimatorListener() {
            override fun onAnimationEnd(animation: Animator) {
                removeFromManager()
            }
        })
    }

    protected open fun removeFromManager() {
        val fragment = fragmentManager.findFragmentByTag(FeatureDiscoveryFragment.TAG)
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss()
        }
    }
}
