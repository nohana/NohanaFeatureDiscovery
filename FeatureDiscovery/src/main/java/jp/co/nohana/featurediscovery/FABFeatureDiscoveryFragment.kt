package jp.co.nohana.featurediscovery

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class FABFeatureDiscoveryFragment : FeatureDiscoveryFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_fab_feature_dicovery_, container, false)
    }

    override fun removeFromManager() {
        //happens when double click back key
        fragmentManager ?: return
        val fragment = fragmentManager!!.findFragmentByTag(FABFeatureDiscoveryFragment.TAG)
        fragment ?: return
        fragmentManager!!.beginTransaction().remove(fragment).commitAllowingStateLoss()

    }

    companion object {
        @JvmField
        val TAG: String = FABFeatureDiscoveryFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(centerX: Int, centerY: Int, @DrawableRes icon: Int, @StringRes title: Int, @StringRes message: Int): FABFeatureDiscoveryFragment {
            val fragment = FABFeatureDiscoveryFragment()
            val bundle = Bundle().apply {
                putInt(FeatureDiscoveryFragment.ARGS_CENTER_X, centerX)
                putInt(FeatureDiscoveryFragment.ARGS_CENTER_Y, centerY)
                putInt(FeatureDiscoveryFragment.ARGS_ICON, icon)
                putInt(FeatureDiscoveryFragment.ARGS_TITLE, title)
                putInt(FeatureDiscoveryFragment.ARGS_MESSAGE, message)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}