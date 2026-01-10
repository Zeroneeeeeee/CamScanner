package gambi.zerone.camscanner.view.splitpdf

import android.graphics.RectF

data class SearchResults(
    val page: Int,
    val results: List<RectF>
)
