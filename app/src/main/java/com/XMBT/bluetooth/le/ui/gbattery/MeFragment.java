package com.XMBT.bluetooth.le.ui.gbattery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.view.TitleBar;

import cn.sharesdk.onekeyshare.OnekeyShare;


public class MeFragment extends Fragment implements View.OnClickListener {
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = View.inflate(getActivity(), R.layout.me_fragment, null);
        initView();
        return rootView;
    }

    private void initView() {
        TitleBar titleBar = (TitleBar) rootView.findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        rootView.findViewById(R.id.ll_refresh).setOnClickListener(this);
        rootView.findViewById(R.id.ll_changePwd).setOnClickListener(this);
        rootView.findViewById(R.id.ll_share).setOnClickListener(this);
        rootView.findViewById(R.id.ll_about).setOnClickListener(this);
        rootView.findViewById(R.id.ll_logout).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_refresh:
                startActivity(new Intent(getContext(), RefreshActivity.class));
                break;
            case R.id.ll_changePwd:
                startActivity(new Intent(getContext(), ChangePwdActivity.class));
                break;
            case R.id.ll_share:
                showShare();
                break;
            case R.id.ll_about:
                startActivity(new Intent(getContext(), AboutActivity.class));
                break;
            case R.id.ll_logout:
                logout();
                break;

        }
    }

    private void logout() {
        if (GlobalConsts.isLogin) {
            new AlertDialog.Builder(getContext())
                    .setTitle("提示")
                    .setMessage("确定退出登录？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            GlobalConsts.isLogin = false;
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }
    }


    private void showShare() {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle("宝利奥");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("德国宝利奥汽车智能硬件客户端，App在手，轻松掌控");
        oks.setTitleUrl("http://a.app.qq.com/o/simple.jsp?pkgname=org.zywx.wbpalmstar.widgetone.uex&from=groupmessage");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        oks.setImageUrl("http://18gps.net/app/chewukong/mobiledownload/images/QRcode.png");
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://a.app.qq.com/o/simple.jsp?pkgname=org.zywx.wbpalmstar.widgetone.uex&from=groupmessage");
        // 启动分享GUI
        oks.show(getContext());
    }
}
