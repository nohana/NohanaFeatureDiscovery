package jp.co.nohana.featurediscovery

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View

class FabFeatureDiscoveryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FeatureDiscoveryView(context, attrs, defStyleAttr) {

    private var fab: FloatingActionButton? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.merge_fab_feature_dicovery, this)
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        if (child.id == R.id.fab) {
            fab = child as FloatingActionButton
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (!changed) {
            return
        }
        val pivotX = pivotX.toInt()
        val pivotY = pivotY.toInt()
        fab?.layout(pivotX - fab!!.measuredWidth / 2, pivotY - fab!!.measuredHeight / 2,
                pivotX + fab!!.measuredWidth / 2, pivotY + fab!!.measuredHeight / 2)
    }

    override fun setIcon(@DrawableRes iconRes: Int) {
        super.setIcon(iconRes)
        fab?.setImageResource(iconRes)
    }
}
