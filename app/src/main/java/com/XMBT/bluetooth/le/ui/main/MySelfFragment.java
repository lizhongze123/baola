package com.XMBT.bluetooth.le.ui.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseFragment;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.event.NotifyEvent;
import com.XMBT.bluetooth.le.ui.gbattery.AboutActivity;
import com.XMBT.bluetooth.le.ui.gbattery.ChangePwdActivity;
import com.XMBT.bluetooth.le.ui.gbattery.RefreshActivity;
import com.XMBT.bluetooth.le.ui.misc.LoginActivity;
import com.XMBT.bluetooth.le.utils.Configure;
import com.XMBT.bluetooth.le.utils.EvenManager;
import com.XMBT.bluetooth.le.utils.LoginUtil;
import com.XMBT.bluetooth.le.view.TitleBar;

import cn.sharesdk.onekeyshare.OnekeyShare;


public class MySelfFragment extends BaseFragment implements View.OnClickListener {

    private View rootView;
    private TextView tvLogin, tvUser;

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
        rootView.findViewById(R.id.ll_queryPwd).setOnClickListener(this);
        rootView.findViewById(R.id.ll_share).setOnClickListener(this);
        rootView.findViewById(R.id.ll_about).setOnClickListener(this);
        rootView.findViewById(R.id.ll_logout).setOnClickListener(this);
        tvLogin = (TextView) rootView.findViewById(R.id.tv_login);
        tvUser = (TextView) rootView.findViewById(R.id.tv_user);
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        if(Configure.isLogin){
            tvLogin.setText("退出登录");
            tvUser.setText(Configure.USERID);
        }else{
            tvLogin.setText("点击登录");
            tvUser.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_refresh:
                LoginUtil.checkLogin(this.getContext(), new LoginUtil.LoginForCallBack() {
                    @Override
                    public void callBack() {
                        startActivity(new Intent(getContext(), RefreshActivity.class));
                    }
                });
                break;
            case R.id.ll_changePwd:
                LoginUtil.checkLogin(this.getContext(), new LoginUtil.LoginForCallBack() {
                    @Override
                    public void callBack() {
                        startActivity(new Intent(getContext(), ChangePwdActivity.class));
                    }
                });
                break;
            case R.id.ll_queryPwd:
                LoginUtil.checkLogin(this.getContext(), new LoginUtil.LoginForCallBack() {
                    @Override
                    public void callBack() {
                        startActivity(new Intent(getContext(), QueryPwdActivity.class));
                    }
                });
                break;
            case R.id.ll_share:
                showShare();
                break;
            case R.id.ll_about:
                startActivity(new Intent(getContext(), AboutActivity.class));
                break;
            case R.id.ll_logout:
                if(Configure.isLogin){
                    logout();
                }else{
                    LoginUtil.checkLogin(this.getContext(), new LoginUtil.LoginForCallBack() {
                        @Override
                        public void callBack() {
                            refresh();
                        }
                    });
                }
                break;

        }
    }

    private void logout() {
        if (Configure.isLogin) {
            new AlertDialog.Builder(getContext())
                    .setTitle("提示")
                    .setMessage("确定退出登录？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Configure.isLogin = false;
                            Configure.USERID = "";
                            refresh();
                            EvenManager.sendEvent(new NotifyEvent(LoginActivity.LOGOUT));
                            showToast("已退出");
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
