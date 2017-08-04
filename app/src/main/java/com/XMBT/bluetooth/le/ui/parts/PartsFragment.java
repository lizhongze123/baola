package com.XMBT.bluetooth.le.ui.parts;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.utils.ToolsUtil;

/**
 * Created by haowenlee on 2016/10/18.
 */
public class PartsFragment extends Fragment {
    View view;
    WebView webView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.parts_fragment, null);
        webView = (WebView) view.findViewById(R.id.webView);
        ToolsUtil.showProgressDialog(getActivity(), "加载中...");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://ycsn.ycqpmall.com/");
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
