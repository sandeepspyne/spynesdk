package com.spyneai.videorecording

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.webkit.WebView
import androidx.databinding.DataBindingUtil
import com.spyneai.R

class ProductShowCaseWebView : WebView {

    constructor(context: Context) : this(context, null){
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {

        /*Setting the basic settings for the webview*/
        settings.javaScriptEnabled = true
        settings.setAppCacheEnabled(true)
        settings.saveFormData = true
        settings.javaScriptEnabled = true
        addJavascriptInterface(
            ProductShowCaseWebView.JavaScriptInterface(),
            "jsinterface"
        )
        scrollBarStyle = SCROLLBARS_OUTSIDE_OVERLAY
    }

    internal class JavaScriptInterface

   

    override fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        super.loadDataWithBaseURL(
            baseUrl,
            """<style>img{display: inline;height: auto;min-width: 100%;max-width: 100%; margin: 10px 0px;}li{margin: 10px 0px;}iframe{display:block;min-width: 100%;max-width: 100%; margin: 10px 0px;}h3{  font-family: ProxyBold}a{ color: #b6b6b6; }body,body *{ word-wrap: break-word; max-width: 100%;}</style><script src="file:///android_asset/js/jquery.min.js"></script>
    <script type="text/javascript" src="file:///android_asset/js/j360.js"></script>
</head>
<body>

<script type="text/javascript" style="height: auto;min-width: 100%;max-width: 100%; margin: 10px 0px; overflow: hidden;">
            jQuery(document).ready(function() {
                jQuery('#product').j360();
            });

</script>
<center>
    <div id="product" style="height: auto;min-width: 100%;max-width: 100%; margin: 10px 0px; overflow: hidden;">$data</div>
</center>

</body>""", mimeType, encoding, historyUrl
        )
    }


}