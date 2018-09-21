package jp.co.nohana.featurediscovery.sample

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import jp.co.nohana.featurediscovery.FABFeatureDiscoveryFragment
import jp.co.nohana.featurediscovery.FeatureDiscoveryFragment
import jp.co.nohana.featurediscovery.FeatureDiscoveryView
import kotlinx.android.synthetic.main.activity_sample.*


class SampleActivity : AppCompatActivity(), FeatureDiscoveryView.TapListener {

    private var fragment : FeatureDiscoveryFragment? =  null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        val fabLayoutParam = fab.layoutParams as CoordinatorLayout.LayoutParams
        val manager = supportFragmentManager
        if (manager.findFragmentByTag(FABFeatureDiscoveryFragment.TAG) == null) {
            showFeatureDiscovery(manager,
                    content.width - fab.measuredWidth / 2 - fabLayoutParam.rightMargin,
                    content.height - fab.measuredHeight / 2 - fabLayoutParam.bottomMargin)
        }
    }

    private fun showFeatureDiscovery(manager: FragmentManager, centerX: Int, centerY: Int) {
        fragment = FABFeatureDiscoveryFragment.newInstance(centerX, centerY, R.drawable.ic_add, R.string.title, R.string.description)
        manager.beginTransaction()
                .add(R.id.content, fragment, FABFeatureDiscoveryFragment.TAG)
                .commit()
    }

    override fun onTapTarget() {
        fragment?.dismissByInteraction()
    }

    override fun onTapOutSide() {
        fragment?.dismiss()
    }
}
