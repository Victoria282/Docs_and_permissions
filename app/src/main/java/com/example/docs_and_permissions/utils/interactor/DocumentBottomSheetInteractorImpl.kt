package com.example.docs_and_permissions.utils.interactor

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.docs_and_permissions.utils.DocumentBottomSheet

class DocumentBottomSheetInteractorImpl @Inject constructor(

) : DocumentBottomSheetInteractor {

    override fun provideFragment(
        fragmentManager: FragmentManager, format: String, path: String, name: String
    ) {
        val fragment: DialogFragment = DocumentBottomSheet.getInstance()
        fragment.isCancelable = false
        fragment.putToArgument(DOCUMENT_FORMAT_KEY, format)
        fragment.putToArgument(DOCUMENT_PATH_KEY, path)
        fragment.putToArgument(DOCUMENT_NAME_KEY, name)
        fragment.show(fragmentManager, DOCUMENT_BOTTOM_SHEET_TAG)
    }

    override fun removeFragment(fragmentManager: FragmentManager) {
        val fragment =
            fragmentManager.findFragmentByTag(DOCUMENT_BOTTOM_SHEET_TAG) ?: return
        fragmentManager.commit { remove(fragment) }
    }

    override fun isAdded(fragmentManager: FragmentManager) =
        fragmentManager.findFragmentByTag(DOCUMENT_BOTTOM_SHEET_TAG) != null

    companion object {
        private const val DOCUMENT_BOTTOM_SHEET_TAG: String = "DocumentBottomSheet"
        const val DOCUMENT_FORMAT_KEY = "document_format_key"
        const val DOCUMENT_NAME_KEY = "document_name_key"
        const val DOCUMENT_PATH_KEY = "document_path_key"
    }
}