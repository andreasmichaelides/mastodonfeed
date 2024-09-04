package com.andreasmichaelides.mastodonfeed.presentation.mapper

import android.text.Spanned
import androidx.core.text.parseAsHtml
import javax.inject.Inject


class HtmlStringToSpannedMapper @Inject constructor() {

    operator fun invoke(htmlText: String): Spanned {
        return htmlText.parseAsHtml()
    }

}