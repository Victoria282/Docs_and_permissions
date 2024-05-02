package com.example.docs_and_permissions.utils.interactor

import androidx.fragment.app.FragmentManager

interface DocumentBottomSheetInteractor {
    public fun provideFragment(
        fragmentManager: FragmentManager, format: String, path: String, name: String
    )

    public fun removeFragment(fragmentManager: FragmentManager)
    public fun isAdded(fragmentManager: FragmentManager): Boolean
}