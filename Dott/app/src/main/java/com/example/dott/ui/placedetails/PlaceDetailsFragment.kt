package com.example.dott.ui.placedetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.dott.R
import com.example.dott.databinding.FragmentItemDetailBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

/**
 * A fragment representing a single Place details screen.
 */
@AndroidEntryPoint
class PlaceDetailsFragment : Fragment() {

    private  val viewModel : PlaceDetailsViewModel by viewModels()
    private lateinit var itemId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                itemId = it.getString(ARG_ITEM_ID) ?: ""
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding : FragmentItemDetailBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_item_detail, container, false)
        binding.viewModel = viewModel

        viewModel.getPlaceDetails(itemId)
        viewModel.updateToolBar.observe(viewLifecycleOwner, {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = it
        })
        viewModel.errorMessage.observe(viewLifecycleOwner, { error ->
            Snackbar.make(binding.root, error, Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.close)) { requireActivity().onBackPressed() }
                .show()
        })
        return binding.root
    }

    companion object {
        const val ARG_ITEM_ID = "item_id"
    }
}