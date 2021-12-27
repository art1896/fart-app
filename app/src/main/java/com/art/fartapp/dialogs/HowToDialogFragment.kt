package com.art.fartapp.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.art.fartapp.R
import com.art.fartapp.model.UserGuidePage
import com.google.android.material.button.MaterialButton
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator

private const val TAG = "HowToDialogFragment"
class HowToDialogFragment : DialogFragment() {

    private lateinit var guideAdapter: GuidePagerAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialog_how_to, container, false)
        rootView.findViewById<MaterialButton>(R.id.skip_button).setOnClickListener {
            setFragmentResult("user_guide_showed", bundleOf("showed" to true))
            dismiss()
        }
        guideAdapter = GuidePagerAdapter()
        viewPager = rootView.findViewById(R.id.dialog_viewPager)
        viewPager.apply {
            clipToPadding = false
            clipChildren = false
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            adapter = guideAdapter
            rootView.findViewById<SpringDotsIndicator>(R.id.spring_dots_indicator)
                .setViewPager2(this)
        }
        initViewPagerList()

        rootView.findViewById<MaterialButton>(R.id.next_button).setOnClickListener {
            if (viewPager.currentItem + 1 != guideAdapter.itemCount) {
                viewPager.post { viewPager.setCurrentItem(viewPager.currentItem + 1, true) }
            } else {
                setFragmentResult("user_guide_showed", bundleOf("showed" to true))
                dismiss()
            }
        }
        return rootView
    }

    private fun initViewPagerList() {
        val list = mutableListOf<UserGuidePage>()
        list.add(
            UserGuidePage(
                ContextCompat.getDrawable(requireContext(), R.drawable.showing_qr),
                "Open QR Code in your friend`s phone"
            )
        )
        list.add(
            UserGuidePage(
                ContextCompat.getDrawable(requireContext(), R.drawable.scanning_qr),
                "To add FARTER enter name and scan the QR code"
            )
        )
        guideAdapter.submitList(list)
    }

    override fun onResume() {
        super.onResume()
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        dialog!!.window!!.setLayout(width, height)
        dialog!!.setCancelable(false)
    }
}