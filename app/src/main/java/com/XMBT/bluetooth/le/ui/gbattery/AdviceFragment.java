package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.utils.ToolsUtil;

/**
 * 咨询fragment
 */

public class AdviceFragment extends Fragment {
    View view;
    WebView webView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.advice_fragment, null);
        webView = (WebView) view.findViewById(R.id.webView);
        ToolsUtil.showProgressDialog(getActivity(), "加载中...");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://bbs.ycqpmall.com/portal.php?mod=list&catid=3&mobile=2");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    ToolsUtil.closeProgressDialog();
                }
            }
        });

        return view;
    }
}
